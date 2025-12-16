'use client'

export const dynamic = 'force-dynamic'

import { useState, useEffect } from 'react'
import { useRouter } from 'next/navigation'
import MapView from '@/components/MapView'
import TopBar from '@/components/TopBar'
import BottomNav from '@/components/BottomNav'
import SplashScreen from '@/components/SplashScreen'
import { useLocation } from '@/providers/LocationProvider'
import { createAlert } from '@/lib/notification'
import { Role } from '@/lib/auth'
import styles from './page.module.css'

interface FacilityTypeItem {
    name: string
    label: string
}

export default function Home() {
  const router = useRouter()
  const location = useLocation()
  const [selectedCategory, setSelectedCategory] = useState<string | null>(null)
  const [isLoggedIn, setIsLoggedIn] = useState(false)
  const [userRole, setUserRole] = useState<string | null>(null)
  const [isAlertLoading, setIsAlertLoading] = useState(false)

  const [facilityTypes, setFacilityTypes] = useState<FacilityTypeItem[]>([])

  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    const loggedIn = localStorage.getItem('isLoggedIn')
    const role = localStorage.getItem('userRole')

    if (loggedIn === 'true') {
      setIsLoggedIn(true)
      setUserRole(role)
    }
  }, [router])

  // 서버에서 활성화된 facility type 목록을 가져옴
  useEffect(() => {
    const load = async () => {
      try {
        const res = await fetch('/api/v1/opendata/facilityTypes')
        if (!res.ok) return

        const json = await res.json() as {
            status?: string
            code?: number
            message?: string
            data?: FacilityTypeItem[] | null
            errors?: any
        }

        const items = Array.isArray(json?.data) ? json.data : []
        setFacilityTypes(items)
      } catch (e) {
        console.error('facility types load failed', e)
      } finally {
          // 최소 2초 표시 후 로딩 완료
          setTimeout(() => {
              setIsLoading(false)
          }, 2000)
      }
   }
   load()
  }, [])

    if (isLoading) {
        return <SplashScreen />
    }

  const handleAlertClick = async () => {
    if (isAlertLoading) return

    try {
      setIsAlertLoading(true)

      const geocoder = new window.kakao.maps.services.Geocoder()

      geocoder.coord2Address(location.lng, location.lat, async (result: any, status: any) => {
        if (status === window.kakao.maps.services.Status.OK && result[0]) {
          const address = result[0].address.address_name

          await createAlert({
            lat: location.lat,
            lng: location.lng,
            address: address,
          })

          alert('알림이 전송되었습니다!')
        } else {
          await createAlert({
            lat: location.lat,
            lng: location.lng,
            address: '주소를 가져올 수 없습니다',
          })

          alert('알림이 전송되었습니다! (주소 정보 없음)')
        }
      })
    } catch (error) {
      console.error('알림 전송 실패:', error)
      alert('알림 전송에 실패했습니다.')
    } finally {
      setIsAlertLoading(false)
    }
  }

  const showAlertButton = isLoggedIn && userRole === Role.USER

  return (
    <div className={styles.container}>
      <TopBar
        selectedCategory={selectedCategory}
        facilityTypes={facilityTypes}
        onCategoryChange={setSelectedCategory}
      />
      <MapView selectedCategory={selectedCategory} />
      <BottomNav />

      {showAlertButton && (
        <button
          className={styles.alertButton}
          onClick={handleAlertClick}
          disabled={isAlertLoading}
        >
          {isAlertLoading ? '전송 중...' : '🚨 긴급 알림'}
        </button>
      )}
    </div>
  )
}


