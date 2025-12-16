'use client'

export const dynamic = 'force-dynamic'

import { useState, useEffect } from 'react'
import { useRouter, useSearchParams } from 'next/navigation'
import { createReport, getReportById, updateReport, type Report } from '@/lib/reports'
import { chargerApi } from '@/lib/stations'
import { isAdmin } from '@/lib/auth'
import BottomNav from '@/components/BottomNav'
import styles from './page.module.css'

export default function ReportPage() {
  const router = useRouter()
  const searchParams = useSearchParams()
  const reportId = searchParams?.get('reportId')
  const placeIdParam = searchParams?.get('placeId')
  const [stationName, setStationName] = useState('')
  const [placeId, setPlaceId] = useState<number | null>(null)
  const [content, setContent] = useState('')
  const [photo, setPhoto] = useState<File | null>(null)
  const [photoPreview, setPhotoPreview] = useState<string | null>(null)
  const [error, setError] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [isEditMode, setIsEditMode] = useState(false)
  const [existingReport, setExistingReport] = useState<Report | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const loadData = async () => {
      const loggedIn = localStorage.getItem('isLoggedIn')
      if (loggedIn !== 'true') {
        router.push('/auth')
        return
      }
      
      try {
        // 수정 모드인 경우 기존 제보 데이터 로드
        if (reportId) {
          const report = await getReportById(reportId)
          const currentUserEmail = localStorage.getItem('userEmail') || ''
          
          if (!report) {
            alert('제보를 찾을 수 없습니다.')
            router.push('/report/list')
            return
          }
          
          // 본인 제보인지 확인 (작성자 이메일과 현재 사용자 이메일 비교 - 동명이인 구분)
          // ADMIN은 모든 제보를 수정할 수 있음
          // 백엔드에서도 권한 체크하지만 프론트엔드에서도 체크
          const authorEmail = report.authorEmail
          if (!isAdmin() && (!authorEmail || authorEmail !== currentUserEmail)) {
            alert('본인의 제보만 수정할 수 있습니다.')
            router.push('/report/list')
            return
          }
          
          setExistingReport(report)
          setStationName(report.stationName)
          setContent(report.content)
          if (report.photoUrl) {
            setPhotoPreview(report.photoUrl)
          }
          setIsEditMode(true)
        } else {
          // 작성 모드: URL 파라미터에서 placeId 또는 stationName 가져오기
          if (placeIdParam) {
            const placeIdNum = parseInt(placeIdParam)
            if (!isNaN(placeIdNum)) {
              setPlaceId(placeIdNum)
              // 충전소 정보 가져오기
              try {
                const station = await chargerApi.getChargerById(placeIdNum)
                setStationName(station.facilityName)
              } catch (err) {
                console.error('충전소 정보 로드 실패:', err)
                setError('충전소 정보를 불러올 수 없습니다.')
              }
            }
          } else {
            // stationName만 있는 경우 (하위 호환성)
            const stationNameParam = searchParams?.get('stationName')
            if (stationNameParam) {
              setStationName(decodeURIComponent(stationNameParam))
            }
          }
        }
      } catch (err) {
        console.error('데이터 로드 실패:', err)
        setError('데이터를 불러오는 중 오류가 발생했습니다.')
      } finally {
        setLoading(false)
      }
    }

    loadData()
  }, [searchParams, router, reportId, placeIdParam])

  // 이미지 압축 함수
  const compressImage = (file: File, maxSizeMB: number = 10): Promise<string> => {
    return new Promise((resolve, reject) => {
      const reader = new FileReader()
      reader.onload = (e) => {
        const img = new Image()
        img.onload = () => {
          const canvas = document.createElement('canvas')
          let width = img.width
          let height = img.height
          
          // 최대 크기 제한 (1920px)
          const maxDimension = 1920
          if (width > maxDimension || height > maxDimension) {
            if (width > height) {
              height = (height / width) * maxDimension
              width = maxDimension
            } else {
              width = (width / height) * maxDimension
              height = maxDimension
            }
          }
          
          canvas.width = width
          canvas.height = height
          
          const ctx = canvas.getContext('2d')
          if (!ctx) {
            reject(new Error('Canvas context를 가져올 수 없습니다.'))
            return
          }
          
          ctx.drawImage(img, 0, 0, width, height)
          
          // 품질 조정하여 압축 (최대 10MB까지 시도)
          let quality = 0.9
          let dataUrl = canvas.toDataURL('image/jpeg', quality)
          
          // Base64 크기 체크 (약 10MB 제한)
          const maxBase64Size = maxSizeMB * 1024 * 1024 * 1.33 // Base64는 약 33% 더 큼
          while (dataUrl.length > maxBase64Size && quality > 0.1) {
            quality -= 0.1
            dataUrl = canvas.toDataURL('image/jpeg', quality)
          }
          
          resolve(dataUrl)
        }
        img.onerror = () => reject(new Error('이미지를 로드할 수 없습니다.'))
        img.src = e.target?.result as string
      }
      reader.onerror = () => reject(new Error('파일을 읽을 수 없습니다.'))
      reader.readAsDataURL(file)
    })
  }

  const handlePhotoChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    if (file) {
      if (file.size > 20 * 1024 * 1024) {
        setError('사진 크기는 20MB 이하여야 합니다.')
        return
      }
      
      // 이미지 파일 타입 검증
      if (!file.type.startsWith('image/')) {
        setError('이미지 파일만 업로드 가능합니다.')
        return
      }
      
      setError('') // 에러 초기화
      
      try {
        // 이미지가 5MB 이상이면 자동 압축
        let photoPreview: string
        if (file.size > 5 * 1024 * 1024) {
          setError('이미지를 압축 중입니다...')
          photoPreview = await compressImage(file, 10) // 최대 10MB로 압축
          setError('')
        } else {
          // FileReader로 base64 변환
          const reader = new FileReader()
          photoPreview = await new Promise<string>((resolve, reject) => {
            reader.onloadend = () => {
              resolve(reader.result as string)
            }
            reader.onerror = () => reject(new Error('이미지 파일을 읽는 중 오류가 발생했습니다.'))
            reader.readAsDataURL(file)
          })
        }
        
        setPhoto(file)
        setPhotoPreview(photoPreview)
      } catch (err) {
        console.error('이미지 처리 실패:', err)
        setError(err instanceof Error ? err.message : '이미지 처리 중 오류가 발생했습니다.')
        setPhoto(null)
        setPhotoPreview(null)
      }
    }
  }

  const handleRemovePhoto = () => {
    setPhoto(null)
    setPhotoPreview(null)
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    setIsSubmitting(true)

    if (!content.trim()) {
      setError('제보내용을 입력해주세요.')
      setIsSubmitting(false)
      return
    }

    // 작성 모드에서 placeId가 없으면 오류
    if (!isEditMode && !placeId) {
      setError('충전소 정보가 없습니다. 다시 시도해주세요.')
      setIsSubmitting(false)
      return
    }

    try {
      let photoUrl: string | undefined

      // 사진이 있으면 base64로 변환 (백엔드 S3Service가 처리)
      if (photoPreview) {
        photoUrl = photoPreview
      }

      if (isEditMode && existingReport) {
        // 수정 모드
        const updated = await updateReport(existingReport.id, {
          content: content.trim(),
          imageUrl: photoUrl,
        })
        
        if (updated) {
          alert('제보가 수정되었습니다.')
          router.push('/report/list')
        } else {
          setError('제보 수정에 실패했습니다.')
        }
      } else {
        // 작성 모드
        if (!placeId) {
          setError('충전소 정보가 없습니다.')
          setIsSubmitting(false)
          return
        }

        await createReport(placeId, {
          content: content.trim(),
          imageUrl: photoUrl,
        })

        alert('제보가 성공적으로 제출되었습니다.')
        router.push('/report/list')
      }
    } catch (err) {
      console.error('제보 제출 실패:', err)
      if (err instanceof Error) {
        setError(err.message)
      } else {
        setError('제보 제출 중 오류가 발생했습니다.')
      }
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <h1 className={styles.title}>{isEditMode ? '제보 수정' : '제보하기'}</h1>
        <button className={styles.cancelButton} onClick={() => router.back()}>
          취소
        </button>
      </div>

      <div className={styles.subtitle}>충전소 제보</div>

      {loading ? (
        <div className={styles.loading}>로딩 중...</div>
      ) : (
        <form className={styles.form} onSubmit={handleSubmit}>
          <div className={styles.inputGroup}>
            <label className={styles.label}>충전소명</label>
            <input
              type="text"
              className={styles.input}
              placeholder="충전소명"
              value={stationName}
              readOnly
              disabled
            />
          </div>

        <div className={styles.inputGroup}>
          <label className={styles.label}>제보내용</label>
          <textarea
            className={styles.textarea}
            placeholder="제보내용을 입력하세요"
            value={content}
            onChange={(e) => setContent(e.target.value)}
            rows={6}
          />
        </div>

        <div className={styles.inputGroup}>
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

        {error && <p className={styles.error}>{error}</p>}

          <button
            type="submit"
            className={styles.submitButton}
            disabled={isSubmitting}
          >
            {isSubmitting ? '처리 중...' : isEditMode ? '수정하기' : '제보하기'}
          </button>
        </form>
      )}

      <BottomNav />
    </div>
  )
}

