## Architecture
<img width="1536" height="1024" alt="ChatGPT Image 2026년 1월 14일 오후 06_17_31" src="https://github.com/user-attachments/assets/3a0e5bb0-b8d2-4200-aba6-4a3f4fab6ab5" />
본 프로젝트는 AWS 기반의 인프라 위에서 동작한다.

사용자의 요청은 Route 53과 CloudFront를 거쳐 Application Load Balancer(ALB)로 전달되며,  
ALB는 Private Subnet에 위치한 EC2 백엔드 서버로 트래픽을 분산시킨다.

백엔드 서버는 다음 리소스들과 통신한다.
- **RDS(MySQL)**: 관계형 데이터 저장소
- **Vector DB**: 벡터 기반 검색 및 추천을 위한 데이터 저장소

외부 AI API 호출은 **NAT Gateway**를 통해서만 이루어지며,  
이를 통해 내부 리소스가 인터넷에 직접 노출되지 않도록 구성하였다.

배포는 **GitHub Actions 기반 CI/CD 파이프라인**으로 자동화되어 있다.  
GitHub에 코드가 푸시되면 Docker 이미지를 빌드하여 Amazon ECR에 업로드하고,  
EC2 서버에서 해당 이미지를 받아 애플리케이션을 실행한다.


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
