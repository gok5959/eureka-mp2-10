<div align="center">
  <h1>📅 그룹 일정 관리 API 서버</h1>
  <p><b>LG U+ 유레카 3기 미니 프로젝트</b><br>
  사용자가 그룹을 생성하고, 그룹 내에서 일정을 조율·확정할 수 있는 일정 관리 백엔드 서비스입니다.</p>
</div>

<br>

## ✨ 주요 기능 (Key Features)

* **🔐 강력한 보안 및 인증**
    * 액세스 토큰(Access Token)과 리프레시 토큰(Refresh Token)의 이중 구조
    * SHA-256 해시 기반의 토큰 위변조 검사로 보안성 강화
* **👥 체계적인 사용자 및 그룹 관리**
    * 회원가입 및 사용자 검색
    * 그룹 생성·수정·삭제 및 멤버 초대·제거 기능 지원
* **🗓️ 유연한 일정 관리 및 투표 시스템**
    * 그룹 일정 및 개인 전용 일정 CRUD 지원
    * **투표 기반 일정 확정 플로우**: 최소 참여 인원(minParticipants) 및 데드라인 기반의 자동 상태 변경(확정/취소)
* **📊 실시간 참여 현황 제공**
    * 일정별 참여 투표 (수락/거절) 및 요약/상세 현황 조회
* **💬 원활한 소통 및 파일 공유**
    * 일정별 댓글 작성 및 관리
    * Google Cloud Storage(GCP) 기반의 안전한 첨부파일 업로드 지원

---

## 🛠 기술 스택 (Tech Stack)

| Category | Technology |
| :--- | :--- |
| **Language** | Java 17 |
| **Framework** | Spring Boot 3.5.8, Spring Security, Spring Data JPA |
| **Database** | MySQL 8.0+, Hibernate |
| **Authentication** | JWT (jjwt 0.12.5), BCrypt |
| **Storage** | Google Cloud Storage (GCP) |
| **API Docs** | SpringDoc OpenAPI (Swagger UI) |
| **Build Tool** | Gradle 8.x |

---

## 📂 프로젝트 구조 (Project Directory)

```text
src/main/java/com/mycom/myapp/
├── common/
│   ├── config/
│   │   ├── security/          # Spring Security 및 JWT 필터
│   │   └── storage/           # GCP Storage 설정 및 연동 서비스
│   └── dto/
│       └── PageResponse.java  # 공통 페이징 응답 객체
└── domain/
    ├── auth/                  # 로그인, 토큰 발급/갱신, 로그아웃
    ├── user/                  # 회원가입, 사용자 조회 및 검색
    ├── group/                 # 그룹 CRUD, 멤버 관리
    ├── schedule/              # 그룹 및 개인 일정 CRUD
    ├── participation/         # 일정 참여 투표 로직 및 현황 조회
    └── schedule_extras/       # 댓글 및 첨부파일 관리
```

---

## 🔄 일정 확정 플로우 (Schedule Flow)

그룹 일정은 참여자들의 투표를 통해 체계적으로 확정되거나 취소됩니다.

```text
일정 생성
   │
   ├─ [투표 없음] ──▶ 🟢 즉시 확정 (CONFIRMED)
   │
   └─ [투표 있음] ──▶ 🟡 투표 진행 중 (VOTING)
                           │
                           ▼ (투표 데드라인 도달)
                     [ACCEPTED 참여자 집계]
                           │
                 (최소 참가자 수와 비교)
                 ┌─────────┴──────────┐
                 ▼                    ▼
               미달                  충족
                 │                    │
                 ▼                    ▼
       🔴 취소 (CANCELED)     🟢 확정 (CONFIRMED)
```

### 📌 상태 코드 안내

**1. 일정 상태 (`ScheduleStatus`)**
* `VOTING`: 참여 투표 진행 중
* `CONFIRMED`: 일정 확정
* `CANCELED`: 최소 참가자 미달로 인한 취소

**2. 참여 상태 (`ParticipationStatus`)**
* `INVITED`: 초대됨 (초기 상태)
* `ACCEPTED`: 참여 수락
* `DECLINED`: 참여 거절

---

## 🗄 주요 데이터 모델 (Data Model)

```text
users ─────────────┐
  id               │
  email (UNIQUE)   │ 1:N
  password_hash    │
  name, role       │
                   ▼
user_groups ─── group_members
  id                 group_id (FK)
  name               user_id  (FK)
  owner_id (FK)      role
                   │
                   ▼
schedule ──────────────────────────────┐
  id                                   │
  title, description                   │
  owner_id / group_id (FK)             │
  start_at, end_at                     │
  status (VOTING/CONFIRMED/CANCELED)   │
  vote_deadline_at, min_participants   │
                   │                   │
        ┌──────────┼──────────┐        │
        ▼          ▼          ▼        │
  participation  comment  attachment   │
  user_id (FK)                         │
  status                               │
  UK(schedule_id, user_id)             │
```

---

## 📡 API 엔드포인트 (API Endpoints)

> 💡 **Swagger UI**: 프로젝트 실행 후 `http://localhost:8080/swagger-ui/index.html`에서 API 명세서 및 테스트 환경을 확인할 수 있습니다.

