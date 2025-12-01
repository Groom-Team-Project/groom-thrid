'use client'

import { useState, useEffect } from 'react'
import { useRouter, useSearchParams } from 'next/navigation'
import styles from './page.module.css'

export default function LoginPage() {
  const router = useRouter()
  const searchParams = useSearchParams()
  const userType = searchParams?.get('type') || 'user'
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')

  useEffect(() => {
    // 타입이 없으면 인증 선택 페이지로 리다이렉트
    if (!searchParams?.get('type')) {
      router.push('/auth')
    }
  }, [searchParams, router])

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')

    if (!email || !password) {
      setError('이메일과 비밀번호를 입력해주세요.')
      return
    }

    // 실제로는 API 호출
    // 여기서는 localStorage에 사용자 정보 저장
    localStorage.setItem('userEmail', email)
    localStorage.setItem('userType', userType)
    localStorage.setItem('isLoggedIn', 'true')
    
    if (userType === 'guardian') {
      router.push('/guardian')
    } else if (userType === 'admin') {
      router.push('/admin')
    } else {
      router.push('/')
    }
  }

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <button className={styles.backButton} onClick={() => router.back()}>
          ←
        </button>
        <h1 className={styles.title}>
          {userType === 'user' ? '사용자 로그인' : userType === 'guardian' ? '보호자 로그인' : '관리자 로그인'}
        </h1>
        <div className={styles.placeholder} />
      </div>

      <form className={styles.form} onSubmit={handleLogin}>
        <div className={styles.inputGroup}>
          <input
            type="email"
            className={styles.input}
            placeholder="이메일"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
          />
        </div>

        <div className={styles.inputGroup}>
          <input
            type="password"
            className={styles.input}
            placeholder="비밀번호"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />
        </div>

        {error && <p className={styles.error}>{error}</p>}

        <div className={styles.buttonGroup}>
          <button
            type="button"
            className={styles.cancelButton}
            onClick={() => router.back()}
          >
            취소
          </button>
          <button type="submit" className={styles.submitButton}>
            로그인
          </button>
        </div>
      </form>
    </div>
  )
}

