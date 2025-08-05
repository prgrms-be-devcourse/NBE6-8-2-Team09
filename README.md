# 가상화폐 모의 투자 시스템
<br>

[![Back9-Home-orange.png](https://i.postimg.cc/vTbwcMt9/Back9-Home-orange.png)](https://postimg.cc/Pp3RRgcr)

[`가상회폐 모의투자 시스템 링크`](https://peuronteuendeu.onrender.com/)

<br>

## 개요
1. [소개](#소개)   
2. [기술스택](#기술스택)   
3. [빌드 및 사용법](#빌드-및-사용법)   
4. [문의](#문의)   

<br>

## 소개
- 데브코스 6기 8회차 9팀의 2차 프로젝트로 사용된 레포지토리 입니다.<br>

- 실제 자금 없이 안전하게 가상화폐 투자를 체험할 수 있는 모의투자 플랫폼입니다. 회원별 지갑이 자동 생성되며, 비트코인, 이더리움, 도지코인 등 주요 코인의 실시간 시세를 기반으로 거래할 수 있는 서비스를 제공합니다.

<br>

## 기술스택
### 백엔드
- [`Spring boot`](https://spring.io/)
- [`JWT`]
- [`OAuth2.0`]
- [`Spring Security`]
- [`Spring Data JPA`]
- [`REST API`]
- [`PostgreSQL(supabase 사용)`](https://supabase.com/)

### 프론트 엔드
- [`TypeScrpt`]
- [`Next.js`](https://nextjs.org/)
- [`Tailwind CSS`](https://tailwindcss.com/)

### 배포 
- [`Render`](https://render.com/)

<br>

## 빌드 및 사용법
### 서비스 접속
- 실제 서비스는 아래 URL에서 접속 가능합니다.<br>
  [https://peuronteuendeu.onrender.com](https://peuronteuendeu.onrender.com)

### 로컬 빌드 및 실행 
1. 깃허브 주소를 프로젝트 폴더로 다운 받는다
<br>ex) ``
git clone project-url .``

2. ngrok을 사용해 로컬을 외부와 연결
3. 받은 프로젝트를 인텔리제이로 열어 gradle 빌드 실행
4. docker 실행 후 src/resources/docker-compose.yml 실행  
5. 프론트엔드 서버를 vercel을 통해 열어 접속한다

#### 주의사항
- 백엔드 설정파일에 환경변수가 들어가야 함  
  `backend/.env`

```env
JWT_SECRET_KEY=your-jwt-secret-key
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret
REDIS_HOST=localhost
REDIS_PORT=6379
```

- 프론트 엔드 설정파일에 백엔드 서버의 도메인이 들어가 있어야 함<br>
  'frontend/.env'

```env
NEXT_PUBLIC_API_URL=http://localhost:8080
```

## 문의
- 이정무(거래소 정보 API 연동, 팀장)
<br>email : nonehvt@gmail.com
- 두효정(코인 거래 내역 관리)
<br>email : doohj1218@naver.com
- 이상림(코인 종류 관리)
<br>email : sanglim7698@gmail.com
- 이찬수(가상 지갑 관리)
<br>email : sanglim7698@gmail.com
- 최세환(사용자 관리)
<br>email : tpghks312@gmail.com





 