# 📁 Backend 프로젝트 구조

## 전체 구조

```text
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── donyang/
│   │           └── anyang/
│   │               │
│   │               ├── AnyangApplication.java
│   │               │
│   │               ├── config/
│   │               │   ├── WebConfig.java
│   │               │   └── S3Config.java
│   │               │
│   │               ├── auth/
│   │               │   ├── AuthController.java
│   │               │   ├── AuthService.java
│   │               │   ├── AuthDto.java
│   │               │   ├── JwtProvider.java
│   │               │   │
│   │               │   └── oauth/
│   │               │       ├── CustomOAuth2UserService.java
│   │               │       ├── OAuth2UserInfo.java
│   │               │       ├── GoogleOAuth2UserInfo.java
│   │               │       ├── NaverOAuth2UserInfo.java
│   │               │       └── OAuth2AuthenticationSuccessHandler.java
│   │               │
│   │               ├── security/
│   │               │   ├── SecurityConfig.java
│   │               │   ├── JwtAuthenticationFilter.java
│   │               │   └── CustomUserDetails.java
│   │               │
│   │               ├── domain/
│   │               │   │
│   │               │   ├── user/
│   │               │   │   ├── User.java
│   │               │   │   ├── UserRepository.java
│   │               │   │   ├── UserService.java
│   │               │   │   ├── UserController.java
│   │               │   │   └── UserDto.java
│   │               │   │
│   │               │   ├── report/
│   │               │   │   ├── Report.java
│   │               │   │   ├── ReportRepository.java
│   │               │   │   ├── ReportService.java
│   │               │   │   ├── ReportController.java
│   │               │   │   └── ReportDto.java
│   │               │   │
│   │               │   ├── image/
│   │               │   │   ├── ReportImage.java
│   │               │   │   ├── ReportImageRepository.java
│   │               │   │   └── ReportImageService.java
│   │               │   │
│   │               │   ├── ai/
│   │               │   │   ├── AiAnalysis.java
│   │               │   │   ├── AiDetection.java
│   │               │   │   ├── AiAnalysisRepository.java
│   │               │   │   ├── AiDetectionRepository.java
│   │               │   │   └── AiAnalysisService.java
│   │               │   │
│   │               │   └── inquiry/
│   │               │       ├── Inquiry.java
│   │               │       ├── InquiryRepository.java
│   │               │       ├── InquiryService.java
│   │               │       ├── InquiryController.java
│   │               │       ├── InquiryDto.java
│   │               │       │
│   │               │       └── file/
│   │               │           ├── InquiryFile.java
│   │               │           ├── InquiryFileRepository.java
│   │               │           └── InquiryFileService.java
│   │               │
│   │               └── s3/
│   │                   ├── S3Service.java
│   │                   └── S3Controller.java
│   │
│   └── resources/
│       ├── application.yaml
│       ├── static/
│       └── templates/
│
└── test/
    └── java/
        └── com/
            └── dongyang/
                └── anyang/
                    └── AnyangApplicationTests.java



# Backend 파일별 역할

## 1. 메인 애플리케이션

### `AnyangApplication.java`


---

# 2. `config`

애플리케이션 전체에서 사용하는 설정을 관리한다.

### `WebConfig.java`

React 프론트엔드와 Spring Boot 백엔드가 통신할 수 있도록 CORS 등 웹 관련 설정을 담당한다.

### `S3Config.java`

AWS S3에 접근하기 위한 AWS 자격 정보와 S3 클라이언트를 설정한다.

---

# 3. `auth`

회원가입, 일반 로그인, JWT 발급 및 OAuth2 로그인을 담당하는 **인증 영역**이다.

> `auth`는 사용자가 **어떻게 인증되는지**를 담당한다.

### `AuthController.java`

회원가입과 일반 로그인 등 인증 관련 API 요청을 받는다.

### `AuthService.java`

회원가입 및 로그인 과정에서 사용자 조회, 비밀번호 검증, JWT 발급 등의 인증 로직을 처리한다.

### `AuthDto.java`

회원가입과 로그인 요청 및 응답에 사용하는 데이터를 정의한다.

### `JwtProvider.java`

JWT를 생성하고 JWT의 유효성을 검증하는 기능을 담당한다.

---

# 4. `auth/oauth`

Google과 Naver의 OAuth2 소셜 로그인을 처리한다.

### `CustomOAuth2UserService.java`

Google과 Naver에서 전달받은 사용자 정보를 처리하고 기존 회원을 조회하거나 신규 회원을 생성한다.

### `OAuth2UserInfo.java`

Google과 Naver의 사용자 정보를 동일한 형태로 다루기 위한 공통 인터페이스이다.

### `GoogleOAuth2UserInfo.java`

Google에서 전달받은 사용자 정보를 프로젝트의 사용자 정보 형태로 변환한다.

### `NaverOAuth2UserInfo.java`

Naver에서 전달받은 사용자 정보를 프로젝트의 사용자 정보 형태로 변환한다.

### `OAuth2AuthenticationSuccessHandler.java`

OAuth2 로그인 성공 후 JWT를 발급하고 React 프론트엔드로 로그인 결과를 전달한다.

---

# 5. `security`

Spring Security를 이용해 **인증된 요청을 검증하고 사용자의 접근 권한을 관리하는 영역**이다.

### `SecurityConfig.java`

Spring Security의 인증 방식, URL별 접근 권한, JWT 및 OAuth2 관련 보안 설정을 정의한다.

### `JwtAuthenticationFilter.java`

HTTP 요청의 Authorization 헤더에서 JWT를 추출하고 검증한 후 인증된 사용자를 SecurityContext에 등록한다.

### `CustomUserDetails.java`

Spring Security가 로그인한 사용자의 ID, 이메일, 비밀번호, 권한 등의 정보를 사용할 수 있도록 사용자 정보를 제공한다.

---

# 6. `domain/user`

사용자라는 도메인 자체를 관리한다.

### `User.java`

MySQL의 `users` 테이블과 매핑되는 JPA Entity로 사용자 정보를 정의한다.

### `UserRepository.java`

`users` 테이블의 사용자 데이터를 조회하고 저장하는 JPA Repository이다.

### `UserService.java`

사용자 조회 및 사용자와 관련된 비즈니스 로직을 처리한다.

### `UserController.java`

사용자 조회 등 사용자 도메인과 관련된 API 요청을 처리한다.

### `UserDto.java`

사용자 정보를 API로 전달하거나 받을 때 사용하는 DTO를 정의한다.

---

# 7. `domain/report`

시민의 도로 위험 신고와 신고 처리 상태를 관리한다.

### `Report.java`

MySQL의 `reports` 테이블과 매핑되는 도로 신고 JPA Entity이다.

### `ReportRepository.java`

도로 신고 데이터를 조회하고 저장하는 JPA Repository이다.

### `ReportService.java`

신고 등록, 신고 조회, 신고 상태 변경 등의 도로 신고 비즈니스 로직을 처리한다.

### `ReportController.java`

시민과 관리자의 도로 신고 관련 API 요청을 처리한다.

### `ReportDto.java`

도로 신고 등록 및 조회에 필요한 요청과 응답 데이터를 정의한다.

---

# 8. `domain/image`

도로 신고에 첨부된 이미지를 관리한다.

### `ReportImage.java`

MySQL의 `report_images` 테이블과 매핑되어 신고 이미지의 S3 URL 및 파일 정보를 저장한다.

### `ReportImageRepository.java`

신고 이미지 데이터를 조회하고 저장하는 JPA Repository이다.

### `ReportImageService.java`

신고 이미지를 저장하고 신고와 이미지의 관계를 처리한다.

---

# 9. `domain/ai`

YOLO 기반 AI 도로 손상 분석 결과를 관리한다.

### `AiAnalysis.java`

MySQL의 `ai_analyses` 테이블과 매핑되어 AI 분석 실행 정보와 결과 이미지 정보를 저장한다.

### `AiDetection.java`

MySQL의 `ai_detections` 테이블과 매핑되어 AI가 탐지한 객체의 종류, confidence, bounding box 정보를 저장한다.

### `AiAnalysisRepository.java`

AI 분석 결과를 조회하고 저장하는 JPA Repository이다.

### `AiDetectionRepository.java`

AI 탐지 결과를 조회하고 저장하는 JPA Repository이다.

### `AiAnalysisService.java`

FastAPI/YOLO AI 서버에 분석을 요청하고 분석 결과를 저장하는 비즈니스 로직을 처리한다.

---

# 10. `domain/inquiry`

시민 문의 등록과 관리자 답변 기능을 관리한다.

### `Inquiry.java`

MySQL의 `inquiries` 테이블과 매핑되어 문의 유형, 제목, 내용, 답변 상태 등의 정보를 저장한다.

### `InquiryRepository.java`

문의 데이터를 조회하고 저장하는 JPA Repository이다.

### `InquiryService.java`

문의 등록, 문의 조회, 관리자 답변 등의 문의 관련 비즈니스 로직을 처리한다.

### `InquiryController.java`

시민과 관리자의 문의 관련 API 요청을 처리한다.

### `InquiryDto.java`

문의 등록, 문의 조회, 관리자 답변에 필요한 요청과 응답 데이터를 정의한다.

---

# 11. `domain/inquiry/file`

문의에 첨부된 파일을 관리한다.

### `InquiryFile.java`

MySQL의 `inquiry_files` 테이블과 매핑되어 첨부파일 이름과 S3 저장 위치를 관리한다.

### `InquiryFileRepository.java`

문의 첨부파일 데이터를 조회하고 저장하는 JPA Repository이다.

### `InquiryFileService.java`

문의 첨부파일을 S3에 업로드하고 문의와 파일을 연결하는 로직을 처리한다.

---

# 12. `s3`

AWS S3를 이용한 이미지 및 파일 저장을 담당한다.

### `S3Service.java`

이미지와 첨부파일을 AWS S3에 업로드하고 삭제하며 저장된 파일의 URL을 관리한다.

### `S3Controller.java`

프론트엔드에서 파일 업로드를 요청할 수 있도록 S3 관련 API를 제공한다.

