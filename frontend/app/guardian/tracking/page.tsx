'use client'

import { useState, useEffect, useRef } from 'react'
import { useRouter } from 'next/navigation'
import { connectSSE, disconnectSSE, SseConnection, LocationData } from '@/lib/sse'
import { getCurrentNavigation, PathNavigationInfo } from '@/lib/path'
import styles from './page.module.css'

interface PathNode {
  type: 'Point' | 'LineString'
  coordinates: [number, number][]
}

export default function GuardianTrackingPage() {
  const router = useRouter()
  const mapRef = useRef<HTMLDivElement>(null)
  const kakaoMapRef = useRef<any>(null)
  const userMarkerRef = useRef<any>(null)
  const startMarkerRef = useRef<any>(null)
  const endMarkerRef = useRef<any>(null)
  const sseConnectionRef = useRef<SseConnection | null>(null)

  const [mapLoaded, setMapLoaded] = useState(false)
  const [userLocation, setUserLocation] = useState<LocationData | null>(null)
  const [pathInfo, setPathInfo] = useState<PathNavigationInfo | null>(null)
  const [isConnected, setIsConnected] = useState(false)
  const [error, setError] = useState<string | null>(null)

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
          setPathInfo(info)
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

      // 지도 범위 조정 (출발지, 도착지 모두 보이도록)
      const bounds = new window.kakao.maps.LatLngBounds()
      bounds.extend(startPosition)
      bounds.extend(endPosition)
      map.setBounds(bounds)

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
        updateUserMarker(data)
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
    </div>
  )
}
