import type { Metadata } from 'next'
import './globals.css'

export const metadata: Metadata = {
  title: 'WheelFinder - 충전소 찾기',
  description: '전기차 충전소를 쉽게 찾아보세요',
}

export default function RootLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <html lang="ko">
      <body>{children}</body>
    </html>
  )
}


