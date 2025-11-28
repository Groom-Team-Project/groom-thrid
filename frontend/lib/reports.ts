// 제보 데이터 타입
export interface Report {
  id: string
  stationName: string
  content: string
  photoUrl?: string
  date: string
  status: 'pending' | 'processing' | 'completed' | 'rejected'
  userId: string
  adminResponse?: string
  adminResponseDate?: string
  adminCheckedDate?: string
}

// 제보 데이터 저장 및 관리
export const getReports = (): Report[] => {
  if (typeof window === 'undefined') return []
  
  const reportsJson = localStorage.getItem('reports')
  if (!reportsJson) return []
  
  try {
    return JSON.parse(reportsJson)
  } catch {
    return []
  }
}

export const saveReport = (report: Omit<Report, 'id' | 'date' | 'status'>): Report => {
  const reports = getReports()
  const newReport: Report = {
    ...report,
    id: Date.now().toString(),
    date: new Date().toISOString().split('T')[0].replace(/-/g, '.'),
    status: 'pending',
  }
  
  reports.push(newReport)
  localStorage.setItem('reports', JSON.stringify(reports))
  return newReport
}

export const getReportById = (id: string): Report | undefined => {
  const reports = getReports()
  return reports.find(r => r.id === id)
}

export const getUserReports = (userId: string): Report[] => {
  const reports = getReports()
  return reports.filter(r => r.userId === userId).sort((a, b) => {
    // 날짜 내림차순 정렬
    return new Date(b.date.replace(/\./g, '-')).getTime() - new Date(a.date.replace(/\./g, '-')).getTime()
  })
}

export const deleteReport = (id: string): boolean => {
  const reports = getReports()
  const filtered = reports.filter(r => r.id !== id)
  localStorage.setItem('reports', JSON.stringify(filtered))
  return reports.length !== filtered.length
}

export const updateReport = (reportId: string, updates: Partial<Report>): Report | null => {
  const reports = getReports()
  const index = reports.findIndex(r => r.id === reportId)
  
  if (index === -1) return null
  
  reports[index] = { ...reports[index], ...updates }
  localStorage.setItem('reports', JSON.stringify(reports))
  return reports[index]
}

