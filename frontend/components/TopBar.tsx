'use client'

import styles from './TopBar.module.css'

interface TopBarProps {
  selectedCategory: 'charging' | 'restroom' | null
  onCategoryChange: (category: 'charging' | 'restroom' | null) => void
}

export default function TopBar({ selectedCategory, onCategoryChange }: TopBarProps) {
  return (
    <div className={styles.topBar}>
      <button
        className={`${styles.categoryButton} ${selectedCategory === 'charging' ? styles.active : ''}`}
        onClick={() => onCategoryChange(selectedCategory === 'charging' ? null : 'charging')}
      >
        충전소
      </button>
      <button
        className={`${styles.categoryButton} ${selectedCategory === 'restroom' ? styles.active : ''}`}
        onClick={() => onCategoryChange(selectedCategory === 'restroom' ? null : 'restroom')}
      >
        화장실
      </button>
    </div>
  )
}


