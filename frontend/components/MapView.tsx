'use client'

import { useState, useEffect, useRef } from 'react'
import { useRouter, useSearchParams } from 'next/navigation'
import { type ChargingStation, chargerApi } from '@/lib/stations'
import { getReviewsByStation, deleteReview, type Review } from '@/lib/reviews'
import { isAdmin } from '@/lib/auth'
import { saveAlert } from '@/lib/alerts'
import StarRating from './StarRating'
import styles from './MapView.module.css'
import { useLocation } from '@/providers/LocationProvider';
import { type Facility, facilityApi } from "@/lib/facilities";

interface MapViewProps {
  selectedCategory: string | null
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
    const clustererRef = useRef<any>(null)
    const userMarkerRef = useRef<any>(null)
    const selectedCategoryRef = useRef(selectedCategory)

    const [stations, setStations] = useState<ChargingStation[]>([])
    const [facilities, setFacilities] = useState<Facility[]>([])
    const [selectedStation, setSelectedStation] = useState<ChargingStation | null>(null)
    const [selectedFacility, setSelectedFacility] = useState<Facility | null>(null)
    const [mapLoaded, setMapLoaded] = useState(false)
    const [showEmergencyDialog, setShowEmergencyDialog] = useState(false)
    const [activeTab, setActiveTab] = useState<'info' | 'review'>('info')
    const [reviews, setReviews] = useState<Review[]>([])
    const [userType, setUserType] = useState<string | null>(null)
    const [showSearchButton, setShowSearchButton] = useState(false)
    const [openMenuId, setOpenMenuId] = useState<string | null>(null)

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
        script.src = `//dapi.kakao.com/v2/maps/sdk.js?appkey=${apiKey}&autoload=false&libraries=services,clusterer`
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

    // 선택된 카테고리 변경 시
    useEffect(() => {
        if (selectedCategory === null) {
            setFacilities([])
            setStations([])
            setSelectedStation(null)
            setSelectedFacility(null)
            setShowSearchButton(false)
            setError(null)
            return
        }

        // 클러스터러 정리
        if (clustererRef.current) {
            clustererRef.current.clear(); // 또는 removeMarkers()
            clustererRef.current.setMap(null);
        }

        // 맵이 준비되지 않았으면 대기
        if (!mapLoaded || !kakaoMapRef.current) return

        let mounted = true


        const loadDatas = async () => {
            const bounds = kakaoMapRef.current.getBounds?.()
            if (!bounds) return

            const sw = bounds.getSouthWest();
            const ne = bounds.getNorthEast();

            try {

                if (selectedCategory === 'charging') {
                    const stationsResult = await chargerApi.getChargersInViewport(
                        sw.getLat(), ne.getLat(), sw.getLng(), ne.getLng()
                    )

                    if (!mounted) return

                    setFacilities([])
                    setStations(stationsResult)

                } else {
                    const facilitiesResult = await facilityApi.getConvenientFacilityInViewport(
                        selectedCategory, sw.getLat(), ne.getLat(), sw.getLng(), ne.getLng()
                    )

                    if (!mounted) return

                    setStations([])
                    setFacilities(facilitiesResult)
                }

                setShowSearchButton(false)
                setError(null) // 성공 시 에러 초기화

            } catch (error) {
                console.error('충전소 데이터 로드 오류:', error)
                setError('충전소 로드에 실패했습니다. 잠시 후 다시 시도해주세요.')
            }
        }

        loadDatas()

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
            setSelectedFacility(null)
        })

        // 지도 이동 이벤트 - 검색 버튼 표시
        window.kakao.maps.event.addListener(map, 'dragend', () => {
            if (selectedCategoryRef.current !== null) {
                setShowSearchButton(true)
            }
        })