### 🔐 인증 & 사용자 (Auth & User)
| Method | Endpoint | Description |
| :---: | :--- | :--- |
| `POST` | `/auth/login` | 로그인 |
| `POST` | `/auth/refresh` | 액세스 토큰 갱신 |
| `POST` | `/auth/logout` | 로그아웃 |
| `POST` | `/users` | 회원가입 |
| `GET` | `/users/{id}` | 사용자 상세 조회 |
| `GET` | `/users?keyword=&role=` | 사용자 검색 (페이징) |

### 👥 그룹 & 멤버 (Group)
| Method | Endpoint | Description |
| :---: | :--- | :--- |
| `POST` | `/groups` | 그룹 생성 |
| `GET` | `/groups` | 그룹 목록 조회 |
| `GET` | `/groups/{groupId}` | 그룹 상세 조회 (멤버 포함) |
| `PUT` | `/groups/{groupId}` | 그룹 수정 (소유자 전용) |
| `DELETE` | `/groups/{groupId}` | 그룹 삭제 (소유자 전용) |
| `GET` | `/groups/users` | 내가 속한 그룹 목록 |
| `POST` | `/groups/{groupId}/members` | 그룹 멤버 추가 |
| `DELETE` | `/groups/{groupId}/members/{userId}` | 그룹 멤버 제거 |

### 🗓️ 일정 관리 (Schedule)
| Method | Endpoint | Description |
| :---: | :--- | :--- |
| `POST` | `/groups/{groupId}/schedules` | 그룹 일정 생성 |
| `GET` | `/groups/{groupId}/schedules` | 그룹 일정 목록 |
| `GET` | `/group-schedules/{scheduleId}` | 그룹 일정 상세 조회 |
| `PUT` | `/group-schedules/{scheduleId}` | 그룹 일정 수정 |
| `DELETE` | `/group-schedules/{scheduleId}` | 그룹 일정 취소 |
| `POST` | `/personal-schedules` | 개인 일정 생성 |
| `GET` | `/personal-schedules` | 개인 일정 목록 |
| `GET` | `/personal-schedules/{scheduleId}` | 개인 일정 상세 조회 |
| `PUT` | `/personal-schedules/{scheduleId}` | 개인 일정 수정 |
| `DELETE` | `/personal-schedules/{scheduleId}` | 개인 일정 삭제 |

### 📊 참여 현황 & 부가 기능 (Extras)
| Method | Endpoint | Description |
| :---: | :--- | :--- |
| `POST` | `/group-schedules/{scheduleId}/participations` | 참여 투표 (수락/거절) |
| `GET` | `/group-schedules/{scheduleId}/participations/me` | 내 참여 상태 조회 |
| `GET` | `/group-schedules/{scheduleId}/participations` | 참여 현황 요약 |
| `GET` | `/group-schedules/{scheduleId}/participations/list`| 참여자 상세 목록 |
| `POST` | `/schedules/{scheduleId}/comments` | 댓글 작성 |
| `GET` | `/schedules/{scheduleId}/comments` | 댓글 목록 조회 |
| `PATCH` | `/schedules/{scheduleId}/comments/{commentId}`| 댓글 수정 (작성자) |
| `DELETE` | `/schedules/{scheduleId}/comments/{commentId}`| 댓글 삭제 (작성자) |
| `POST` | `/schedules/{scheduleId}/attachments` | 파일 업로드 (GCP) |
| `GET` | `/schedules/{scheduleId}/attachments` | 파일 목록 조회 |
| `DELETE` | `/schedules/{scheduleId}/attachments/{attachmentId}`| 파일 삭제 (작성자) |

---

## 🔐 보안 및 인증 정책 (Security Policy)

* **인증 흐름**: 이메일/비밀번호 로그인 → Access Token (20시간) + Refresh Token (14일) 발급
* **토큰 전달 방식**: 보안 강화를 위해 `HttpOnly`, `Secure` 옵션이 적용된 쿠키 사용
* **안전한 토큰 갱신**: Refresh Token으로 Access Token 재발급 시 Refresh Token도 함께 교체 (RTR 방식)
* **위변조 방지**: 발급된 Refresh Token은 SHA-256으로 해싱하여 DB에 저장 및 검증
* **인증 예외 경로**: `/auth/**`, `POST /users`, Swagger UI (`/swagger-ui/**`, `/v3/api-docs/**`)

---

## ⚙️ 환경 변수 설정 (Environment Variables)

프로젝트를 실행하기 전, `application.yml` 또는 시스템 환경 변수에 다음 값을 설정해야 합니다.

```properties
# 🗄️ Database (MySQL)
DB_HOST=localhost
DB_PORT=3306
DB_DATABASE=eureka_db
DB_USER=root
DB_PASSWORD=your_password

# 🔐 JWT Security (256-bit 이상 권장)
JWT_SECRET_KEY=your-secret-key-min-32-characters

# ☁️ Google Cloud Storage (GCP)
GCP_PROJECT_ID=your-gcp-project-id
GCP_BUCKET_NAME=your-bucket-name
GOOGLE_CLOUD_CREDENTIALS_PATH=/path/to/credentials.json
```
