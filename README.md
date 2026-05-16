# Ebooki

실시간 교환 독서를 위한 협업 플랫폼입니다.

교환 독서란?<br>
한 권의 책을 순서를 정해 읽는 방식으로, 내부에 자유롭게 본인의 의견을 남겨 공유할 수 있습니다.
<br>오프라인으로 진행하는 경우 시간이 오래 걸린다는 단점이 있어
<br>온라인 상에서 실시간으로 공유할 수 있는 플랫폼을 개발했습니다.

사용자들은 팀을 생성하고 함께 책을 읽으며,
하이라이트, 댓글, 이모티콘 반응 등을 실시간으로 공유할 수 있습니다.

## Features

- JWT 기반 인증/인가
- Redis 기반 초대 링크 관리
- WebSocket + STOMP 실시간 협업 기능
- 하이라이트 / 댓글 / 이모티콘 기능
- 팀 단위 권한 검증
- 동시성 문제 대응 및 데이터 정합성 관리

## Tech Stack

### Backend
- Java 17
- Spring Boot
- Spring Security
- JPA / Hibernate
- MySQL
- Redis

### Realtime
- WebSocket
- STOMP

### Infra
- AWS EC2
- AWS S3
- Docker

## Architecture
<img width="1057" height="614" alt="image" src="https://github.com/user-attachments/assets/3fa27801-1604-45bd-8869-f022c948ce20" />

- WebSocket + STOMP 기반 실시간 협업 기능 구현
- JWT 인증을 위한 HandshakeInterceptor + ChannelInterceptor 적용
- Redis 기반 초대 링크 및 분산 락 관리
- 팀 단위 권한 검증을 통한 IDOR 방어 적용

## ERD
<img width="1677" height="1056" alt="image" src="https://github.com/user-attachments/assets/c826bd9d-db93-425b-8f92-b2cacc694ebb" />

- Team 중심의 Book, Highlight, Comment 계층 구조 설계
- Highlight 기반 댓글 및 이모티콘 기능 구현
- 팀 단위 데이터 접근 제어가 가능하도록 관계 구성

## Troubleshooting

### 동시 접속 시 팀 인원 초과 문제

일회성 초대 링크를 통한 팀 참여 과정에서 동시 요청이 발생할 경우,  
팀 최대 인원을 초과하여 등록될 수 있는 문제가 발생했습니다.

초기에는 DB Lock과 synchronized 등을 고려했지만,  
데이터베이스 부하와 단일 서버 환경에서만 동작하는 한계를 확인했습니다.

이를 해결하기 위해 Redis 기반 분산 락을 적용하여  
초대 링크 검증부터 팀원 등록까지의 과정을 하나의 임계 구역으로 처리했습니다.

그 결과 동시 접근 상황에서도 팀 정원을 안정적으로 유지하며  
데이터 일관성과 서비스 신뢰성을 확보할 수 있었습니다.

## My Contribution

- 실시간 협업 기능 구현
- Redis 기반 초대 시스템 구현
- JWT 인증/인가 처리
- WebSocket 인증 구조 설계
- 하이라이트/댓글/이모티콘 API 개발

### Collaboration
- GitHub
- Notion
