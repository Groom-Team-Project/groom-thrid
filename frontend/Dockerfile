FROM node:20

WORKDIR /app

# package.json과 package-lock.json 복사
COPY package*.json ./

# 의존성 설치
RUN npm ci

# 나머지 파일 복사
COPY . .

# Next.js 빌드 (선택사항이지만 권장)
# RUN npm run build

EXPOSE 3000

# 개발 모드로 실행 (0.0.0.0으로 바인딩하여 외부 접근 허용)
CMD ["npm", "run", "dev", "--", "-H", "0.0.0.0", "-p", "3000"]

