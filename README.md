## Architecture Overview

본 프로젝트는 AWS 기반 클라우드 인프라 위에서 동작하는 **백엔드 중심 아키텍처**로,  
**보안성**, **확장성**, 그리고 **RAG(Retrieval-Augmented Generation) 기반 AI 기능 확장**을  
고려하여 설계되었다.

외부 요청은 **Route 53**과 **CloudFront**를 거쳐  
**Application Load Balancer(ALB)** 로 전달되며,  
ALB는 **유일한 진입 지점(Single Entry Point)** 으로서  
트래픽을 **Private Subnet**에 위치한 **EC2 백엔드 서버**로 분산 처리한다.  
이를 통해 백엔드 서버는 인터넷에 직접 노출되지 않는다.

백엔드 애플리케이션은 EC2 인스턴스에서 **Docker 컨테이너**로 실행되며,  
서비스의 핵심 데이터는 **RDS(MySQL)** 에 저장된다.  
또한 벡터 기반 검색 및 추천 기능을 위해 **Vector DB**를 사용하며,  
이는 RAG 구조에서 **내부 컨텍스트 검색(Retrieval)** 을 담당한다.

외부 AI API 호출은 **NAT Gateway를 통한 Outbound 트래픽만 허용**하여  
내부 리소스의 직접적인 인터넷 노출을 방지하고 보안을 강화하였다.

배포는 **GitHub Actions 기반 CI/CD 파이프라인**으로 자동화되어 있으며,  
코드가 GitHub에 푸시되면 **Docker 이미지를 빌드하여 Docker Hub에 업로드**한 뒤,  
EC2 서버에서 최신 이미지를 pull 받아 애플리케이션을 실행한다.

---

## Architecture Diagram

아래 다이어그램은 본 프로젝트의 전체 아키텍처 구조와 요청 흐름을 나타낸다.

<img width="1536" height="1024" alt="ChatGPT Image 2026년 1월 19일 오후 04_39_47" src="https://github.com/user-attachments/assets/9bf8b5d3-3cc3-4427-8ef8-9f247447284b" />



---

## Request Flow

1. 사용자의 요청은 **Route 53**을 통해 도메인 기반으로 라우팅된다.
2. **CloudFront**에서 요청을 수신하여 전송 최적화 및 캐싱을 수행한다.
3. **Application Load Balancer(ALB)** 가 트래픽을 수신하고 백엔드 서버로 분산한다.
4. **Private Subnet**의 EC2 백엔드 서버가 요청을 처리하며  
   내부적으로 **RDS(MySQL)** 및 **Vector DB**와 통신한다.
5. AI 기능이 필요한 경우, **NAT Gateway**를 통해 외부 **AI API**와 통신한다.

---

## Key Design Points

- **ALB 단일 진입 구조**를 통한 백엔드 서버 직접 노출 방지
- 모든 핵심 리소스를 **Private Subnet**에 배치하여 보안 강화
- **Vector DB 기반 RAG 구조**를 통한 AI 응답 정확도 향상
- **NAT Gateway 기반 Outbound 전용 통신**으로 외부 연동 제어
- **GitHub Actions + Docker Hub 기반 CI/CD**를 통한 자동 배포 환경



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
