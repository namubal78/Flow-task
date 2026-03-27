# Flow Task — 파일 확장자 차단

악성 파일 업로드를 차단하기 위한 **파일 확장자 차단 목록 관리** 웹 애플리케이션을 구현했습니다.

**배포 URL: http://3.38.100.49:8080**

---

## 기술 스택

| 구분 | 기술 |
|------|------|
| Language | Java 17 |
| Framework | Spring Boot 3.4.4 (Gradle) |
| ORM | Spring Data JPA + Hibernate |
| DB | PostgreSQL 15 |
| View | Thymeleaf + jQuery 3.7.1 |
| Excel | Apache POI 5.3.0 |
| Infra | AWS EC2 t3.micro (Amazon Linux 2023) |
| Dev | spring-boot-devtools, Lombok |
| AI tools | Claude Code CLI |

---

## 기능 목록

### 고정 확장자

| # | 기능 | 설명 |
|---|------|------|
| 1 | 고정 확장자 목록 조회 | bat, cmd, com, cpl, exe, scr, js — Enum으로 코드 관리 |
| 2 | 고정 확장자 체크/언체크 | 체크 = DB INSERT, 언체크 = DB DELETE (상태 컬럼 없이 행 존재 여부로 표현) |
| 3 | 고정/커스텀 교차 중복 방지 | 고정 확장자는 커스텀으로 추가 불가, 커스텀 확장자는 고정 체크 불가 |

### 커스텀 확장자

| # | 기능 | 설명 |
|---|------|------|
| 4 | 실시간 중복 체크 | 입력 중 300ms debounce, 고정·커스텀 목록 동시 조회 |
| 5 | 엑셀 양식 다운로드 | `blocked_extension_list.xlsx` 단일 컬럼 양식 제공 |
| 6 | 엑셀 업로드 일괄 추가 | 중복 제외 후 실제 추가 수 기준 200개 초과 시 전체 실패, 중복 목록 알럿 포함 |
| 7 | 커스텀 확장자 단건 추가 | 영문·숫자 only, 최대 20자, 최대 200개 제한 |
| 8 | 확장자 검색 | 목록 내 스크롤 + 하이라이트, 실행 후 인풋 초기화 |
| 9 | 정렬 | 최신순(기본) / 알파벳 오름차순 / 알파벳 내림차순 |
| 10 | 전체 삭제 | 커스텀 확장자 전체 삭제 (확인 다이얼로그) |
| 11 | 개별 삭제 | 태그 클릭으로 개별 삭제 (확인 다이얼로그) |
| 12 | 현재 등록 수 표시 | `N / 200` 실시간 표시 |

### 차단 테스트

| # | 기능 | 설명 |
|---|------|------|
| 13 | 파일 업로드 | 드래그앤드롭 또는 파일 첨부 버튼으로 다중 파일 업로드 (최대 5MB) |
| 14 | 차단 확장자 검사 | 업로드 시 blocked_sub 전체 조회 (체크된 고정 + 커스텀) 기준으로 차단 |
| 15 | 업로드 결과 알럿 | 차단된 확장자 목록 표시, 전체 차단/일부 차단/전체 성공 메시지 분기 |
| 16 | 파일 목록 관리 | 업로드된 파일 태그 표시, 개별 삭제 / 전체 삭제 (서버 파일도 함께 삭제) |

---

## DB 설계

### blocked_system

| 컬럼 | 타입 | 설명 |
|------|------|------|
| id | BIGINT PK | 시스템 ID |
| comment | VARCHAR | 설명 |
| insert_date | TIMESTAMP | 생성일시 |
| update_date | TIMESTAMP | 수정일시 |
| insert_id | VARCHAR | 생성자 |
| update_id | VARCHAR | 수정자 |

### blocked_sub

| 컬럼 | 타입 | 설명 |
|------|------|------|
| id | BIGINT PK | PK |
| system_id | BIGINT FK | blocked_system 참조 |
| blocked_extension | VARCHAR(20) | 확장자 (소문자 정규화) |
| blocked_type | VARCHAR | FIXED / CUSTOM |
| insert_date | TIMESTAMP | 생성일시 |
| insert_id | VARCHAR | 생성자 (과제 한정 admin 고정) |

### uploaded_file

| 컬럼 | 타입 | 설명 |
|------|------|------|
| id | BIGINT PK | PK |
| system_id | BIGINT FK | blocked_system 참조 |
| original_name | VARCHAR(255) | 원본 파일명 |
| stored_name | VARCHAR(255) | 서버 저장 파일명 (UUID) |
| file_size | BIGINT | 파일 크기 (bytes) |
| extension | VARCHAR(20) | 확장자 (소문자) |
| insert_date | TIMESTAMP | 업로드 일시 |
| insert_id | VARCHAR | 생성자 (admin 고정) |

