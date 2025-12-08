'use client'

import { useState, useEffect, useRef } from 'react'
import { useRouter, useSearchParams } from 'next/navigation'
import { type ChargingStation, chargerApi } from '@/lib/stations'
import { getReviewsByStation, deleteReview, type Review } from '@/lib/reviews'
import { saveAlert } from '@/lib/alerts'
import StarRating from './StarRating'
import styles from './MapView.module.css'
import { useLocation } from '@/providers/LocationProvider';

interface MapViewProps {
  selectedCategory: 'charging' | 'restroom' | null
}

declare global {
    interface Window {
        kakao: any
    }
}

export default function MapView({ selectedCategory }: MapViewProps) {
    const router = useRouter()
    const searchParams = useSearchParams()

    const mapRef = useRef<HTMLDivElement>(null)
    const kakaoMapRef = useRef<any>(null)
    const markersRef = useRef<any[]>([])
    const userMarkerRef = useRef<any>(null)
    const selectedCategoryRef = useRef(selectedCategory)

    const [stations, setStations] = useState<ChargingStation[]>([])
    const [selectedStation, setSelectedStation] = useState<ChargingStation | null>(null)
    const [mapLoaded, setMapLoaded] = useState(false)
    const [showEmergencyDialog, setShowEmergencyDialog] = useState(false)
    const [activeTab, setActiveTab] = useState<'info' | 'review'>('info')
    const [reviews, setReviews] = useState<Review[]>([])
    const [userType, setUserType] = useState<string | null>(null)
    const [showSearchButton, setShowSearchButton] = useState(false)
    const [mapMoved, setMapMoved] = useState(false)

    // 에러 상태 추가
    const [error, setError] = useState<string | null>(null)

    const apiKey = process.env.NEXT_PUBLIC_KAKAO_MAP_KEY ?? ''

    const location = useLocation();
    const { lat, lng, timestamp } = location;

    // 에러 자동 해제 (5초)
    useEffect(() => {
        if (!error) return
        const t = setTimeout(() => setError(null), 5000)
        return () => clearTimeout(t)
    }, [error])

    // 카카오 지도 로드
    useEffect(() => {
        if (mapLoaded) return
        if (typeof window === 'undefined') return;

        const script = document.createElement('script')
        script.src = `//dapi.kakao.com/v2/maps/sdk.js?appkey=${apiKey}&autoload=false&libraries=services`
        script.async = true
        script.onload = () => {
            window.kakao.maps.load(() => {
                setMapLoaded(true)
            })
        }
        document.head.appendChild(script)

        return () => {
            // 클린업은 필요 시에만
        }
    }, [apiKey, mapLoaded])

    // 사용자 타입 확인
    useEffect(() => {
        const type = localStorage.getItem('userType')
        setUserType(type)
    }, [])

    // 충전소 데이터 로드
    useEffect(() => {
        if (selectedCategory !== 'charging') {
            // 충전소 카테고리가 아니면 마커 제거
            setStations([])
            setSelectedStation(null)
            setShowSearchButton(false)
            setMapMoved(false)
            setError(null)
            // 기존 마커들 제거
            markersRef.current.forEach(marker => marker.setMap(null))
            markersRef.current = []
            return
        }

        // 맵이 준비되지 않았으면 대기
        if (!mapLoaded || !kakaoMapRef.current) return

        let mounted = true

        const loadStations = async () => {
            const bounds = kakaoMapRef.current.getBounds?.()
            if (!bounds) return

            try {
                const stationsResult = await chargerApi.getChargersInViewport(
                    bounds.qa, bounds.pa, bounds.ha, bounds.oa
                )
                if (!mounted) return

                setStations(stationsResult)
                setMapMoved(false)
                setShowSearchButton(false)
                setError(null) // 성공 시 에러 초기화
            } catch (error) {
                console.error('충전소 데이터 로드 오류:', error)
                setError('충전소 로드에 실패했습니다. 잠시 후 다시 시도해주세요.')
            }
        }

        loadStations()

        return () => {
            mounted = false
        }
    }, [selectedCategory, mapLoaded])

    // 카카오맵 초기화
    useEffect(() => {
        if (!mapLoaded || !mapRef.current || !lat || !lng) return

        const container = mapRef.current
        const options = {
            center: new window.kakao.maps.LatLng(lat, lng),
            level: 3, // 확대 레벨
        }

        const map = new window.kakao.maps.Map(container, options)
        kakaoMapRef.current = map

        // 지도 클릭 이벤트 - 선택 해제
        window.kakao.maps.event.addListener(map, 'click', () => {
            setSelectedStation(null)
        })

        // 지도 이동 이벤트 - 검색 버튼 표시
        window.kakao.maps.event.addListener(map, 'dragend', () => {
            if (selectedCategoryRef.current === 'charging') {
                setMapMoved(true)
                setShowSearchButton(true)
            }
        })

        // 지도 줌 변경 이벤트 - 검색 버튼 표시
        window.kakao.maps.event.addListener(map, 'zoom_changed', () => {
            if (selectedCategoryRef.current === 'charging') {
                setMapMoved(true)
                setShowSearchButton(true)
            }
        })

        // 사용자 위치 마커 생성
        createUserMarker(map, { lat, lng })

    }, [mapLoaded, lat, lng])

    // 사용자 위치 마커 생성
    const createUserMarker = (map: any, location: { lat: number; lng: number }) => {
        // 기존 마커 제거
        if (userMarkerRef.current) {
            userMarkerRef.current.setMap(null)
        }

        const markerPosition = new window.kakao.maps.LatLng(location.lat, location.lng)

        // 커스텀 오버레이로 사용자 위치 표시
        const content = `
                          <div style="
                            width: 20px;
                            height: 20px;
                            border-radius: 50%;
                            background: #4A90E2;
                            border: 3px solid white;
                            box-shadow: 0 2px 4px rgba(0,0,0,0.3);
                          "></div>
                        `

        const customOverlay = new window.kakao.maps.CustomOverlay({
            position: markerPosition,
            content: content,
            zIndex: 3
        })

        customOverlay.setMap(map)
        userMarkerRef.current = customOverlay
    }

    // 충전소 마커들 생성
    useEffect(() => {
        if (!kakaoMapRef.current || !mapLoaded || selectedCategory !== 'charging') return

        // 기존 마커들 제거
        markersRef.current.forEach(marker => marker.setMap(null))
        markersRef.current = []

        // 새 마커들 생성
        stations.forEach((station) => {
            const markerPosition = new window.kakao.maps.LatLng(station.lat, station.lng)

            // 마커 이미지 설정
            const imageSrc = selectedStation?.placeId === station.placeId
                ? 'https://t1.daumcdn.net/localimg/localimages/07/mapapidoc/markerStar.png'
                : 'https://t1.daumcdn.net/localimg/localimages/07/mapapidoc/marker_red.png'

            const imageSize = new window.kakao.maps.Size(24, 35)
            const markerImage = new window.kakao.maps.MarkerImage(imageSrc, imageSize)

            const marker = new window.kakao.maps.Marker({
                position: markerPosition,
                image: markerImage,
                title: station.facilityName,
                clickable: true
            })

            // 마커 클릭 이벤트
            window.kakao.maps.event.addListener(marker, 'click', () => {
                handleStationClick(station)
            })

            marker.setMap(kakaoMapRef.current)
            markersRef.current.push(marker)

            // 커스텀 오버레이로 라벨 추가
            const content = `
        <div style="
          padding: 5px 10px;
          background: white;
          border: 2px solid #4A90E2;
          border-radius: 15px;
          font-size: 12px;
          font-weight: bold;
          color: #333;
          box-shadow: 0 2px 4px rgba(0,0,0,0.2);
          white-space: nowrap;
          cursor: pointer;
        ">
          ${station.facilityName}
        </div>
      `

            const labelPosition = new window.kakao.maps.LatLng(
                station.lat + 0.0008, // 라벨을 마커 위에 표시
                station.lng
            )

            const customOverlay = new window.kakao.maps.CustomOverlay({
                position: labelPosition,
                content: content,
                yAnchor: 1
            })

            customOverlay.setMap(kakaoMapRef.current)
            markersRef.current.push(customOverlay)
        })

    }, [stations, selectedStation, mapLoaded, selectedCategory])

    // 선택된 충전소로 지도 이동
    useEffect(() => {
        if (selectedStation && kakaoMapRef.current) {
            const moveLatLon = new window.kakao.maps.LatLng(selectedStation.lat, selectedStation.lng)
            kakaoMapRef.current.panTo(moveLatLon)
        }
    }, [selectedStation])

    // 디버그: MapView 내부에 추가
    useEffect(() => {
        selectedCategoryRef.current = selectedCategory
    }, [selectedCategory])


    const handleStationClick = (station: ChargingStation) => {
        setSelectedStation(station)
        // 리뷰 데이터 로드
        const stationReviews = getReviewsByStation(station.placeId)
        setReviews(stationReviews)
    }

    const checkLogin = () => {
        const loggedIn = localStorage.getItem('isLoggedIn')
        if (loggedIn !== 'true') {
            router.push('/auth')
            return false
        }
        return true
    }

    const handleGuide = () => {
        // 안내 버튼 클릭 시 제보 신청 페이지로 이동
        if (!checkLogin()) return
        if (selectedStation) {
            router.push(`/report?stationName=${encodeURIComponent(selectedStation.facilityName)}`)
        }
    }

    const handleSetEnd = () => {
        if (!checkLogin()) return
        if (selectedStation) {
            // 지도에 표시된 충전소만 도착지로 설정 가능
            const isStationOnMap = stations.some(s => s.placeId === selectedStation.placeId)
            if (isStationOnMap) {
                // 출발지 확인
                const startStationId = localStorage.getItem('startStationId')

                if (startStationId) {
                    // 출발지가 있으면 길찾기 화면으로 이동
                    router.push(`/directions?startId=${startStationId}&endId=${selectedStation.placeId}`)
                    // localStorage 정리
                    localStorage.removeItem('startStationId')
                    localStorage.removeItem('startStationName')
                } else {
                    // 출발지가 없으면 현재 위치를 출발지로 사용
                    router.push(
                        `/directions?start-lat=${lat}` +
                        `&start-lng=${lng}` +
                        `&end-lat=${selectedStation.lat}` +
                        `&end-lng=${selectedStation.lng}` +
                        `&start-name=현재 위치` +
                        `&end-name=${encodeURIComponent(selectedStation.facilityName)}`
                    )

                }
            }
        }
    }

    const handleMyLocation = () => {
        if (navigator.geolocation && kakaoMapRef.current) {
            navigator.geolocation.getCurrentPosition(
                (position) => {
                    const moveLatLon = new window.kakao.maps.LatLng(position.coords.latitude, position.coords.longitude)
                    kakaoMapRef.current.panTo(moveLatLon)
                    createUserMarker(kakaoMapRef.current, {
                        lat: position.coords.latitude,
                        lng: position.coords.longitude
                    })
                }
            )
        }
    }

    const handleEmergencyClick = () => {
        if (!checkLogin()) return
        // 사용자 타입일 때만 긴급 알림 가능
        if (userType !== 'user') {
            alert('긴급 알림 기능은 사용자만 사용할 수 있습니다.')
            return
        }
        setShowEmergencyDialog(true)
    }

    const handleEmergencyConfirm = () => {
        // 사용자 타입 확인
        const loggedIn = localStorage.getItem('isLoggedIn')
        const currentUserType = localStorage.getItem('userType')

        if (loggedIn !== 'true' || currentUserType !== 'user') {
            setShowEmergencyDialog(false)
            alert('긴급 알림 기능은 사용자만 사용할 수 있습니다.')
            return
        }

        // 보호자에게 알림 전송
        if (location) {
            const guardianEmail = localStorage.getItem('guardianEmail')
            const userId = localStorage.getItem('userEmail') || ''
            const userName = localStorage.getItem('userName') || '사용자'

            if (guardianEmail) {
                // 긴급 알림 저장
                saveAlert({
                    userId,
                    userName,
                    guardianEmail,
                    lat: location.lat,
                    lng: location.lng,
                })

                // 보호자 이메일이 등록되어 있으면 이메일로 알림 전송 (실제로는 API 호출)
                // 여기서는 시뮬레이션
                console.log(`긴급 알림을 ${guardianEmail}로 전송합니다.`)
                console.log(`위치: ${location.lat}, ${location.lng}`)

                alert('긴급 알림이 보호자에게 전송되었습니다.')
            } else {
                // 보호자 이메일이 없으면 보호자 화면으로만 이동
                alert('보호자 이메일이 등록되지 않았습니다. 마이페이지에서 보호자를 등록해주세요.')
            }
        }
        setShowEmergencyDialog(false)
    }

    const handleEmergencyCancel = () => {
        setShowEmergencyDialog(false)
    }

    const handleSearchCurrentArea = async () => {
        if (!kakaoMapRef.current) return

        const bounds = kakaoMapRef.current.getBounds()
        if (!bounds) return

        try {
            const stationsResult = await chargerApi.getChargersInViewport(
                bounds.qa, bounds.pa, bounds.ha, bounds.oa
            )
            setStations(stationsResult)
            setShowSearchButton(false)
            setMapMoved(false)
            setError(null) // 성공 시 에러 초기화
        } catch (error) {
            console.error('충전소 데이터 로드 오류:', error)
            setError('현 주소 검색 중 오류가 발생했습니다. 잠시 후 다시 시도하세요.')
        }
    }

  const handleMapClick = (e: React.MouseEvent<HTMLDivElement>) => {
    // 마커나 버튼이 아닌 지도 배경을 클릭한 경우에만 패널 닫기
    if (e.target === e.currentTarget) {
      setSelectedStation(null)
    }
  }

  const handleReport = () => {
    if (!checkLogin()) return
    if (selectedStation) {
      router.push(`/report?stationName=${encodeURIComponent(selectedStation.facilityName)}`)
    }
  }

  // 마커 위치 계산 (간단한 상대 위치)
  const getMarkerPosition = (station: ChargingStation, index: number) => {
    // 지도 영역 내에서 상대적 위치 계산
    const positions = [
      { top: '20%', left: '60%' },
      { top: '35%', left: '25%' },
      { top: '50%', left: '70%' },
      { top: '65%', left: '40%' },
    ]
    return positions[index % positions.length]
  }

  return (
    <div className={styles.mapContainer}>
        {/* 에러 배너 */}
        {error && (
            <div
                role="alert"
                onClick={() => setError(null)}
                className={styles.errorBanner}
            >
                {error}
            </div>
        )}

        <div ref={mapRef} className={styles.map} />
      
      {/* 긴급 버튼 - 사용자 타입일 때만 표시 */}
      {userType === 'user' && (
        <button className={styles.emergencyButton} onClick={handleEmergencyClick}>
          <span className={styles.emergencyIcon}>🚨</span>
          <span className={styles.emergencyLabel}>긴급</span>
        </button>
      )}

      {/* 긴급 알림 확인 다이얼로그 */}
      {showEmergencyDialog && (
        <div className={styles.dialogOverlay} onClick={handleEmergencyCancel}>
          <div className={styles.dialog} onClick={(e) => e.stopPropagation()}>
            <p className={styles.dialogMessage}>
              정말 긴급 알림을 보호자에게 보내시겠습니까?
            </p>
            <div className={styles.dialogButtons}>
              <button className={styles.dialogButton} onClick={handleEmergencyConfirm}>
                예
              </button>
              <button className={`${styles.dialogButton} ${styles.dialogButtonCancel}`} onClick={handleEmergencyCancel}>
                아니오
              </button>
            </div>
          </div>
        </div>
      )}

      {/* 내 위치로 버튼 */}
      <button className={styles.myLocationButton} onClick={handleMyLocation}>
        <span className={styles.myLocationIcon}>📍</span>
      </button>

        {/* 현 주소에서 검색 버튼 */}
        {showSearchButton && selectedCategory === 'charging' && (
            <button className={styles.searchCurrentButton} onClick={handleSearchCurrentArea}>
                현 주소에서 검색
            </button>
        )}

      {/* 충전소 정보 패널 */}
      {selectedStation && (
        <div className={styles.infoPanel}>
          {/* 장소명, 주소, 제보, 도착 버튼을 탭 위에 배치 */}
          <div className={styles.stationHeader}>
            <div className={styles.stationInfoText}>
              <h3 className={styles.stationName}>{selectedStation.facilityName}</h3>
              <p className={styles.stationAddress}>{selectedStation.districtName}</p>
            </div>
            <div className={styles.actionButtons}>
              <button className={styles.guideButton} onClick={handleGuide}>
                제보
              </button>
              <button className={styles.endButton} onClick={handleSetEnd}>
                도착
              </button>
            </div>
          </div>
          <div className={styles.panelHeader}>
            <button 
              className={`${styles.panelTab} ${activeTab === 'info' ? styles.active : ''}`}
              onClick={() => setActiveTab('info')}
            >
              정보
            </button>
            <button 
              className={`${styles.panelTab} ${activeTab === 'review' ? styles.active : ''}`}
              onClick={() => setActiveTab('review')}
            >
              리뷰
            </button>
          </div>
          <div className={styles.panelContent}>
            {activeTab === 'info' ? (
              <div className={styles.infoContent}>
                <div className={styles.stationInfo}>
                  <p>운영시간: 24시간</p>
                  <p>충전기 타입: 급속/완속</p>
                  <p>이용 가능: 4대</p>
                </div>
              </div>
            ) : activeTab === 'review' ? (
              <div className={styles.reviewSection}>
                <div className={styles.reviewHeaderSection}>
                  <h4 className={styles.reviewTitle}>리뷰</h4>
                  <button 
                    className={styles.writeReviewButton}
                    onClick={() => {
                      if (!checkLogin()) return
                      if (selectedStation) {
                        router.push(`/review/write?stationId=${selectedStation.placeId}`)
                      }
                    }}
                  >
                    리뷰 쓰기
                  </button>
                </div>
                <div className={styles.reviewList}>
                  {reviews.length === 0 ? (
                    <div className={styles.emptyReview}>
                      <p>아직 리뷰가 없습니다.</p>
                    </div>
                  ) : (
                    reviews.map((review) => {
                      const userId = localStorage.getItem('userEmail') || ''
                      const isOwner = review.userId === userId
                      
                      return (
                        <div 
                          key={review.id} 
                          className={styles.reviewItem}
                          onClick={() => router.push(`/review/detail?reviewId=${review.id}`)}
                        >
                          <div className={styles.reviewHeader}>
                            <div className={styles.reviewAuthorSection}>
                              <span className={styles.reviewAuthor}>{review.userName}</span>
                              <StarRating 
                                rating={review.rating} 
                                editable={false}
                                size="small"
                              />
                            </div>
                            {isOwner && (
                              <div 
                                className={styles.reviewActions}
                                onClick={(e) => e.stopPropagation()}
                              >
                                <button
                                  className={styles.menuIcon}
                                  onClick={() => router.push(`/review/detail?reviewId=${review.id}`)}
                                >
                                  ⋯
                                </button>
                              </div>
                            )}
                          </div>
                          <p className={styles.reviewText}>{review.content}</p>
                          {review.photoUrl && (
                            <div className={styles.reviewPhotoContainer}>
                              <img src={review.photoUrl} alt="Review" className={styles.reviewPhoto} />
                            </div>
                          )}
                          <span className={styles.reviewDate}>{review.date}</span>
                        </div>
                      )
                    })
                  )}
                </div>
              </div>
            ) : null}
          </div>
        </div>
      )}
    </div>
  )
}

