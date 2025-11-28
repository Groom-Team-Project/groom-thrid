'use client'

import { useState } from 'react'
import { useRouter } from 'next/navigation'
import styles from './page.module.css'

export default function AuthPage() {
  const router = useRouter()
  const [userType, setUserType] = useState<'user' | 'guardian' | 'admin' | null>(null)
  const [authMode, setAuthMode] = useState<'signup' | 'login' | null>(null)

  if (!userType) {
    return (
      <div className={styles.container}>
        <div className={styles.header}>
          <button className={styles.backButton} onClick={() => router.push('/')}>
            ←
          </button>
          <h1 className={styles.title}>WheelFinder</h1>
          <div className={styles.placeholder} />
        </div>
        <div className={styles.content}>
          <h2 className={styles.subtitle}>계정 유형을 선택해주세요</h2>
          <div className={styles.typeButtons}>
            <button
              className={styles.typeButton}
              onClick={() => setUserType('user')}
            >
              <span className={styles.typeIcon}>👤</span>
              <span className={styles.typeLabel}>사용자</span>
              <span className={styles.typeDescription}>전동보장구 사용자</span>
            </button>
            <button
              className={styles.typeButton}
              onClick={() => setUserType('guardian')}
            >
              <span className={styles.typeIcon}>🛡️</span>
              <span className={styles.typeLabel}>보호자</span>
              <span className={styles.typeDescription}>보호자 계정</span>
            </button>
            <button
              className={styles.typeButton}
              onClick={() => setUserType('admin')}
            >
              <span className={styles.typeIcon}>👨‍💼</span>
              <span className={styles.typeLabel}>관리자</span>
              <span className={styles.typeDescription}>관리자 계정</span>
            </button>
          </div>
        </div>
      </div>
    )
  }

  if (!authMode) {
    return (
      <div className={styles.container}>
        <div className={styles.header}>
          <button className={styles.backButton} onClick={() => setUserType(null)}>
            ←
          </button>
          <h1 className={styles.title}>
            {userType === 'user' ? '사용자' : userType === 'guardian' ? '보호자' : '관리자'}
          </h1>
          <div className={styles.placeholder} />
        </div>
        <div className={styles.content}>
          <div className={styles.modeButtons}>
            {userType !== 'admin' && (
              <button
                className={styles.modeButton}
                onClick={() => setAuthMode('signup')}
              >
                회원가입
              </button>
            )}
            <button
              className={styles.modeButton}
              onClick={() => setAuthMode('login')}
            >
              로그인
            </button>
          </div>
        </div>
      </div>
    )
  }

  // 회원가입 또는 로그인 폼으로 리다이렉트
  if (authMode === 'signup') {
    router.push(`/signup?type=${userType}`)
    return null
  } else {
    router.push(`/login?type=${userType}`)
    return null
  }
}

