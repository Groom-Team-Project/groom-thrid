'use client'

import { useState, useEffect } from 'react'
import { useRouter, useSearchParams } from 'next/navigation'
import { saveReview, updateReview, getReviewById, type Review } from '@/lib/reviews'
import { getStations } from '@/lib/stations'
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

  useEffect(() => {
    const loggedIn = localStorage.getItem('isLoggedIn')
    if (loggedIn !== 'true') {
      router.push('/auth')
      return
    }

    // 수정 모드인 경우 기존 리뷰 데이터 로드
    if (reviewId) {
      const review = getReviewById(reviewId)
      const userId = localStorage.getItem('userEmail') || ''
      
      if (!review) {
        router.push('/')
        return
      }
      
      // 본인 리뷰인지 확인
      if (review.userId !== userId) {
        alert('본인의 리뷰만 수정할 수 있습니다.')
        router.push('/')
        return
      }
      
      setRating(review.rating)
      setContent(review.content)
      setStationName(review.stationName)
      setStationId(review.stationId)
      if (review.photoUrl) {
        setPhotoPreview(review.photoUrl)
      }
      setIsEditMode(true)
    } else if (stationIdParam) {
      // 작성 모드: 충전소 정보 로드
      const stations = getStations()
      const station = stations.find(s => s.id === stationIdParam)
      if (station) {
        setStationName(station.name)
        setStationId(stationIdParam)
      } else {
        router.push('/')
      }
    } else {
      router.push('/')
    }
  }, [router, stationIdParam, reviewId])

  const handlePhotoChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    if (file) {
      if (file.size > 5 * 1024 * 1024) {
        setError('사진 크기는 5MB 이하여야 합니다.')
        return
      }
      setPhoto(file)
      const reader = new FileReader()
      reader.onloadend = () => {
        setPhotoPreview(reader.result as string)
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

    setIsSubmitting(true)

    try {
      const userId = localStorage.getItem('userEmail') || ''
      const userName = localStorage.getItem('userName') || '사용자'
      let photoUrl: string | undefined

      // 사진이 있으면 base64로 저장 (실제로는 서버에 업로드)
      if (photoPreview) {
        photoUrl = photoPreview
      }

      if (isEditMode && reviewId) {
        // 수정
        const updated = updateReview(reviewId, {
          rating,
          content: content.trim(),
          photoUrl,
        })
        
        if (updated) {
          alert('리뷰가 수정되었습니다.')
          router.push(`/?stationId=${stationId || updated.stationId}&tab=review`)
        } else {
          setError('리뷰 수정에 실패했습니다.')
        }
      } else {
        // 작성
        if (!stationId) {
          setError('충전소 정보가 없습니다.')
          setIsSubmitting(false)
          return
        }

        const stations = getStations()
        const station = stations.find(s => s.id === stationId)
        if (!station) {
          setError('충전소를 찾을 수 없습니다.')
          setIsSubmitting(false)
          return
        }

        saveReview({
          stationId,
          stationName: station.name,
          userId,
          userName,
          rating,
          content: content.trim(),
          photoUrl,
        })

        alert('리뷰가 작성되었습니다.')
        router.push(`/?stationId=${stationId}&tab=review`)
      }
    } catch (err) {
      setError('리뷰 작성 중 오류가 발생했습니다.')
      console.error(err)
    } finally {
      setIsSubmitting(false)
    }
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

