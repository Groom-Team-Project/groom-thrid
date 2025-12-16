'use client'

export const dynamic = 'force-dynamic'

import { useState, useEffect } from 'react'
import { useRouter, useSearchParams } from 'next/navigation'
import { getReviewById, deleteReview, type Review } from '@/lib/reviews'
import { chargerApi } from '@/lib/stations'
import { isAdmin } from '@/lib/auth'
import StarRating from '@/components/StarRating'
import BottomNav from '@/components/BottomNav'
import styles from './page.module.css'

export default function ReviewDetailPage() {
  const router = useRouter()
  const searchParams = useSearchParams()
  const reviewId = searchParams?.get('reviewId')
  
  const [review, setReview] = useState<Review | null>(null)
  const [showMenu, setShowMenu] = useState(false)
  const [showDeleteModal, setShowDeleteModal] = useState(false)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const loadReview = async () => {
      if (!reviewId) {
        router.push('/')
        return
      }

      try {
        // 먼저 리뷰 정보를 가져옴 (비로그인 사용자도 가능)
        const reviewData = await getReviewById(reviewId)
        
        // 충전소 정보를 가져와서 stationName 설정
        if (reviewData.stationId) {
          try {
            const placeId = parseInt(reviewData.stationId)
            const station = await chargerApi.getChargerById(placeId)
            // 충전소명으로 업데이트
            reviewData.stationName = station.facilityName
          } catch (stationErr) {
            console.error('충전소 정보 로드 실패:', stationErr)
            // 충전소 정보를 가져오지 못해도 리뷰는 표시
          }
        }
        
        setReview(reviewData)
      } catch (err) {
        console.error('리뷰 로드 실패:', err)
        alert('리뷰를 찾을 수 없습니다.')
        router.push('/')
      } finally {
        setLoading(false)
      }
    }

    loadReview()
  }, [router, reviewId])

  // 권한 체크:
  // - USER, PROTECTOR: 자기가 작성한 리뷰만 수정/삭제 가능
  // - ADMIN: 모든 리뷰 수정/삭제 가능
  // 실제 권한 검증은 백엔드에서 처리됨
  const userName = localStorage.getItem('userName') || ''
  const isOwner = review?.userName === userName
  const canEditOrDelete = isOwner || isAdmin()

  const handleEdit = () => {
    if (review) {
      router.push(`/review/write?reviewId=${review.id}&stationId=${review.stationId}`)
    }
  }

  const handleDelete = () => {
    setShowDeleteModal(true)
  }

  const confirmDelete = async () => {
    if (review) {
      try {
        await deleteReview(review.id)
        alert('리뷰가 삭제되었습니다.')
        // placeId를 사용하여 리다이렉트
        const placeId = parseInt(review.stationId)
        router.push(`/?stationId=${placeId}&tab=review`)
      } catch (err) {
        console.error('리뷰 삭제 실패:', err)
        if (err instanceof Error) {
          alert(err.message || '리뷰 삭제에 실패했습니다.')
        } else {
          alert('리뷰 삭제에 실패했습니다.')
        }
      }
    }
  }

  const cancelDelete = () => {
    setShowDeleteModal(false)
  }

  if (loading) {
    return (
      <div className={styles.container}>
        <div className={styles.header}>
          <button className={styles.backButton} onClick={() => router.back()}>
            ←
          </button>
          <h1 className={styles.title}>리뷰 상세</h1>
          <div className={styles.placeholder} />
        </div>
        <div style={{ padding: '20px', textAlign: 'center' }}>로딩 중...</div>
        <BottomNav />
      </div>
    )
  }

  if (!review) {
    return null
  }

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <button className={styles.backButton} onClick={() => router.back()}>
          ←
        </button>
        <h1 className={styles.title}>리뷰 상세</h1>
        {canEditOrDelete && (
          <div className={styles.menuContainer}>
            <button 
              className={styles.menuButton}
              onClick={() => setShowMenu(!showMenu)}
            >
              ⋯
            </button>
            {showMenu && (
              <>
                <div 
                  className={styles.menuOverlay}
                  onClick={() => setShowMenu(false)}
                />
                <div className={styles.menu}>
                  <button 
                    className={styles.menuItem}
                    onClick={() => {
                      setShowMenu(false)
                      handleEdit()
                    }}
                  >
                    수정
                  </button>
                  <button 
                    className={`${styles.menuItem} ${styles.deleteMenuItem}`}
                    onClick={() => {
                      setShowMenu(false)
                      handleDelete()
                    }}
                  >
                    삭제
                  </button>
                </div>
              </>
            )}
          </div>
        )}
        {!canEditOrDelete && <div className={styles.placeholder} />}
      </div>

      <div className={styles.content}>
        <div className={styles.reviewCard}>
          <div className={styles.reviewHeader}>
            <div className={styles.authorInfo}>
              <span className={styles.authorName}>{review.userName}</span>
              <span className={styles.reviewDate}>{review.date}</span>
            </div>
            <StarRating 
              rating={review.rating} 
              editable={false}
              size="large"
            />
          </div>

          <div className={styles.reviewContent}>
            <p className={styles.reviewText}>{review.content}</p>
            {review.photoUrl && (
              <div className={styles.photoContainer}>
                <img 
                  src={review.photoUrl} 
                  alt="Review" 
                  className={styles.reviewPhoto} 
                />
              </div>
            )}
          </div>

          <div className={styles.stationInfo}>
            <span className={styles.stationLabel}>충전소</span>
            <span className={styles.stationName}>{review.stationName}</span>
          </div>
        </div>
      </div>

      {/* 삭제 확인 모달 */}
      {showDeleteModal && (
        <div className={styles.modal} onClick={cancelDelete}>
          <div className={styles.modalContent} onClick={(e) => e.stopPropagation()}>
            <div className={styles.modalHeader}>
              <h3>리뷰 삭제 확인</h3>
              <button
                className={styles.closeButton}
                onClick={cancelDelete}
              >
                ✕
              </button>
            </div>
            <div className={styles.modalBody}>
              <p className={styles.deleteMessage}>
                정말로 이 리뷰를 삭제하시겠습니까?
              </p>
              <p className={styles.deleteWarning}>
                이 작업은 되돌릴 수 없습니다.
              </p>
              <div className={styles.modalButtons}>
                <button
                  className={styles.cancelButton}
                  onClick={cancelDelete}
                >
                  취소
                </button>
                <button
                  className={styles.deleteConfirmButton}
                  onClick={confirmDelete}
                >
                  삭제
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      <BottomNav />
    </div>
  )
}

