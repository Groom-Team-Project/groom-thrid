'use client'

import { useState, useEffect } from 'react'
import { useRouter } from 'next/navigation'
import MapView from '@/components/MapView'
import TopBar from '@/components/TopBar'
import BottomNav from '@/components/BottomNav'
import styles from './page.module.css'

export default function Home() {
  const router = useRouter()
  const [selectedCategory, setSelectedCategory] = useState<'charging' | 'restroom' | null>(null)
  const [isLoggedIn, setIsLoggedIn] = useState(false)

  useEffect(() => {
    const loggedIn = localStorage.getItem('isLoggedIn')
    
    if (loggedIn === 'true') {
      setIsLoggedIn(true)
    }
    // 비로그인 사용자, 사용자, 보호자, 관리자 모두 지도와 충전소/화장실 위치는 볼 수 있음
  }, [router])

  return (
    <div className={styles.container}>
      <TopBar 
        selectedCategory={selectedCategory}
        onCategoryChange={setSelectedCategory}
      />
      <MapView selectedCategory={selectedCategory} />
      <BottomNav />
    </div>
  )
}


