import { apiRequest, saveTokens, clearTokens, getRoleFromToken, getNameFromToken } from './api'

// Role enum (백엔드와 일치)
export enum Role {
  USER = 'USER',
  GUARDIAN = 'GUARDIAN',
}

// 사용자 타입 (화면 구분용)
export type UserType = 'user' | 'guardian'

// 회원가입 요청
export interface SignupRequest {
  name: string
  email: string
  password: string
  phone?: string
  role: Role
}

// 로그인 요청
export interface LoginRequest {
  email: string
  password: string
}

// 인증 응답
export interface AuthResponse {
  accessToken: string
  refreshToken: string
}

// 비밀번호 검증 정규식
const PASSWORD_REGEX = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,20}$/

export const validatePassword = (password: string): { valid: boolean; message?: string } => {
  if (password.length < 8 || password.length > 20) {
    return { valid: false, message: '비밀번호는 8-20자이어야 합니다.' }
  }

  if (!PASSWORD_REGEX.test(password)) {
    return {
      valid: false,
      message: '비밀번호는 대소문자, 숫자, 특수문자를 포함해야 합니다.'
    }
  }

  return { valid: true }
}

export const validateEmail = (email: string): boolean => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return emailRegex.test(email)
}

// 회원가입 API
export const signup = async (request: SignupRequest): Promise<AuthResponse> => {
  try {
    const response = await apiRequest<AuthResponse>('/auth/signup', {
      method: 'POST',
      body: JSON.stringify(request),
    })

    if (response.data) {
      saveTokens(response.data.accessToken, response.data.refreshToken)

      // JWT에서 role과 name 추출
      const role = getRoleFromToken(response.data.accessToken)
      const name = getNameFromToken(response.data.accessToken)

      // 사용자 정보 저장
      localStorage.setItem('userEmail', request.email)
      localStorage.setItem('userName', name || request.name)
      localStorage.setItem('userRole', role || request.role)
      localStorage.setItem('isLoggedIn', 'true')

      return response.data
    }

    throw new Error('회원가입에 실패했습니다.')
  } catch (error) {
    if (error instanceof Error) {
      throw error
    }
    throw new Error('회원가입 중 오류가 발생했습니다.')
  }
}

// 로그인 API
export const login = async (request: LoginRequest): Promise<AuthResponse> => {
  try {
    const response = await apiRequest<AuthResponse>('/auth/form-login', {
      method: 'POST',
      body: JSON.stringify(request),
    })

    if (response.data) {
      saveTokens(response.data.accessToken, response.data.refreshToken)

      // JWT에서 role과 name 추출
      const role = getRoleFromToken(response.data.accessToken)
      const name = getNameFromToken(response.data.accessToken)

      // 사용자 정보 저장
      localStorage.setItem('userEmail', request.email)
      localStorage.setItem('userName', name || '')
      localStorage.setItem('userRole', role || 'USER')
      localStorage.setItem('isLoggedIn', 'true')

      return response.data
    }

    throw new Error('로그인에 실패했습니다.')
  } catch (error) {
    if (error instanceof Error) {
      throw error
    }
    throw new Error('로그인 중 오류가 발생했습니다.')
  }
}

// 현재 사용자의 role 가져오기
export const getCurrentUserRole = (): Role | null => {
  const role = localStorage.getItem('userRole')
  return role as Role | null
}

// 현재 사용자 타입 확인 (화면 구분용)
export const getUserType = (): UserType | null => {
  const role = getCurrentUserRole()
  if (role === Role.GUARDIAN) return 'guardian'
  if (role === Role.USER) return 'user'
  return null
}

// 로그아웃
export const logout = () => {
  clearTokens()
}
