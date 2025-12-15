'use client'

export const dynamic = 'force-dynamic'

import { useState, useEffect } from 'react'
import { useRouter } from 'next/navigation'
import { getAlertList, type AlertCheckResponse } from '@/lib/notification'
import BottomNav from '@/components/BottomNav'
import styles from './page.module.css'

export default function NotificationPage() {
  const router = useRouter()
  const [notifications, setNotifications] = useState<AlertCheckResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    const loggedIn = localStorage.getItem('isLoggedIn')
    if (loggedIn !== 'true') {
      router.push('/auth')
      return
    }

    loadNotifications()
  }, [router])

  const loadNotifications = async () => {
    try {
      setLoading(true)
      const data = await getAlertList()
      setNotifications(data)
      setError(null)
    } catch (err) {
      console.error('알림 목록 로드 실패:', err)
      setError('알림 목록을 불러오는데 실패했습니다.')
    } finally {
      setLoading(false)
    }
  }

  const handleNotificationClick = (notification: AlertCheckResponse) => {
    router.push(`/?lat=${notification.lat}&lng=${notification.lng}`)
  }

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <h1 className={styles.title}>긴급 알림 내역</h1>
      </div>

      <div className={styles.content}>
        {loading ? (
          <div className={styles.emptyState}>
            <p className={styles.emptyText}>로딩 중...</p>
          </div>
        ) : error ? (
          <div className={styles.emptyState}>
            <p className={styles.errorText}>{error}</p>
            <button className={styles.retryButton} onClick={loadNotifications}>
              다시 시도
            </button>
          </div>
        ) : notifications.length === 0 ? (
          <div className={styles.emptyState}>
            <p className={styles.emptyText}>알림 내역이 없습니다.</p>
          </div>
        ) : (
          <div className={styles.notificationList}>
            {notifications.map((notification, index) => (
              <div
                key={index}
                className={styles.notificationItem}
                onClick={() => handleNotificationClick(notification)}
              >
                <div className={styles.notificationIcon}>🚨</div>
                <div className={styles.notificationContent}>
                  <p className={styles.notificationAddress}>{notification.address}</p>
                  <div className={styles.notificationLocation}>
                    <span className={styles.coordinates}>
                      위도: {notification.lat.toFixed(6)}, 경도: {notification.lng.toFixed(6)}
                    </span>
                  </div>
                </div>
                <div className={styles.notificationArrow}>›</div>
              </div>
            ))}
          </div>
        )}
      </div>

      <BottomNav />
    </div>
  )
}
