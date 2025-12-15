import { apiRequest } from './api'

export interface CreateNotificationRequest {
  lat: number
  lng: number
  address: string
}

export interface AlertCheckResponse {
  lat: number
  lng: number
  address: string
}

export const createAlert = async (request: CreateNotificationRequest): Promise<void> => {
  await apiRequest<void>('/notification/alert', {
    method: 'POST',
    body: JSON.stringify(request),
  })
}

export const alertCheck = async (): Promise<AlertCheckResponse> => {
  const response = await apiRequest<AlertCheckResponse>('/notification/alert-check', {
    method: 'GET',
  })

  if (!response.data) {
    throw new Error('알림 정보를 가져올 수 없습니다.')
  }

  return response.data
}

// 모든 알림 조회 (relationId 기반)
export const getAlertList = async (): Promise<AlertCheckResponse[]> => {
  const response = await apiRequest<AlertCheckResponse[]>('/notification/', {
    method: 'GET',
  })

  return response.data || []
}
