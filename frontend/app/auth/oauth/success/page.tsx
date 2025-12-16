'use client'

import { useEffect } from 'react'
import { useRouter, useSearchParams } from 'next/navigation'
import { saveTokens, getRoleFromToken, getNameFromToken, getRelationIdFromToken } from '@/lib/api'
import { getUserType } from '@/lib/auth'
import { getRelationInfo, saveRelationInfo } from '@/lib/user'

export default function OAuthSuccessPage() {
  const router = useRouter()
  const searchParams = useSearchParams()

  useEffect(() => {
    const handleSuccess = async () => {
      try {
        // URL에서 토큰 추출
        const accessToken = searchParams.get('accessToken')
        const refreshToken = searchParams.get('refreshToken')

        if (!accessToken || !refreshToken) {
          router.push('/auth?error=토큰이 없습니다')
          return
        }

        // 토큰 저장
        saveTokens(accessToken, refreshToken)

        // JWT에서 사용자 정보 추출
        const role = getRoleFromToken(accessToken)
        const name = getNameFromToken(accessToken)
        const relationId = getRelationIdFromToken(accessToken)

        // 사용자 정보 저장
        localStorage.setItem('userName', name || '')
        localStorage.setItem('userRole', role || 'USER')
        localStorage.setItem('isLoggedIn', 'true')

        // relationId가 있으면 관계 정보 가져오기
        if (relationId !== null) {
          try {
            const relationInfo = await getRelationInfo()
            saveRelationInfo(relationInfo)
          } catch (error) {
            console.error('관계 정보 조회 실패:', error)
          }
        }

        // 사용자 타입에 따라 리다이렉트
        const userType = getUserType()
        if (userType === 'guardian') {
          router.push('/guardian')
        } else {
          router.push('/')
        }
      } catch (error) {
        console.error('OAuth 로그인 처리 오류:', error)
        router.push('/auth?error=로그인 처리 중 오류가 발생했습니다')
      }
    }

    handleSuccess()
  }, [searchParams, router])

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
      <div style={{
        width: '40px',
        height: '40px',
        border: '4px solid #e0e0e0',
        borderTop: '4px solid #333',
        borderRadius: '50%',
        animation: 'spin 1s linear infinite',
      }} />
      <p style={{ marginTop: '16px', color: '#666', fontSize: '16px' }}>
        로그인 처리 중...
      </p>
      <style>{`
        @keyframes spin {
          0% { transform: rotate(0deg); }
          100% { transform: rotate(360deg); }
        }
      `}</style>
    </div>
  )
}
