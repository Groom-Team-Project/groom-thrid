'use client'

import { useState, useEffect } from 'react'
import { useRouter } from 'next/navigation'
import { getGuardianRequestsByGuardianEmail, type GuardianRequest } from '@/lib/guardian'
import BottomNav from '@/components/BottomNav'
import styles from './page.module.css'

export default function ProfilePage() {
  const router = useRouter()
  const [userName, setUserName] = useState('')
  const [userEmail, setUserEmail] = useState('')
  const [guardianEmail, setGuardianEmail] = useState('')
  const [isLoggedIn, setIsLoggedIn] = useState(false)
  const [userType, setUserType] = useState<string | null>(null)
  const [connectedUsers, setConnectedUsers] = useState<GuardianRequest[]>([])

  useEffect(() => {
    const loggedIn = localStorage.getItem('isLoggedIn')
    if (loggedIn === 'true') {
      setIsLoggedIn(true)
      setUserName(localStorage.getItem('userName') || '')
      setUserEmail(localStorage.getItem('userEmail') || '')
      setGuardianEmail(localStorage.getItem('guardianEmail') || '')
      
      const type = localStorage.getItem('userType')
      setUserType(type)
      
      // 보호자 타입인 경우 연동된 사용자 목록 가져오기
      if (type === 'guardian') {
        const guardianEmail = localStorage.getItem('userEmail') || ''
        const requests = getGuardianRequestsByGuardianEmail(guardianEmail)
        const approved = requests.filter(r => r.status === 'approved')
        setConnectedUsers(approved)
      }
    } else {
      router.push('/login')
    }
  }, [router])

  const handleLogout = () => {
    localStorage.removeItem('isLoggedIn')
    localStorage.removeItem('userName')
    localStorage.removeItem('userEmail')
    router.push('/login')
  }

  const handleGuardianClick = () => {
    router.push('/guardian/register')
  }

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <h1 className={styles.title}>마이페이지</h1>
      </div>

      <div className={styles.content}>
        {/* 사용자 정보 */}
        <div className={styles.section}>
          <div className={styles.sectionHeader}>
            <h2 className={styles.sectionTitle}>사용자 정보</h2>
          </div>
          <div className={styles.infoBlock}>
            <div className={styles.infoItem}>
              <span className={styles.infoLabel}>이름</span>
              <span className={styles.infoValue}>{userName || '미설정'}</span>
            </div>
            <div className={styles.infoItem}>
              <span className={styles.infoLabel}>이메일</span>
              <span className={styles.infoValue}>{userEmail || '미설정'}</span>
            </div>
          </div>
        </div>

        {/* 사용자 타입인 경우에만 보호자 등록 섹션 표시 */}
        {userType === 'user' && (
          <>
            {/* 정보 수정 */}
            <div className={styles.section}>
              <div className={styles.sectionHeader}>
                <h2 className={styles.sectionTitle}>정보 수정</h2>
              </div>
              <div className={styles.infoBlock}>
                <button className={styles.guardianButton} onClick={handleGuardianClick}>
                  <span className={styles.buttonText}>보호자</span>
                  {guardianEmail && (
                    <span className={styles.guardianEmail}>{guardianEmail}</span>
                  )}
                  <span className={styles.arrow}>→</span>
                </button>
              </div>
            </div>

            {/* 연동된 보호자 */}
            {guardianEmail && (
              <div className={styles.section}>
                <div className={styles.sectionHeader}>
                  <h2 className={styles.sectionTitle}>연동된 보호자</h2>
                </div>
                <div className={styles.infoBlock}>
                  <div className={styles.guardianInfo}>
                    <span className={styles.guardianLabel}>보호자 이메일</span>
                    <span className={styles.guardianValue}>{guardianEmail}</span>
                  </div>
                </div>
              </div>
            )}
          </>
        )}

        {/* 보호자 타입인 경우 연동된 사용자 목록 표시 */}
        {userType === 'guardian' && (
          <div className={styles.section}>
            <div className={styles.sectionHeader}>
              <h2 className={styles.sectionTitle}>연동된 사용자</h2>
            </div>
            <div className={styles.infoBlock}>
              {connectedUsers.length === 0 ? (
                <div className={styles.emptyUsers}>
                  <p className={styles.emptyText}>연동된 사용자가 없습니다.</p>
                </div>
              ) : (
                connectedUsers.map((request) => (
                  <div key={request.id} className={styles.userItem}>
                    <div className={styles.userInfo}>
                      <span className={styles.userName}>{request.userName}</span>
                      <span className={styles.userEmail}>{request.userEmail}</span>
                    </div>
                    <span className={styles.connectedDate}>연동일: {request.responseDate || request.requestDate}</span>
                  </div>
                ))
              )}
            </div>
          </div>
        )}

        {/* 로그아웃 */}
        <div className={`${styles.section} ${styles.logoutSection}`}>
          <button className={styles.logoutButton} onClick={handleLogout}>
            로그아웃
          </button>
        </div>
      </div>

      <BottomNav />
    </div>
  )
}

