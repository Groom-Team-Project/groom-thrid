// 리뷰 데이터 타입
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

// 리뷰 데이터 저장 및 관리
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

export const getReviewsByStation = (stationId: string): Review[] => {
  const reviews = getReviews()
  return reviews
    .filter(r => r.stationId === stationId)
    .sort((a, b) => {
      // 날짜 내림차순 정렬
      return new Date(b.date.replace(/\./g, '-')).getTime() - new Date(a.date.replace(/\./g, '-')).getTime()
    })
}

export const saveReview = (review: Omit<Review, 'id' | 'date'>): Review => {
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

export const updateReview = (reviewId: string, updates: Partial<Omit<Review, 'id' | 'date' | 'stationId' | 'stationName' | 'userId' | 'userName'>>): Review | null => {
  const reviews = getReviews()
  const index = reviews.findIndex(r => r.id === reviewId)
  
  if (index === -1) return null
  
  reviews[index] = { ...reviews[index], ...updates }
  localStorage.setItem('reviews', JSON.stringify(reviews))
  return reviews[index]
}

export const deleteReview = (reviewId: string): boolean => {
  const reviews = getReviews()
  const filtered = reviews.filter(r => r.id !== reviewId)
  localStorage.setItem('reviews', JSON.stringify(filtered))
  return reviews.length !== filtered.length
}

export const getReviewById = (reviewId: string): Review | undefined => {
  const reviews = getReviews()
  return reviews.find(r => r.id === reviewId)
}

export const getUserReviews = (userId: string): Review[] => {
  const reviews = getReviews()
  return reviews
    .filter(r => r.userId === userId)
    .sort((a, b) => {
      return new Date(b.date.replace(/\./g, '-')).getTime() - new Date(a.date.replace(/\./g, '-')).getTime()
    })
}

