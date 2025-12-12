// 긴급 알림 데이터 타입
export interface EmergencyAlert {
  id: string
  userId: string
  userName: string
  guardianEmail: string
  lat: number
  lng: number
  date: string
  time: string
  read: boolean
}

// 긴급 알림 데이터 저장 및 관리
export const getAlerts = (): EmergencyAlert[] => {
  if (typeof window === 'undefined') return []
  
  const alertsJson = localStorage.getItem('emergencyAlerts')
  if (!alertsJson) return []
  
  try {
    return JSON.parse(alertsJson)
  } catch {
    return []
  }
}

export const saveAlert = (alert: Omit<EmergencyAlert, 'id' | 'date' | 'time' | 'read'>): EmergencyAlert => {
  const alerts = getAlerts()
  const now = new Date()
  const newAlert: EmergencyAlert = {
    ...alert,
    id: Date.now().toString(),
    date: now.toISOString().split('T')[0].replace(/-/g, '.'),
    time: now.toTimeString().split(' ')[0].substring(0, 5),
    read: false,
  }
  
  alerts.push(newAlert)
  localStorage.setItem('emergencyAlerts', JSON.stringify(alerts))
  return newAlert
}

export const getAlertsByGuardianEmail = (guardianEmail: string): EmergencyAlert[] => {
  const alerts = getAlerts()
  return alerts
    .filter(a => a.guardianEmail === guardianEmail)
    .sort((a, b) => {
      // 날짜와 시간 내림차순 정렬
      const dateA = new Date(`${a.date.replace(/\./g, '-')} ${a.time}`)
      const dateB = new Date(`${b.date.replace(/\./g, '-')} ${b.time}`)
      return dateB.getTime() - dateA.getTime()
    })
}

export const markAlertAsRead = (alertId: string): boolean => {
  const alerts = getAlerts()
  const index = alerts.findIndex(a => a.id === alertId)
  
  if (index === -1) return false
  
  alerts[index].read = true
  localStorage.setItem('emergencyAlerts', JSON.stringify(alerts))
  return true
}

export const deleteAlert = (alertId: string): boolean => {
  const alerts = getAlerts()
  const filtered = alerts.filter(a => a.id !== alertId)
  localStorage.setItem('emergencyAlerts', JSON.stringify(filtered))
  return alerts.length !== filtered.length
}

export const getUnreadAlertCount = (guardianEmail: string): number => {
  const alerts = getAlertsByGuardianEmail(guardianEmail)
  return alerts.filter(a => !a.read).length
}

