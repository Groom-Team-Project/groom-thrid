'use client'

import { useState, useEffect } from 'react'
import { useRouter } from 'next/navigation'
import { getUserReports, deleteReport, type Report } from '@/lib/reports'
import { isAdmin } from '@/lib/auth'
import BottomNav from '@/components/BottomNav'
import styles from './page.module.css'

export default function ReportListPage() {
  const router = useRouter()
  const [allReports, setAllReports] = useState<Report[]>([])
  const [reports, setReports] = useState<Report[]>([])
  const [selectedReports, setSelectedReports] = useState<Set<string>>(new Set())
  const [showDeleteModal, setShowDeleteModal] = useState(false)
  const [deleteMode, setDeleteMode] = useState<'selected' | 'all' | null>(null)
  const [deleteCount, setDeleteCount] = useState(0)
  const [statusFilter, setStatusFilter] = useState<Report['status'] | 'all'>('all')
  const [openMenuId, setOpenMenuId] = useState<string | null>(null)
  const [showDeleteConfirmModal, setShowDeleteConfirmModal] = useState(false)
  const [reportToDelete, setReportToDelete] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)

  const loadReports = async () => {
    try {
      const userReports = await getUserReports()
      setAllReports(userReports)
      setReports(userReports)
    } catch (error) {
      console.error('제보 목록 로드 실패:', error)
      alert('제보 목록을 불러오는 중 오류가 발생했습니다.')
    } finally {
      setLoading(false)
    }
  }

  // 상태 필터 적용
  useEffect(() => {
    if (statusFilter === 'all') {
      setReports(allReports)
    } else {
      setReports(allReports.filter(r => r.status === statusFilter))
    }
  }, [statusFilter, allReports])

  useEffect(() => {
    const loggedIn = localStorage.getItem('isLoggedIn')
    if (loggedIn !== 'true') {
      router.push('/auth')
      return
    }
    
    loadReports()
  }, [router])

  const handleReportClick = (reportId: string) => {
    setOpenMenuId(null)
    router.push(`/report/detail?id=${reportId}`)
  }

  const handleSelectReport = (reportId: string, e: React.MouseEvent) => {
    e.stopPropagation()
    const newSelected = new Set(selectedReports)
    if (newSelected.has(reportId)) {
      newSelected.delete(reportId)
    } else {
      newSelected.add(reportId)
    }
    setSelectedReports(newSelected)
  }

  const handleSelectAll = () => {
    if (selectedReports.size === reports.length) {
      setSelectedReports(new Set())
    } else {
      setSelectedReports(new Set(reports.map(r => r.id)))
    }
  }

  const handleEditReport = (reportId: string, e: React.MouseEvent) => {
    e.stopPropagation()
    router.push(`/report?reportId=${reportId}`)
  }

  const handleDeleteReport = (reportId: string) => {
    setReportToDelete(reportId)
    setShowDeleteConfirmModal(true)
    setOpenMenuId(null)
  }

  const confirmDeleteReport = async () => {
    if (reportToDelete) {
      try {
        await deleteReport(reportToDelete)
        const updatedAllReports = allReports.filter(r => r.id !== reportToDelete)
        setAllReports(updatedAllReports)
        
        // 필터 적용
        if (statusFilter === 'all') {
          setReports(updatedAllReports)
        } else {
          setReports(updatedAllReports.filter(r => r.status === statusFilter))
        }
        
        const newSelected = new Set(selectedReports)
        newSelected.delete(reportToDelete)
        setSelectedReports(newSelected)
        alert('제보가 삭제되었습니다.')
      } catch (error) {
        console.error('제보 삭제 실패:', error)
        alert('제보 삭제에 실패했습니다.')
      } finally {
        setShowDeleteConfirmModal(false)
        setReportToDelete(null)
      }
    }
  }

  const cancelDeleteReport = () => {
    setShowDeleteConfirmModal(false)
    setReportToDelete(null)
  }

  const handleDeleteSelected = () => {
    if (selectedReports.size === 0) {
      return
    }
    
    setDeleteMode('selected')
    setDeleteCount(selectedReports.size)
    setShowDeleteModal(true)
  }

  const confirmDelete = async () => {
    if (!deleteMode) return

    try {
      if (deleteMode === 'selected') {
        // 선택된 제보들 삭제
        await Promise.all(Array.from(selectedReports).map(reportId => deleteReport(reportId)))
        
        const updatedAllReports = allReports.filter(r => !selectedReports.has(r.id))
        setAllReports(updatedAllReports)
        
        // 필터 적용
        if (statusFilter === 'all') {
          setReports(updatedAllReports)
        } else {
          setReports(updatedAllReports.filter(r => r.status === statusFilter))
        }
        
        setSelectedReports(new Set())
        alert(`${deleteCount}개의 제보가 삭제되었습니다.`)
      }
    } catch (error) {
      console.error('제보 삭제 실패:', error)
      alert('제보 삭제에 실패했습니다.')
    } finally {
      setShowDeleteModal(false)
      setDeleteMode(null)
      setDeleteCount(0)
    }
  }

  const cancelDelete = () => {
    setShowDeleteModal(false)
    setDeleteMode(null)
    setDeleteCount(0)
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

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <div className={styles.placeholder} />
        <h1 className={styles.title}>{isAdmin() ? '제보 목록 관리' : '나의 제보 목록'}</h1>
        <div className={styles.placeholder} />
      </div>

      <div className={styles.content}>
        {allReports.length > 0 && (
          <div className={styles.filterSection}>
            <button
              className={`${styles.filterButton} ${statusFilter === 'all' ? styles.active : ''}`}
              onClick={() => setStatusFilter('all')}
            >
              전체
            </button>
            <button
              className={`${styles.filterButton} ${statusFilter === 'pending' ? styles.active : ''}`}
              onClick={() => setStatusFilter('pending')}
            >
              대기
            </button>
            <button
              className={`${styles.filterButton} ${statusFilter === 'processing' ? styles.active : ''}`}
              onClick={() => setStatusFilter('processing')}
            >
              처리중
            </button>
            <button
              className={`${styles.filterButton} ${statusFilter === 'completed' ? styles.active : ''}`}
              onClick={() => setStatusFilter('completed')}
            >
              승인
            </button>
            <button
              className={`${styles.filterButton} ${statusFilter === 'rejected' ? styles.active : ''}`}
              onClick={() => setStatusFilter('rejected')}
            >
              반려
            </button>
          </div>
        )}

        {loading ? (
          <div className={styles.emptyState}>
            <p className={styles.emptyText}>로딩 중...</p>
          </div>
        ) : reports.length === 0 ? (
          <div className={styles.emptyState}>
            <p className={styles.emptyText}>{isAdmin() ? '제보 내역이 없습니다.' : '제보한 내역이 없습니다.'}</p>
          </div>
        ) : (
          <div className={styles.reportList}>
            <div className={styles.selectAllSection}>
              <label className={styles.selectAllLabel}>
                <input
                  type="checkbox"
                  checked={selectedReports.size === reports.length && reports.length > 0}
                  onChange={handleSelectAll}
                  className={styles.checkbox}
                />
                <span>전체 선택</span>
              </label>
              {selectedReports.size > 0 && (
                <button
                  className={styles.deleteSelectedButton}
                  onClick={handleDeleteSelected}
                >
                  삭제 ({selectedReports.size})
                </button>
              )}
            </div>
            {reports.map((report) => {
              // 본인 제보인지 확인 (작성자 이메일과 현재 사용자 이메일 비교 - 동명이인 구분)
              const currentUserEmail = localStorage.getItem('userEmail') || ''
              const authorEmail = report.authorEmail
              const canEditOrDelete = isAdmin() || (authorEmail && authorEmail === currentUserEmail)

              return (
                <div
                  key={report.id}
                  className={`${styles.reportItem} ${selectedReports.has(report.id) ? styles.selected : ''}`}
                  onClick={() => handleReportClick(report.id)}
                >
                  <div className={styles.reportCheckbox}>
                    <input
                      type="checkbox"
                      checked={selectedReports.has(report.id)}
                      onChange={(e) => handleSelectReport(report.id, e)}
                      onClick={(e) => e.stopPropagation()}
                      className={styles.checkbox}
                    />
                  </div>
                  <div className={styles.reportContent}>
                    <p className={styles.reportText}>{report.content}</p>
                    <div className={styles.reportMeta}>
                      <span className={styles.reportDate}>{report.date}</span>
                      <span
                        className={styles.reportStatus}
                        style={{ color: getStatusColor(report.status) }}
                      >
                        {getStatusText(report.status)}
                      </span>
                    </div>
                  </div>
                  {canEditOrDelete && (
                    <div className={styles.reportActions}>
                      <div className={styles.menuContainer}>
                        <button
                          className={styles.menuButton}
                          onClick={(e) => {
                            e.stopPropagation()
                            setOpenMenuId(openMenuId === report.id ? null : report.id)
                          }}
                        >
                          ⋯
                        </button>
                        {openMenuId === report.id && (
                          <>
                            <div 
                              className={styles.menuOverlay}
                              onClick={() => setOpenMenuId(null)}
                            />
                            <div className={styles.menu}>
                              <button
                                className={styles.menuItem}
                                onClick={(e) => {
                                  e.stopPropagation()
                                  setOpenMenuId(null)
                                  handleEditReport(report.id, e)
                                }}
                              >
                                수정
                              </button>
                              <button
                                className={`${styles.menuItem} ${styles.deleteMenuItem}`}
                                onClick={(e) => {
                                  e.stopPropagation()
                                  handleDeleteReport(report.id)
                                }}
                              >
                                삭제
                              </button>
                            </div>
                          </>
                        )}
                      </div>
                    </div>
                  )}
                </div>
              )
            })}
          </div>
        )}
      </div>

      {/* 개별 제보 삭제 확인 모달 */}
      {showDeleteConfirmModal && (
        <div className={styles.modal} onClick={cancelDeleteReport}>
          <div className={styles.modalContent} onClick={(e) => e.stopPropagation()}>
            <div className={styles.modalHeader}>
              <h3>제보 삭제 확인</h3>
              <button
                className={styles.closeButton}
                onClick={cancelDeleteReport}
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
                  onClick={cancelDeleteReport}
                >
                  취소
                </button>
                <button
                  className={styles.deleteConfirmButton}
                  onClick={confirmDeleteReport}
                >
                  삭제
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* 일괄 삭제 확인 모달 */}
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
                {deleteMode === 'selected' 
                  ? `선택한 ${deleteCount}개의 제보를 삭제하시겠습니까?`
                  : `모든 제보(${deleteCount}개)를 삭제하시겠습니까?`}
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

