// API base URL
export const API_BASE_URL = 'http://localhost:8080/api/v1'

// API response wrapper
export interface ApiResponse<T> {
  status: 'success' | 'error'
  code: number
  message: string
  data: T | null
  errors: ErrorDetail[] | null
}

export interface ErrorDetail {
  field: string
  rejectedValue: string
  reason: string
  code: string
}

// JWT 토큰 저장 및 조회
export const saveTokens = (accessToken: string, refreshToken: string) => {
  localStorage.setItem('accessToken', accessToken)
  localStorage.setItem('refreshToken', refreshToken)
}

export const getAccessToken = (): string | null => {
  return localStorage.getItem('accessToken')
}

export const getRefreshToken = (): string | null => {
  return localStorage.getItem('refreshToken')
}

export const clearTokens = () => {
  localStorage.removeItem('accessToken')
  localStorage.removeItem('refreshToken')
  localStorage.removeItem('userEmail')
  localStorage.removeItem('userName')
  localStorage.removeItem('userRole')
  localStorage.removeItem('isLoggedIn')
}

// JWT 디코딩 함수
interface JwtPayload {
  sub: string // userId
  role: string // USER, GUARDIAN
  name: string // 사용자 이름
  iat: number // issued at
  exp: number // expiration
}

export const decodeJwt = (token: string): JwtPayload | null => {
  try {
    // JWT는 header.payload.signature 형식
    const parts = token.split('.')
    if (parts.length !== 3) {
      return null
    }

    // Base64 URL 디코딩
    const payload = parts[1]
    const base64 = payload.replace(/-/g, '+').replace(/_/g, '/')
    const decoded = atob(base64)

    // UTF-8 디코딩 (한글 등 멀티바이트 문자 처리)
    const bytes = new Uint8Array(decoded.split('').map(c => c.charCodeAt(0)))
    const utf8Decoded = new TextDecoder('utf-8').decode(bytes)

    return JSON.parse(utf8Decoded) as JwtPayload
  } catch (error) {
    console.error('JWT 디코딩 실패:', error)
    return null
  }
}

// JWT에서 role 추출
export const getRoleFromToken = (token: string): string | null => {
  const payload = decodeJwt(token)
  return payload?.role || null
}

// JWT에서 name 추출
export const getNameFromToken = (token: string): string | null => {
  const payload = decodeJwt(token)
  return payload?.name || null
}

// 인증이 필요 없는 엔드포인트 목록
const PUBLIC_ENDPOINTS = ['/auth/form-login', '/auth/signup']

// API 요청 헬퍼
export const apiRequest = async <T>(
  endpoint: string,
  options: RequestInit = {}
): Promise<ApiResponse<T>> => {
  // 리뷰 조회(GET)는 비로그인에서도 가능, 생성/수정/삭제는 인증 필요
  const isReviewGetRequest = 
    options.method === 'GET' && 
    (endpoint.startsWith('/reviews/place/') || endpoint.startsWith('/reviews/'))
  
  const isPublicEndpoint = 
    PUBLIC_ENDPOINTS.some(path => endpoint.startsWith(path)) || 
    isReviewGetRequest
  
  const token = !isPublicEndpoint ? getAccessToken() : null

  const headers: HeadersInit = {
    'Content-Type': 'application/json',
    ...(token && { Authorization: `Bearer ${token}` }),
    ...options.headers,
  }

  try {
    const response = await fetch(`${API_BASE_URL}${endpoint}`, {
      ...options,
      headers,
    })

    const data: ApiResponse<T> = await response.json()

    if (!response.ok) {
      throw new Error(data.message || '요청이 실패했습니다.')
    }

    return data
  } catch (error) {
    if (error instanceof Error) {
      throw error
    }
    throw new Error('알 수 없는 오류가 발생했습니다.')
  }
}
