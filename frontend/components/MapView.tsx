'use client'

import { useState, useEffect } from 'react'
import { useRouter, useSearchParams } from 'next/navigation'
import { getStations, type ChargingStation } from '@/lib/stations'
import { getReviewsByStation, deleteReview, type Review } from '@/lib/reviews'
import { saveAlert } from '@/lib/alerts'
import StarRating from './StarRating'
import styles from './MapView.module.css'

interface MapViewProps {
  selectedCategory: 'charging' | 'restroom' | null
}

export default function MapView({ selectedCategory }: MapViewProps) {
  const router = useRouter()
  const searchParams = useSearchParams()
  const [userLocation, setUserLocation] = useState<{ lat: number; lng: number } | null>(null)
  const [stations, setStations] = useState<ChargingStation[]>([])
  const [selectedStation, setSelectedStation] = useState<ChargingStation | null>(null)
  const [mapLoaded, setMapLoaded] = useState(false)
  const [showEmergencyDialog, setShowEmergencyDialog] = useState(false)
  const [activeTab, setActiveTab] = useState<'info' | 'review'>('info')
  const [reviews, setReviews] = useState<Review[]>([])
  const [userType, setUserType] = useState<string | null>(null)

  // 사용자 위치 가져오기
  useEffect(() => {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          setUserLocation({
            lat: position.coords.latitude,
            lng: position.coords.longitude,
          })
        },
        () => {
          // 기본 위치 (서울 시청)
          setUserLocation({ lat: 37.5665, lng: 126.9780 })
        }
      )
    } else {
      setUserLocation({ lat: 37.5665, lng: 126.9780 })
    }
  }, [])

  // 충전소 데이터 로드
  useEffect(() => {
    if (selectedCategory === 'charging' && userLocation) {
      const stations = getStations()
      setStations(stations)
      
      // URL 파라미터에서 stationId 확인
      const stationId = searchParams?.get('stationId')
      if (stationId) {
        const station = stations.find(s => s.id === stationId)
        if (station) {
          setSelectedStation(station)
          const stationReviews = getReviewsByStation(station.id)
          setReviews(stationReviews)
        }
      }
      
      // URL 파라미터에서 tab 확인
      const tab = searchParams?.get('tab')
      if (tab === 'review') {
        setActiveTab('review')
      }
    } else {
      setStations([])
      setSelectedStation(null)
    }
  }, [selectedCategory, userLocation, searchParams])

  // 사용자 타입 확인
  useEffect(() => {
    const type = localStorage.getItem('userType')
    setUserType(type)
  }, [])

  // 네이버 지도 로드
  useEffect(() => {
    if (typeof window !== 'undefined' && !mapLoaded) {
      const script = document.createElement('script')
      script.src = `https://openapi.map.naver.com/openapi/v3/maps.js?ncpClientId=YOUR_CLIENT_ID`
      script.async = true
      script.onload = () => setMapLoaded(true)
      document.head.appendChild(script)
    }
  }, [mapLoaded])

  const handleStationClick = (station: ChargingStation) => {
    setSelectedStation(station)
    // 리뷰 데이터 로드
    const stationReviews = getReviewsByStation(station.id)
    setReviews(stationReviews)
  }

  const handleMapClick = (e: React.MouseEvent<HTMLDivElement>) => {
    // 마커나 버튼이 아닌 지도 배경을 클릭한 경우에만 패널 닫기
    if (e.target === e.currentTarget || (e.target as HTMLElement).id === 'map') {
      setSelectedStation(null)
    }
  }

  const checkLogin = () => {
    const loggedIn = localStorage.getItem('isLoggedIn')
    if (loggedIn !== 'true') {
      router.push('/auth')
      return false
    }
    return true
  }

  const handleReport = () => {
    if (!checkLogin()) return
    if (selectedStation) {
      router.push(`/report?stationName=${encodeURIComponent(selectedStation.name)}`)
    }
  }

  const handleGuide = () => {
    // 안내 버튼 클릭 시 제보 신청 페이지로 이동
    if (!checkLogin()) return
    if (selectedStation) {
      router.push(`/report?stationName=${encodeURIComponent(selectedStation.name)}`)
    }
  }


  const handleSetEnd = () => {
    if (!checkLogin()) return
    if (selectedStation && userLocation) {
      // 지도에 표시된 충전소만 도착지로 설정 가능
      const isStationOnMap = stations.some(s => s.id === selectedStation.id)
      if (isStationOnMap) {
        // 출발지 확인
        const startStationId = localStorage.getItem('startStationId')
        
        if (startStationId) {
          // 출발지가 있으면 길찾기 화면으로 이동
          router.push(`/directions?startId=${startStationId}&endId=${selectedStation.id}`)
          // localStorage 정리
          localStorage.removeItem('startStationId')
          localStorage.removeItem('startStationName')
        } else {
          // 출발지가 없으면 현재 위치를 출발지로 사용
          router.push(`/directions?endId=${selectedStation.id}`)
        }
      }
    }
  }

  const handleMyLocation = () => {
    if (userLocation && navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          setUserLocation({
            lat: position.coords.latitude,
            lng: position.coords.longitude,
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
    if (userLocation) {
      const guardianEmail = localStorage.getItem('guardianEmail')
      const userId = localStorage.getItem('userEmail') || ''
      const userName = localStorage.getItem('userName') || '사용자'
      
      if (guardianEmail) {
        // 긴급 알림 저장
        saveAlert({
          userId,
          userName,
          guardianEmail,
          lat: userLocation.lat,
          lng: userLocation.lng,
        })
        
        // 보호자 이메일이 등록되어 있으면 이메일로 알림 전송 (실제로는 API 호출)
        // 여기서는 시뮬레이션
        console.log(`긴급 알림을 ${guardianEmail}로 전송합니다.`)
        console.log(`위치: ${userLocation.lat}, ${userLocation.lng}`)
        
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
      <div id="map" className={styles.map} onClick={handleMapClick}>
        {/* 사용자 위치 마커 */}
        {userLocation && (
          <div className={styles.userMarker}>
            <div className={styles.userMarkerDot} />
          </div>
        )}

        {/* 충전소 마커들 */}
        {selectedCategory === 'charging' && stations.map((station, index) => {
          const position = getMarkerPosition(station, index)
          return (
            <button
              key={station.id}
              className={`${styles.stationMarker} ${selectedStation?.id === station.id ? styles.selected : ''}`}
              style={position}
              onClick={() => handleStationClick(station)}
            >
              <div className={styles.markerPin} />
              <span className={styles.markerLabel}>{station.name}</span>
            </button>
          )
        })}
      </div>
      
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

      {/* 충전소 정보 패널 */}
      {selectedStation && (
        <div className={styles.infoPanel}>
          {/* 장소명, 주소, 제보, 도착 버튼을 탭 위에 배치 */}
          <div className={styles.stationHeader}>
            <div className={styles.stationInfoText}>
              <h3 className={styles.stationName}>{selectedStation.name}</h3>
              <p className={styles.stationAddress}>{selectedStation.address}</p>
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
                        router.push(`/review/write?stationId=${selectedStation.id}`)
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

