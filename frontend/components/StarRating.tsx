'use client'

import { useState, useEffect } from 'react'
import styles from './StarRating.module.css'

interface StarRatingProps {
  rating: number
  onRatingChange?: (rating: number) => void
  editable?: boolean
  size?: 'small' | 'medium' | 'large'
}

export default function StarRating({ 
  rating, 
  onRatingChange, 
  editable = false,
  size = 'medium'
}: StarRatingProps) {
  const [hoverRating, setHoverRating] = useState<number | null>(null)
  const [displayRating, setDisplayRating] = useState(rating)

  useEffect(() => {
    setDisplayRating(rating)
  }, [rating])

  const handleStarClick = (value: number, e: React.MouseEvent<HTMLSpanElement>) => {
    if (!editable || !onRatingChange) return
    
    // 클릭한 별의 위치에 따라 정확한 점수 계산
    const rect = e.currentTarget.getBoundingClientRect()
    const clickX = e.clientX - rect.left
    const starWidth = rect.width
    const isHalf = clickX < starWidth / 2
    const finalRating = isHalf ? value - 0.5 : value
    setDisplayRating(finalRating)
    onRatingChange(finalRating)
  }

  const handleStarHover = (value: number, e: React.MouseEvent<HTMLSpanElement>) => {
    if (!editable) return
    const rect = e.currentTarget.getBoundingClientRect()
    const hoverX = e.clientX - rect.left
    const starWidth = rect.width
    const isHalf = hoverX < starWidth / 2
    setHoverRating(isHalf ? value - 0.5 : value)
  }

  const handleMouseLeave = () => {
    if (!editable) return
    setHoverRating(null)
  }

  const currentRating = hoverRating !== null ? hoverRating : displayRating

  const renderStar = (index: number) => {
    const starValue = index + 1
    const isFull = currentRating >= starValue
    const isHalf = currentRating >= starValue - 0.5 && currentRating < starValue

    return (
      <span
        key={index}
        className={`${styles.star} ${styles[size]} ${editable ? styles.editable : ''}`}
        onClick={(e) => handleStarClick(starValue, e)}
        onMouseMove={(e) => handleStarHover(starValue, e)}
        onMouseLeave={handleMouseLeave}
      >
        <span className={styles.starOutline}>★</span>
        <span 
          className={`${styles.starFill} ${isFull ? styles.full : isHalf ? styles.half : ''}`}
          style={{ width: isFull ? '100%' : isHalf ? '50%' : '0%' }}
        >
          ★
        </span>
      </span>
    )
  }

  return (
    <div className={styles.starRating}>
      {[0, 1, 2, 3, 4].map(renderStar)}
      {editable && (
        <span className={styles.ratingText}>{currentRating.toFixed(1)}</span>
      )}
    </div>
  )
}

