'use client'

import { useState, useEffect, useRef } from 'react'
import { useRouter } from 'next/navigation'
import { connectSSE, disconnectSSE, SseConnection, LocationData } from '@/lib/sse'
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
  const sseConnectionRef = useRef<SseConnection | null>(null)

  const [mapLoaded, setMapLoaded] = useState(false)
  const [userLocation, setUserLocation] = useState<LocationData | null>(null)
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

  // SSE 연결
  useEffect(() => {
    if (!mapLoaded) return

    const connection = connectSSE({
      onLocation: (data: LocationData) => {
        console.log('사용자 위치 수신:', data)
        setUserLocation(data)
        updateUserMarker(data)
      },
      onError: (err: Error) => {
        console.error('SSE 에러:', err)
        setError('실시간 위치 연결에 실패했습니다.')
        setIsConnected(false)
      },
      onOpen: () => {
        console.log('SSE 연결 성공')
        setIsConnected(true)
        setError(null)
      },
    })

    sseConnectionRef.current = connection

    return () => {
      if (sseConnectionRef.current) {
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

      {/* 사용자 정보 패널 */}
      {userLocation && (
        <div className={styles.infoPanel}>
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
