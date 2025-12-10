import { apiRequest } from './api'

// 백엔드 응답 타입
export interface ReviewResponse {
  id: number
  placeId: number
  content: string
  rating: number
  author: string
  imageUrl: string | null
  isActive: boolean
  createdAt: string
  updatedAt: string
}

// 프론트엔드에서 사용하는 리뷰 타입 (호환성 유지)
export interface Review {
  id: string
  stationId: string
  stationName: string
  userId: string
  userName: string
  rating: number // 0.5 단위로 0.5 ~ 5.0
  content: string
  photoUrl?: string
  date: string
}

// 백엔드 응답을 프론트엔드 타입으로 변환
const convertReviewResponse = (response: ReviewResponse, stationName?: string): Review => {
  const createdAt = new Date(response.createdAt)
  return {
    id: response.id.toString(),
    stationId: response.placeId.toString(),
    stationName: stationName || `충전소 ${response.placeId}`,
    userId: '', // 백엔드에서 제공하지 않음 (보안상)
    userName: response.author,
    rating: response.rating,
    content: response.content,
    photoUrl: response.imageUrl || undefined,
    date: createdAt.toISOString().split('T')[0].replace(/-/g, '.'),
  }
}

// 리뷰 생성 요청 타입
export interface CreateReviewRequest {
  content: string
  rating: number
  imageUrl?: string
}

// 리뷰 수정 요청 타입
export interface UpdateReviewRequest {
  content: string
  rating: number
  imageUrl?: string
}

// 장소별 리뷰 목록 조회
export const getReviewsByStation = async (placeId: number | string, stationName?: string): Promise<Review[]> => {
  try {
    const response = await apiRequest<ReviewResponse[]>(`/reviews/place/${placeId}`, {
      method: 'GET',
    })

    if (response.data) {
      // 최신 작성 순으로 정렬 (createdAt 기준 내림차순)
      const sortedReviews = [...response.data].sort((a, b) => {
        const dateA = new Date(a.createdAt).getTime()
        const dateB = new Date(b.createdAt).getTime()
        return dateB - dateA // 최신순 (내림차순)
      })
      
      return sortedReviews.map(review => convertReviewResponse(review, stationName))
    }
    return []
  } catch (error) {
    console.error('리뷰 목록 조회 실패:', error)
    throw error
  }
}

// 리뷰 상세 조회
export const getReviewById = async (reviewId: number | string, stationName?: string): Promise<Review> => {
  try {
    const response = await apiRequest<ReviewResponse>(`/reviews/${reviewId}`, {
      method: 'GET',
    })

    if (response.data) {
      return convertReviewResponse(response.data, stationName)
    }
    throw new Error('리뷰를 찾을 수 없습니다.')
  } catch (error) {
    console.error('리뷰 조회 실패:', error)
    throw error
  }
}

// 리뷰 생성
export const createReview = async (
  placeId: number | string,
  request: CreateReviewRequest
): Promise<Review> => {
  try {
    // rating을 0.5 단위로 반올림 (백엔드 RatingValidator 요구사항)
    const rating = Math.round(Number(request.rating) * 2) / 2
    
    // 요청 본문 구성
    const requestBody: any = {
      content: request.content.trim(),
      rating: rating,
    }
    
    // imageUrl 처리
    // 백엔드 DTO의 @Size(max = 500) 제약이 있지만, S3Service가 base64를 처리하므로
    // base64 이미지는 길 수 있지만 백엔드에서 validation을 통과할 수 있도록 처리
    // 백엔드가 base64를 받을 수 있도록 imageUrl 필드를 항상 포함 (null 가능)
    if (request.imageUrl && request.imageUrl.trim().length > 0) {
      // base64 이미지 전송 (백엔드 S3Service가 처리)
      requestBody.imageUrl = request.imageUrl.trim()
    }
    // 이미지가 없으면 필드를 제외 (백엔드에서 null로 처리)

    console.log('리뷰 생성 요청:', {
      ...requestBody,
      imageUrl: requestBody.imageUrl ? `${requestBody.imageUrl.substring(0, 50)}... (${requestBody.imageUrl.length} chars)` : null
    }) // 디버깅용 (base64는 너무 길어서 일부만 표시)

    const response = await apiRequest<ReviewResponse>(`/reviews/place/${placeId}`, {
      method: 'POST',
      body: JSON.stringify(requestBody),
    })

    if (response.data) {
      return convertReviewResponse(response.data)
    }
    throw new Error('리뷰 생성에 실패했습니다.')
  } catch (error) {
    console.error('리뷰 생성 실패:', error)
    throw error
  }
}

// 리뷰 수정
export const updateReview = async (
  reviewId: number | string,
  request: UpdateReviewRequest
): Promise<Review> => {
  try {
    // rating을 0.5 단위로 반올림 (백엔드 RatingValidator 요구사항)
    const rating = Math.round(Number(request.rating) * 2) / 2
    
    // 요청 본문 구성 (imageUrl이 없으면 필드 제외)
    const requestBody: any = {
      content: request.content.trim(),
      rating: rating,
    }
    
    // imageUrl이 있으면 추가 (null이나 빈 문자열이 아닐 때만)
    if (request.imageUrl && request.imageUrl.trim().length > 0) {
      requestBody.imageUrl = request.imageUrl.trim()
    }

    console.log('리뷰 수정 요청:', requestBody) // 디버깅용

    const response = await apiRequest<ReviewResponse>(`/reviews/${reviewId}`, {
      method: 'PUT',
      body: JSON.stringify(requestBody),
    })

    if (response.data) {
      return convertReviewResponse(response.data)
    }
    throw new Error('리뷰 수정에 실패했습니다.')
  } catch (error) {
    console.error('리뷰 수정 실패:', error)
    throw error
  }
}

// 리뷰 삭제
export const deleteReview = async (reviewId: number | string): Promise<void> => {
  try {
    await apiRequest(`/reviews/${reviewId}`, {
      method: 'DELETE',
    })
  } catch (error) {
    console.error('리뷰 삭제 실패:', error)
    throw error
  }
}

// 하위 호환성을 위한 기존 함수들 (deprecated - localStorage 사용)
export const getReviews = (): Review[] => {
  if (typeof window === 'undefined') return []
  
  const reviewsJson = localStorage.getItem('reviews')
  if (!reviewsJson) return []
  
  try {
    return JSON.parse(reviewsJson)
  } catch {
    return []
  }
}

export const saveReview = (review: Omit<Review, 'id' | 'date'>): Review => {
  console.warn('saveReview is deprecated. Use createReview instead.')
  const reviews = getReviews()
  const newReview: Review = {
    ...review,
    id: Date.now().toString(),
    date: new Date().toISOString().split('T')[0].replace(/-/g, '.'),
  }
  
  reviews.push(newReview)
  localStorage.setItem('reviews', JSON.stringify(reviews))
  return newReview
}

export const getUserReviews = (userId: string): Review[] => {
  const reviews = getReviews()
  return reviews
    .filter(r => r.userId === userId)
    .sort((a, b) => {
      return new Date(b.date.replace(/\./g, '-')).getTime() - new Date(a.date.replace(/\./g, '-')).getTime()
    })
}
