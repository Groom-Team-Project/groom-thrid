// 보호자 연동 요청 데이터 타입
export interface GuardianRequest {
  id: string
  userId: string
  userName: string
  userEmail: string
  guardianEmail: string
  status: 'pending' | 'approved' | 'rejected'
  requestDate: string
  responseDate?: string
}

// 보호자 연동 요청 저장 및 관리
export const getGuardianRequests = (): GuardianRequest[] => {
  if (typeof window === 'undefined') return []
  
  const requestsJson = localStorage.getItem('guardianRequests')
  if (!requestsJson) return []
  
  try {
    return JSON.parse(requestsJson)
  } catch {
    return []
  }
}

export const saveGuardianRequest = (request: Omit<GuardianRequest, 'id' | 'requestDate' | 'status'>): GuardianRequest => {
  const requests = getGuardianRequests()
  
  // 이미 같은 사용자-보호자 조합의 대기 중인 요청이 있는지 확인
  const existingRequest = requests.find(
    r => r.userId === request.userId && 
         r.guardianEmail === request.guardianEmail && 
         r.status === 'pending'
  )
  
  if (existingRequest) {
    return existingRequest
  }
  
  const newRequest: GuardianRequest = {
    ...request,
    id: Date.now().toString(),
    requestDate: new Date().toISOString().split('T')[0].replace(/-/g, '.'),
    status: 'pending',
  }
  
  requests.push(newRequest)
  localStorage.setItem('guardianRequests', JSON.stringify(requests))
  return newRequest
}

export const getGuardianRequestsByGuardianEmail = (guardianEmail: string): GuardianRequest[] => {
  const requests = getGuardianRequests()
  return requests
    .filter(r => r.guardianEmail === guardianEmail)
    .sort((a, b) => {
      return new Date(b.requestDate.replace(/\./g, '-')).getTime() - new Date(a.requestDate.replace(/\./g, '-')).getTime()
    })
}

export const getGuardianRequestByUserId = (userId: string): GuardianRequest | undefined => {
  const requests = getGuardianRequests()
  return requests.find(r => r.userId === userId && r.status === 'approved')
}

export const updateGuardianRequest = (requestId: string, status: 'approved' | 'rejected'): GuardianRequest | null => {
  const requests = getGuardianRequests()
  const index = requests.findIndex(r => r.id === requestId)
  
  if (index === -1) return null
  
  requests[index] = {
    ...requests[index],
    status,
    responseDate: new Date().toISOString().split('T')[0].replace(/-/g, '.'),
  }
  
  localStorage.setItem('guardianRequests', JSON.stringify(requests))
  
  // 승인된 경우 사용자의 guardianEmail도 업데이트
  if (status === 'approved') {
    const request = requests[index]
    localStorage.setItem('guardianEmail', request.guardianEmail)
  }
  
  return requests[index]
}

export const deleteGuardianRequest = (requestId: string): boolean => {
  const requests = getGuardianRequests()
  const filtered = requests.filter(r => r.id !== requestId)
  localStorage.setItem('guardianRequests', JSON.stringify(filtered))
  return requests.length !== filtered.length
}

