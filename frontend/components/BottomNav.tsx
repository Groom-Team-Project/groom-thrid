'use client'

import { usePathname, useRouter } from 'next/navigation'
import { useEffect, useState } from 'react'
import styles from './BottomNav.module.css'

export default function BottomNav() {
  const pathname = usePathname()
  const router = useRouter()
  const [isLoggedIn, setIsLoggedIn] = useState(false)

  useEffect(() => {
    const loggedIn = localStorage.getItem('isLoggedIn')
    setIsLoggedIn(loggedIn === 'true')
  }, [])

  const navItems = [
    { id: 'home', icon: '🏠', label: '홈', path: '/', requiresLogin: false },
    { id: 'map', icon: '🗺️', label: '지도', path: '/', requiresLogin: false },
    { id: 'emergency', icon: '🚨', label: '긴급', path: '/emergency', requiresLogin: true },
    { id: 'reports', icon: '📝', label: '나의 제보', path: '/report/list', requiresLogin: true },
    { id: 'profile', icon: '👤', label: '마이페이지', path: '/profile', requiresLogin: true },
  ]

  const handleNavClick = (item: typeof navItems[0]) => {
    if (item.requiresLogin && !isLoggedIn) {
      router.push('/auth')
    } else {
      router.push(item.path)
    }
  }

  return (
    <div className={styles.bottomNav}>
      {navItems.map((item) => (
        <button
          key={item.id}
          className={`${styles.navItem} ${pathname === item.path ? styles.active : ''}`}
          onClick={() => handleNavClick(item)}
        >
          <span className={styles.icon}>{item.icon}</span>
        </button>
      ))}
    </div>
  )
}


