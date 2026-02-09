## 🍽️Urisik | 알레르기 가족 모두가 안전하게, 한 식탁에서 즐겁게 먹을 수 있도록 돕는 맞춤형 식단 관리 서비스

## Project Introduction
<img src="https://github.com/user-attachments/assets/dbeabecb-9501-4323-8f41-533b2eef2231" width="500" />

### 🧩 서비스 한 줄 요약
알레르기 정보를 기반으로  
**안전한 레시피만 골라 추천하는 가족 맞춤 식단 서비스**

### ✨ 주요 기능
- 알레르기 자동 판별
- 안전 / 별점 / 위시리스트 기반 추천
- 변형 레시피 제공
<br>

---

## Tech Stack

### 🛠 Backend & Framework
<div>
  <img src="https://img.shields.io/badge/Java 17-007396?style=flat-square&logo=java&logoColor=white">
  <img src="https://img.shields.io/badge/Gradle-02303A?style=flat-square&logo=gradle&logoColor=white">
  <img src="https://img.shields.io/badge/Spring Boot-6DB33F?style=flat-square&logo=springboot&logoColor=white">
  <img src="https://img.shields.io/badge/Spring Data JPA-6DB33F?style=flat-square&logo=hibernate&logoColor=white">
  <img src="https://img.shields.io/badge/Spring Security-6DB33F?style=flat-square&logo=springsecurity&logoColor=white">
  <img src="https://img.shields.io/badge/OAuth2-000000?style=flat-square&logo=oauth&logoColor=white">
</div>

### 🗄 Database
<div>
  <img src="https://img.shields.io/badge/MySQL-4479A1?style=flat-square&logo=mysql&logoColor=white">
  <img src="https://img.shields.io/badge/Amazon RDS-527FFF?style=flat-square&logo=amazonrds&logoColor=white">
</div>

### ☁ Cloud & Infrastructure
<div>
  <img src="https://img.shields.io/badge/AWS EC2-FF9900?style=flat-square&logo=amazonec2&logoColor=white">
  <img src="https://img.shields.io/badge/AWS S3-569A31?style=flat-square&logo=amazons3&logoColor=white">
  <img src="https://img.shields.io/badge/AWS Route53-FF9900?style=flat-square&logo=amazonroute53&logoColor=white">
  <img src="https://img.shields.io/badge/Linux-FCC624?style=flat-square&logo=linux&logoColor=black">
</div>

### 🚀 CI/CD & Deployment
<div>
  <img src="https://img.shields.io/badge/GitHub-181717?style=flat-square&logo=github&logoColor=white">
  <img src="https://img.shields.io/badge/GitHub Actions-2088FF?style=flat-square&logo=githubactions&logoColor=white">
  <img src="https://img.shields.io/badge/Docker-2496ED?style=flat-square&logo=docker&logoColor=white">
  <img src="https://img.shields.io/badge/Docker Hub-2496ED?style=flat-square&logo=docker&logoColor=white">
  <img src="https://img.shields.io/badge/Nginx-009639?style=flat-square&logo=nginx&logoColor=white">
</div>

---

##  Architecture Diagram

<img src="https://github.com/user-attachments/assets/ba8962eb-3e53-45d6-ac7d-4050eae0dc2d" width="800" />

---

## ERD

<img src="https://github.com/user-attachments/assets/0e7dfd3d-b7b9-4b58-a1b4-fb30660d9be6" width="800" />

---

## Directory Structure
```
📦 src/main/java/com/urisik/backend
 ├──📁 domain          #핵심 비즈니스 로직
     ├── 📁 allergy          # 알레르기 관련 패키지
     ├── 📁 familyroom       # 가족방 관련 패키지
     ├── 📁 home             # 홈 화면 관련 패키지
     ├── 📁 mealplan         # 식단 계획 관련 패키지
     ├── 📁 member           # 사용자 관련 패키지
     ├── 📁 notification     # 알림 관련 패키지
     ├── 📁 recipe           # 레시피 관련 패키지
     └── 📁 review           # 리뷰 관련 패키지
├── 📁 global          #공통 모듈
     ├── 📁 ai               # ai 연동
     ├── 📁 apiPayload       # 공통 응답, 에러 코드, 예외 처리 등 API 응답 관련 패키지
     ├── 📁 auth             # 인증/인가 
     ├── 📁 config           # 공통 설정 정의
     ├── 📁 external         # AWS S3 연동
     ├── 📁 util             # 공통 사용 유틸리티
     └── 📄 BaseEntity.java  # JPA 엔티티의 생성·수정 시간 관
└── 📄 UrisikBackendApplication.java
```

---

## Devloper
| 서정춘 | 이원준 | 이은채 | 허건우 |
|:------:|:------:|:------:|:------:|
| <img src="https://github.com/chunny-k.png" alt="서정춘" width="150"> | <img src="https://github.com/wonjun-lee-fcwj245.png" alt="이원준" width="150"> | <img src="https://github.com/euuunchae.png" alt="이은채" width="150"> | <img src="https://github.com/woo6629058.png" alt="허건우" width="150"> |
| BE | BE | BE | BE |
| [GitHub](https://github.com/chunny-k) | [GitHub](https://github.com/wonjun-lee-fcwj245) | [GitHub](https://github.com/euuunchae) | [GitHub](https://github.com/woo6629058) |

---