        // 지도 줌 변경 이벤트 - 검색 버튼 표시
        window.kakao.maps.event.addListener(map, 'zoom_changed', () => {
            if (selectedCategoryRef.current !== null) {
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
        // 클러스터러 정리
        if (clustererRef.current) {
            clustererRef.current.clear(); // 또는 removeMarkers()
            clustererRef.current.setMap(null);
        }

        if (!kakaoMapRef.current || !mapLoaded || selectedCategory === null) return

        const clusterer = new window.kakao.maps.MarkerClusterer({
            map: kakaoMapRef.current, // 마커들을 클러스터로 관리하고 표시할 지도 객체
            averageCenter: true, // 클러스터에 포함된 마커들의 평균 위치를 클러스터 마커 위치로 설정
            minLevel: 5 // 클러스터 할 최소 지도 레벨
        });
        clustererRef.current = clusterer;

        if (selectedCategory === 'charging') {
            // 새 마커들 생성
            stations.forEach((station) => {
                const markerPosition = new window.kakao.maps.LatLng(station.lat, station.lng)

                // 마커 이미지 설정
                const imageSrc = selectedStation?.placeId === station.placeId
                    ? 'https://img.icons8.com/external-phatplus-lineal-color-phatplus/64/external-point-ev-car-phatplus-lineal-color-phatplus.png'
                    : 'https://img.icons8.com/external-phatplus-lineal-phatplus/64/external-point-ev-car-phatplus-lineal-phatplus.png'


                const imageSize = new window.kakao.maps.Size(36, 36)
                const imageOptions = {offset: new window.kakao.maps.Point(18, 30)}
                const markerImage = new window.kakao.maps.MarkerImage(
                    imageSrc,
                    imageSize,
                    imageOptions
                )
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

                clusterer.addMarker(marker)
            })
        } else {
            // 새 마커들 생성
            facilities.forEach((facility) => {
                const markerPosition = new window.kakao.maps.LatLng(facility.lat, facility.lng)

                // 마커 이미지 설정
                const imageSrc = selectedFacility?.facilityId === facility.facilityId
                    ? 'https://img.icons8.com/tiny-color/48/marker.png'
                    : 'https://img.icons8.com/tiny-glyph/48/marker.png'


                const imageSize = new window.kakao.maps.Size(32, 32)
                const imageOptions = {offset: new window.kakao.maps.Point(16, 32)}
                const markerImage = new window.kakao.maps.MarkerImage(
                    imageSrc,
                    imageSize,
                    imageOptions
                )
                const marker = new window.kakao.maps.Marker({
                    position: markerPosition,
                    image: markerImage,
                    title: facility.facilityName,
                    clickable: true
                })

                // 마커 클릭 이벤트
                window.kakao.maps.event.addListener(marker, 'click', () => {
                    handleFacilityClick(facility)
                })

                clusterer.addMarker(marker)
            })
        }
    }, [stations, selectedStation, facilities, selectedFacility, mapLoaded, selectedCategory])

    // 선택된 충전소로 지도 이동
    useEffect(() => {
        if ((!selectedStation && !selectedFacility) || !kakaoMapRef.current) return

        const map = kakaoMapRef.current

        const latVal = selectedStation ? selectedStation.lat : selectedFacility!.lat
        const lngVal = selectedStation ? selectedStation.lng : selectedFacility!.lng

        // 마커의 좌표
        const markerPosition = new window.kakao.maps.LatLng(
            latVal,
            lngVal

        )

        // 마커를 화면의 특정 위치(중앙보다 위쪽)에 배치
        // 지도를 이동시켜서 마커가 패널에 가려지지 않게 함
        const projection = map.getProjection()
        const point = projection.pointFromCoords(markerPosition)

        // 패널 높이만큼 아래로 이동 (픽셀 단위, 줌 레벨 무관)
        // 패널이 하단 40% 차지한다고 가정, 지도 높이의 20% 위치로 조정
        const mapHeight = map.getNode().offsetHeight
        const panelOffset = mapHeight * 0.2  // 픽셀 단위

        point.y += panelOffset  // y축 아래로 이동 (마커를 위로 올림)

        // 조정된 포인트를 좌표로 변환
        const newCenter = projection.coordsFromPoint(point)

        map.panTo(newCenter)

    }, [selectedStation, selectedFacility])

    // 디버그: MapView 내부에 추가
    useEffect(() => {
        setSelectedFacility(null)
        setSelectedStation(null)
        selectedCategoryRef.current = selectedCategory
    }, [selectedCategory])


    const handleStationClick = async (station: ChargingStation) => {
        setSelectedStation(station)
        // 리뷰 데이터 로드 (충전소명 전달)
        try {
            const stationReviews = await getReviewsByStation(station.placeId, station.facilityName)
            setReviews(stationReviews)
        } catch (error) {
            console.error('리뷰 로드 실패:', error)
            setReviews([])
        }
    }

    const handleFacilityClick = async (facility: Facility) => {
        if (facility.convenientFacilityInfo == null) {
            const updatedFacility = await facilityApi.getConvenientFacilityInfo(facility.facilityId)
        }

        setSelectedFacility(facility)
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
            router.push(`/report?placeId=${selectedStation.placeId}&stationName=${encodeURIComponent(selectedStation.facilityName)}`)
        }
    }

