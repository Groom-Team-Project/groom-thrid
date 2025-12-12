import { apiRequest } from './api'
import { chargerApi } from './stations'

// 백엔드 응답 DTO
export interface ReportResponse {
  id: number
  placeId: number
  content: string
  author: string
  authorEmail?: string // 작성자 이메일 (동명이인 구분용)
  status: string // "대기 중", "처리 중", "승인", "반려"
  imageUrl?: string
  adminReply?: string
  createdAt: string
  updatedAt: string
}

// 프론트엔드 제보 타입
export interface Report {
  id: string
  stationName: string
  content: string
  photoUrl?: string
  date: string
  status: 'pending' | 'processing' | 'completed' | 'rejected'
  userId: string // 하위 호환성을 위해 유지 (작성자 이름)
  authorName?: string // 작성자 이름
  authorEmail?: string // 작성자 이메일 (동명이인 구분용)
  adminResponse?: string
  adminResponseDate?: string
  adminCheckedDate?: string
}

// 백엔드 상태를 프론트엔드 상태로 변환
const convertBackendStatus = (backendStatus: string): 'pending' | 'processing' | 'completed' | 'rejected' => {
  switch (backendStatus) {
    case '대기 중':
      return 'pending'
    case '처리 중':
      return 'processing'
    case '승인':
      return 'completed'
    case '반려':
      return 'rejected'
    default:
      return 'pending'
  }
}

// 백엔드 응답을 프론트엔드 타입으로 변환
const convertReportResponse = async (response: ReportResponse): Promise<Report> => {
  // placeId로 충전소 정보 가져오기
  let stationName = '알 수 없음'
  try {
    const station = await chargerApi.getChargerById(response.placeId)
    stationName = station.facilityName
  } catch (err) {
    console.error('충전소 정보 로드 실패:', err)
  }

  // createdAt을 date 형식으로 변환
  const createdAt = new Date(response.createdAt)
  const date = `${createdAt.getFullYear()}.${String(createdAt.getMonth() + 1).padStart(2, '0')}.${String(createdAt.getDate()).padStart(2, '0')}`

  // adminCheckedDate: processing, completed, rejected 상태일 때 updatedAt 사용 (시, 분 포함)
  let adminCheckedDate: string | undefined
  const status = convertBackendStatus(response.status)
  if (status !== 'pending' && response.updatedAt) {
    const updatedAt = new Date(response.updatedAt)
    adminCheckedDate = `${updatedAt.getFullYear()}.${String(updatedAt.getMonth() + 1).padStart(2, '0')}.${String(updatedAt.getDate()).padStart(2, '0')} ${String(updatedAt.getHours()).padStart(2, '0')}:${String(updatedAt.getMinutes()).padStart(2, '0')}`
  }

  // adminResponseDate: adminReply가 있을 때 updatedAt 사용 (시, 분 포함)
  let adminResponseDate: string | undefined
  if (response.adminReply && response.updatedAt) {
    const updatedAt = new Date(response.updatedAt)
    adminResponseDate = `${updatedAt.getFullYear()}.${String(updatedAt.getMonth() + 1).padStart(2, '0')}.${String(updatedAt.getDate()).padStart(2, '0')} ${String(updatedAt.getHours()).padStart(2, '0')}:${String(updatedAt.getMinutes()).padStart(2, '0')}`
  }

  return {
    id: response.id.toString(),
    stationName,
    content: response.content,
    photoUrl: response.imageUrl,
    date,
    status,
    userId: response.author, // author(작성자 이름)를 userId로 저장 (하위 호환성)
    authorName: response.author, // 작성자 이름을 별도로 저장
    authorEmail: response.authorEmail, // 작성자 이메일 저장 (동명이인 구분용)
    adminResponse: response.adminReply,
    adminResponseDate,
    adminCheckedDate,
  }
}

// 제보 목록 조회 (USER/GUARDIAN: 자신의 제보만, ADMIN: 모든 제보)
export const getUserReports = async (): Promise<Report[]> => {
  try {
    const response = await apiRequest<ReportResponse[]>(`/reports`, {
      method: 'GET',
    })

    if (response.data) {
      // 모든 응답을 변환
      const reports = await Promise.all(response.data.map(convertReportResponse))
      // createdAt 기준 내림차순 정렬 (최신순)
      return reports.sort((a, b) => {
        const dateA = new Date(a.date.replace(/\./g, '-'))
        const dateB = new Date(b.date.replace(/\./g, '-'))
        return dateB.getTime() - dateA.getTime()
      })
    }

    return []
  } catch (error) {
    console.error('제보 목록 조회 실패:', error)
    throw error
  }
}

