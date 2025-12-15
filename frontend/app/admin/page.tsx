'use client'

import { useState, useEffect } from 'react'
import { useRouter } from 'next/navigation'
import { getUserReports, updateReportStatus, deleteReport, type Report } from '@/lib/reports'
import { isAdmin, logout } from '@/lib/auth'
import BottomNav from '@/components/BottomNav'
import styles from './page.module.css'

export default function AdminPage() {
  const router = useRouter()
  const [reports, setReports] = useState<Report[]>([])
  const [selectedReport, setSelectedReport] = useState<Report | null>(null)
  const [showResponseModal, setShowResponseModal] = useState(false)
  const [responseStatus, setResponseStatus] = useState<'completed' | 'rejected' | null>(null)
  const [responseText, setResponseText] = useState('')
  const [stationLocation, setStationLocation] = useState<{ lat: number; lng: number } | null>(null)
  const [statusFilter, setStatusFilter] = useState<Report['status'] | 'all'>('all')
  const [allReports, setAllReports] = useState<Report[]>([])
  const [selectedReports, setSelectedReports] = useState<Set<string>>(new Set())
  const [showDeleteModal, setShowDeleteModal] = useState(false)
  const [deleteMode, setDeleteMode] = useState<'selected' | 'all' | null>(null)
  const [deleteCount, setDeleteCount] = useState(0)
  const [openMenuId, setOpenMenuId] = useState<string | null>(null)
  const [showDeleteConfirmModal, setShowDeleteConfirmModal] = useState(false)
  const [reportToDelete, setReportToDelete] = useState<string | null>(null)
  const [showDetailMenu, setShowDetailMenu] = useState(false)

  useEffect(() => {
    // 로그인 및 관리자 권한 확인
    const loggedIn = localStorage.getItem('isLoggedIn')
    
    if (loggedIn !== 'true' || !isAdmin()) {
      router.push('/auth')
      return
    }

    // 모든 제보 목록 가져오기 (백엔드 API)
    const loadReports = async () => {
      try {
        const reportsData = await getUserReports()
        setAllReports(reportsData)
        setReports(reportsData)
      } catch (error) {
        console.error('제보 목록 로드 실패:', error)
        alert('제보 목록을 불러오는 중 오류가 발생했습니다.')
      }
    }
    
    loadReports()
  }, [router])

  // 상태 필터 적용
  useEffect(() => {
    if (statusFilter === 'all') {
      setReports(allReports)
    } else {
      setReports(allReports.filter(r => r.status === statusFilter))
    }
  }, [statusFilter, allReports])

  const handleStatusChange = (reportId: string, newStatus: Report['status']) => {
    // 완료 또는 반려인 경우 답변 모달 표시
    if (newStatus === 'completed' || newStatus === 'rejected') {
      setResponseStatus(newStatus)
      setShowResponseModal(true)
    } else {
      // 처리 중으로 변경 시 바로 업데이트 (백엔드 API 호출)
      updateReportStatusAsync(reportId, newStatus)
    }
  }

  const updateReportStatusAsync = async (reportId: string, newStatus: Report['status'], adminReply?: string) => {
    try {
      const updated = await updateReportStatus(reportId, {
        status: newStatus,
        adminReply: adminReply,
      })
      
      if (updated) {
        const updatedAllReports = allReports.map(r => r.id === reportId ? updated : r)
        setAllReports(updatedAllReports)
        
        // 필터 적용
        if (statusFilter === 'all') {
          setReports(updatedAllReports)
        } else {
          setReports(updatedAllReports.filter(r => r.status === statusFilter))
        }
        
        if (selectedReport?.id === reportId) {
          setSelectedReport(updated)
        }
      }
    } catch (error) {
      console.error('제보 상태 변경 실패:', error)
      alert('제보 상태 변경에 실패했습니다.')
    }
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
        
        if (selectedReport?.id === reportToDelete) {
          setSelectedReport(null)
        }
        
        // 선택 목록에서도 제거
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

  const handleSelectReport = (reportId: string) => {
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

  const handleDeleteSelected = () => {
    if (selectedReports.size === 0) {
      alert('삭제할 제보를 선택해주세요.')
      return
    }
    
    setDeleteMode('selected')
    setDeleteCount(selectedReports.size)
    setShowDeleteModal(true)
  }

  const handleDeleteAll = () => {
    if (reports.length === 0) {
      alert('삭제할 제보가 없습니다.')
      return
    }
    
    setDeleteMode('all')
    setDeleteCount(reports.length)
    setShowDeleteModal(true)
  }

  const confirmDelete = async () => {
    if (!deleteMode) return

    try {
      if (deleteMode === 'selected') {
        await Promise.all(Array.from(selectedReports).map(reportId => deleteReport(reportId)))
        
        const updatedAllReports = allReports.filter(r => !selectedReports.has(r.id))
        setAllReports(updatedAllReports)
        
        // 필터 적용
        if (statusFilter === 'all') {
          setReports(updatedAllReports)
        } else {
          setReports(updatedAllReports.filter(r => r.status === statusFilter))
        }
        
        if (selectedReport && selectedReports.has(selectedReport.id)) {
          setSelectedReport(null)
        }
        
        setSelectedReports(new Set())
        alert(`${deleteCount}개의 제보가 삭제되었습니다.`)
      } else if (deleteMode === 'all') {
        const reportIdsToDelete = new Set(reports.map(r => r.id))
        
        await Promise.all(Array.from(reportIdsToDelete).map(reportId => deleteReport(reportId)))
        
        const updatedAllReports = allReports.filter(r => !reportIdsToDelete.has(r.id))
        setAllReports(updatedAllReports)
        setReports([])
        setSelectedReport(null)
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

  const handleResponseSubmit = async () => {
    if (!selectedReport || !responseStatus || !responseText.trim()) {
      alert('답변을 입력해주세요.')
      return
    }

    try {
      await updateReportStatusAsync(selectedReport.id, responseStatus, responseText.trim())
      setShowResponseModal(false)
      setResponseText('')
      setResponseStatus(null)
      alert('답변이 전송되었습니다.')
    } catch (error) {
      console.error('답변 전송 실패:', error)
      alert('답변 전송에 실패했습니다.')
    }
  }

  const handleReportSelect = (report: Report) => {
    setSelectedReport(report)
    // 충전소 위치는 필요시 chargerApi로 가져올 수 있음
    setStationLocation(null)
  }

  const getStatusText = (status: Report['status']) => {
    switch (status) {
      case 'pending':
        return '대기'
      case 'processing':
        return '처리 중'
      case 'completed':
        return '완료'
      case 'rejected':
        return '반려'
      default:
        return '대기'
    }
  }

  const getStatusColor = (status: Report['status']) => {
    switch (status) {
      case 'pending':
        return '#007AFF'
      case 'processing':
        return '#FF9500'
      case 'completed':
        return '#34C759'
      case 'rejected':
        return '#ff3b30'
      default:
        return '#007AFF'
    }
  }

  const handleLogout = async () => {
    try {
      await logout()
      router.push('/auth')
    } catch (error) {
      console.error('로그아웃 중 오류 발생:', error)
      // 에러가 발생해도 로그인 페이지로 이동
      router.push('/auth')
    }
  }

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <h1 className={styles.title}>관리자 페이지</h1>
        <button className={styles.logoutButton} onClick={handleLogout}>
          로그아웃
        </button>
      </div>

      <div className={styles.content}>
        <h2 className={styles.sectionTitle}>제보 관리</h2>
        
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

        {reports.length === 0 ? (
          <div className={styles.emptyState}>
            <p>처리할 제보가 없습니다.</p>
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
            {reports.map((report) => (
              <div
                key={report.id}
                className={`${styles.reportItem} ${selectedReports.has(report.id) ? styles.selected : ''}`}
                onClick={() => handleReportSelect(report)}
              >
                <div className={styles.reportCheckbox}>
                  <input
                    type="checkbox"
                    checked={selectedReports.has(report.id)}
                    onChange={() => handleSelectReport(report.id)}
                    onClick={(e) => e.stopPropagation()}
                    className={styles.checkbox}
                  />
                </div>
                <div className={styles.reportInfo}>
                  <h3 className={styles.reportStation}>{report.stationName}</h3>
                  <p className={styles.reportContent}>{report.content}</p>
                  {isAdmin() && report.authorName && (
                    <p className={styles.reportAuthor}>
                      작성자: {report.authorName}
                      {report.authorEmail && ` (${report.authorEmail})`}
                    </p>
                  )}
                  <div className={styles.reportMeta}>
                    <span className={styles.reportDate}>{report.date}</span>
                    <div className={styles.reportActions}>
                      <span
                        className={styles.reportStatus}
                        style={{ color: getStatusColor(report.status) }}
                      >
                        {getStatusText(report.status)}
                      </span>
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
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {selectedReport && (
        <div className={styles.modal} onClick={() => {
          setSelectedReport(null)
          setShowDetailMenu(false)
        }}>
          <div className={styles.modalContent} onClick={(e) => e.stopPropagation()}>
            <div className={styles.modalHeader}>
              <h3>제보 상세</h3>
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                <div className={styles.menuContainer}>
                  <button 
                    className={styles.menuButton}
                    onClick={() => setShowDetailMenu(!showDetailMenu)}
                  >
                    ⋯
                  </button>
                  {showDetailMenu && (
                    <>
                      <div 
                        className={styles.menuOverlay}
                        onClick={() => setShowDetailMenu(false)}
                      />
                      <div className={styles.menu}>
                        <button
                          className={`${styles.menuItem} ${styles.deleteMenuItem}`}
                          onClick={(e) => {
                            e.stopPropagation()
                            setShowDetailMenu(false)
                            handleDeleteReport(selectedReport.id)
                          }}
                        >
                          삭제
                        </button>
                      </div>
                    </>
                  )}
                </div>
                <button
                  className={styles.closeButton}
                  onClick={() => {
                    setSelectedReport(null)
                    setShowDetailMenu(false)
                  }}
                >
                  ✕
                </button>
              </div>
            </div>
            <div className={styles.modalBody}>
              <div className={styles.detailItem}>
                <label>작성자</label>
                <p>{selectedReport.authorName || selectedReport.userId}</p>
              </div>
              {selectedReport.authorEmail && (
                <div className={styles.detailItem}>
                  <label>작성자 이메일</label>
                  <p>{selectedReport.authorEmail}</p>
                </div>
              )}
              <div className={styles.detailItem}>
                <label>충전소명</label>
                <p>{selectedReport.stationName}</p>
              </div>
              <div className={styles.detailItem}>
                <label>제보내용</label>
                <p>{selectedReport.content}</p>
              </div>
              {selectedReport.photoUrl && (
                <div className={styles.detailItem}>
                  <label>첨부 사진</label>
                  <img src={selectedReport.photoUrl} alt="Report" className={styles.reportPhoto} />
                </div>
              )}
              <div className={styles.detailItem}>
                <label>제보일</label>
                <p>{selectedReport.date}</p>
              </div>
              {selectedReport.adminCheckedDate && (
                <div className={styles.detailItem}>
                  <label>관리자 확인 시간</label>
                  <p>{selectedReport.adminCheckedDate}</p>
                </div>
              )}
              {stationLocation && (
                <div className={styles.detailItem}>
                  <label>충전소 위치</label>
                  <div className={styles.mapContainer}>
                    <div className={styles.map}>
                      <div 
                        className={styles.mapMarker}
                        style={{
                          position: 'absolute',
                          top: '50%',
                          left: '50%',
                          transform: 'translate(-50%, -50%)',
                        }}
                      >
                        📍
                      </div>
                    </div>
                    <p className={styles.mapInfo}>
                      {selectedReport.stationName}
                    </p>
                  </div>
                </div>
              )}
              {selectedReport.adminResponse && (
                <div className={styles.detailItem}>
                  <label>관리자 답변</label>
                  <div className={styles.adminResponse}>
                    <p>{selectedReport.adminResponse}</p>
                    {selectedReport.adminResponseDate && (
                      <span className={styles.responseDate}>{selectedReport.adminResponseDate}</span>
                    )}
                  </div>
                </div>
              )}
              <div className={styles.statusSection}>
                <label>상태 변경</label>
                <div className={styles.statusButtons}>
                  <button
                    className={`${styles.statusButton} ${selectedReport.status === 'pending' ? styles.active : ''}`}
                    onClick={() => handleStatusChange(selectedReport.id, 'pending')}
                  >
                    대기
                  </button>
                  <button
                    className={`${styles.statusButton} ${selectedReport.status === 'processing' ? styles.active : ''}`}
                    onClick={() => handleStatusChange(selectedReport.id, 'processing')}
                  >
                    처리 중
                  </button>
                  <button
                    className={`${styles.statusButton} ${selectedReport.status === 'completed' ? styles.active : ''}`}
                    onClick={() => handleStatusChange(selectedReport.id, 'completed')}
                  >
                    완료
                  </button>
                  <button
                    className={`${styles.statusButton} ${selectedReport.status === 'rejected' ? styles.active : ''}`}
                    onClick={() => handleStatusChange(selectedReport.id, 'rejected')}
                  >
                    반려
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}

      {showResponseModal && selectedReport && (
        <div className={styles.modal} onClick={() => setShowResponseModal(false)}>
          <div className={styles.modalContent} onClick={(e) => e.stopPropagation()}>
            <div className={styles.modalHeader}>
              <h3>{responseStatus === 'completed' ? '승인 답변 작성' : '반려 답변 작성'}</h3>
              <button
                className={styles.closeButton}
                onClick={() => {
                  setShowResponseModal(false)
                  setResponseText('')
                  setResponseStatus(null)
                }}
              >
                ✕
              </button>
            </div>
            <div className={styles.modalBody}>
              <div className={styles.detailItem}>
                <label>충전소명</label>
                <p>{selectedReport.stationName}</p>
              </div>
              <div className={styles.responseSection}>
                <label>답변 내용</label>
                <textarea
                  className={styles.responseTextarea}
                  value={responseText}
                  onChange={(e) => setResponseText(e.target.value)}
                  placeholder={responseStatus === 'completed' ? '승인 사유를 입력해주세요.' : '반려 사유를 입력해주세요.'}
                  rows={6}
                  maxLength={500}
                />
                <div className={styles.charCount}>
                  {responseText.length} / 500
                </div>
              </div>
              <div className={styles.responseButtons}>
                <button
                  className={styles.cancelButton}
                  onClick={() => {
                    setShowResponseModal(false)
                    setResponseText('')
                    setResponseStatus(null)
                  }}
                >
                  취소
                </button>
                <button
                  className={styles.submitButton}
                  onClick={handleResponseSubmit}
                >
                  전송
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

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
                  : `현재 필터된 모든 제보(${deleteCount}개)를 삭제하시겠습니까?`}
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

