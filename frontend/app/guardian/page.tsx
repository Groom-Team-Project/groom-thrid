'use client'

export const dynamic = 'force-dynamic'

import { useSearchParams, useRouter } from 'next/navigation'
import { useState, useEffect } from 'react'
import MapView from '@/components/MapView'
import TopBar from '@/components/TopBar'
import BottomNav from '@/components/BottomNav'
import styles from './page.module.css'

export default function GuardianPage() {
  const searchParams = useSearchParams()
  const router = useRouter()
  const lat = searchParams.get('lat')
  const lng = searchParams.get('lng')
  const alert = searchParams.get('alert')
  const email = searchParams.get('email')

  const [userLocation, setUserLocation] = useState<{ lat: number; lng: number } | null>(null)
  const [showAlert, setShowAlert] = useState(false)
  const [alertLocation, setAlertLocation] = useState<{ lat: number; lng: number } | null>(null)
  const [userName, setUserName] = useState('준영')
  const [selectedCategory, setSelectedCategory] = useState<'charging' | 'restroom' | null>(null)

  useEffect(() => {
    // 로그인 및 사용자 타입 확인
    const loggedIn = localStorage.getItem('isLoggedIn')
    const userRole = localStorage.getItem('userRole')

    if (loggedIn !== 'true' || userRole !== 'GUARDIAN') {
      // 로그인하지 않았거나 보호자 타입이 아니면 인증 페이지로 리다이렉트
      router.push('/auth')
      return
    }

    // 사용자 이름 가져오기
    const savedName = localStorage.getItem('userName')
    if (savedName) {
      setUserName(savedName)
    }

    // 알림이 있는 경우
    if (alert === 'true' && lat && lng) {
      setShowAlert(true)
      setAlertLocation({ lat: parseFloat(lat), lng: parseFloat(lng) })

      // 이메일이 있으면 알림 전송 로그 (실제로는 API 호출)
      if (email) {
        console.log(`긴급 알림이 ${email}로 전송되었습니다.`)
      }
    }

    // 사용자 위치 가져오기
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          setUserLocation({
            lat: position.coords.latitude,
            lng: position.coords.longitude,
          })
        },
        () => {
          setUserLocation({ lat: 37.5665, lng: 126.9780 })
        }
      )
    } else {
      setUserLocation({ lat: 37.5665, lng: 126.9780 })
    }
  }, [alert, lat, lng, router])

  const handleCloseAlert = () => {
    setShowAlert(false)
  }

  return (
    <div className={styles.container}>
      <TopBar selectedCategory={selectedCategory} onCategoryChange={setSelectedCategory} />
      <MapView selectedCategory={selectedCategory} />
      <div className={styles.alertOverlay}>

        {/* 알림 배너 */}
        {showAlert && (
          <div className={styles.alertBanner}>
            <div className={styles.alertContent}>
              <span className={styles.alertIcon}>🚨</span>
              <span className={styles.alertText}>{userName}님이 도움 요청 하셨습니다.</span>
            </div>
            <button className={styles.alertCloseButton} onClick={handleCloseAlert}>
              ✕
            </button>
          </div>
        )}
      </div>

      <BottomNav />
    </div>
  )
}

