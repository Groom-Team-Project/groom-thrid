'use client'

import { useState, useEffect } from 'react'
import { useSearchParams, useRouter } from 'next/navigation'
import { getReportById, deleteReport, updateReportStatus, type Report } from '@/lib/reports'
import { isAdmin } from '@/lib/auth'
import BottomNav from '@/components/BottomNav'
import styles from './page.module.css'

export default function ReportDetailPage() {
  const searchParams = useSearchParams()
  const router = useRouter()
  const reportId = searchParams.get('id')
  const [report, setReport] = useState<Report | null>(null)
  const [userId, setUserId] = useState('')
  const [isOwner, setIsOwner] = useState(false)
  const [showMenu, setShowMenu] = useState(false)
  const [showDeleteModal, setShowDeleteModal] = useState(false)
  const [showResponseModal, setShowResponseModal] = useState(false)
  const [responseText, setResponseText] = useState('')
  const [responseStatus, setResponseStatus] = useState<'processing' | 'completed' | 'rejected' | null>(null)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const loadReport = async () => {
      const loggedIn = localStorage.getItem('isLoggedIn')
      if (loggedIn !== 'true') {
        router.push('/auth')
        return
      }

      const currentUserEmail = localStorage.getItem('userEmail') || ''
      setUserId(currentUserEmail)

      if (reportId) {
        try {
          const foundReport = await getReportById(reportId)
          if (foundReport) {
            setReport(foundReport)
            // 작성자 이메일과 현재 사용자 이메일을 비교 (동명이인 구분)
            // ADMIN은 모든 제보를 수정/삭제할 수 있음
            const authorEmail = foundReport.authorEmail
            setIsOwner(isAdmin() || (authorEmail && authorEmail === currentUserEmail))
          } else {
            alert('제보를 찾을 수 없습니다.')
            router.push('/report/list')
          }
        } catch (error) {
          console.error('제보 로드 실패:', error)
          alert('제보를 불러오는 중 오류가 발생했습니다.')
          router.push('/report/list')
        } finally {
          setLoading(false)
        }
      } else {
        router.push('/report/list')
      }
    }

    loadReport()
  }, [reportId, router])

  const handleDelete = () => {
    setShowDeleteModal(true)
    setShowMenu(false)
  }

  const confirmDelete = async () => {
    if (!report) return
    
    try {
      await deleteReport(report.id)
      alert('제보가 삭제되었습니다.')
      router.push('/report/list')
    } catch (error) {
      console.error('제보 삭제 실패:', error)
      alert('제보 삭제에 실패했습니다.')
    } finally {
      setShowDeleteModal(false)
    }
  }

  const cancelDelete = () => {
    setShowDeleteModal(false)
  }

  const handleEdit = () => {
    if (!report) return
    setShowMenu(false)
    router.push(`/report?reportId=${report.id}`)
  }

  const handleReply = () => {
    setShowResponseModal(true)
  }

  const handleResponseSubmit = async () => {
    if (!report || !responseStatus) {
      alert('상태를 선택해주세요.')
      return
    }

    // 승인 또는 반려인 경우 답변 메시지 필수
    if ((responseStatus === 'completed' || responseStatus === 'rejected') && !responseText.trim()) {
      alert('답변 메시지를 입력해주세요.')
      return
    }

    setIsSubmitting(true)
    try {
      const updated = await updateReportStatus(report.id, {
        status: responseStatus,
        adminReply: responseText.trim() || undefined, // 처리 중인 경우 adminReply는 선택사항
      })
      
      if (updated) {
        setReport(updated)
        setShowResponseModal(false)
        setResponseText('')
        setResponseStatus(null)
        alert('상태가 변경되었습니다.')
      }
    } catch (error) {
      console.error('상태 변경 실패:', error)
      alert('상태 변경에 실패했습니다.')
    } finally {
      setIsSubmitting(false)
    }
  }

  const cancelResponse = () => {
    setShowResponseModal(false)
    setResponseText('')
    setResponseStatus(null)
  }

  const getStatusText = (status: Report['status']) => {
    switch (status) {
      case 'pending':
        return '대기'
      case 'processing':
        return '처리 중'
      case 'completed':
        return '처리 완료'
      case 'rejected':
        return '반려'
      default:
        return '대기'
    }
  }

  const getStatusMessage = (status: Report['status']) => {
    switch (status) {
      case 'pending':
        return '관리자가 아직 확인하지 않았습니다.'
      case 'processing':
        return report?.adminCheckedDate 
          ? `관리자가 확인했습니다. (${report.adminCheckedDate})`
          : '관리자가 확인했습니다.'
      case 'completed':
        return '관리자가 확인하여 처리 완료되었습니다.'
      case 'rejected':
        return '제보가 반려되었습니다.'
      default:
        return '관리자가 아직 확인하지 않았습니다.'
    }
  }

  const getStatusColor = (status: Report['status']) => {
    switch (status) {
      case 'pending':
      case 'processing':
        return '#007AFF'
      case 'completed':
        return '#34C759'
      case 'rejected':
        return '#ff3b30'
      default:
        return '#007AFF'
    }
  }

  if (loading || !report) {
    return (
      <div className={styles.container}>
        <div className={styles.loading}>로딩 중...</div>
      </div>
    )
  }

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <button className={styles.backButton} onClick={() => router.back()}>
          ←
        </button>
        <h1 className={styles.title}>제보 상세 조회</h1>
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
                    onClick={handleEdit}
                  >
                    수정
                  </button>
                  <button
                    className={`${styles.menuItem} ${styles.deleteMenuItem}`}
                    onClick={handleDelete}
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

      <div className={styles.subtitle}>{isAdmin() ? '제보 상세 조회' : '나의 제보 조회'}</div>

      <div className={styles.content}>
        {isAdmin() && report.authorName && (
          <>
            <div className={styles.inputGroup}>
              <label className={styles.label}>작성자</label>
              <div className={styles.value}>{report.authorName}</div>
            </div>
            {report.authorEmail && (
              <div className={styles.inputGroup}>
                <label className={styles.label}>작성자 이메일</label>
                <div className={styles.value}>{report.authorEmail}</div>
              </div>
            )}
          </>
        )}
        <div className={styles.inputGroup}>
          <label className={styles.label}>충전소명</label>
          <div className={styles.value}>{report.stationName}</div>
        </div>

        <div className={styles.inputGroup}>
          <label className={styles.label}>제보내용</label>
          <div className={styles.value}>{report.content}</div>
        </div>

        {report.photoUrl && (
          <div className={styles.inputGroup}>
            <label className={styles.label}>사진 첨부(선택)</label>
            <div className={styles.photoContainer}>
              <img src={report.photoUrl} alt="Report" className={styles.photo} />
            </div>
          </div>
        )}

        {report.adminResponse && (
          <div className={styles.inputGroup}>
            <label className={styles.label}>관리자 답변</label>
            <div className={styles.adminResponse}>
              <p>{report.adminResponse}</p>
              {report.adminResponseDate && (
                <span className={styles.responseDate}>{report.adminResponseDate}</span>
              )}
            </div>
          </div>
        )}

        <div className={styles.statusSection}>
          <p className={styles.statusMessage}>{getStatusMessage(report.status)}</p>
          <div
            className={styles.statusBadge}
            style={{ color: getStatusColor(report.status) }}
          >
            {getStatusText(report.status)}
          </div>
        </div>

        {isAdmin() && (
          <div className={styles.adminActions}>
            <button
              className={styles.replyButton}
              onClick={handleReply}
            >
              답변하기
            </button>
          </div>
        )}

      </div>

      {/* 답변 모달 */}
      {showResponseModal && report && (
        <div className={styles.modal} onClick={cancelResponse}>
          <div className={styles.modalContent} onClick={(e) => e.stopPropagation()}>
            <div className={styles.modalHeader}>
              <h3>상태 변경 및 답변</h3>
              <button
                className={styles.closeButton}
                onClick={cancelResponse}
              >
                ✕
              </button>
            </div>
            <div className={styles.modalBody}>
              <div className={styles.inputGroup}>
                <label className={styles.label}>충전소명</label>
                <div className={styles.value}>{report.stationName}</div>
              </div>
              <div className={styles.inputGroup}>
                <label className={styles.label}>상태 변경</label>
                <div className={styles.statusButtons}>
                  <button
                    className={`${styles.statusButton} ${responseStatus === 'processing' ? styles.active : ''}`}
                    onClick={() => setResponseStatus('processing')}
                  >
                    처리 중
                  </button>
                  <button
                    className={`${styles.statusButton} ${responseStatus === 'completed' ? styles.active : ''}`}
                    onClick={() => setResponseStatus('completed')}
                  >
                    승인
                  </button>
                  <button
                    className={`${styles.statusButton} ${styles.rejectStatusButton} ${responseStatus === 'rejected' ? styles.active : ''}`}
                    onClick={() => setResponseStatus('rejected')}
                  >
                    반려
                  </button>
                </div>
              </div>
              <div className={styles.responseSection}>
                <label className={styles.label}>
                  답변 내용
                  {(responseStatus === 'completed' || responseStatus === 'rejected') && (
                    <span className={styles.required}> *</span>
                  )}
                </label>
                <textarea
                  className={styles.responseTextarea}
                  value={responseText}
                  onChange={(e) => setResponseText(e.target.value)}
                  placeholder={
                    responseStatus === 'processing' 
                      ? '답변 메시지를 입력해주세요. (선택사항)'
                      : responseStatus === 'completed'
                      ? '승인 사유를 입력해주세요. (필수)'
                      : responseStatus === 'rejected'
                      ? '반려 사유를 입력해주세요. (필수)'
                      : '상태를 선택한 후 답변을 입력해주세요.'
                  }
                  rows={6}
                  maxLength={2000}
                  disabled={!responseStatus}
                />
                <div className={styles.charCount}>
                  {responseText.length} / 2000
                </div>
              </div>
              <div className={styles.modalButtons}>
                <button
                  className={styles.cancelButton}
                  onClick={cancelResponse}
                  disabled={isSubmitting}
                >
                  취소
                </button>
                <button
                  className={styles.submitButton}
                  onClick={handleResponseSubmit}
                  disabled={
                    isSubmitting || 
                    !responseStatus || 
                    ((responseStatus === 'completed' || responseStatus === 'rejected') && !responseText.trim())
                  }
                >
                  {isSubmitting ? '전송 중...' : '전송'}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* 삭제 확인 모달 */}
      {showDeleteModal && (
        <div className={styles.modal} onClick={cancelDelete}>
          <div className={styles.modalContent} onClick={(e) => e.stopPropagation()}>
            <div className={styles.modalHeader}>
              <h3>제보 삭제 확인</h3>
              <button
                className={styles.closeButton}
                onClick={cancelDelete}
              >
                ✕
              </button>
            </div>
            <div className={styles.modalBody}>
              <p className={styles.deleteMessage}>
                정말로 이 제보를 삭제하시겠습니까?
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

