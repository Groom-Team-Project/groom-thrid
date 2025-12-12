import { apiRequest } from './api'

// 위치 업데이트 요청 타입
export interface LocationUpdateRequest {
  lat: number
  lng: number
  time: string // ISO 8601 형식 (LocalDateTime)
}

// 위치 업데이트 API 호출
export const updateLocation = async (lat: number, lng: number): Promise<void> => {
  const now = new Date()
  const time = now.toISOString().slice(0, 19) // "YYYY-MM-DDTHH:mm:ss" 형식

  await apiRequest('/locations', {
    method: 'POST',
    body: JSON.stringify({ lat, lng, time }),
  })
}