> 애플리케이션 기동 시 `blocked_system(id=1)` 레코드가 없으면 자동 생성됩니다 (`DataInitializer`).

---

## API

### 확장자 차단

| Method | URL | 설명 |
|--------|-----|------|
| GET | `/api/extensions?sort=` | 화면 초기 데이터 (고정+커스텀) |
| PATCH | `/api/extensions/fixed/{ext}?checked=` | 고정 확장자 체크 토글 |
| GET | `/api/extensions/check?ext=` | 실시간 중복 체크 |
| POST | `/api/extensions/custom` | 커스텀 확장자 단건 추가 |
| GET | `/api/extensions/custom?sort=` | 커스텀 목록 조회 |
| DELETE | `/api/extensions/custom` | 커스텀 전체 삭제 |
| DELETE | `/api/extensions/custom/{id}` | 커스텀 개별 삭제 |
| GET | `/api/extensions/template` | 엑셀 양식 다운로드 |
| POST | `/api/extensions/upload` | 엑셀 업로드 일괄 추가 |

### 차단 테스트

| Method | URL | 설명 |
|--------|-----|------|
| GET | `/api/files` | 업로드된 파일 목록 조회 |
| POST | `/api/files/upload` | 파일 업로드 (차단 검사 포함, 다중) |
| DELETE | `/api/files/{id}` | 파일 개별 삭제 (서버 파일 포함) |
| DELETE | `/api/files` | 파일 전체 삭제 (서버 파일 포함) |

---

## 추가 구현 사항

- **대소문자 정규화**: 입력값을 모두 소문자로 변환하여 저장 및 비교
- **공백 처리**: trim() 적용으로 앞뒤 공백 제거
- **클라이언트 + 서버 이중 유효성 검증**: 영문·숫자 정규식, 길이 제한
- **XSS 방어**: Thymeleaf `th:text`로 출력, 서버 사이드 이스케이프 처리
- **엑셀 호환성**: `.xls`(HSSF) / `.xlsx`(XSSF) 모두 지원 (`WorkbookFactory`)
- **교차 중복 검사**: 고정↔커스텀 양방향 중복 차단
- **체크박스 롤백**: AJAX 실패 시 UI 상태 원복
- **파일 업로드 경로 분리**: `app.upload-dir` 프로파일별 설정 (로컬: `./uploads`, EC2: `/home/ec2-user/uploads`)

---

## 로컬 실행

### 사전 요구사항

- Java 17+
- PostgreSQL 실행 중 (`localhost:5432`)
- DB `flow_task` 생성 필요

```sql
CREATE DATABASE flow_task;
```

### 실행

```bash
./gradlew bootRun
```

접속: [http://localhost:8080](http://localhost:8080)

### 프로파일

`application.properties`에서 `spring.profiles.active` 변경

| 프로파일 | 설명 |
|----------|------|
| `local` | localhost PostgreSQL, ddl-auto=update, SQL 로그 출력 |
| `prod` | 환경변수 기반 DB 설정 (`DB_HOST`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`) |

---

## 프로젝트 구조

```
src/main/java/com/flowtask/
├── config/
│   ├── AppProperties.java            # app.system-id, app.upload-dir 바인딩
│   └── DataInitializer.java          # blocked_system 초기 데이터
├── controller/
│   ├── PageController.java           # GET /
│   ├── GlobalExceptionHandler.java
│   └── api/
│       ├── BlockedExtensionApiController.java
│       └── FileApiController.java
├── domain/
│   ├── entity/
│   │   ├── BlockedSystem.java
│   │   ├── BlockedSub.java
│   │   └── UploadedFile.java
│   ├── enums/
│   │   ├── FixedExtension.java       # 고정 확장자 Enum
│   │   └── BlockedType.java
│   └── repository/
│       ├── BlockedSystemRepository.java
│       ├── BlockedSubRepository.java
│       └── UploadedFileRepository.java
├── dto/
│   ├── request/CustomExtensionRequest.java
│   └── response/
│       ├── ApiResponse.java
│       ├── CheckResultDto.java
│       ├── CustomExtensionDto.java
│       ├── ExtensionPageResponse.java
│       ├── UploadResultDto.java
│       ├── FileUploadResultDto.java
│       └── UploadedFileDto.java
└── service/
    ├── BlockedExtensionService.java
    ├── ExcelService.java
    └── FileUploadService.java
```
