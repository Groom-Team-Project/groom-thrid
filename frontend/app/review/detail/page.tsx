'use client'

import { useState, useEffect } from 'react'
import { useRouter, useSearchParams } from 'next/navigation'
import { getReviewById, deleteReview, type Review } from '@/lib/reviews'
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

  useEffect(() => {
    const loggedIn = localStorage.getItem('isLoggedIn')
    if (loggedIn !== 'true') {
      router.push('/auth')
      return
    }

    if (!reviewId) {
      router.push('/')
      return
    }

    const reviewData = getReviewById(reviewId)
    if (!reviewData) {
      alert('리뷰를 찾을 수 없습니다.')
      router.push('/')
      return
    }

    setReview(reviewData)
  }, [router, reviewId])

  const userId = localStorage.getItem('userEmail') || ''
  const isOwner = review?.userId === userId

  const handleEdit = () => {
    if (review) {
      router.push(`/review/write?reviewId=${review.id}&stationId=${review.stationId}`)
    }
  }

  const handleDelete = () => {
    setShowDeleteModal(true)
  }

  const confirmDelete = () => {
    if (review) {
      deleteReview(review.id)
      alert('리뷰가 삭제되었습니다.')
      router.push(`/?stationId=${review.stationId}&tab=review`)
    }
  }

  const cancelDelete = () => {
    setShowDeleteModal(false)
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
        {isOwner && (
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
        {!isOwner && <div className={styles.placeholder} />}
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

