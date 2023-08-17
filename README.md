# 주식 서비스

## 프로젝트 설정
1. spring boot 2.7.13
2. MariaDB 10.6.14
3. H2 - TEST 용

## public DNS
ec2-43-201-193-154.ap-northeast-2.compute.amazonaws.com


## 시스템 아키텍처
- Github Action을 통해 CI후 빌드파일 S3에 저장 $rarr; Codedeploy에 CD 요청해 S3의 빌드파일을 EC2에 배포합니다.
- EC2는 NginX를 통해 가장 최근의 배포되어 실행중인 Application 서비스로 리버스 프록시됩니다.
- Application 서비스는 Api 서비스와 Batch 서비스가 같이 실행됩니다.
- DB는 MariaDB RDS로 EC2서비스에서 연결되어 있습니다.
![img_2.png](img_2.png)
## API 목록
http://ec2-43-201-193-154.ap-northeast-2.compute.amazonaws.com/docs/index.html
1. 회원가입 - docs x
   - POST /api/members
2. 로그인  - docs x
   - POST /api/login
3. 토큰 재발급  - docs x
   - POST /api/reissue
4. 계좌등록
   - POST /api/accounts
5. 계좌삭제
   - DELETE /api/accounts/{accountId}
6. 계좌목록조회
   - GET /api/accounts
7. 계좌상세조회
   - GET /api/accounts/{accountId}
8. 송금 
   - POST /api/accounts/{accountId}/trades
9. 결제
   - POST /api/accounts/{accountId}/payments
10. 주식목록조회
    - GET /api/stocks
11. 주식상세조회
    - GET /api/stocks/{stockId}
12. 주식구매
    - POST /api/accounts/{accountId}/stocks/{stockId}
13. 주식판매
    - POST /api/accounts/{accountId}/stocks/{stockId}/selling
14. 예약결제
    - POST /api/accounts/{accountId}/reservations
15. 회원탈퇴 - docs x
    - DELETE /api/members

## ERD
![img.png](img.png)

## DB Schema
![img_1.png](img_1.png)

## 고민사항
1. Exception handling
   1. 상황에 맞는 HTTP 상태 코드와 사용자가 요청을 수정해 재시도할 수 있도록 오류 사유를 나타내는 메시지를 입력값과 함께 전달할 수 있도록 노력했습니다.
   2. Exception은 `ApplicationException - DomainException - 상황별Exception` 의 계층으로 구현해 오류시 로그에서 확인이 용이하도록 구현했습니다.
2. Entity 연관관계 설정
   1. 처음 연관관계의 주인 설정을 잘못해서 도메인 요청등이 다른 도메인에 노출되는 경우가 발생하고, 테스트도 어려워졌습니다.
   2. 대부분의 경우는 연관관계 주인을 상위 엔티티에서 단방향으로 참조하는 형태로 해결했습니다.
   3. 이렇게 구현하고 보니, FK를 항상 하위 엔티티에서 갖고 있는 상태인데, 테스트시에 각각의 하위 엔티티를 새로 생성해줘야하다보니 굉장히 귀찮았습니다. 이 부분은 추가로 개선해볼 계획입니다.
3. 테스트 코드 작성과 예외 테스트
   1. TDD 기반으로 구현을 진행할 목적으로 테스트 도구를 선택하고 진행했습니다. 고민했던 선택지는 JUnit5과 Spock 두가지였는데, spock으로 결정했습니다.
      1. 이유는 Mocking이 간편하고 Parameterized Test가 많을것으로 예상해 이 부분에서 좀 더 용이한 spock을 선택했습니다.
      2. 다만, 실제로 사용해보니, 위에서 예상한 간편함이 줄여준 시간보단 동적타입언어인 groovy로 인해 발생하는 문제들을 해결하는데 소요하는 시간이 더 길었습니다.
      3. 정적타입언어였다면 `컴파일에러로 잡을수 있는 많은 문제들이(대표적으로 메서드 시그니처 오류) 런타임에 확인이 가능해져` 한번 수정한 이후 모든 테스트를 수행하는 시간만큼 에러체크가 늦어진게 가장 큰 불편함이었습니다.
   2. 테스트간 isolation 을 구축하려했습니다.
      1. 각 테스트의 멱등성을 보장하기 위해, 테스트 메서드별로 독립적인 상황에서 온전한 테스트를 진행할 수 있도록 구성하려 계획했습니다.
      2. `cleanup.sql` 과 `@SpringBootAcceptanceTest` 를 통해 데이터독립적인 테스트를 구성할 장치를 마련했습니다.
      3. 하지만, 처음부터 위 장치를 마련해두고 진행한게 아니어서 시간 관계상 모든 상황의 테스트 데이터를 세팅하는 `test.sql`로 테스트를 구성했습니다. 
   3. 놓치는 `예외케이스에 대한 정량적 판단 방법이나 기준`이 있는지 궁금합니다. `테스트 커버리지` 관점에서 `실제 현업에서 사용하시는 방법`이 있으신지 궁금합니다.
