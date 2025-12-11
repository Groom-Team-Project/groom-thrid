import { API_BASE_URL, getAccessToken } from './api'

// SSE로 받을 위치 데이터 타입
export interface LocationData {
  lat: number
  lng: number
  timestamp: string
}

// SSE 이벤트 핸들러 타입
export interface SseEventHandlers {
  onLocation?: (data: LocationData) => void
  onError?: (error: Error) => void
  onOpen?: () => void
}

// SSE 연결 관리를 위한 클래스
export class SseConnection {
  private abortController: AbortController | null = null
  private handlers: SseEventHandlers

  constructor(handlers: SseEventHandlers) {
    this.handlers = handlers
  }

  // SSE 연결 시작
  async connect(): Promise<void> {
    const token = getAccessToken()

    if (!token) {
      const error = new Error('토큰이 없습니다. 로그인이 필요합니다.')
      console.error(error.message)
      this.handlers.onError?.(error)
      return
    }

    this.abortController = new AbortController()

    try {
      const response = await fetch(`${API_BASE_URL}/sse/connect`, {
        method: 'GET',
        headers: {
          Authorization: `Bearer ${token}`,
          Accept: 'text/event-stream',
        },
        signal: this.abortController.signal,
      })

      if (!response.ok) {
        throw new Error(`SSE 연결 실패: ${response.status}`)
      }

      console.log('SSE 연결 성공')
      this.handlers.onOpen?.()

      // 스트림 읽기
      const reader = response.body?.getReader()
      const decoder = new TextDecoder()

      if (!reader) {
        throw new Error('스트림을 읽을 수 없습니다.')
      }

      while (true) {
        const { done, value } = await reader.read()

        if (done) {
          console.log('SSE 연결 종료')
          break
        }

        // 청크 데이터 디코딩
        const chunk = decoder.decode(value, { stream: true })
        const lines = chunk.split('\n')

        for (const line of lines) {
          // SSE 형식: "event: location\ndata: {...}\n\n"
          if (line.startsWith('data:')) {
            const data = line.substring(5).trim()

            // "connect" 이벤트는 무시 (초기 연결 확인용)
            if (data === 'SSE 연결이 성공적으로 생성되었습니다') {
              continue
            }

            try {
              const locationData = JSON.parse(data) as LocationData
              this.handlers.onLocation?.(locationData)
            } catch (error) {
              console.error('SSE 메시지 파싱 실패:', error, 'data:', data)
            }
          }
        }
      }
    } catch (error) {
      if (error instanceof Error) {
        if (error.name === 'AbortError') {
          console.log('SSE 연결이 중단되었습니다.')
        } else {
          console.error('SSE 연결 에러:', error)
          this.handlers.onError?.(error)
        }
      }
    }
  }

  // SSE 연결 종료
  disconnect(): void {
    if (this.abortController) {
      this.abortController.abort()
      this.abortController = null
      console.log('SSE 연결 종료')
    }
  }
}

// SSE 연결 함수 (편의성을 위한 래퍼)
export const connectSSE = (handlers: SseEventHandlers): SseConnection => {
  const connection = new SseConnection(handlers)
  connection.connect()
  return connection
}

// SSE 연결 종료 함수
export const disconnectSSE = (connection: SseConnection | null) => {
  if (connection) {
    connection.disconnect()
  }
}
