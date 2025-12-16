'use client'

import { useState, useEffect, useRef } from 'react'
import { useRouter } from 'next/navigation'
import { connectSSE, disconnectSSE, SseConnection, LocationData, NotificationData } from '@/lib/sse'
import { getCurrentNavigation, PathNavigationInfo } from '@/lib/path'
import styles from './page.module.css'

export default function GuardianTrackingPage() {
  const router = useRouter()
  const mapRef = useRef<HTMLDivElement>(null)
  const kakaoMapRef = useRef<any>(null)
  const userMarkerRef = useRef<any>(null)
  const startMarkerRef = useRef<any>(null)
  const endMarkerRef = useRef<any>(null)
  const pathPolylineRef = useRef<any>(null)
  const sseConnectionRef = useRef<SseConnection | null>(null)
  const alertMarkerRef = useRef<any>(null)

  const [mapLoaded, setMapLoaded] = useState(false)
  const [userLocation, setUserLocation] = useState<LocationData | null>(null)
  const [pathInfo, setPathInfo] = useState<PathNavigationInfo | null>(null)
  const [isConnected, setIsConnected] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [isLoadingPath, setIsLoadingPath] = useState(false)
  const [notification, setNotification] = useState<NotificationData | null>(null)
  const [showNotificationModal, setShowNotificationModal] = useState(false)

  const apiKey = process.env.NEXT_PUBLIC_KAKAO_MAP_KEY ?? ''

  // 카카오 지도 로드
  useEffect(() => {
    if (mapLoaded) return
    if (typeof window === 'undefined') return

    const script = document.createElement('script')
    script.src = `//dapi.kakao.com/v2/maps/sdk.js?appkey=${apiKey}&autoload=false`
    script.async = true
    script.onload = () => {
      window.kakao.maps.load(() => {
        setMapLoaded(true)
      })
    }
    document.head.appendChild(script)
  }, [apiKey, mapLoaded])

  // 경로 정보 조회 (처음 한 번만)
  useEffect(() => {
    const fetchPathInfo = async () => {
      try {
        console.log('[보호자 추적] 📍 경로 정보 조회 중...')
        const info = await getCurrentNavigation()
        console.log('[보호자 추적] 📍 경로 정보 수신:', JSON.stringify(info, null, 2))

        if (info && info.isNavigating) {
          console.log('[보호자 추적] ✅ 사용자가 길안내 중입니다!')
          console.log('[보호자 추적] 출발지:', info.startName, `(${info.startY}, ${info.startX})`)
          console.log('[보호자 추적] 도착지:', info.endName, `(${info.endY}, ${info.endX})`)

          // 전체 경로 데이터 조회 (pathNodeList 포함)
          setIsLoadingPath(true)
          const requestBody = {
            startY: parseFloat(info.startY),
            startX: parseFloat(info.startX),
            endY: parseFloat(info.endY),
            endX: parseFloat(info.endX),
            startName: info.startName,
            endName: info.endName,
          }

          console.log('[보호자 추적] 🛣️  상세 경로 데이터 요청 중...')
          const pathRes = await fetch('/api/v1/paths', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(requestBody),
          })

          const pathData = await pathRes.json()
          console.log('[보호자 추적] 🛣️  상세 경로 데이터 수신:', pathData)

          if (pathData && pathData.data && 'pathNodeList' in pathData.data) {
            // pathNodeList를 포함한 완전한 경로 정보 설정
            const fullPathInfo = {
              ...info,
              pathNodeList: pathData.data.pathNodeList,
            }
            console.log('[보호자 추적] ✅ 경로 노드 개수:', pathData.data.pathNodeList.length)
            setPathInfo(fullPathInfo)
          } else {
            console.log('[보호자 추적] ⚠️  경로 데이터를 가져왔지만 pathNodeList가 없습니다.')
            setPathInfo(info)
          }
          setIsLoadingPath(false)
        } else {
          console.log('[보호자 추적] ⚠️  사용자가 현재 길안내 중이 아닙니다.')
          console.log('[보호자 추적] 응답 데이터:', info)
          setPathInfo(null)
        }
      } catch (err) {
        console.error('[보호자 추적] ❌ 경로 정보 조회 실패:', err)
        if (err instanceof Error) {
          console.error('[보호자 추적] 에러 메시지:', err.message)
        }
        setError('경로 정보를 불러올 수 없습니다.')
        setIsLoadingPath(false)
      }
    }

    if (mapLoaded) {
      fetchPathInfo() // 처음 한 번만 조회
    }
  }, [mapLoaded])

  // 카카오맵 초기화
  useEffect(() => {
    if (!mapLoaded || !mapRef.current) return

    const container = mapRef.current
    const options = {
      center: new window.kakao.maps.LatLng(37.5665, 126.978), // 서울 시청 기본값
      level: 3,
    }

    const map = new window.kakao.maps.Map(container, options)
    kakaoMapRef.current = map
  }, [mapLoaded])

  // 출발지/도착지 마커 표시
  useEffect(() => {
    console.log('[보호자 추적] 🗺️  마커 표시 useEffect 실행')
    console.log('[보호자 추적] kakaoMapRef.current:', !!kakaoMapRef.current)
    console.log('[보호자 추적] pathInfo:', pathInfo)

    if (!kakaoMapRef.current) {
      console.log('[보호자 추적] ⚠️  카카오맵이 아직 초기화되지 않음')
      return
    }

    if (!pathInfo) {
      console.log('[보호자 추적] ⚠️  경로 정보가 없음')
      return
    }

    if (!pathInfo.isNavigating) {
      console.log('[보호자 추적] ⚠️  사용자가 길안내 중이 아님')
      return
    }

    const map = kakaoMapRef.current

    console.log('[보호자 추적] 🗺️  마커 생성 시작')
    console.log('[보호자 추적] 출발지 좌표:', pathInfo.startY, pathInfo.startX)
    console.log('[보호자 추적] 도착지 좌표:', pathInfo.endY, pathInfo.endX)

    // 기존 마커 제거
    if (startMarkerRef.current) {
      startMarkerRef.current.setMap(null)
      console.log('[보호자 추적] 기존 출발지 마커 제거')
    }
    if (endMarkerRef.current) {
      endMarkerRef.current.setMap(null)
      console.log('[보호자 추적] 기존 도착지 마커 제거')
    }

    try {
      // 좌표 파싱
      const startLat = parseFloat(pathInfo.startY)
      const startLng = parseFloat(pathInfo.startX)
      const endLat = parseFloat(pathInfo.endY)
      const endLng = parseFloat(pathInfo.endX)

      console.log('[보호자 추적] 파싱된 출발지:', startLat, startLng)
      console.log('[보호자 추적] 파싱된 도착지:', endLat, endLng)

      // 좌표 유효성 검증
      if (isNaN(startLat) || isNaN(startLng) || isNaN(endLat) || isNaN(endLng)) {
        console.error('[보호자 추적] ❌ 좌표 파싱 실패:', { startLat, startLng, endLat, endLng })
        return
      }

      // 출발지 마커
      const startPosition = new window.kakao.maps.LatLng(startLat, startLng)
      const startMarker = new window.kakao.maps.Marker({
        position: startPosition,
        map: map,
      })
      startMarkerRef.current = startMarker
      console.log('[보호자 추적] ✅ 출발지 마커 생성 완료')

      // 도착지 마커
      const endPosition = new window.kakao.maps.LatLng(endLat, endLng)
      const endMarker = new window.kakao.maps.Marker({
        position: endPosition,
        map: map,
      })
      endMarkerRef.current = endMarker
      console.log('[보호자 추적] ✅ 도착지 마커 생성 완료')

      // 경로 그리기 (pathNodeList가 있는 경우)
      if (pathInfo.pathNodeList && pathInfo.pathNodeList.length > 0) {
        console.log('[보호자 추적] 🛣️  경로 그리기 시작')

        // 기존 경로 제거
        if (pathPolylineRef.current) {
          pathPolylineRef.current.setMap(null)
          console.log('[보호자 추적] 기존 경로 제거')
        }

        const linePaths: any[] = []

        // LineString 타입의 노드들에서 좌표 추출
        pathInfo.pathNodeList.forEach((node) => {
          if (node.type === 'LineString') {
            node.coordinates.forEach(([lng, lat]) => {
              linePaths.push(new window.kakao.maps.LatLng(lat, lng))
            })
          }
        })

        console.log('[보호자 추적] 경로 좌표 개수:', linePaths.length)

        // 경로가 있으면 Polyline 그리기
        if (linePaths.length > 1) {
          const polyline = new window.kakao.maps.Polyline({
            path: linePaths,
            strokeWeight: 5,
            strokeColor: '#005AFF',
            strokeOpacity: 0.8,
            strokeStyle: 'solid',
            map: map,
          })

          pathPolylineRef.current = polyline
          console.log('[보호자 추적] ✅ 경로 그리기 완료!')

          // 지도 범위를 경로 전체가 보이도록 조정
          const bounds = new window.kakao.maps.LatLngBounds()
          linePaths.forEach((p) => bounds.extend(p))
          map.setBounds(bounds)
        } else {
          console.log('[보호자 추적] ⚠️  경로 좌표가 부족하여 Polyline을 그리지 않음')
          // 경로가 없으면 출발지, 도착지만 보이도록
          const bounds = new window.kakao.maps.LatLngBounds()
          bounds.extend(startPosition)
          bounds.extend(endPosition)
          map.setBounds(bounds)
        }
      } else {
        console.log('[보호자 추적] ⚠️  pathNodeList가 없어 경로를 그리지 않음')
        // 경로 정보가 없으면 출발지, 도착지만 보이도록
        const bounds = new window.kakao.maps.LatLngBounds()
        bounds.extend(startPosition)
        bounds.extend(endPosition)
        map.setBounds(bounds)
      }

      console.log('[보호자 추적] 🗺️  출발지/도착지 마커 표시 완료!')
    } catch (error) {
      console.error('[보호자 추적] ❌ 마커 생성 중 오류:', error)
    }
  }, [pathInfo, mapLoaded])

  // SSE 연결
  useEffect(() => {
    if (!mapLoaded) return

    console.log('[보호자 추적] 📡 SSE 연결 준비 중...')

    const connection = connectSSE({
      onLocation: (data: LocationData) => {
        console.log('[보호자 추적] 📍 사용자 위치 수신:', data)
        setUserLocation(data)
      },
      onNotification: (data: NotificationData) => {
        console.log('[보호자 추적] 🔔 긴급 알림 수신:', data)
        setNotification(data)
        setShowNotificationModal(true)
      },
      onError: (err: Error) => {
        console.error('[보호자 추적] ❌ SSE 에러:', err)
        setError('실시간 위치 연결에 실패했습니다.')
        setIsConnected(false)
      },
      onOpen: () => {
        console.log('[보호자 추적] ✅ SSE 연결 성공! 사용자의 실시간 위치를 추적합니다.')
        setIsConnected(true)
        setError(null)
      },
    })

    sseConnectionRef.current = connection

    return () => {
      if (sseConnectionRef.current) {
        console.log('[보호자 추적] 🔌 페이지 이탈로 SSE 연결 해제')
        disconnectSSE(sseConnectionRef.current)
      }
    }
  }, [mapLoaded])

  // 사용자 위치가 업데이트되면 마커 표시
  useEffect(() => {
    if (!userLocation || !kakaoMapRef.current) {
      console.log('[보호자 추적] ⚠️ 마커 업데이트 스킵 - 위치 또는 지도 없음')
      return
    }

    console.log('[보호자 추적] 🎯 사용자 위치 마커 업데이트:', userLocation)
    updateUserMarker(userLocation)
  }, [userLocation, mapLoaded])

  // 사용자 위치 마커 업데이트
  const updateUserMarker = (location: LocationData) => {
    if (!kakaoMapRef.current) return

    const map = kakaoMapRef.current
    const markerPosition = new window.kakao.maps.LatLng(location.lat, location.lng)

    // 기존 마커 제거
    if (userMarkerRef.current) {
      userMarkerRef.current.setMap(null)
    }

    // 커스텀 오버레이로 사용자 위치 표시
    const content = `
      <div style="
        width: 24px;
        height: 24px;
        border-radius: 50%;
        background: #FF4444;
        border: 4px solid white;
        box-shadow: 0 2px 6px rgba(0,0,0,0.4);
      "></div>
    `

    const customOverlay = new window.kakao.maps.CustomOverlay({
      position: markerPosition,
      content: content,
      zIndex: 10,
    })

    customOverlay.setMap(map)
    userMarkerRef.current = customOverlay

    // 지도 중심을 사용자 위치로 이동
    map.panTo(markerPosition)
  }

  const handleMyLocation = () => {
    if (userLocation && kakaoMapRef.current) {
      const moveLatLon = new window.kakao.maps.LatLng(userLocation.lat, userLocation.lng)
      kakaoMapRef.current.panTo(moveLatLon)
    }
  }

  const handleNotificationConfirm = () => {
    if (!notification || !kakaoMapRef.current) return

    const map = kakaoMapRef.current
    const alertPosition = new window.kakao.maps.LatLng(notification.lat, notification.lng)

    if (alertMarkerRef.current) {
      alertMarkerRef.current.setMap(null)
    }

    const content = `
      <div style="
        padding: 10px;
        background: white;
        border: 3px solid #ff4444;
        border-radius: 8px;
        box-shadow: 0 2px 8px rgba(0,0,0,0.3);
        text-align: center;
        min-width: 120px;
      ">
        <div style="font-size: 24px; margin-bottom: 4px;">🚨</div>
        <div style="font-weight: bold; color: #ff4444; font-size: 14px;">긴급 알림</div>
      </div>
    `

    const customOverlay = new window.kakao.maps.CustomOverlay({
      position: alertPosition,
      content: content,
      zIndex: 20,
    })

    customOverlay.setMap(map)
    alertMarkerRef.current = customOverlay

    map.panTo(alertPosition)
    map.setLevel(3)

    setShowNotificationModal(false)
  }

  const handleNotificationClose = () => {
    setShowNotificationModal(false)
  }

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <button className={styles.backButton} onClick={() => router.back()}>
          ←
        </button>
        <h1 className={styles.title}>실시간 위치 추적</h1>
        <div className={styles.placeholder} />
      </div>

      {/* 연결 상태 표시 */}
      <div className={`${styles.statusBar} ${isConnected ? styles.connected : styles.disconnected}`}>
        <div className={styles.statusDot} />
        <span className={styles.statusText}>
          {isConnected ? '연결됨' : '연결 중...'}
        </span>
        {isLoadingPath && (
          <span className={styles.statusText} style={{ marginLeft: '8px' }}>
            | 경로 불러오는 중...
          </span>
        )}
      </div>

      {/* 에러 메시지 */}
      {error && (
        <div className={styles.errorBanner}>
          {error}
        </div>
      )}

      {/* 지도 */}
      <div ref={mapRef} className={styles.map} />

      {/* 내 위치로 버튼 */}
      {userLocation && (
        <button className={styles.myLocationButton} onClick={handleMyLocation}>
          <span className={styles.myLocationIcon}>📍</span>
        </button>
      )}

      {/* 경로 정보 패널 */}
      {pathInfo && pathInfo.isNavigating && (
        <div className={styles.infoPanel} style={{ bottom: userLocation ? '140px' : '20px' }}>
          <h3 style={{ margin: '0 0 10px 0', fontSize: '14px', fontWeight: 'bold' }}>경로 정보</h3>
          <div className={styles.infoRow}>
            <span className={styles.infoLabel}>출발지:</span>
            <span className={styles.infoValue}>{pathInfo.startName}</span>
          </div>
          <div className={styles.infoRow}>
            <span className={styles.infoLabel}>도착지:</span>
            <span className={styles.infoValue}>{pathInfo.endName}</span>
          </div>
        </div>
      )}

      {/* 사용자 위치 정보 패널 */}
      {userLocation && (
        <div className={styles.infoPanel}>
          <h3 style={{ margin: '0 0 10px 0', fontSize: '14px', fontWeight: 'bold' }}>실시간 위치</h3>
          <div className={styles.infoRow}>
            <span className={styles.infoLabel}>위도:</span>
            <span className={styles.infoValue}>{userLocation.lat.toFixed(6)}</span>
          </div>
          <div className={styles.infoRow}>
            <span className={styles.infoLabel}>경도:</span>
            <span className={styles.infoValue}>{userLocation.lng.toFixed(6)}</span>
          </div>
          <div className={styles.infoRow}>
            <span className={styles.infoLabel}>업데이트:</span>
            <span className={styles.infoValue}>{new Date(userLocation.timestamp).toLocaleString('ko-KR')}</span>
          </div>
        </div>
      )}

      {/* 긴급 알림 모달 */}
      {showNotificationModal && notification && (
        <div className={styles.modalOverlay}>
          <div className={styles.modalContent}>
            <div className={styles.modalIcon}>🚨</div>
            <h2 className={styles.modalTitle}>긴급 알림</h2>
            <p className={styles.modalMessage}>사용자가 긴급 알림을 보냈습니다!</p>
            <div className={styles.modalInfo}>
              <div className={styles.modalInfoRow}>
                <span className={styles.modalInfoLabel}>주소:</span>
                <span className={styles.modalInfoValue}>{notification.address}</span>
              </div>
              <div className={styles.modalInfoRow}>
                <span className={styles.modalInfoLabel}>위도:</span>
                <span className={styles.modalInfoValue}>{notification.lat.toFixed(6)}</span>
              </div>
              <div className={styles.modalInfoRow}>
                <span className={styles.modalInfoLabel}>경도:</span>
                <span className={styles.modalInfoValue}>{notification.lng.toFixed(6)}</span>
              </div>
            </div>
            <div className={styles.modalButtons}>
              <button className={styles.modalButtonPrimary} onClick={handleNotificationConfirm}>
                위치 확인
              </button>
              <button className={styles.modalButtonSecondary} onClick={handleNotificationClose}>
                닫기
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
