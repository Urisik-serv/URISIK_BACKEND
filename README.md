# 🍽️ URISIK  
### 알레르기 가족 모두가 안전하게,  
### 한 식탁에서 즐겁게 먹을 수 있도록 돕는 맞춤형 식단 관리 서비스

---

## 📌 Project Introduction
<img width="1886" height="1128" alt="1" src="https://github.com/user-attachments/assets/2920881e-b2ef-42f4-a7da-69dad7a8ee42" />



### ✨ 주요 기능
- 🛡️ 알레르기 자동 판별
- ⭐ 안전 / 별점 / 위시리스트 기반 추천
- 🔁 알레르기 대체 레시피 제공
- 👨‍👩‍👧 가족 단위 알레르기 관리
- 📝 리뷰 및 평점 기반 추천 고도화

</td>
</tr>
</table>

---

## 🎯 Problem & Solution

### ❗ Problem
- 가족 구성원마다 서로 다른 알레르기
- 레시피 선택 시 매번 성분 확인 필요
- 기존 레시피 서비스는 **“개인 기준” 추천에만 집중**

### ✅ Solution
- 가족방 단위 알레르기 정보 관리
- 레시피별 알레르기 위험 자동 판별
- 안전 / 선호 / 행동 데이터를 결합한 추천 제공

---

## 📱 주요 화면 미리보기
<img width="1886" height="3481" alt="step1" src="https://github.com/user-attachments/assets/940686ef-e00e-443b-a7a6-16531c8273bc" />
<img width="1886" height="6031" alt="step2" src="https://github.com/user-attachments/assets/87b88f05-b71e-4f7e-91ad-21016b27c623" />
<img width="1886" height="3075" alt="step3" src="https://github.com/user-attachments/assets/a86731dc-3d21-4100-8813-0971174fc9a2" />
<img width="1886" height="2408" alt="step4" src="https://github.com/user-attachments/assets/82f3aa0f-8181-4831-8767-85907079e355" />






---

## 🛠 Tech Stack

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

### 🧪 Testing & Performance
<div>
  <img src="https://img.shields.io/badge/Swagger-85EA2D?style=flat-square&logo=swagger&logoColor=black">
  <img src="https://img.shields.io/badge/Postman-FF6C37?style=flat-square&logo=postman&logoColor=white">
  <img src="https://img.shields.io/badge/JUnit5-25A162?style=flat-square&logo=java&logoColor=white">
  <img src="https://img.shields.io/badge/K6-7D64FF?style=flat-square&logo=k6&logoColor=white">
</div>

---

## 🏗  Architecture Diagram

<img src="https://github.com/user-attachments/assets/ba8962eb-3e53-45d6-ac7d-4050eae0dc2d" width="800" />

---

## 🗄 ERD

<img src="https://github.com/user-attachments/assets/0e7dfd3d-b7b9-4b58-a1b4-fb30660d9be6" width="800" />

---

## Directory Structure
```
📦 src/main/java/com/urisik/backend
 ├──📁 domain          # 핵심 비즈니스 로직
     ├── 📁 allergy          # 알레르기 관련 패키지
     ├── 📁 familyroom       # 가족방 관련 패키지
     ├── 📁 home             # 홈 화면 관련 패키지
     ├── 📁 mealplan         # 식단 계획 관련 패키지
     ├── 📁 member           # 사용자 관련 패키지
     ├── 📁 notification     # 알림 관련 패키지
     ├── 📁 recipe           # 레시피 관련 패키지
     └── 📁 review           # 리뷰 관련 패키지
├── 📁 global          # 공통 모듈
     ├── 📁 ai               # ai 연동 관련 패키지
     ├── 📁 apiPayload       # 공통 응답, 에러 코드, 예외 처리 등 API 응답 관련 패키지
     ├── 📁 auth             # 인증/인가 관련 패키지
     ├── 📁 config           # 공통 설정 정의 관련 패키지
     ├── 📁 external         # AWS S3 연동 관련 패키지
     ├── 📁 util             # 공통 사용 유틸리티 관련 패키지
     └── 📄 BaseEntity.java  # JPA 엔티티의 생성·수정 시간 관련 패키지
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

