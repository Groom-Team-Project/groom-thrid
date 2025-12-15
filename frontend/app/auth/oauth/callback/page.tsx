'use client'

export default function OAuthCallbackPage() {
  // 백엔드에서 OAuth 처리를 하므로 이 페이지는 로딩 화면만 표시
  // 실제로는 백엔드에서 /auth/oauth/success 또는 /auth/oauth/signup으로 리다이렉트됨

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
        OAuth 인증 처리 중...
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
