import { apiRequest } from './api'

// 경로 정보 응답 타입
export interface PathNavigationInfo {
  startX: string
  startY: string
  startName: string
  endX: string
  endY: string
  endName: string
  isNavigating: boolean
}

// 현재 길안내 정보 조회 (보호자용)
export const getCurrentNavigation = async (): Promise<PathNavigationInfo> => {
  const response = await apiRequest<PathNavigationInfo>('/paths/navigation', {
    method: 'GET',
  })

  return response.data!
}
