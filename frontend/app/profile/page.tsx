'use client'

import { useState, useEffect } from 'react'
import { useRouter } from 'next/navigation'
import { getStoredRelationInfo, getRelationInfo, saveRelationInfo, type RelationInfo } from '@/lib/user'
import { getRelationIdFromToken, getAccessToken } from '@/lib/api'
import { logout } from '@/lib/auth'
import BottomNav from '@/components/BottomNav'
import styles from './page.module.css'

export default function ProfilePage() {
  const router = useRouter()
  const [userName, setUserName] = useState('')
  const [userEmail, setUserEmail] = useState('')
  const [userRole, setUserRole] = useState('')
  const [isLoggedIn, setIsLoggedIn] = useState(false)
  const [userType, setUserType] = useState<string | null>(null)
  const [relationInfo, setRelationInfo] = useState<RelationInfo | null>(null)

  useEffect(() => {
    const loggedIn = localStorage.getItem('isLoggedIn')
    if (loggedIn === 'true') {
      setIsLoggedIn(true)
      setUserName(localStorage.getItem('userName') || '')
      setUserEmail(localStorage.getItem('userEmail') || '')
      setUserRole(localStorage.getItem('userRole') || '')

      // userRole을 기반으로 userType 설정
      const role = localStorage.getItem('userRole')
      const type = role === 'GUARDIAN' ? 'guardian' : 'user'
      setUserType(type)

      // 관계 정보 가져오기
      const loadRelationInfo = async () => {
        // JWT에서 relationId 확인
        const token = getAccessToken()
        if (token) {
          const relationId = getRelationIdFromToken(token)

          if (relationId !== null) {
            // relationId가 있으면 서버에서 최신 정보 가져오기
            try {
              const fetchedRelationInfo = await getRelationInfo()
              saveRelationInfo(fetchedRelationInfo)
              setRelationInfo(fetchedRelationInfo)
            } catch (error) {
              console.error('관계 정보 조회 실패:', error)
              // 서버 조회 실패 시 로컬 저장된 정보 사용
              const storedRelationInfo = getStoredRelationInfo()
              if (storedRelationInfo) {
                setRelationInfo(storedRelationInfo)
              }
            }
          } else {
            // relationId가 없으면 로컬 저장된 정보도 없어야 함
            setRelationInfo(null)
          }
        }
      }

      loadRelationInfo()
    } else {
      router.push('/login')
    }
  }, [router])

  const handleLogout = async () => {
    try {
      await logout()
      router.push('/login')
    } catch (error) {
      console.error('로그아웃 중 오류 발생:', error)
      // 에러가 발생해도 로그인 페이지로 이동
      router.push('/login')
    }
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
            <div className={styles.infoItem}>
              <span className={styles.infoLabel}>권한</span>
              <span className={styles.infoValue}>
                {userRole === 'GUARDIAN' ? '🛡️ 보호자' : userRole === 'USER' ? '👤 사용자' : '미설정'}
              </span>
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
                  {relationInfo && (
                    <span className={styles.guardianEmail}>{relationInfo.GuardianEmail}</span>
                  )}
                  <span className={styles.arrow}>→</span>
                </button>
              </div>
            </div>

            {/* 연동된 보호자 */}
            {relationInfo && (
              <div className={styles.section}>
                <div className={styles.sectionHeader}>
                  <h2 className={styles.sectionTitle}>연동된 보호자</h2>
                </div>
                <div className={styles.infoBlock}>
                  <div className={styles.infoItem}>
                    <span className={styles.infoLabel}>보호자 이름</span>
                    <span className={styles.infoValue}>{relationInfo.GuardianName}</span>
                  </div>
                  <div className={styles.infoItem}>
                    <span className={styles.infoLabel}>보호자 이메일</span>
                    <span className={styles.infoValue}>{relationInfo.GuardianEmail}</span>
                  </div>
                </div>
              </div>
            )}
          </>
        )}

        {/* 보호자 타입인 경우 연동된 사용자 표시 */}
        {userType === 'guardian' && (
          <div className={styles.section}>
            <div className={styles.sectionHeader}>
              <h2 className={styles.sectionTitle}>연동된 사용자</h2>
            </div>
            <div className={styles.infoBlock}>
              {!relationInfo ? (
                <div className={styles.emptyUsers}>
                  <p className={styles.emptyText}>연동된 사용자가 없습니다.</p>
                </div>
              ) : (
                <div
                  className={styles.userItem}
                  onClick={() => router.push('/guardian/tracking')}
                  style={{ cursor: 'pointer' }}
                >
                  <div className={styles.userInfo}>
                    <span className={styles.userName}>{relationInfo.UserName}</span>
                    <span className={styles.userEmail}>{relationInfo.UserEmail}</span>
                  </div>
                  <div className={styles.userActions}>
                    <span className={styles.arrow}>→</span>
                  </div>
                </div>
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

