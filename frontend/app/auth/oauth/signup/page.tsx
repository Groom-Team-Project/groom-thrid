'use client'

import { useState, useEffect } from 'react'
import { useRouter, useSearchParams } from 'next/navigation'
import styles from '../../../auth/page.module.css'
import { oauthSignup, Role, Provider, getUserType } from '@/lib/auth'

export default function OAuthSignupPage() {
  const router = useRouter()
  const searchParams = useSearchParams()

  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  // URL에서 전달된 정보
  const [provider, setProvider] = useState<Provider | null>(null)
  const [providerId, setProviderId] = useState('')
  const [email, setEmail] = useState('')
  const [name, setName] = useState('')

  // 사용자 입력 정보
  const [role, setRole] = useState<Role>(Role.USER)
  const [phone, setPhone] = useState('')

  useEffect(() => {
    // URL 파라미터에서 정보 추출
    const providerParam = searchParams.get('provider')
    const providerIdParam = searchParams.get('providerId')
    const emailParam = searchParams.get('email')
    const nameParam = searchParams.get('name')

    if (!providerParam || !providerIdParam) {
      setError('잘못된 접근입니다.')
      setTimeout(() => router.push('/auth'), 2000)
      return
    }

    // Provider enum 매핑
    let mappedProvider: Provider
    switch (providerParam) {
      case 'Naver':
        mappedProvider = Provider.NAVER
        break
      case 'Google':
        mappedProvider = Provider.GOOGLE
        break
      case 'Kakao':
        mappedProvider = Provider.KAKAO
        break
      default:
        setError('알 수 없는 OAuth 제공자입니다.')
        setTimeout(() => router.push('/auth'), 2000)
        return
    }

    setProvider(mappedProvider)
    setProviderId(providerIdParam)
    setEmail(emailParam || '')
    setName(nameParam || '')
  }, [searchParams, router])

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')

    // Validation
    if (!name) {
      setError('이름을 입력해주세요.')
      return
    }

    // 이메일은 선택사항 (OAuth에서 제공하지 않을 수 있음)
    // 사용자가 직접 입력 가능

    if (!provider || !providerId) {
      setError('OAuth 정보가 누락되었습니다.')
      return
    }

    setLoading(true)

    try {
      await oauthSignup({
        name,
        email,
        phone: phone || undefined,
        role,
        provider,
        providerId,
      })

      // 회원가입 성공 후 리다이렉트
      const userType = getUserType()
      if (userType === 'guardian') {
        router.push('/guardian')
      } else {
        router.push('/')
      }
    } catch (err) {
      if (err instanceof Error) {
        setError(err.message)
      } else {
        setError('회원가입에 실패했습니다. 다시 시도해주세요.')
      }
    } finally {
      setLoading(false)
    }
  }

  const getProviderName = () => {
    switch (provider) {
      case Provider.NAVER:
        return '네이버'
      case Provider.GOOGLE:
        return '구글'
      case Provider.KAKAO:
        return '카카오'
      default:
        return 'OAuth'
    }
  }

  if (!provider) {
    return (
      <div style={{
        width: '100vw',
        height: '100vh',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        background: 'white',
      }}>
        <p style={{ color: '#666', fontSize: '16px' }}>
          페이지를 불러오는 중...
        </p>
      </div>
    )
  }

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <button className={styles.backButton} onClick={() => router.push('/auth')}>
          ←
        </button>
        <h1 className={styles.title}>
          {getProviderName()} 회원가입
        </h1>
        <div className={styles.placeholder} />
      </div>

      <div className={styles.content}>
        <form className={styles.form} onSubmit={handleSubmit}>
          <div style={{
            padding: '16px',
            borderRadius: '8px',
            background: '#f5f5f5',
            marginBottom: '16px',
          }}>
            <p style={{ fontSize: '14px', color: '#666', margin: 0 }}>
              {getProviderName()} 계정으로 로그인하시려면 추가 정보를 입력해주세요.
            </p>
          </div>

          <div className={styles.roleSelector}>
            <button
              type="button"
              className={`${styles.roleButton} ${role === Role.USER ? styles.active : ''}`}
              onClick={() => setRole(Role.USER)}
            >
              👤 사용자
            </button>
            <button
              type="button"
              className={`${styles.roleButton} ${role === Role.GUARDIAN ? styles.active : ''}`}
              onClick={() => setRole(Role.GUARDIAN)}
            >
              🛡️ 보호자
            </button>
          </div>

          <div className={styles.inputGroup}>
            <label className={styles.label}>이름</label>
            <input
              type="text"
              className={styles.input}
              placeholder="이름을 입력하세요"
              value={name}
              onChange={(e) => setName(e.target.value)}
            />
          </div>

          <div className={styles.inputGroup}>
            <label className={styles.label}>이메일 (선택)</label>
            <input
              type="email"
              className={styles.input}
              placeholder="example@email.com (선택사항)"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
            />
          </div>

          <div className={styles.inputGroup}>
            <label className={styles.label}>전화번호 (선택)</label>
            <input
              type="tel"
              className={styles.input}
              placeholder="010-1234-5678"
              value={phone}
              onChange={(e) => setPhone(e.target.value)}
            />
          </div>

          {error && <p className={styles.error}>{error}</p>}

          <button
            type="submit"
            className={styles.submitButton}
            disabled={loading}
          >
            {loading ? '처리 중...' : '회원가입 완료'}
          </button>
        </form>

        <div className={styles.switchContainer}>
          <span className={styles.switchText}>
            이미 계정이 있으신가요?
          </span>
          <span className={styles.switchLink} onClick={() => router.push('/auth')}>
            로그인
          </span>
        </div>
      </div>
    </div>
  )
}
