'use client'

import { useState } from 'react'
import { useRouter } from 'next/navigation'
import { matchGuardian } from '@/lib/guardian'
import { refreshAccessToken } from '@/lib/auth'
import styles from './page.module.css'

export default function GuardianRegisterPage() {
  const router = useRouter()
  const [guardianEmail, setGuardianEmail] = useState('')
  const [error, setError] = useState('')

  const validateEmail = (email: string) => {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
    return emailRegex.test(email)
  }

  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')

    if (!guardianEmail) {
      setError('보호자 이메일을 입력해주세요.')
      return
    }

    if (!validateEmail(guardianEmail)) {
      setError('올바른 이메일 형식을 입력해주세요.')
      return
    }

    try {
      // 백엔드 API 호출: 보호자 연결
      await matchGuardian(guardianEmail.trim())

      // 보호자 연결 성공 후 JWT 재발급 (relationId 업데이트를 위해)
      try {
        await refreshAccessToken()
        console.log('JWT 재발급 성공 - relationId가 업데이트되었습니다.')
      } catch (refreshError) {
        console.error('JWT 재발급 실패:', refreshError)
        // 재발급 실패해도 보호자 연결은 성공했으므로 계속 진행
      }

      alert('보호자 연동이 완료되었습니다.')
      router.push('/profile')
    } catch (error) {
      console.error('보호자 연결 실패:', error)
      setError(error instanceof Error ? error.message : '보호자 연결에 실패했습니다.')
    }
  }

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <button className={styles.backButton} onClick={() => router.back()}>
          ←
        </button>
        <h1 className={styles.title}>보호자 등록</h1>
        <div className={styles.placeholder} />
      </div>

      <form className={styles.form} onSubmit={handleRegister}>
        <div className={styles.inputGroup}>
          <label className={styles.label}>보호자 이메일</label>
          <input
            type="email"
            className={styles.input}
            placeholder="guardian@example.com"
            value={guardianEmail}
            onChange={(e) => setGuardianEmail(e.target.value)}
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
            등록
          </button>
        </div>
      </form>
    </div>
  )
}

