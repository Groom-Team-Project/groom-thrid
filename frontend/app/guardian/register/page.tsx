'use client'

import { useState, useEffect } from 'react'
import { useRouter } from 'next/navigation'
import { saveGuardianRequest, getGuardianRequestByUserId, type GuardianRequest } from '@/lib/guardian'
import styles from './page.module.css'

export default function GuardianRegisterPage() {
  const router = useRouter()
  const [guardianEmail, setGuardianEmail] = useState('')
  const [error, setError] = useState('')
  const [currentRequest, setCurrentRequest] = useState<GuardianRequest | null>(null)

  useEffect(() => {
    const userId = localStorage.getItem('userEmail') || ''
    const request = getGuardianRequestByUserId(userId)
    
    if (request) {
      setCurrentRequest(request)
      setGuardianEmail(request.guardianEmail)
    } else {
      const savedEmail = localStorage.getItem('guardianEmail')
      if (savedEmail) {
        setGuardianEmail(savedEmail)
      }
    }
  }, [])

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

    const userId = localStorage.getItem('userEmail') || ''
    const userName = localStorage.getItem('userName') || '사용자'
    const userEmail = localStorage.getItem('userEmail') || ''

    // 보호자 연동 요청 생성
    const request = saveGuardianRequest({
      userId,
      userName,
      userEmail,
      guardianEmail: guardianEmail.trim(),
    })
    
    if (request.status === 'pending') {
      alert('보호자에게 연동 요청이 전송되었습니다. 보호자가 승인하면 연동이 완료됩니다.')
    } else {
      alert('보호자 이메일이 등록되었습니다.')
    }
    
    router.push('/profile')
  }

  const handleRemove = () => {
    if (confirm('보호자 연동을 해제하시겠습니까?')) {
      localStorage.removeItem('guardianEmail')
      setGuardianEmail('')
      setCurrentRequest(null)
      alert('보호자 연동이 해제되었습니다.')
      router.push('/profile')
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
          {currentRequest && (
            <div className={styles.statusInfo}>
              {currentRequest.status === 'pending' && (
                <p className={styles.pendingStatus}>
                  승인 대기 중: {currentRequest.guardianEmail}
                </p>
              )}
              {currentRequest.status === 'approved' && (
                <p className={styles.approvedStatus}>
                  연동 완료: {currentRequest.guardianEmail}
                </p>
              )}
              {currentRequest.status === 'rejected' && (
                <p className={styles.rejectedStatus}>
                  거절됨: {currentRequest.guardianEmail}
                </p>
              )}
            </div>
          )}
        </div>

        {error && <p className={styles.error}>{error}</p>}

        <div className={styles.buttonGroup}>
          {currentRequest && currentRequest.status === 'approved' && (
            <button
              type="button"
              className={styles.removeButton}
              onClick={handleRemove}
            >
              연동 해제
            </button>
          )}
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

