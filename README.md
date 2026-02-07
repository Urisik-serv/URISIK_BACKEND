## Architecture Overview

본 프로젝트는 **보안성과 확장성을 고려한 AWS 기반 클라우드 아키텍처**로 구성되어 있습니다.

사용자의 요청은 **Route 53**을 통해 도메인 기반으로 라우팅되며,  
**Public Subnet에 위치한 Application Load Balancer(ALB)**를 거쳐  
**Private Subnet에 위치한 EC2 백엔드 서버**로 전달됩니다.

백엔드 서버는 **Docker 컨테이너 기반의 Spring Boot(Java 17) 애플리케이션**으로 실행되며,  
데이터는 **Amazon RDS(MySQL)**에 저장되고 파일 데이터는 **Amazon S3**를 사용합니다.

배포는 **GitHub Actions 기반 CI/CD 파이프라인**으로 자동화되어 있으며,  
Docker 이미지를 **Docker Hub**에 푸시한 뒤 EC2에서 pull & run 방식으로 실행됩니다.

---

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



## Commit Message Convention

We follow Conventional Commits.

- feat: 기능 추가
- fix: 버그 수정
- refactor: 리팩토링
- docs: 문서
- test: 테스트
- chore: 기타 설정

## 브랜치 네이밍 규칙

- 기능 개발: `feature/#이슈번호-기능명`
- 버그 수정: `fix/#이슈번호-버그명`
- (선택) 긴급 수정: `hotfix/#이슈번호-설명`
