'use client'

import { useEffect } from 'react'
import { useRouter } from 'next/navigation'

export default function LoginPage() {
  const router = useRouter()

  useEffect(() => {
    // auth 페이지로 리다이렉트
    router.replace('/auth')
  }, [router])

  return null
}

