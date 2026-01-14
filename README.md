## Architecture
flowchart LR
    User[User] --> Route53[Route 53]
    Route53 --> CloudFront[CloudFront]
    CloudFront --> ALB[ALB]

    subgraph AWS[AWS Cloud]
        subgraph VPC[VPC]
            subgraph PublicSubnet[Public Subnet]
                ALB
            end

            subgraph PrivateSubnet[Private Subnet]
                EC2[EC2 - Backend Server]
                RDS[(RDS - MySQL)]
                VectorDB[(Vector DB)]
                NAT[NAT Gateway]

                EC2 --> RDS
                EC2 --> VectorDB
                EC2 --> NAT
            end
        end
    end

    NAT --> ExternalAI[External AI APIs]

    GitHub[GitHub] --> Actions[GitHub Actions]
    Actions --> ECR[ECR]
    ECR --> EC2



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