// 제보 상세 조회
export const getReportById = async (reportId: string): Promise<Report> => {
  try {
    const response = await apiRequest<ReportResponse>(`/reports/${reportId}`, {
      method: 'GET',
    })

    if (response.data) {
      return await convertReportResponse(response.data)
    }

    throw new Error('제보를 찾을 수 없습니다.')
  } catch (error) {
    console.error('제보 상세 조회 실패:', error)
    throw error
  }
}

// 제보 생성 요청
export interface CreateReportRequest {
  content: string
  imageUrl?: string
}

// 제보 생성
export const createReport = async (
  placeId: number | string,
  request: CreateReportRequest
): Promise<Report> => {
  try {
    const requestBody: any = {
      content: request.content.trim(),
    }
    if (request.imageUrl && request.imageUrl.trim().length > 0) {
      requestBody.imageUrl = request.imageUrl.trim()
    }

    const response = await apiRequest<ReportResponse>(`/reports/place/${placeId}`, {
      method: 'POST',
      body: JSON.stringify(requestBody),
    })

    if (response.data) {
      return await convertReportResponse(response.data)
    }

    throw new Error('제보 생성에 실패했습니다.')
  } catch (error) {
    console.error('제보 생성 실패:', error)
    throw error
  }
}

// 제보 수정 요청
export interface UpdateReportRequest {
  content: string
  imageUrl?: string
}

// 제보 수정
export const updateReport = async (
  reportId: string,
  request: UpdateReportRequest
): Promise<Report> => {
  try {
    const requestBody: any = {
      content: request.content.trim(),
    }
    if (request.imageUrl && request.imageUrl.trim().length > 0) {
      requestBody.imageUrl = request.imageUrl.trim()
    }

    const response = await apiRequest<ReportResponse>(`/reports/${reportId}`, {
      method: 'PUT',
      body: JSON.stringify(requestBody),
    })

    if (response.data) {
      return await convertReportResponse(response.data)
    }

    throw new Error('제보 수정에 실패했습니다.')
  } catch (error) {
    console.error('제보 수정 실패:', error)
    throw error
  }
}

// 제보 삭제
export const deleteReport = async (reportId: string): Promise<void> => {
  try {
    await apiRequest(`/reports/${reportId}`, {
      method: 'DELETE',
    })
  } catch (error) {
    console.error('제보 삭제 실패:', error)
    throw error
  }
}

// 관리자 제보 상태 변경 요청
export interface UpdateReportStatusRequest {
  status: 'pending' | 'processing' | 'completed' | 'rejected'
  adminReply?: string
}

// 백엔드 상태로 변환 (enum 이름으로 전송)
const convertToBackendStatus = (status: 'pending' | 'processing' | 'completed' | 'rejected'): string => {
  switch (status) {
    case 'pending':
      return 'PENDING'
    case 'processing':
      return 'PROCESSING'
    case 'completed':
      return 'APPROVED'
    case 'rejected':
      return 'REJECTED'
    default:
      return 'PENDING'
  }
}

// 관리자 제보 상태 변경
export const updateReportStatus = async (
  reportId: string,
  request: UpdateReportStatusRequest
): Promise<Report> => {
  try {
    const requestBody: any = {
      status: convertToBackendStatus(request.status),
    }
    if (request.adminReply && request.adminReply.trim().length > 0) {
      requestBody.adminReply = request.adminReply.trim()
    }

    const response = await apiRequest<ReportResponse>(`/reports/${reportId}/status`, {
      method: 'PUT',
      body: JSON.stringify(requestBody),
    })

    if (response.data) {
      return await convertReportResponse(response.data)
    }

    throw new Error('제보 상태 변경에 실패했습니다.')
  } catch (error) {
    console.error('제보 상태 변경 실패:', error)
    throw error
  }
}

// 하위 호환성을 위한 기존 함수들 (deprecated)
export const getReports = (): Report[] => {
  console.warn('getReports()는 deprecated입니다. getUserReports()를 사용하세요.')
  return []
}

export const saveReport = (report: Omit<Report, 'id' | 'date' | 'status'>): Report => {
  console.warn('saveReport()는 deprecated입니다. createReport()를 사용하세요.')
  throw new Error('saveReport()는 더 이상 사용할 수 없습니다. createReport()를 사용하세요.')
}

