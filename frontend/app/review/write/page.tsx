'use client'

export const dynamic = 'force-dynamic'

import { useState, useEffect } from 'react'
import { useRouter, useSearchParams } from 'next/navigation'
import { createReview, updateReview, getReviewById, type Review } from '@/lib/reviews'
import { chargerApi, type ChargingStation } from '@/lib/stations'
import StarRating from '@/components/StarRating'
import BottomNav from '@/components/BottomNav'
import styles from './page.module.css'

export default function WriteReviewPage() {
  const router = useRouter()
  const searchParams = useSearchParams()
  const stationIdParam = searchParams?.get('stationId')
  const reviewId = searchParams?.get('reviewId') // 수정 모드
  
  const [stationId, setStationId] = useState<string | null>(stationIdParam)
  const [stationName, setStationName] = useState('')
  const [rating, setRating] = useState<number>(5)
  const [content, setContent] = useState('')
  const [photo, setPhoto] = useState<File | null>(null)
  const [photoPreview, setPhotoPreview] = useState<string | null>(null)
  const [error, setError] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [isEditMode, setIsEditMode] = useState(false)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const loadData = async () => {
      const loggedIn = localStorage.getItem('isLoggedIn')
      if (loggedIn !== 'true') {
        router.push('/auth')
        return
      }

      try {
        // 수정 모드인 경우 기존 리뷰 데이터 로드
        if (reviewId) {
          const review = await getReviewById(reviewId)
          
          if (review) {
            setRating(review.rating)
            setContent(review.content)
            setStationName(review.stationName)
            setStationId(review.stationId)
            // S3 URL이거나 base64 이미지 모두 표시 가능
            if (review.photoUrl) {
              setPhotoPreview(review.photoUrl)
            }
            setIsEditMode(true)
          } else {
            setError('리뷰를 찾을 수 없습니다.')
            router.push('/')
          }
        } else if (stationIdParam) {
          // 작성 모드: 충전소 정보 로드
          try {
            const placeId = parseInt(stationIdParam)
            const station = await chargerApi.getChargerById(placeId)
            setStationName(station.facilityName)
            setStationId(stationIdParam)
          } catch (err) {
            console.error('충전소 정보 로드 실패:', err)
            setError('충전소 정보를 불러올 수 없습니다.')
          }
        } else {
          router.push('/')
          return
        }
      } catch (err) {
        console.error('데이터 로드 실패:', err)
        if (err instanceof Error) {
          setError(err.message)
        } else {
          setError('데이터를 불러오는 중 오류가 발생했습니다.')
        }
        router.push('/')
      } finally {
        setLoading(false)
      }
    }

    loadData()
  }, [router, stationIdParam, reviewId])

  const handlePhotoChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    if (file) {
      if (file.size > 5 * 1024 * 1024) {
        setError('사진 크기는 5MB 이하여야 합니다.')
        return
      }
      
      // 이미지 파일 타입 검증
      if (!file.type.startsWith('image/')) {
        setError('이미지 파일만 업로드 가능합니다.')
        return
      }
      
      setPhoto(file)
      setError('') // 에러 초기화
      
      // FileReader로 base64 변환 (백엔드 S3Service가 base64를 받아서 S3에 업로드)
      const reader = new FileReader()
      reader.onloadend = () => {
        // data:image/jpeg;base64,... 형식으로 변환됨
        setPhotoPreview(reader.result as string)
      }
      reader.onerror = () => {
        setError('이미지 파일을 읽는 중 오류가 발생했습니다.')
        setPhoto(null)
        setPhotoPreview(null)
      }
      reader.readAsDataURL(file)
    }
  }

  const handleRemovePhoto = () => {
    setPhoto(null)
    setPhotoPreview(null)
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')

    if (content.trim().length < 5) {
      setError('리뷰 내용은 최소 5자 이상 입력해주세요.')
      return
    }

    if (!stationId) {
      setError('충전소 정보가 없습니다.')
      return
    }

    setIsSubmitting(true)

    try {
      let photoUrl: string | undefined

      // 사진이 있으면 base64 문자열 전송 (백엔드 S3Service가 S3에 업로드 후 URL 반환)
      // photoPreview는 data:image/jpeg;base64,... 형식 또는 이미 S3 URL일 수 있음
      if (photoPreview && photoPreview.trim().length > 0) {
        photoUrl = photoPreview.trim()
      }

      const placeId = parseInt(stationId)

      if (isEditMode && reviewId) {
        // 수정: 백엔드에서 기존 S3 이미지 삭제 후 새 이미지 업로드
        await updateReview(reviewId, {
          rating: Number(rating),
          content: content.trim(),
          imageUrl: photoUrl,
        })
        
        alert('리뷰가 수정되었습니다.')
        router.push(`/?stationId=${placeId}&tab=review`)
      } else {
        // 작성: 백엔드에서 base64를 S3에 업로드하고 URL을 받아서 저장
        await createReview(placeId, {
          rating: Number(rating),
          content: content.trim(),
          imageUrl: photoUrl,
        })

        alert('리뷰가 작성되었습니다.')
        router.push(`/?stationId=${placeId}&tab=review`)
      }
    } catch (err) {
      console.error('리뷰 제출 실패:', err)
      if (err instanceof Error) {
        setError(err.message)
      } else {
        setError('리뷰 작성 중 오류가 발생했습니다.')
      }
    } finally {
      setIsSubmitting(false)
    }
  }

  if (loading) {
    return (
      <div className={styles.container}>
        <div className={styles.header}>
          <button className={styles.backButton} onClick={() => router.back()}>
            ←
          </button>
          <h1 className={styles.title}>{isEditMode ? '리뷰 수정' : '리뷰 작성'}</h1>
          <div className={styles.placeholder} />
        </div>
        <div style={{ padding: '20px', textAlign: 'center' }}>로딩 중...</div>
      </div>
    )
  }

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <button className={styles.backButton} onClick={() => router.back()}>
          ←
        </button>
        <h1 className={styles.title}>{isEditMode ? '리뷰 수정' : '리뷰 작성'}</h1>
        <div className={styles.placeholder} />
      </div>

      <form className={styles.form} onSubmit={handleSubmit}>
        <div className={styles.stationInfo}>
          <h2 className={styles.stationName}>{stationName}</h2>
        </div>

        <div className={styles.ratingSection}>
          <label className={styles.label}>별점</label>
          <StarRating 
            rating={rating} 
            onRatingChange={setRating}
            editable={true}
            size="large"
          />
        </div>

        <div className={styles.contentSection}>
          <label className={styles.label}>리뷰 내용</label>
          <textarea
            className={styles.textarea}
            value={content}
            onChange={(e) => setContent(e.target.value)}
            placeholder="리뷰를 작성해주세요 (최소 5자 이상)"
            rows={8}
            maxLength={500}
          />
          <div className={styles.charCount}>
            {content.length} / 500
          </div>
        </div>

        <div className={styles.photoSection}>
          <label className={styles.label}>사진 첨부(선택)</label>
          {photoPreview ? (
            <div className={styles.photoPreview}>
              <img src={photoPreview} alt="Preview" className={styles.previewImage} />
              <button
                type="button"
                className={styles.removePhotoButton}
                onClick={handleRemovePhoto}
              >
                삭제
              </button>
            </div>
          ) : (
            <label className={styles.photoUpload}>
              <input
                type="file"
                accept="image/*"
                onChange={handlePhotoChange}
                className={styles.fileInput}
              />
              <span className={styles.uploadText}>사진 선택</span>
            </label>
          )}
        </div>

        {error && (
          <div className={styles.error}>{error}</div>
        )}

        <button 
          type="submit" 
          className={styles.submitButton}
          disabled={isSubmitting}
        >
          {isSubmitting ? '처리 중...' : isEditMode ? '수정하기' : '작성하기'}
        </button>
      </form>

      <BottomNav />
    </div>
  )
}

