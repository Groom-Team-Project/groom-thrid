'use client'

import { useSearchParams, useRouter } from 'next/navigation'
import { useState, useEffect } from 'react'
import { getStations, calculateDistance, calculateEstimatedTime, type ChargingStation } from '@/lib/stations'
import styles from './page.module.css'

export default function DirectionsPage() {
  const searchParams = useSearchParams()
  const router = useRouter()
  const startId = searchParams.get('startId')
  const endId = searchParams.get('endId')
  const stationId = searchParams.get('stationId') // 기존 호환성 유지
  
  const [startLocation, setStartLocation] = useState<{ lat: number; lng: number; name: string } | null>(null)
  const [endLocation, setEndLocation] = useState<{ lat: number; lng: number; name: string } | null>(null)
  const [distance, setDistance] = useState<number>(0)
  const [estimatedTime, setEstimatedTime] = useState<number>(0)
  const [routePath, setRoutePath] = useState<string>('')

  useEffect(() => {
    const loggedIn = localStorage.getItem('isLoggedIn')
    if (loggedIn !== 'true') {
      router.push('/auth')
      return
    }
    
    const stations = getStations()
    
    // 출발지 설정
    if (startId) {
      const startStation = stations.find(s => s.id === startId)
      if (startStation) {
        setStartLocation({
          lat: startStation.lat,
          lng: startStation.lng,
          name: startStation.name
        })
      }
    } else {
      // 출발지가 없으면 현재 위치 사용
      if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(
          (position) => {
            setStartLocation({
              lat: position.coords.latitude,
              lng: position.coords.longitude,
              name: '현재 위치'
            })
          },
          () => {
            setStartLocation({ lat: 37.5665, lng: 126.9780, name: '현재 위치' })
          }
        )
      } else {
        setStartLocation({ lat: 37.5665, lng: 126.9780, name: '현재 위치' })
      }
    }

    // 도착지 설정
    if (endId) {
      const endStation = stations.find(s => s.id === endId)
      if (endStation) {
        setEndLocation({
          lat: endStation.lat,
          lng: endStation.lng,
          name: endStation.name
        })
      }
    } else if (stationId) {
      // 기존 호환성: stationId가 있으면 도착지로 사용
      const endStation = stations.find(s => s.id === stationId)
      if (endStation) {
        setEndLocation({
          lat: endStation.lat,
          lng: endStation.lng,
          name: endStation.name
        })
      }
    }

    // 도착지가 없으면 메인으로 리다이렉트
    if (!endId && !stationId) {
      router.push('/')
    }
  }, [startId, endId, stationId, router])

  // 거리 및 시간 계산
  useEffect(() => {
    if (startLocation && endLocation) {
      const dist = calculateDistance(
        startLocation.lat,
        startLocation.lng,
        endLocation.lat,
        endLocation.lng
      )
      const time = calculateEstimatedTime(dist)
      setDistance(dist)
      setEstimatedTime(time)

      // 경로 경로 생성 (간단한 곡선)
      const startX = 20
      const startY = 80
      const endX = 80
      const endY = 20
      const midX = (startX + endX) / 2
      const midY = (startY + endY) / 2 - 20 // 곡선을 위해 조정
      setRoutePath(`M ${startX} ${startY} Q ${midX} ${midY}, ${endX} ${endY}`)
    }
  }, [startLocation, endLocation])

  if (!startLocation || !endLocation) {
    return (
      <div className={styles.container}>
        <div className={styles.loading}>로딩 중...</div>
      </div>
    )
  }

  return (
    <div className={styles.container}>
      <div className={styles.topBar}>
        <div className={styles.inputGroup}>
          <label className={styles.label}>시작점</label>
          <input
            type="text"
            className={styles.input}
            value={startLocation.name}
            readOnly
          />
        </div>
        <div className={styles.inputGroup}>
          <label className={styles.label}>종료점</label>
          <input
            type="text"
            className={styles.input}
            value={endLocation.name}
            readOnly
          />
        </div>
      </div>

      <div className={styles.mapContainer}>
        <div id="map" className={styles.map}>
          {/* 경로 라인 */}
          {routePath && (
            <svg className={styles.routeLine} viewBox="0 0 100 100" preserveAspectRatio="none">
              <path
                d={routePath}
                fill="none"
                stroke="#007AFF"
                strokeWidth="2.5"
                strokeLinecap="round"
                strokeLinejoin="round"
              />
            </svg>
          )}
          
          {/* 시작점 마커 */}
          <div className={styles.startMarker}>
            <div className={styles.markerDot} />
            <span className={styles.markerLabel}>출발</span>
          </div>
          
          {/* 종료점 마커 */}
          <div className={styles.endMarker}>
            <div className={styles.markerPin} />
            <span className={styles.markerLabel}>{endLocation.name}</span>
          </div>
        </div>
      </div>

      <div className={styles.bottomPanel}>
        <div className={styles.routeInfo}>
          <div className={styles.infoItem}>
            <span className={styles.infoLabel}>예상 시간</span>
            <span className={styles.infoValue}>{estimatedTime}분</span>
          </div>
          <div className={styles.infoItem}>
            <span className={styles.infoLabel}>거리</span>
            <span className={styles.infoValue}>{distance.toFixed(1)}km</span>
          </div>
        </div>
        <button className={styles.cancelButton} onClick={() => router.back()}>
          취소
        </button>
      </div>
    </div>
  )
}

