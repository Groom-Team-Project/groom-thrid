'use client'

import { useState } from 'react'
import { useRouter } from 'next/navigation'
import styles from './page.module.css'
import { login, signup, validatePassword, validateEmail, Role, getUserType, Provider } from '@/lib/auth'

export default function AuthPage() {
  const router = useRouter()
  const [mode, setMode] = useState<'login' | 'signup'>('login')
  const [role, setRole] = useState<Role>(Role.USER)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  // Form fields
  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [phone, setPhone] = useState('')

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')

    // Basic validation
    if (!email || !password) {
      setError('이메일과 비밀번호를 입력해주세요.')
      return
    }

    if (!validateEmail(email)) {
      setError('올바른 이메일 형식이 아닙니다.')
      return
    }

    const passwordValidation = validatePassword(password)
    if (!passwordValidation.valid) {
      setError(passwordValidation.message || '비밀번호가 올바르지 않습니다.')
      return
    }

    if (mode === 'signup') {
      if (!name) {
        setError('이름을 입력해주세요.')
        return
      }

      if (password !== confirmPassword) {
        setError('비밀번호가 일치하지 않습니다.')
        return
      }
    }

    setLoading(true)

    try {
      if (mode === 'login') {
        await login({ email, password })

        // 로그인 성공 후 JWT에서 추출한 role에 따라 리다이렉트
        const userType = getUserType()
        if (userType === 'guardian') {
          router.push('/guardian')
        } else {
          router.push('/')
        }
      } else {
        await signup({
          name,
          email,
          password,
          phone: phone || undefined,
          role,
        })

        // 회원가입 성공 후 리다이렉트
        if (role === Role.GUARDIAN) {
          router.push('/guardian')
        } else {
          router.push('/')
        }
      }
    } catch (err) {
      if (err instanceof Error) {
        setError(err.message)
      } else {
        setError('요청이 실패했습니다. 다시 시도해주세요.')
      }
    } finally {
      setLoading(false)
    }
  }

  const toggleMode = () => {
    setMode(mode === 'login' ? 'signup' : 'login')
    setError('')
    setName('')
    setPassword('')
    setConfirmPassword('')
    setPhone('')
  }

  // 소셜 로그인 핸들러 - 백엔드 OAuth callback URL 사용
  const handleSocialLogin = (provider: Provider) => {
    const backendUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api'
    const backendRedirectUri = `${backendUrl}/v1/auth/oauth/callback/${provider.toLowerCase()}`
    let authUrl = ''
    let clientId = ''

    switch (provider) {
      case Provider.NAVER:
        clientId = process.env.NEXT_PUBLIC_NAVER_CLIENT_ID || ''
        authUrl = `https://nid.naver.com/oauth2.0/authorize?response_type=code&client_id=${clientId}&redirect_uri=${backendRedirectUri}&state=naver`
        break
      case Provider.GOOGLE:
        clientId = process.env.NEXT_PUBLIC_GOOGLE_CLIENT_ID || ''
        authUrl = `https://accounts.google.com/o/oauth2/v2/auth?response_type=code&client_id=${clientId}&redirect_uri=${backendRedirectUri}&scope=openid%20email%20profile&state=google`
        break
      case Provider.KAKAO:
        clientId = process.env.NEXT_PUBLIC_KAKAO_CLIENT_ID || ''
        authUrl = `https://kauth.kakao.com/oauth/authorize?response_type=code&client_id=${clientId}&redirect_uri=${backendRedirectUri}&state=kakao`
        break
    }

    if (!clientId) {
      setError(`${provider} Client ID가 설정되지 않았습니다.`)
      return
    }

    window.location.href = authUrl
  }

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <button className={styles.backButton} onClick={() => router.push('/')}>
          ←
        </button>
        <h1 className={styles.title}>
          {mode === 'login' ? '로그인' : '회원가입'}
        </h1>
        <div className={styles.placeholder} />
      </div>

      <div className={styles.content}>
        <form className={styles.form} onSubmit={handleSubmit}>
          {mode === 'signup' && (
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
          )}

          {mode === 'signup' && (
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
          )}

          <div className={styles.inputGroup}>
            <label className={styles.label}>이메일</label>
            <input
              type="email"
              className={styles.input}
              placeholder="example@email.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
            />
          </div>

          <div className={styles.inputGroup}>
            <label className={styles.label}>비밀번호</label>
            <input
              type="password"
              className={styles.input}
              placeholder="8-20자, 대소문자+숫자+특수문자"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
            />
          </div>

          {mode === 'signup' && (
            <>
              <div className={styles.inputGroup}>
                <label className={styles.label}>비밀번호 확인</label>
                <input
                  type="password"
                  className={styles.input}
                  placeholder="비밀번호를 다시 입력하세요"
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
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
            </>
          )}

          {error && <p className={styles.error}>{error}</p>}

          <button
            type="submit"
            className={styles.submitButton}
            disabled={loading}
          >
            {loading ? '처리 중...' : mode === 'login' ? '로그인' : '회원가입'}
          </button>
        </form>

        {mode === 'login' && (
          <>
            <div className={styles.divider}>
              <span className={styles.dividerText}>또는</span>
            </div>

            <div className={styles.socialButtons}>
              <button
                type="button"
                className={`${styles.socialButton} ${styles.naver}`}
                onClick={() => handleSocialLogin(Provider.NAVER)}
                disabled={loading}
              >
                <span className={styles.socialIcon}>N</span>
                네이버로 로그인
              </button>

              <button
                type="button"
                className={`${styles.socialButton} ${styles.google}`}
                onClick={() => handleSocialLogin(Provider.GOOGLE)}
                disabled={loading}
              >
                <span className={styles.socialIcon}>G</span>
                구글로 로그인
              </button>

              <button
                type="button"
                className={`${styles.socialButton} ${styles.kakao}`}
                onClick={() => handleSocialLogin(Provider.KAKAO)}
                disabled={loading}
              >
                <span className={styles.socialIcon}>K</span>
                카카오로 로그인
              </button>
            </div>
          </>
        )}

        <div className={styles.switchContainer}>
          <span className={styles.switchText}>
            {mode === 'login' ? '계정이 없으신가요?' : '이미 계정이 있으신가요?'}
          </span>
          <span className={styles.switchLink} onClick={toggleMode}>
            {mode === 'login' ? '회원가입' : '로그인'}
          </span>
        </div>
      </div>
    </div>
  )
}

