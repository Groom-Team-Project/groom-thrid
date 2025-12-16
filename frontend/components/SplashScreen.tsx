import styles from './SplashScreen.module.css'

export default function SplashScreen() {
    return (
        <div className={styles.container}>
            <div className={styles.content}>
                {/* 로고 컨테이너 */}
                <div className={styles.logoContainer}>
                    {/* 로고 - 위치 핀 모양 */}
                    <div className={styles.logo}>
                        {/* 번개 아이콘 */}
                        <div className={styles.lightning}>⚡</div>
                    </div>
                </div>

                {/* 타이틀 - 한글 */}
                <h1 className={styles.titleKo}>휠 파인더</h1>

                {/* 타이틀 - 영문 */}
                <h2 className={styles.titleEn}>Wheel Finder</h2>

                {/* 설명 */}
                <p className={styles.description}>
                    휠 파인더는 실질적 최신 전동보장구 충전소 정보와<br/>
                    맞춤형 경로 제공으로 사회적 가치를 실현하는<br/>
                    플랫폼입니다.
                </p>

                {/* 로딩 점 3개 */}
                <div className={styles.loadingDots}>
                    <div className={styles.dot} />
                    <div className={styles.dot} />
                    <div className={styles.dot} />
                </div>
            </div>
        </div>
    )
}