// API base URL
export const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api/v1'

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
  relationId: number | null // 관계 ID (없으면 null)
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

// JWT에서 relationId 추출
export const getRelationIdFromToken = (token: string): number | null => {
  const payload = decodeJwt(token)
  return payload?.relationId ?? null
}

// 인증이 필요 없는 엔드포인트 목록
const PUBLIC_ENDPOINTS = ['/auth/form-login', '/auth/form-signup', '/auth/oauth-login', '/auth/oauth-signup']

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

    // 204 No Content 응답인 경우 body가 없으므로 빈 응답 반환
    if (response.status === 204) {
      return {
        status: 'success',
        code: 204,
        message: 'No Content',
        data: null as T,
        errors: null, 
      }
    }

    // 응답 본문을 먼저 텍스트로 읽기 (Response body는 한 번만 읽을 수 있음)
    const responseText = await response.text()
    
    // 빈 응답 처리 (DELETE 등)
    if (!responseText || responseText.trim().length === 0) {
      if (response.ok) {
        return {
          status: 'success',
          code: response.status,
          message: '요청이 성공했습니다.',
          data: null,
          errors: null,
        } as ApiResponse<T>
      } else {
        throw new Error(`서버 오류 (${response.status}): 응답 본문이 비어있습니다.`)
      }
    }

    // JSON 파싱 시도
    let data: ApiResponse<T>
    try {
      data = JSON.parse(responseText)
    } catch (parseError) {
      console.error('JSON 파싱 실패:', {
        status: response.status,
        statusText: response.statusText,
        responseText: responseText.substring(0, 500), // 처음 500자만 표시
        endpoint,
      })
      throw new Error(`서버 응답을 파싱할 수 없습니다. (${response.status} ${response.statusText})`)
    }

    if (!response.ok) {
      // 에러 상세 정보 로깅
      console.error('API 요청 실패:', {
        status: response.status,
        statusText: response.statusText,
        endpoint,
        message: data.message,
        errors: data.errors,
        responseText: responseText.substring(0, 1000), // 처음 1000자만 표시
      })
      
      // 에러 메시지 구성
      let errorMessage = data.message || '요청이 실패했습니다.'
      
      // 500 오류인 경우 더 자세한 정보 제공
      if (response.status === 500) {
        errorMessage = `서버 내부 오류가 발생했습니다.${data.message ? ` (${data.message})` : ''}`
        if (data.errors && data.errors.length > 0) {
          errorMessage += `\n상세: ${data.errors.map(e => `${e.field}: ${e.reason}`).join(', ')}`
        }
      } else if (data.errors && data.errors.length > 0) {
        // 검증 오류가 있는 경우
        const validationErrors = data.errors.map(e => `${e.field}: ${e.reason}`).join(', ')
        errorMessage = `${errorMessage}\n${validationErrors}`
      }
      
      throw new Error(errorMessage)
    }

    return data
  } catch (error) {
    if (error instanceof Error) {
      throw error
    }
    throw new Error('알 수 없는 오류가 발생했습니다.')
  }
}
