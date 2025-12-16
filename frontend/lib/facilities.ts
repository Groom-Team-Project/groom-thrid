// 편의시설 데이터 공유용
export interface Facility {
    facilityId: string
    facilitySeq: number
    establishedDate: string
    lat: number
    lng: number
    facilityName: string
    facilityType: string
    roadAddr: string
    isOperating: boolean
    operationStatusName: string
    convenientFacilityInfo: string
}

// ===== 1. API 클라이언트 =====
const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api'

async function parseJsonOrThrow(response: Response): Promise<any> {
    if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`)
    }
    return response.json()
}

export const facilityApi = {

    // Viewport 기반 로드
    getConvenientFacilityInViewport: async (
        facilityType: string,
        minLat: number,
        maxLat: number,
        minLng: number,
        maxLng: number
    ): Promise<Facility[]> => {
        console.log(' Viewport 기반 로드')
        try {
            const params = new URLSearchParams({
                facilityType: facilityType,
                minLat: minLat.toString(),
                maxLat: maxLat.toString(),
                minLng: minLng.toString(),
                maxLng: maxLng.toString(),
            })
            const response = await fetch(`${API_BASE_URL}/ConvenientFacilities/viewport?${params}`)
            const data = await parseJsonOrThrow(response)
            return data?.data ?? []
        } catch (err) {
            console.error('getConvenientFacilityInViewport error:', err)
            throw err
        }

    },

    // 충전소 상세 조회
    getConvenientFacilityById: async (id: string): Promise<Facility> => {
        try {
            const response = await fetch(`${API_BASE_URL}/ConvenientFacilities/${id}`)
            const data = await parseJsonOrThrow(response)
            return data.data
        } catch (err) {
            console.error('getConvenientFacilityById error:', err)
            throw err
        }

    },

    getConvenientFacilityInfo: async (id: string): Promise<Facility> => {
        try {
            const response = await fetch(`${API_BASE_URL}/ConvenientFacilities/info/${id}/refresh` , { method: 'POST' })
            const data = await parseJsonOrThrow(response)
            return data.data
        } catch (err) {
            console.error('getConvenientFacilityInfo error:', err)
        }
    },
}
