// 충전소 데이터 공유용
export interface ChargingStation {
  id: string
  name: string
  lat: number
  lng: number
  address: string
}

export const getStations = (): ChargingStation[] => {
  return [
    { id: '1', name: '강남 충전소', lat: 37.4979, lng: 127.0276, address: '서울시 강남구 테헤란로 123' },
    { id: '2', name: '홍대 충전소', lat: 37.5563, lng: 126.9236, address: '서울시 마포구 홍익로 456' },
    { id: '3', name: '잠실 충전소', lat: 37.5133, lng: 127.1028, address: '서울시 송파구 올림픽로 789' },
    { id: '4', name: '여의도 충전소', lat: 37.5219, lng: 126.9242, address: '서울시 영등포구 여의대로 321' },
  ]
}

// 두 지점 간 거리 계산 (Haversine 공식)
export function calculateDistance(
  lat1: number,
  lng1: number,
  lat2: number,
  lng2: number
): number {
  const R = 6371 // 지구 반지름 (km)
  const dLat = ((lat2 - lat1) * Math.PI) / 180
  const dLng = ((lng2 - lng1) * Math.PI) / 180
  const a =
    Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos((lat1 * Math.PI) / 180) *
      Math.cos((lat2 * Math.PI) / 180) *
      Math.sin(dLng / 2) *
      Math.sin(dLng / 2)
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
  return R * c
}

// 예상 시간 계산 (평균 속도 30km/h 가정)
export function calculateEstimatedTime(distance: number): number {
  const averageSpeed = 30 // km/h
  return Math.round((distance / averageSpeed) * 60) // 분 단위
}

