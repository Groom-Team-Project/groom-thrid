'use client'

import { useSearchParams, useRouter } from 'next/navigation'
import { useState, useEffect } from 'react'
import { getGuardianRequestsByGuardianEmail, updateGuardianRequest, type GuardianRequest } from '@/lib/guardian'
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
  const [pendingRequests, setPendingRequests] = useState<GuardianRequest[]>([])
  const [showRequestModal, setShowRequestModal] = useState(false)
  const [selectedRequest, setSelectedRequest] = useState<GuardianRequest | null>(null)
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

    // 보호자 이메일로 연동 요청 목록 가져오기
    const guardianEmail = localStorage.getItem('userEmail') || ''
    const requests = getGuardianRequestsByGuardianEmail(guardianEmail)
    const pending = requests.filter(r => r.status === 'pending')
    setPendingRequests(pending)

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
  }, [alert, lat, lng])

  const handleCloseAlert = () => {
    setShowAlert(false)
  }

  const handleRequestClick = (request: GuardianRequest) => {
    setSelectedRequest(request)
    setShowRequestModal(true)
  }

  const handleApproveRequest = () => {
    if (!selectedRequest) return
    
    const updated = updateGuardianRequest(selectedRequest.id, 'approved')
    if (updated) {
      setPendingRequests(prev => prev.filter(r => r.id !== selectedRequest.id))
      setShowRequestModal(false)
      setSelectedRequest(null)
      alert('연동 요청이 승인되었습니다.')
    }
  }

  const handleRejectRequest = () => {
    if (!selectedRequest) return
    
    if (confirm('연동 요청을 거절하시겠습니까?')) {
      const updated = updateGuardianRequest(selectedRequest.id, 'rejected')
      if (updated) {
        setPendingRequests(prev => prev.filter(r => r.id !== selectedRequest.id))
        setShowRequestModal(false)
        setSelectedRequest(null)
        alert('연동 요청이 거절되었습니다.')
      }
    }
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

        {/* 연동 요청 알림 */}
        {pendingRequests.length > 0 && (
          <div className={styles.requestBanner}>
            <div className={styles.requestContent}>
              <span className={styles.requestIcon}>🔔</span>
              <span className={styles.requestText}>
                연동 요청 {pendingRequests.length}개
              </span>
            </div>
            <button 
              className={styles.requestButton}
              onClick={() => pendingRequests.length > 0 && handleRequestClick(pendingRequests[0])}
            >
              확인
            </button>
          </div>
        )}
      </div>

      {/* 연동 요청 모달 */}
      {showRequestModal && selectedRequest && (
        <div className={styles.modal} onClick={() => setShowRequestModal(false)}>
          <div className={styles.modalContent} onClick={(e) => e.stopPropagation()}>
            <div className={styles.modalHeader}>
              <h3>연동 요청</h3>
              <button
                className={styles.closeButton}
                onClick={() => {
                  setShowRequestModal(false)
                  setSelectedRequest(null)
                }}
              >
                ✕
              </button>
            </div>
            <div className={styles.modalBody}>
              <div className={styles.requestInfo}>
                <p className={styles.requestLabel}>사용자 이름</p>
                <p className={styles.requestValue}>{selectedRequest.userName}</p>
              </div>
              <div className={styles.requestInfo}>
                <p className={styles.requestLabel}>사용자 이메일</p>
                <p className={styles.requestValue}>{selectedRequest.userEmail}</p>
              </div>
              <div className={styles.requestInfo}>
                <p className={styles.requestLabel}>요청일</p>
                <p className={styles.requestValue}>{selectedRequest.requestDate}</p>
              </div>
              <div className={styles.requestMessage}>
                <p>{selectedRequest.userName}님이 보호자 연동을 요청했습니다.</p>
              </div>
              <div className={styles.modalButtons}>
                <button
                  className={styles.rejectButton}
                  onClick={handleRejectRequest}
                >
                  거절
                </button>
                <button
                  className={styles.approveButton}
                  onClick={handleApproveRequest}
                >
                  승인
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

