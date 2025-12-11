import { apiRequest } from './api'

// 백엔드 API 호출: 보호자 연결
export const matchGuardian = async (email: string): Promise<void> => {
  await apiRequest('/user/guardian', {
    method: 'POST',
    body: JSON.stringify({ email }),
  })
}

