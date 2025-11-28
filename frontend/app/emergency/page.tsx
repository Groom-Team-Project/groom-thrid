'use client'

import { useState, useEffect } from 'react'
import { useRouter } from 'next/navigation'
import { getAlertsByGuardianEmail, markAlertAsRead, deleteAlert, type EmergencyAlert } from '@/lib/alerts'
import BottomNav from '@/components/BottomNav'
import styles from './page.module.css'

export default function EmergencyPage() {
  const router = useRouter()
  const [alerts, setAlerts] = useState<EmergencyAlert[]>([])
  const [selectedAlert, setSelectedAlert] = useState<EmergencyAlert | null>(null)
  const [showDeleteModal, setShowDeleteModal] = useState(false)
  const [alertToDelete, setAlertToDelete] = useState<string | null>(null)

  useEffect(() => {
    const loggedIn = localStorage.getItem('isLoggedIn')
    if (loggedIn !== 'true') {
      router.push('/auth')
      return
    }

    const guardianEmail = localStorage.getItem('userEmail') || ''
    const userAlerts = getAlertsByGuardianEmail(guardianEmail)
    setAlerts(userAlerts)
  }, [router])

  const handleAlertClick = (alert: EmergencyAlert) => {
    setSelectedAlert(alert)
    if (!alert.read) {
      markAlertAsRead(alert.id)
      setAlerts(prev => prev.map(a => a.id === alert.id ? { ...a, read: true } : a))
    }
  }

  const handleDelete = (alertId: string, e: React.MouseEvent) => {
    e.stopPropagation()
    setAlertToDelete(alertId)
    setShowDeleteModal(true)
  }

  const confirmDelete = () => {
    if (alertToDelete) {
      deleteAlert(alertToDelete)
      setAlerts(prev => prev.filter(a => a.id !== alertToDelete))
      if (selectedAlert?.id === alertToDelete) {
        setSelectedAlert(null)
      }
      setShowDeleteModal(false)
      setAlertToDelete(null)
    }
  }

  const cancelDelete = () => {
    setShowDeleteModal(false)
    setAlertToDelete(null)
  }

  const handleViewLocation = (alert: EmergencyAlert) => {
    router.push(`/guardian?lat=${alert.lat}&lng=${alert.lng}&alert=true`)
  }

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <button className={styles.backButton} onClick={() => router.back()}>
          ←
        </button>
        <h1 className={styles.title}>긴급 알림</h1>
        <div className={styles.placeholder} />
      </div>

      <div className={styles.content}>
        {alerts.length === 0 ? (
          <div className={styles.emptyState}>
            <p className={styles.emptyText}>받은 긴급 알림이 없습니다.</p>
          </div>
        ) : (
          <div className={styles.alertList}>
            {alerts.map((alert) => (
              <div
                key={alert.id}
                className={`${styles.alertItem} ${!alert.read ? styles.unread : ''}`}
                onClick={() => handleAlertClick(alert)}
              >
                <div className={styles.alertContent}>
                  <div className={styles.alertHeader}>
                    <span className={styles.alertIcon}>🚨</span>
                    <div className={styles.alertInfo}>
                      <span className={styles.alertUserName}>{alert.userName}</span>
                      <span className={styles.alertDateTime}>
                        {alert.date} {alert.time}
                      </span>
                    </div>
                  </div>
                  <p className={styles.alertMessage}>도움 요청</p>
                </div>
                <div className={styles.alertActions}>
                  <button
                    className={styles.locationButton}
                    onClick={(e) => {
                      e.stopPropagation()
                      handleViewLocation(alert)
                    }}
                    title="위치 보기"
                  >
                    📍
                  </button>
                  <button
                    className={styles.deleteButton}
                    onClick={(e) => handleDelete(alert.id, e)}
                    title="삭제"
                  >
                    🗑️
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* 알림 상세 모달 */}
      {selectedAlert && (
        <div className={styles.modal} onClick={() => setSelectedAlert(null)}>
          <div className={styles.modalContent} onClick={(e) => e.stopPropagation()}>
            <div className={styles.modalHeader}>
              <h3>긴급 알림 상세</h3>
              <button
                className={styles.closeButton}
                onClick={() => setSelectedAlert(null)}
              >
                ✕
              </button>
            </div>
            <div className={styles.modalBody}>
              <div className={styles.detailItem}>
                <label>사용자</label>
                <p>{selectedAlert.userName}</p>
              </div>
              <div className={styles.detailItem}>
                <label>발신 시간</label>
                <p>{selectedAlert.date} {selectedAlert.time}</p>
              </div>
              <div className={styles.detailItem}>
                <label>위치</label>
                <p>위도: {selectedAlert.lat.toFixed(6)}, 경도: {selectedAlert.lng.toFixed(6)}</p>
              </div>
              <div className={styles.modalButtons}>
                <button
                  className={styles.locationButton}
                  onClick={() => handleViewLocation(selectedAlert)}
                >
                  위치 보기
                </button>
                <button
                  className={styles.deleteButton}
                  onClick={() => {
                    setSelectedAlert(null)
                    handleDelete(selectedAlert.id, { stopPropagation: () => {} } as React.MouseEvent)
                  }}
                >
                  삭제
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* 삭제 확인 모달 */}
      {showDeleteModal && (
        <div className={styles.modal} onClick={cancelDelete}>
          <div className={styles.modalContent} onClick={(e) => e.stopPropagation()}>
            <div className={styles.modalHeader}>
              <h3>알림 삭제 확인</h3>
              <button
                className={styles.closeButton}
                onClick={cancelDelete}
              >
                ✕
              </button>
            </div>
            <div className={styles.modalBody}>
              <p className={styles.deleteMessage}>
                정말로 이 알림을 삭제하시겠습니까?
              </p>
              <div className={styles.modalButtons}>
                <button
                  className={styles.cancelButton}
                  onClick={cancelDelete}
                >
                  취소
                </button>
                <button
                  className={styles.deleteConfirmButton}
                  onClick={confirmDelete}
                >
                  삭제
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      <BottomNav />
    </div>
  )
}

