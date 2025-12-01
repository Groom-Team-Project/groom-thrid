# WheelFinder - 충전소 찾기 앱

피그마 디자인을 기반으로 구현한 Next.js 기반 전기차 충전소 찾기 웹 애플리케이션입니다.

## 주요 기능

1. **메인 화면**
   - 지도 표시
   - 충전소/화장실 카테고리 선택 버튼
   - 하단 네비게이션 바

2. **충전소 목록**
   - 지도에 충전소 마커 표시
   - 충전소 클릭 시 상세 정보 패널 표시

3. **충전소 상세 정보**
   - 충전소 이름, 주소, 운영시간 등 정보 표시
   - 길찾기 버튼

4. **길찾기**
   - 시작점/종료점 표시
   - 예상 시간 및 거리 표시
   - 경로 시각화

## 기술 스택

- **Framework**: Next.js 14 (App Router)
- **Language**: TypeScript
- **Styling**: CSS Modules
- **Map**: 네이버 지도 API (구현 필요)

## 시작하기

### 설치

```bash
npm install
```

### 개발 서버 실행

```bash
npm run dev
```

브라우저에서 [http://localhost:3000](http://localhost:3000)을 열어 확인하세요.

### 빌드

```bash
npm run build
npm start
```

## 환경 설정

네이버 지도 API를 사용하려면 `.env.local` 파일을 생성하고 다음을 추가하세요:

```
NEXT_PUBLIC_NAVER_MAP_CLIENT_ID=your_client_id
```

그리고 `components/MapView.tsx`에서 API 키를 설정하세요.

## 프로젝트 구조

```
wheelFinder/
├── app/
│   ├── layout.tsx          # 루트 레이아웃
│   ├── page.tsx            # 메인 페이지
│   ├── globals.css         # 전역 스타일
│   └── directions/
│       └── page.tsx        # 길찾기 페이지
├── components/
│   ├── TopBar.tsx          # 상단 카테고리 바
│   ├── BottomNav.tsx       # 하단 네비게이션
│   └── MapView.tsx         # 지도 뷰 컴포넌트
└── package.json
```

## 향후 개선 사항

- 실제 네이버 지도 API 연동
- 실제 충전소 데이터 API 연동
- 경로 계산 및 시각화
- 사용자 위치 추적
- 리뷰 기능
- 제보 기능


