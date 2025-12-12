import { apiRequest } from './api'

// 관계 정보 타입
export interface RelationInfo {
  UserName: string
  UserEmail: string
  GuardianName: string
  GuardianEmail: string
}

// 관계 정보 조회
export const getRelationInfo = async (): Promise<RelationInfo> => {
  const response = await apiRequest<RelationInfo>('/user/relationInfo', {
    method: 'GET',
  })

  return response.data!
}

// localStorage에 관계 정보 저장
export const saveRelationInfo = (relationInfo: RelationInfo) => {
  localStorage.setItem('relationInfo', JSON.stringify(relationInfo))
}

// localStorage에서 관계 정보 조회
export const getStoredRelationInfo = (): RelationInfo | null => {
  const stored = localStorage.getItem('relationInfo')
  if (!stored) return null

  try {
    return JSON.parse(stored) as RelationInfo
  } catch {
    return null
  }
}

// localStorage에서 관계 정보 삭제
export const clearRelationInfo = () => {
  localStorage.removeItem('relationInfo')
}