4. 테스트 위한 시스템 공통 사용 데이터 처리: BatchExecutionTime
   1. 예약결제배치를 1시간 단위로 수행하도록 설계했습니다. 그 과정에서 요청시간이 다음 수행시간 이전인지를 판단하는 로직이 필요했고 판단 기준이 되는 시간을 주입해줘야 할 필요가 생겼습니다.
   2. 그래서 정적으로 애플리케이션 실행시점과 배치 종료시점에 다음 수행시간을 설정하도록 했으나, 모든 애플리케이션 영역에서 열려있는 데이터라 좋은 방향은 아닌것 같습니다.
   3. DB를 통해 관리하는게 더 나은 방향이라 생각되는데, Spring Batch의 구현 이해도가 부족해 시간 관계상 개선을 못했습니다.
5. API 서버와 배치서버의 분리 - 멀티모듈 프로젝트
   1. API 서버와 배치 서버의 분리를 생각하고 진행하질 않아서 API 모듈 애플리케이션에서 배치 모듈까지 같이 실행하게 구현되었습니다.
6. 목록조회 페이징 처리
   1. 이 부분은 해결을 못했습니다.
   2. native Query를 사용하지 않고, 자식 컬렉션 엔티티의 갯수를 제한하는 방법이 궁금합니다.
7. @Transactional 의 적용 범위: Controller vs Service
   1. 트랜잭션의 범위를 `Controller 단위`로 해야하는지 `Service 단위`로 해야하는지 궁금합니다.
8. 목록조회 API 자료구조: Set vs List
   1. 여러개의 리턴목록을 갖는 타입의 API 엔티티에 Set과 List 중 어느 타입을 사용해야 하는지 고민했습니다.
   2. 중복과 sort 처리에 각각 장점이 있어서 고민했으나, entity 상에선 sort는 필요하지 않다고 판단해, entity는 set을 사용하고 dto 반환값에 list를 사용해 순서를 보장하게 했습니다.
9. @Embeddable 을 통한 일급컬렉션 엔티티 구성
   1. 이 부분은 리팩터링 진행을 하지 못했습니다.
10. git branch 전략: github flow
    1. 브랜치는 각 도메인 단위로 생성해, 기능단위로 커밋하도록 노력했습니다.
    2. 브랜치 관리 전략은 혼자 관리하는 프로젝트이나, PC두대에서 나눠 진행했던터라 github flow로 merge등을 좀 더 용이하게 했습니다.
11. reservationExecute batch 오류
    - Reservation &rarr; Account &rarr; Trades 를 참조하고 있는 관계에서,
    - 첫번째는 ManyToOne / LAZY / cascade ALL
    - 두번째는 OneToMany / LAZY / cascade ALL
    - 위와 같이 설정했는데 배치 실행시 한번의 배치 수행시 같은 Account 에 대한 예약이 여러건이면, Account 와 Trades 의 연관관계 테이블에 마지막 반영분만 insert 된다.
    - 전체 대상 select &rarr; account select &rarr; trades select &rarr; item processor 건별처리 &rarr; [reservation select 한건 &rarr; bank select 한건 &rarr; member select 한건 &rarr; trade insert 한건 &rarr; trades insert 한건 &rarr;] 반복 &rarr; account update &rarr; reservation update all &rarr; account_trades insert 1건
    - 이 문제는 chunkSize를 1개로 줄이고, ItemProcessor에서 건별로 save를 하니 해결되었습니다만, 정확한 원인 파악은 못한상태라 추가적인 검증이 필요합니다.