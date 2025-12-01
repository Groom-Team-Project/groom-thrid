'use client'

import { useState, useEffect } from 'react'
import { useRouter, useSearchParams } from 'next/navigation'
import styles from './page.module.css'

export default function SignUpPage() {
  const router = useRouter()
  const searchParams = useSearchParams()
  const userType = searchParams?.get('type') || 'user'
  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [error, setError] = useState('')

  useEffect(() => {
    // 타입이 없으면 인증 선택 페이지로 리다이렉트
    if (!searchParams?.get('type')) {
      router.push('/auth')
    }
  }, [searchParams, router])

  const handleSignUp = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')

    if (!name || !email || !password || !confirmPassword) {
      setError('모든 필드를 입력해주세요.')
      return
    }

    if (password !== confirmPassword) {
      setError('비밀번호가 일치하지 않습니다.')
      return
    }

    if (password.length < 6) {
      setError('비밀번호는 6자 이상이어야 합니다.')
      return
    }

    // 실제로는 API 호출
    // 여기서는 localStorage에 사용자 정보 저장
    localStorage.setItem('userName', name)
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
          {userType === 'user' ? '사용자 회원가입' : userType === 'guardian' ? '보호자 회원가입' : '관리자 회원가입'}
        </h1>
        <div className={styles.placeholder} />
      </div>

      <form className={styles.form} onSubmit={handleSignUp}>
        <div className={styles.inputGroup}>
          <input
            type="text"
            className={styles.input}
            placeholder="이름"
            value={name}
            onChange={(e) => setName(e.target.value)}
          />
        </div>

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

        <div className={styles.inputGroup}>
          <input
            type="password"
            className={styles.input}
            placeholder="비밀번호 확인"
            value={confirmPassword}
            onChange={(e) => setConfirmPassword(e.target.value)}
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
            회원가입
          </button>
        </div>
      </form>
    </div>
  )
}

