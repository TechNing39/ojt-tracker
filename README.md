# ojt-tracker

영화관 아르바이트 OJT 체크리스트/진행상황 공유 모바일 웹

## 배경
OJT(온보딩)를 진행하는 트레이너와 받는 신입이 다수라 정보 공유가 비효율적인 문제를 해결하기 위한 서비스. 로그인 없이, 신입 이름만으로 구분해서 사용한다.

## 배포
- 프론트: https://ojt-tracker-m8h9.vercel.app
- 백엔드: https://ojt-tracker-sjaz.onrender.com
- API 문서(Swagger): https://ojt-tracker-sjaz.onrender.com/swagger-ui/index.html

## 기능
- 신입 등록/삭제, 특이사항 메모
- 체크리스트 항목 관리 (플로어/매점/매표/마감/투썸 카테고리 구분, 추가/삭제)
- 신입별 진행상황 체크
- 초기 데이터 로딩 상태 표시 (백엔드 cold start 시 빈 화면처럼 보이는 문제 방지)

## 기술 스택
- Backend: Java 17, Spring Boot, Spring Data JPA
- Frontend: React, TypeScript, Vite
- DB: PostgreSQL (Neon, 배포), H2 (로컬 개발)
- 배포: Render(백엔드) + Vercel(프론트)

## 로컬 실행

### 백엔드
```
cd backend
./gradlew bootRun
```
기본 설정으로 `localhost:8080`에서 뜨고, 로컬 H2 DB(`backend/data/`)를 사용한다.

### 프론트
```
cd frontend
npm install
npm run dev
```
`localhost:5173`에서 뜨고, 기본적으로 `localhost:8080`의 백엔드를 바라본다. 다른 백엔드 주소를 쓰려면 `.env.example`을 참고해 `.env`에 `VITE_API_BASE_URL`을 설정한다.

## 워크플로우
이슈 생성 → `feat/{설명}` 브랜치 → 커밋(`Closes #이슈번호`) → PR → main 머지
