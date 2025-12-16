'use client'

import styles from './TopBar.module.css'

interface FacilityTypeItem {
    name: string
    label: string
}

interface TopBarProps {
  selectedCategory: string | null
  facilityTypes?: FacilityTypeItem[]
  onCategoryChange: (category: string | null) => void
}

export default function TopBar({ selectedCategory, facilityTypes = [], onCategoryChange }: TopBarProps) {
  return (
    <div className={styles.topBar}>
      <button
        className={`${styles.categoryButton} ${selectedCategory === 'charging' ? styles.active : ''}`}
        onClick={() => onCategoryChange(selectedCategory === 'charging' ? null : 'charging')}
      >
        충전소
      </button>
      {/* 서버에서 내려온 활성화된 FacilityType 목록 렌더링 */}
      {facilityTypes.map((ft) => (
        <button
          key={ft.name}
          className={`${styles.categoryButton} ${selectedCategory === ft.name ? styles.active : ''}`}
          onClick={() => onCategoryChange(selectedCategory === ft.name ? null : ft.name)}
        >
          {ft.label}
        </button>
      ))}
    </div>
  )
}