    const handleSetEnd = () => {
        if (!checkLogin()) return
        if (selectedStation || selectedFacility) {
            // 지도에 표시된 충전소만 도착지로 설정 가능


            const target = (selectedCategory === 'charging' ? selectedStation : selectedFacility) as ChargingStation | Facility | null
            if(!target) return

            // 출발지가 없으면 현재 위치를 출발지로 사용
            router.push(
                `/directions?start-lat=${lat}` +
                `&start-lng=${lng}` +
                `&end-lat=${target.lat}` +
                `&end-lng=${target.lng}` +
                `&start-name=현재 위치` +
                `&end-name=${encodeURIComponent(target.facilityName)}`
            )

        }
    }

    const handleMyLocation = () => {
        const moveLatLon = new window.kakao.maps.LatLng(lat, lng)
        kakaoMapRef.current.panTo(moveLatLon)
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
        if (!kakaoMapRef.current || selectedCategory === null) return

        const bounds = kakaoMapRef.current.getBounds()
        if (!bounds) return

        const sw = bounds.getSouthWest();
        const ne = bounds.getNorthEast();

        try {
            if (selectedCategory === 'charging')  {
                const stationsResult = await chargerApi.getChargersInViewport(
                    sw.getLat(), ne.getLat(), sw.getLng(), ne.getLng()
                )
                setStations(stationsResult)
                setFacilities([])
            } else {
                const facilitiesResult = await facilityApi.getConvenientFacilityInViewport(
                    selectedCategory, sw.getLat(), ne.getLat(), sw.getLng(), ne.getLng()
                )
                setFacilities(facilitiesResult)
                setStations([])
            }

            setShowSearchButton(false)
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
      setSelectedFacility(null)
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

    const formatTime = (value?: string | number | null): string => {
        if (value === null || value === undefined) return '-'
        const raw = String(value).trim()
        if (!raw) return '-'

        // 이미 HH:mm 형식이면 간단히 정규화
        if (raw.includes(':')) {
            const [h, m] = raw.split(':')
            return `${h.padStart(2, '0')}:${(m || '00').padStart(2, '0')}`
        }

        // 숫자만 추출
        let digits = raw.replace(/\D/g, '')
        if (!digits) return '-'

        // 너무 길면 뒤 4자리 사용
        if (digits.length > 4) digits = digits.slice(-4)

        if (digits.length === 1 || digits.length === 2) {
            return `${digits.padStart(2, '0')}:00`
        }

        let hour: string, minute: string
        if (digits.length === 3) {
            hour = digits.slice(0, 1)
            minute = digits.slice(1)
        } else { // 4
            hour = digits.slice(0, 2)
            minute = digits.slice(2, 4)
        }
        return `${hour.padStart(2, '0')}:${minute.padStart(2, '0')}`
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
        {showSearchButton && selectedCategory && (
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
              <p className={styles.stationAddress}>{selectedStation.cityName}, {selectedStation.districtName}</p>
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
          <div className={styles.infoContent}>
            {activeTab === 'info' ? (
              <div className={styles.infoScroll}>

                  <section className={styles.section}>
                      <h4 className={styles.sectionTitle}>설치장소</h4>
                      <p className={styles.textPrimary}>{selectedStation.roadAddr}</p>
                      {selectedStation.landAddr && <p className={styles.textSecondary}>({selectedStation.landAddr})</p>}
                      {selectedStation.description && <p className={styles.description}>{selectedStation.description}</p>}
                  </section>

                  <section className={styles.section}>
                      <h4 className={styles.sectionTitle}>운영 정보</h4>
                      <div className={styles.infoGrid}>
                          <div className={styles.infoItem}>
                              <span className={styles.infoLabel}>평일</span>
                              <span className={styles.infoValue}>{formatTime(selectedStation.weekdayStart)} ~ {formatTime(selectedStation.weekdayEnd)}</span>
                          </div>
                          <div className={styles.infoItem}>
                              <span className={styles.infoLabel}>토요일</span>
                              <span className={styles.infoValue}>{formatTime(selectedStation.saturdayStart)} ~ {formatTime(selectedStation.saturdayEnd)}</span>
                          </div>
                          <div className={styles.infoItem}>
                              <span className={styles.infoLabel}>공휴일</span>
                              <span className={styles.infoValue}>{formatTime(selectedStation.holidayStart)} ~ {formatTime(selectedStation.holidayEnd)}</span>
                          </div>
                      </div>
                  </section>

                  <section className={styles.section}>
                      <h4 className={styles.sectionTitle}>설비</h4>
                      <div className={styles.features}>
                          <div className={styles.featureCard}>
                              <div className={styles.featureTitle}>동시사용</div>
                              <div className={styles.featureValue}>{selectedStation.capacity ?? '-'}</div>
                          </div>
                          <div className={styles.featureCard}>
                              <div className={styles.featureTitle}>공기주입</div>
                              <div className={styles.featureValue}>{selectedStation.isAirPump ? 'Y' : 'N'}</div>
                          </div>
                          <div className={styles.featureCard}>
                              <div className={styles.featureTitle}>충전기</div>
                              <div className={styles.featureValue}>{selectedStation.isCharger ? 'Y' : 'N'}</div>
                          </div>
                      </div>
                  </section>

                  <section className={styles.section}>
                      <h4 className={styles.sectionTitle}>관리정보</h4>
                      <p className={styles.textPrimary}>{selectedStation.manageOrgName ?? '-'}</p>
                      <p className={styles.textSecondary}>{selectedStation.manageOrgContact ?? '-'}</p>
                  </section>

                  <section className={styles.section}>
                      <h4 className={styles.sectionTitle}>출처</h4>
                      <p className={styles.smallText}>전국전동휠체어급속충전기표준데이터</p>
                      <p className={styles.smallText}>지방자치단체, 보건복지부</p>
                      <br/>
                      <p className={styles.smallText}>데이터 제공기관: {selectedStation.providerName} ({selectedStation.providerCode})</p>
                      <p className={styles.smallText}>데이터 기준일자: {selectedStation.dataUpdated}</p>
                  </section>
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
                      // 권한 체크:
                      // - USER, PROTECTOR: 자기가 작성한 리뷰만 수정/삭제 가능
                      // - ADMIN: 모든 리뷰 수정/삭제 가능
                      // 실제 권한 검증은 백엔드에서 처리됨
                      const userName = localStorage.getItem('userName') || ''
                      const isOwner = review.userName === userName
                      const canEditOrDelete = isOwner || isAdmin()
                      const isMenuOpen = openMenuId === review.id
                      
                      const handleMenuClick = (e: React.MouseEvent) => {
                        e.stopPropagation()
                        setOpenMenuId(isMenuOpen ? null : review.id)
                      }
                      
                      const handleEdit = (e: React.MouseEvent) => {
                        e.stopPropagation()
                        setOpenMenuId(null)
                        if (selectedStation) {
                          router.push(`/review/write?reviewId=${review.id}&stationId=${selectedStation.placeId}`)
                        }
                      }
                      
                      const handleDelete = async (e: React.MouseEvent) => {
                        e.stopPropagation()
                        setOpenMenuId(null)
                        if (confirm('정말로 이 리뷰를 삭제하시겠습니까?')) {
                          try {
                            await deleteReview(review.id)
                            alert('리뷰가 삭제되었습니다.')
                            // 리뷰 목록 새로고침
                            if (selectedStation) {
                              const stationReviews = await getReviewsByStation(selectedStation.placeId, selectedStation.facilityName)
                              setReviews(stationReviews)
                            }
                          } catch (err) {
                            console.error('리뷰 삭제 실패:', err)
                            if (err instanceof Error) {
                              alert(err.message || '리뷰 삭제에 실패했습니다.')
                            } else {
                              alert('리뷰 삭제에 실패했습니다.')
                            }
                          }
                        }
                      }
                      
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
                            {canEditOrDelete && (
                              <div 
                                className={styles.reviewActions}
                                onClick={(e) => e.stopPropagation()}
                              >
                                <button
                                  className={styles.menuIcon}
                                  onClick={handleMenuClick}
                                  title="메뉴"
                                >
                                  ⋯
                                </button>
                                {isMenuOpen && (
                                  <>
                                    <div 
                                      className={styles.menuOverlay}
                                      onClick={() => setOpenMenuId(null)}
                                    />
                                    <div className={styles.menu}>
                                      <button 
                                        className={styles.menuItem}
                                        onClick={handleEdit}
                                      >
                                        수정
                                      </button>
                                      <button 
                                        className={`${styles.menuItem} ${styles.deleteMenuItem}`}
                                        onClick={handleDelete}
                                      >
                                        삭제
                                      </button>
                                    </div>
                                  </>
                                )}
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

        {/* 편의시설 정보 패널 */}
        {selectedFacility && (
            <div className={styles.infoPanel}>
                {/* 장소명, 주소, 제보, 도착 버튼을 탭 위에 배치 */}
                <div className={styles.stationHeader}>
                    <div className={styles.stationInfoText}>
                        <h3 className={styles.stationName}>{selectedFacility.facilityName}</h3>
                        <p className={styles.stationAddress}>{selectedFacility.roadAddr??'-'}</p>
                    </div>
                    <div className={styles.actionButtons}>
                        <button className={styles.endButton} onClick={handleSetEnd}>
                            도착
                        </button>
                    </div>
                </div>
                <div className={styles.infoContent}>
                    <div className={styles.infoScroll}>
                        <section className={styles.section}>
                            <h4 className={styles.sectionTitle}>시설 유형</h4>
                            <p className={styles.textPrimary}>{selectedFacility.facilityType}</p>
                        </section>

                        <section className={styles.section}>
                            <h4 className={styles.sectionTitle}>편의 시설 기구 목록</h4>
                            <p className={styles.textPrimary}>{selectedFacility.convenientFacilityInfo ?? '-'}</p>
                        </section>

                        <section className={styles.section}>
                            <h4 className={styles.sectionTitle}>출처</h4>
                            <p className={styles.smallText}>한국국사회보장정보원_장애인편의시설 현황</p>
                        </section>
                    </div>
                </div>
            </div>
        )}

    </div>
  )
}

