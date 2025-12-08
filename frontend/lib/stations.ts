// 충전소 데이터 공유용
export interface ChargingStation {
    placeId: string
    facilityName: string
    cityName: string
    districtName: string
    districtCode: number
    roadAddr: string
    landAddr: string
    lat: number
    lng: number
    description: string
    weekdayStart: string
    weekdayEnd: string
    saturdayStart: string
    saturdayEnd: string
    holidayStart: string
    holidayEnd: string
    capacity: number
    isAirPump: boolean
    isCharger: boolean
    manageOrgName: string
    manageOrgContact: string
    dataUpdated: string
    providerCode: string
    providerName: string
}

// ===== 1. API 클라이언트 =====
const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api'

async function parseJsonOrThrow(response: Response): Promise<any> {
    if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`)
    }
    return response.json()
}

export const chargerApi = {
    // 전략 1: 전체 로드 (Redis 캐싱)
    getAllChargers: async (): Promise<ChargingStation[]> => {
        console.log('🔵 전략 1: 전체 충전소 로드 (Redis 캐싱)')
        try {
            const response = await fetch(`${API_BASE_URL}/opendata/chargers`)
            const data = await parseJsonOrThrow(response)
            return data?.data ?? []
        } catch (error) {
            console.error('getAllChargers error:', error)
            throw error
        }

    },

    // 전략 2: Viewport 기반 로드
    getChargersInViewport: async (
        minLat: number,
        maxLat: number,
        minLng: number,
        maxLng: number
    ): Promise<ChargingStation[]> => {
        console.log('🟢 전략 2: Viewport 기반 로드')
        try {
            const params = new URLSearchParams({
                minLat: minLat.toString(),
                maxLat: maxLat.toString(),
                minLng: minLng.toString(),
                maxLng: maxLng.toString(),
            })
            const response = await fetch(`${API_BASE_URL}/opendata/chargers/viewport?${params}`)
            const data = await parseJsonOrThrow(response)
            return data?.data ?? []
        } catch (err) {
            console.error('getChargersInViewport error:', err)
            throw err
        }

    },

    // 전략 3: 주변 검색 (반경 기반)
    getNearbyChargers: async (
        lat: number,
        lng: number,
        radiusKm: number = 5.0
    ): Promise<ChargingStation[]> => {
        console.log('🟡 전략 3: 주변 충전소 검색 (반경 기반)')
        try {
            const params = new URLSearchParams({
                lat: lat.toString(),
                lng: lng.toString(),
                radiusKm: radiusKm.toString(),
            })
            const response = await fetch(`${API_BASE_URL}/opendata/chargers/nearby?${params}`)
            const data = await parseJsonOrThrow(response)
            return data?.data ?? []
        } catch (error) {
            console.error('getNearbyChargers error:', error)
            throw error
        }

    },

    // 충전소 상세 조회
    getChargerById: async (id: number): Promise<ChargingStation> => {
        try {
            const response = await fetch(`${API_BASE_URL}/opendata/chargers/${id}`)
            const data = await parseJsonOrThrow(response)
            return data.data
        } catch (err) {
            console.error('getChargerById error:', err)
            throw err
        }

    },
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

