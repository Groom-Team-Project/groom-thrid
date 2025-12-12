'use client'

import { useState, useEffect } from 'react'
import { useRouter, useSearchParams } from 'next/navigation'
import { saveReport, getReportById, updateReport, type Report } from '@/lib/reports'
import BottomNav from '@/components/BottomNav'
import styles from './page.module.css'

export default function ReportPage() {
  const router = useRouter()
  const searchParams = useSearchParams()
  const reportId = searchParams?.get('reportId')
  const [stationName, setStationName] = useState('')
  const [content, setContent] = useState('')
  const [photo, setPhoto] = useState<File | null>(null)
  const [photoPreview, setPhotoPreview] = useState<string | null>(null)
  const [error, setError] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [isEditMode, setIsEditMode] = useState(false)
  const [existingReport, setExistingReport] = useState<Report | null>(null)

  useEffect(() => {
    const loggedIn = localStorage.getItem('isLoggedIn')
    if (loggedIn !== 'true') {
      router.push('/auth')
      return
    }
    
    // 수정 모드인 경우 기존 제보 데이터 로드
    if (reportId) {
      const report = getReportById(reportId)
      const userId = localStorage.getItem('userEmail') || ''
      
      if (!report) {
        alert('제보를 찾을 수 없습니다.')
        router.push('/report/list')
        return
      }
      
      // 본인 제보인지 확인
      if (report.userId !== userId) {
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
      // 작성 모드: URL 파라미터에서 충전소명 가져오기
      const stationNameParam = searchParams?.get('stationName')
      if (stationNameParam) {
        setStationName(decodeURIComponent(stationNameParam))
      }
    }
  }, [searchParams, router, reportId])

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
    setIsSubmitting(true)

    if (!stationName.trim()) {
      setError('충전소명을 입력해주세요.')
      setIsSubmitting(false)
      return
    }

    if (!content.trim()) {
      setError('제보내용을 입력해주세요.')
      setIsSubmitting(false)
      return
    }

    try {
      const userId = localStorage.getItem('userEmail') || 'anonymous'
      let photoUrl: string | undefined

      // 사진이 있으면 base64로 저장 (실제로는 서버에 업로드)
      if (photoPreview) {
        photoUrl = photoPreview
      }

      if (isEditMode && existingReport) {
        // 수정 모드
        const updated = updateReport(existingReport.id, {
          stationName: stationName.trim(),
          content: content.trim(),
          photoUrl,
        })
        
        if (updated) {
          alert('제보가 수정되었습니다.')
          router.push(`/report/detail?id=${existingReport.id}`)
        } else {
          setError('제보 수정에 실패했습니다.')
        }
      } else {
        // 작성 모드
        saveReport({
          stationName: stationName.trim(),
          content: content.trim(),
          photoUrl,
          userId,
        })

        alert('제보가 성공적으로 제출되었습니다.')
        router.push('/report/list')
      }
    } catch (err) {
      setError('제보 제출 중 오류가 발생했습니다.')
      console.error(err)
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

      <form className={styles.form} onSubmit={handleSubmit}>
        <div className={styles.inputGroup}>
          <label className={styles.label}>충전소명</label>
          <input
            type="text"
            className={styles.input}
            placeholder="충전소명을 입력하세요"
            value={stationName}
            onChange={(e) => setStationName(e.target.value)}
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

      <BottomNav />
    </div>
  )
}

