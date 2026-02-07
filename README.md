

## 🏗️ Architecture Diagram

<img width="1536" height="1024" alt="ChatGPT Image 2026년 1월 29일 오후 03_06_54" src="https://github.com/user-attachments/assets/ba8962eb-3e53-45d6-ac7d-4050eae0dc2d" />


---

## Tech Stack

<div>
<img src="https://img.shields.io/badge/Java-007396?style=flat-square&logo=java&logoColor=white">
<img src="https://img.shields.io/badge/Gradle-02303A?style=flat-square&logo=gradle&logoColor=white">
<img src="https://img.shields.io/badge/Spring Boot-6DB33F?style=flat-square&logo=springboot&logoColor=white">
<img src="https://img.shields.io/badge/Spring Data JPA-6DB33F?style=flat-square&logo=databricks&logoColor=white">
<img src="https://img.shields.io/badge/Spring Security-6DB33F?style=flat-square&logo=springsecurity&logoColor=white">
<img src="https://img.shields.io/badge/OAuth2.0-000000?style=flat-square&logoColor=white">
</div>
<div>
<img src="https://img.shields.io/badge/MySQL-4479A1?style=flat-square&logo=mysql&logoColor=white">
</div>
<div>
<img src="https://img.shields.io/badge/Github Actions-2088FF?style=flat-square&logo=githubactions&logoColor=white">
<img src="https://img.shields.io/badge/Docker-2496ED?style=flat-square&logo=docker&logoColor=white">
<img src="https://img.shields.io/badge/AWS EC2-FF9900?style=flat-square&logo=amazonec2&logoColor=white">
<img src="https://img.shields.io/badge/AWS RDS-527FFF?style=flat-square&logo=amazonrds&logoColor=white">
<img src="https://img.shields.io/badge/AWS Route53-FF9900?style=flat-square&logo=amazonroute53&logoColor=white">
</div>
<br>

## Directory Structure
```
📦 src/main/java/com/urisik/backend
 ├──📁 domain
     ├── 📁 allergy          # 알레르기 관련 패키지
     ├── 📁 familyroom       # 가족방 관련 패키지
     ├── 📁 home             # 홈 화면 관련 패키지
     ├── 📁 mealplan         # 식단 계획 관련 패키지
     ├── 📁 member           # 사용자 관련 패키지
     ├── 📁 notification     # 알림 관련 패키지
     ├── 📁 recipe           # 레시피 관련 패키지
     └── 📁 review           # 리뷰 관련 패키지
├── 📁 global
     ├── 📁 ai               # ai 연동
     ├── 📁 apiPayload       # 공통 응답, 에러 코드, 예외 처리 등 API 응답 관련 패키지
     ├── 📁 auth             # 인증/인가 
     ├── 📁 config           # 공통 설정 정의
     ├── 📁 external         # AWS S3 연동
     ├── 📁 util             # 공통 사용 유틸리티
     └── 📄 BaseEntity.java  # JPA 엔티티의 생성·수정 시간 관
└── 📄 UrisikBackendApplication.java



