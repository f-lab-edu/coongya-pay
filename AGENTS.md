# coongya-pay

실시간 송금 시스템. 사용자 지갑(페이머니)에 대한 충전·송금·정산을 다루는 결제 도메인 프로젝트다.

## 기술 스택

- Java 21, Spring Boot, Gradle
- 영속성: **MyBatis** (JPA 아님) — SQL은 `src/main/resources/mapper/<도메인>/`의 XML 매퍼에 작성
- MySQL (스키마: `src/main/resources/schema.sql`), Redis (Spring Session)
- Spring Security, Lombok
- 테스트: JUnit 5, AssertJ, Testcontainers(MySQL)

## 빌드 & 테스트

- 빌드: `./gradlew build`
- 테스트: `./gradlew test` — test 프로필이 자동 적용되며 Testcontainers로 MySQL을 띄우므로 **Docker가 필요**하다
- 로컬 실행: `docker compose up -d` (MySQL + Redis) 후 `./gradlew bootRun` (local 프로필, 루트 `.env` 로드)

## 아키텍처

- 패키지: `com.flab.coongyapay.{account, auth, bank, charge, common, config, idempotency, kyc, reconciliation, transaction, user, wallet}`
- 레이어: controller → service → repository → mapper(MyBatis XML)
- 도메인 객체는 불변으로 설계한다: private 생성자 + 정적 팩토리 메서드(`create`, `from`), setter 없음
- 지갑 잔액은 원장(ledger) 기반이다: `transaction` 도메인의 거래 헤더 + `TransactionEntry`(CREDIT/DEBIT) append로 기록하고, DB UNIQUE 제약으로 이중 기입과 지갑별 순번 무결성을 보장한다
- 오래 걸리는 작업(외부 은행 연동 등)은 워커 + 상태머신 패턴으로 비동기 처리하며, 워커는 lease 선점과 펜싱 토큰으로 중복 실행을 막는다
- 주석·커밋 메시지·테스트명은 한국어를 사용한다

## Code Review Rules

리뷰 코멘트는 **한국어**로 작성한다.

### 금액 처리

- 금액은 `BigDecimal`만 사용한다. `double`/`float`으로 금액을 표현하거나 연산하는 코드는 지적한다.
- 금액 값 비교는 `compareTo`를 사용한다. `equals`로 금액을 비교하는 코드는 스케일 차이로 오동작하므로 지적한다.
- 금액 연산에서 반올림·스케일 정책이 암묵적으로 바뀌는 변경은 지적한다.

### 원장·잔액 무결성

- 지갑 잔액을 UPDATE로 직접 덮어써 계산하는 변경은 금지다. 잔액 변동은 반드시 원장 항목(CREDIT/DEBIT) append로 기록되어야 한다.
- 원장의 소유 키는 `user_id`가 아니라 `wallet_id`다. 원장 조회·기록을 `user_id`로 키잉하는 코드는 지적한다.
- 이중 기입 방지·지갑별 순번 무결성은 DB UNIQUE 제약이 최종 방어선이다. 이 제약을 우회하거나 제거하는 변경은 지적한다.

### 동시성·트랜잭션 경계

- 외부 시스템(은행 API 등) 호출은 DB 트랜잭션과 락 **밖**에서 수행해야 한다. 트랜잭션을 잡은 채 외부 호출이나 장시간 블로킹을 하는 코드는 지적한다.
- 공유 자원(지갑 잔액 등) 갱신은 비관적 락(`SELECT ... FOR UPDATE`) **안**에서 수행해야 한다. 락 없이 read-modify-write 하는 코드는 지적한다.
- 워커가 수행하는 상태 기록은 펜싱 토큰 검증을 거쳐야 한다. 펜싱 없이 상태를 기록하는 경로가 추가되면 지적한다.

### 멱등성·재시도 안전성

- 상태를 바꾸는 요청과 외부 호출은 재시도되어도 이중 처리(이중 출금·이중 크레딧)가 발생하지 않아야 한다. 재시도 경로의 중복 부작용 가능성을 지적한다.
- 외부 호출에는 idempotency key를 사용해야 한다.
- UNIQUE 제약 위반(`DuplicateKeyException`)을 "이미 처리됨"으로 간주하고 정상 진행하는 기존 패턴은 의도된 안전 경로이므로 지적하지 않는다.

### 실패 분류

- 외부 호출 실패는 명확한 거절(4xx)과 결과 불명(timeout, 5xx)을 구분해야 한다.
- 결과 불명을 실패로 단정하고 종결 처리하는 코드는 결함이다. 불명 상태는 대사(reconciliation)나 재조회로 확정될 때까지 종결하지 않는다.
- 외부 부작용 발생 여부를 확신할 수 없는 상태에서 임의로 되돌리거나 재실행하는 코드는 지적한다.

### 보안·민감정보

- 리소스 접근 시 소유권 검증(요청 사용자의 지갑·계좌인지)이 누락되면 지적한다.
- PIN·비밀번호는 해시로만 저장·비교한다. 원문을 보관, 로깅, 응답에 노출하는 코드는 지적한다.
- 계좌번호 등 민감정보를 로그에 그대로 출력하는 코드는 지적한다.
- MyBatis 매퍼에서 `${}` 문자열 치환은 SQL 인젝션 위험이 있으므로 금지다. 파라미터 바인딩은 `#{}`만 사용한다 (현재 코드베이스는 `${}` 0건).

### 예외 처리

- 비즈니스 오류는 `BusinessException` + `ErrorCode` + `GlobalExceptionHandler` 패턴을 따른다. 이 패턴을 우회한 임의의 예외 체계 추가는 지적한다.
- 예외를 삼키거나(catch 후 무시) 의미 없는 `RuntimeException`으로 다시 감싸는 코드는 지적한다.

### 테스트

- 도메인 로직 변경에는 테스트가 동반되어야 한다. 테스트 없는 로직 변경은 지적한다.
- 테스트명은 한글 스네이크 케이스로 동작을 서술한다 (예: `선점_후_처리하면_잔액이_증가하고_COMPLETED된다`).
- 통합 테스트는 `@SpringBootTest` + `@Transactional`을 사용한다. `@Transactional`은 공유 Testcontainer 오염을 막는 장치이므로 제거하는 변경은 지적한다.
- 단언은 AssertJ를 사용한다.
