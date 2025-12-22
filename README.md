
<details>
<summary>ch01: ping-test</summary>

## 1. 개요

**Rate Limiter가 없는 서버 환경**에서, 매우 가벼운 REST API(`GET /ping`)에 대해 **Java SE 기반 공격(부하) CLI**를 사용하여 동시 요청을 발생시키고 문제가 발생하는지 확인

---

## 2. Server 구성

### 2.1 Filter Chain

### CustomFilterChain

- **HttpLoggingFilter & ResponseWrapper**
    - HTTP 요청/응답을 로깅하기 위한 필터
    - 실험 시 로깅으로 인한 오버헤드를 제거하기 위해 비활성화
    - `@ConditionalOnProperty` 기반으로 on/off 제어
- **CorsFilter**
    - Local 환경(IPv4, IPv6)에서 허용된 요청만 통과
    - 그 외 접근은 차단

---

### 2.2 Endpoint

### PingController

- 비즈니스 로직이 없는 단일 REST API Endpoint
- 요청
    
    ```bash
    curl -X GET http://localhost:8080/ping
    ```
    
- 응답
    
    ```
    pong
    ```
    

---

## 3. attack-cli 구성

### 3.1 기술 스택

- Java 21 (SE)
- Gradle Wrapper (`gradlew`)
- `java.net.http.HttpClient` 기반 구현

---

### 3.2 cli-module

- **AttackConfig**
    - 공격에 사용될 변수가 담기는 불변 객체(record)
- **cliParser**
    - cli script를 파싱하여 AttackConfig로 변환
- **AttackScenario**
    - 공격 방식을 정의할 인터페이스, 구현체로 PingScenario 사용
- **PingScenario**
    - Ping 공격에 사용될 HttpRequest를 구성하고 반환
- **ScenarioRequestResolver**
    - Scenario를 해석하고 올바른 HttpRequest로 변환
- **AttackRunner**
    - HttpClient를 구성하고 HttpRequest 전송 (latch, platform thread, thread pool)
    - 실행 결과 로깅
- **Main**
    - cli에서 전달받은 args를 올바른 모듈에 전달
    
    ---
    

### 3.3 config 구성

```java
public record AttackConfig(
    String url,
    int threads,              // 동시에 실행될 워커 스레드 수
    int requestsPerThread,    // 단일 스레드가 보내는 요청 수
    boolean burst,            // burst 모드 여부
    String scenario            // 공격 시나리오명 (예: ping)
) {}

```

- **threads**: 병렬로 실행되는 플랫폼 스레드 수
- **requestsPerThread**: 각 스레드가 수행할 요청 횟수
- **burst**:
    - `false`: 모든 스레드가 준비된 뒤 동시에 요청 시작
    - `true`: 준비 대기 없이 즉시 요청을 발사
- **scenario**: CLI 실행 시 선택되는 공격 시나리오

---

## 4. 실행 스크립트 예시

### 4.1 Build

```java
cd attacker-java/cli/attack
./gradlew clean jar
```

### 4.2.1 Linux / WSL / Bash

```bash
java -jar build/libs/attack.jar \
  --scenario ping \
  --url http://localhost:8080/ping \
  --threads 10 \
  --rpt 1000 \
  --burst true
```

---

### 4.2.2 Windows (PowerShell / CMD)

```bash
java -jar build/libs/attack.jar ^
  --scenario ping ^
  --url http://localhost:8080/ping ^
  --threads 10 ^
  --rpt 1000 ^
  --burst true
```

---

## 5. 실행 방식 상세

### 5.1 burst = false (Non-Burst Mode)

1. `threads` 개수만큼 플랫폼 스레드를 생성
2. 각 스레드를 스레드 풀에 적재
3. 동시에 `CountDownLatch`의 카운트를 감소
4. 모든 스레드가 준비되어 latch가 `0`이 되면
5. 각 스레드가 `requestsPerThread` 만큼 요청 전송

> 모든 스레드가 동일한 시점에서 시작

---

### 5.2 burst = true (Burst Mode)

1. `threads` 개수만큼 플랫폼 스레드를 생성
2. 스레드 적재와 동시에 대기 없이 요청 전송
3. 각 스레드는 `requestsPerThread` 만큼 즉시 요청 수행
4. 전체 과정을 **10ms delay** 후 반복

>

---

## 6. 배치 파일 (in /scripts dir)

- **ping_10000req.bat**
    - burst = false
    - 10개 스레드 × 1000 요청
    - 동시 시작을 보장하는 실험
- **ping_10000req_burst.bat**
    - burst = true
    - 동일한 요청 수
    - 최대한 빠르게 요청을 몰아서 전송

---

## 7. 집계

```java
AtomicInteger success = new AtomicInteger(); // thread-safe한 카운트
AtomicInteger fail = new AtomicInteger();
```

- Success
    - HTTP Status `200` 응답 수
- Fail
    - `200`이 아닌 모든 응답 수
- Elapsed(ms) : `end - start (currentTimeMillis)`
- Total Requests: `threads × requestsPerThread`
- RPS(Requests Per Second): `totalRequets / (elapsed(ms) / 1000)`

---

## 8. 실행 결과 예시

### **ping_10000req.bat**

![image.png](./imgs/ch01-ping/image1.png)

### **wsl (ubuntu, intellij terminal)**

![image.png](./imgs/ch01-ping/image2.png)

---

## 9. 인사이트

rate limiter가 없는 환경에서도 단순하지만 많은 요청을 실패없이 처리했다. 이는 요청 수보다 단일 요청이 유발하는 비용의 크기가 더 큰 영향을 미친다는 점을 시사했다. 그럼에도, 비정상적으로 반복되고 많은 요청에도 서버가 아무런 제한을 하지않는 상태임을 확인할 수 있었다.

현재로써는 rate limiter의 당위성이 부족하기 때문에, 먼저 고비용 작업(network I/O, DB wrihte) API를 구성하는 방향으로 확장한다.
</details>
간단한 동작을 수행하는 엔드포인트에 1만번의 요청 공격을 보냅니다. 장애가 없음을 확인하고 서버 로직을 비용이 큰 작업(db-write)으로 확장합니다.

</br>
<details>
<summary>ch02: db-write attack</summary>
## 실험 환경

- **DB**: MySQL 8.0 (InnoDB)
  - `isolation level = REPEATABLE_READ (default)`
  - `auto commit = true (default)` (단, @Transactional 진입 시 트랜잭션 시작과 함께 autoCommit=false로 설정됨)
- **ORM**: Spring Data JPA (with Hibernate, ConnectorJ)
- **Connection Pool**: HikariCP
  - `maximumPoolSize = 10 (default)`
  - `connectionTimeout = 30000ms (default)`
---

## 실험 시나리오 (CLI 공격)

CLI를 사용해 다음과 같은 공격 시나리오를 실행

- 대상 게시물: `postId = 1`
- 요청 사용자: `userId = 1`
- 총 요청 수: **10,000건**
- 실행 방식:
  - **10개의 플랫폼 스레드**
  - 각 스레드당 **1,000개의 요청**을 worker 큐에 적재
  - `CountDownLatch`가 0이 되는 순간 **동시에 실행**

의도적으로 *동일 userId + 동일 postId* 요청을 동시에 발생시켜, 일반적인 웹 환경에서는 잘 드러나지 않는 문제 상황을 재현

---

## 실험 결과 요약

- **최초 두 개의 트랜잭션만으로 Deadlock 발생**
- 이후 트랜잭션들은 커넥션을 점유한 채 대기
- 일부 요청은 커넥션을 할당받지 못하고 timeout

### CLI 결과

```text
Total Requests : 10000
Success        : 9991
Fail           : 9
Elapsed(ms)    : 49708
RPS            : 201
```

---
### Post Entity

```java
@Entity
@Table(name = "post")
public class Post extends BaseTime {

  @Id @GeneratedValue
  private Long id;

  private int likeCount;

  public void increaseLike() {
    this.likeCount++;
  }
}
```

- 게시물 엔티티
- `likeCount`는 트랜잭션 내에서 증가

---

### PostLike Entity

```java
@Entity
@Table(
  name = "post_like",
  uniqueConstraints = @UniqueConstraint(columnNames = {"post_id", "user_id"})
)
public class PostLike extends BaseTime {

  @Id @GeneratedValue
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  private Post post;

  private Long userId;
}
```

- 게시물과 사용자 간의 좋아요 관계
- `(post_id, user_id)` 조합에 **유니크 제약조건** 적용
  - 한 사용자는 하나의 게시물에 한 번만 Like 가능

---
### LikeService

```java
@Service
@RequiredArgsConstructor
public class LikeService {

  private final PostRepository postRepository;
  private final PostLikeRepository postLikeRepository;

  @Transactional
  public void like(LikeRequest request) {

    Post post = postRepository.findById(request.postId())
        .orElseThrow();

    if (postLikeRepository
        .existsByPostIdAndUserId(request.postId(), request.userId())) {
      return;
    }

    postLikeRepository.save(new PostLike(post, request.userId()));
    post.increaseLike();
  }
}
```

- 하나의 트랜잭션 안에서 **조회 → 존재 확인 → 삽입 → 업데이트**를 모두 수행
- 동시다발적 요청이 없는 경우에만 적합한 코드(이하 후술)

---
### Cli Request (in attacker-java dir)
```java
// ch02: DB-Write baseline
public class LikeScenario implements AttackScenario {

  AttackConfig config;

  public LikeScenario(AttackConfig config) {
    this.config = config;
  }

  @Override
  public HttpRequest toRequest() {

    String body = """
        { "postId": 1,
         "userId": 1}
        """;

    return HttpRequest.newBuilder()
        .uri(URI.create(config.url()))
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(body))
        .build();
  }
}
```

- postId=1인 게시물에 userId=1로 Like 요청, InnoDB의 유니크 제약조건 검증 유도
  - 한 사용자는 하나의 게시물에 한 번만 Like 가능

---
### like_10000req.bat (in attacker-java dir)
```powershell
@echo off
cd /d "%~dp0.."
call scripts\common.bat

java -jar %JAR_PATH% ^
  --scenario like ^
  --url %BASE_URL%/post/like ^
  --threads 10 ^
  --rpt 1000 ^
  --burst false

pause
```
- 10개의 thread에서 각 1000번의 req 전송(Java Platform Thread, CountDownLatch 기반)
- 비정상적(동일 Id에 대한 동시다발적)요청으로 InnoDB lock 결함 유도
- 유니크 제약 조건 처리를 위해 **InnoDB는 내부적으로 lock을 사용**(select .. for update 등이 아니어도)
- 초기 2개의 요청만으로 **deadlock** 발생
---

## 데드락 원인

```
T1: post S-lock 보유  → post_like S-lock 대기
T2: post_like X-lock → post X-lock 대기

→ 순환 대기 발생
→ InnoDB가 T1을 롤백
```

<details>
<summary>개념 상세</summary>

1. T1은 post 레코드에 s-lock을 걸었다.
```text
index PRIMARY of table `bdt`.`post`
trx id 479984 lock mode S locks rec but not gap
```
단순 select 문에는 s-lock이 걸리지 않는다. 그럼에도 t1은 post 조회 시점에 s-lock을 걸었다.
이후 쿼리에서 lickCnt를 update하기 때문에 미리 걸어놓은 것일까? 아니다. InnoDB는 미래의 동작을 예측하지 않는다.
이유는, 유니크 제약조건이 걸려있기 때문이다. reapeatable read 격리 수준에서는 트랜잭션의 버전관리를 통해 일관성있는 읽기를 제공한다.
하지만, 유니크 제약조건에서 필요한 것은 과거의 스냅샷이 아닌 현재 레코드의 상태이다.

가령, t1과 t2 조회시점에 모두 레코드가 존재하지 않는 상태였고 이후 t1, t2에서 insert를 진행한다면 중복된 쓰기가 발생한다.
이는 t1과 t2 조회에서는 일관성있는 읽기가 보장되었지만, 결과적으로 유니크 제약조건을 위배한다.

때문에, t1 조회 시점에서 s-lock을 걸어 t2가 x-lock을 획득하지 못하게 해야한다. 이런 이유로 select문이 for update 등이 아니여도 innoDB는 무조건 s-lock을 걸게된다.

2. T2는 post_like 레코드에 x-lock을 걸었다.
```text
index UKpmmko3h7yonaqhy5gxvnmdeue of table `bdt`.`post_like`
trx id 479983 lock_mode X locks rec but not gap
```
요청이 동시에 발생했기 때문에 순서는 보장되지 않는다. 여기서는 T1의 트랜잭션이 마무리 되기 이전에 T2이 시작된다.
select가 아닌 update(increase likeCnt)가 이뤄져야 하기 때문에 x-lock을 점유한다.
특이사항으로는 T2의 tid가 더 작은(먼저 생성된) 값임을 확인할 수 있다. 이 역시 T2가 먼저 시작되었지만 순서가 보장되지 않았음을 의미한다.

3. T2는 post 레코드에 x-lock을 걸려고 기다린다.
```text
index PRIMARY of table `bdt`.`post`
trx id 479983 lock_mode X locks rec but not gap **waiting**
```
하지만, t1이 post의 s-lock을 선점한 상태이기 때문에 waiting한다.
(만약 s-lock을 걸려 했다면 s-lock은 공유가 가능하므로 기다리지 않는다. 하지만, x-lock을 걸려고하며 이는 t1이 s-lock을 쥐고있을 때 불가하다)

4. T1은 post_like 레코드에 s-lock을 걸려고 기다린다.
```text
index UKpmmko3h7yonaqhy5gxvnmdeue of table `bdt`.`post_like`
trx id 479984 lock mode S **waiting**
```
하지만, t2가 post_like x-lock을 선점한 상태이기 때문에 waiting한다.
(t2가 x-lock을 쥐고있을 때 t1이 s-lock을 거는 것 또한 불가하다)

=> 두 트랜잭션이 서로를 무한정 기다리며 데드락이 발생한다.

</details>
<details>
<summary>만약, 유니크 조건이 없었다면?</summary>
현재 서비스 로직은 Check-Then-Act 패턴을 취하고있다. (if exists, then return)
현재 실험은 유니크 조건이 걸려있기 때문에 데이터는 1개만 insert 되었다. (데드락이 발생할 지언정)
만약, 유니크 조건이 없었다면 서버의 exist 체크로는 db-write가 발생하는 시점의 정확한 스냅샷을 제공하지 못한다.
exists=false인 짧은 시간안에 요청이 모두 db로 접근하게되고, 그만큼의 중복 데이터가 생겨 일관성이 무너진다.
</details>
<details>
<summary>InnoDB 로그 상세</summary>

```text
-- Query: SHOW ENGINE INNODB STATUS;
-- Pretty Printed
ENGINE: InnoDB

=====================================
2025-12-18 18:27:12 0x3ccc
INNODB MONITOR OUTPUT
=====================================
Per second averages calculated from the last 32 seconds


-----------------
BACKGROUND THREAD
-----------------
srv_master_thread loops:
  26 srv_active
   0 srv_shutdown
1863 srv_idle

srv_master_thread log flush and writes: 0


----------
SEMAPHORES
----------
OS WAIT ARRAY INFO:
  reservation count 1081
  signal count      1054

RW-shared spins 0, rounds 0, OS waits 0
RW-excl   spins 0, rounds 0, OS waits 0
RW-sx     spins 0, rounds 0, OS waits 0

Spin rounds per wait:
  0.00 RW-shared
  0.00 RW-excl
  0.00 RW-sx


------------------------
LATEST DETECTED DEADLOCK
------------------------
2025-12-18 18:09:15 0x3d7c


*** (1) TRANSACTION:
TRANSACTION 479984, ACTIVE 30 sec inserting
mysql tables in use 1, locked 1
LOCK WAIT 4 lock struct(s), heap size 1128, 2 row lock(s), undo log entries 1
MySQL thread id 62, OS thread handle 17320, query id 51157
localhost 127.0.0.1 sh update

SQL:
insert into post_like
(created_at, post_id, updated_at, user_id, id)
values
('2025-12-18 18:09:15.35154', 1, '2025-12-18 18:09:15.35154', 1, 2)


*** (1) HOLDS THE LOCK(S):
RECORD LOCKS space id 2625 page no 4 n bits 72
index PRIMARY of table `bdt`.`post`
trx id 479984 lock mode S locks rec but not gap

Record lock, heap no 2
PHYSICAL RECORD: n_fields 6; compact format; info bits 0
 0: len 8; hex 8000000000000001
 1: len 6; hex 0000000752ea
 2: len 7; hex 81000001070110
 3: len 4; hex 80000000
 4: len 8; hex 99b86522290336b7
 5: len 8; hex 99b86522290336b7


*** (1) WAITING FOR THIS LOCK TO BE GRANTED:
RECORD LOCKS space id 2629 page no 5 n bits 72
index UKpmmko3h7yonaqhy5gxvnmdeue of table `bdt`.`post_like`
trx id 479984 lock mode S waiting

Record lock, heap no 2
PHYSICAL RECORD: n_fields 3; compact format; info bits 0
 0: len 8; hex 8000000000000001
 1: len 8; hex 8000000000000001
 2: len 8; hex 8000000000000001


*** (2) TRANSACTION:
TRANSACTION 479983, ACTIVE 30 sec starting index read
mysql tables in use 1, locked 1
LOCK WAIT 6 lock struct(s), heap size 1128, 3 row lock(s), undo log entries 1
MySQL thread id 60, OS thread handle 17172, query id 51159
localhost 127.0.0.1 sh updating

SQL:
update post
set like_count = 1,
    updated_at = '2025-12-18 18:09:15.352537'
where id = 1


*** (2) HOLDS THE LOCK(S):
RECORD LOCKS space id 2629 page no 5 n bits 72
index UKpmmko3h7yonaqhy5gxvnmdeue of table `bdt`.`post_like`
trx id 479983 lock_mode X locks rec but not gap

Record lock, heap no 2
PHYSICAL RECORD: n_fields 3; compact format; info bits 0
 0: len 8; hex 8000000000000001
 1: len 8; hex 8000000000000001
 2: len 8; hex 8000000000000001


*** (2) WAITING FOR THIS LOCK TO BE GRANTED:
RECORD LOCKS space id 2625 page no 4 n bits 72
index PRIMARY of table `bdt`.`post`
trx id 479983 lock_mode X locks rec but not gap waiting

Record lock, heap no 2
PHYSICAL RECORD: n_fields 6; compact format; info bits 0
 0: len 8; hex 8000000000000001
 1: len 6; hex 0000000752ea
 2: len 7; hex 81000001070110
 3: len 4; hex 80000000
 4: len 8; hex 99b86522290336b7
 5: len 8; hex 99b86522290336b7


*** WE ROLL BACK TRANSACTION (1)


------------
TRANSACTIONS
------------
Trx id counter 480001
Purge done for trx's n:o < 479999 undo n:o < 0
state: running but idle
History list length 0


LIST OF TRANSACTIONS FOR EACH SESSION:
---TRANSACTION 283709979839384, not started
0 lock struct(s), heap size 1128, 0 row lock(s)

---TRANSACTION 283709979838608, not started
0 lock struct(s), heap size 1128, 0 row lock(s)

---TRANSACTION 283709979837832, not started
0 lock struct(s), heap size 1128, 0 row lock(s)

---TRANSACTION 283709979837056, not started
0 lock struct(s), heap size 1128, 0 row lock(s)

---TRANSACTION 283709979836280, not started
0 lock struct(s), heap size 1128, 0 row lock(s)

---TRANSACTION 283709979835504, not started
0 lock struct(s), heap size 1128, 0 row lock(s)

---TRANSACTION 283709979834728, not started
0 lock struct(s), heap size 1128, 0 row lock(s)

---TRANSACTION 283709979833952, not started
0 lock struct(s), heap size 1128, 0 row lock(s)

---TRANSACTION 283709979833176, not started
0 lock struct(s), heap size 1128, 0 row lock(s)

---TRANSACTION 283709979832400, not started
0 lock struct(s), heap size 1128, 0 row lock(s)

---TRANSACTION 283709979825416, not started
0 lock struct(s), heap size 1128, 0 row lock(s)

---TRANSACTION 283709979824640, not started
0 lock struct(s), heap size 1128, 0 row lock(s)

---TRANSACTION 283709979823864, not started
0 lock struct(s), heap size 1128, 0 row lock(s)

---TRANSACTION 283709979823088, not started
0 lock struct(s), heap size 1128, 0 row lock(s)


--------
FILE I/O
--------
I/O thread 0 state: wait Windows aio (insert buffer thread)
I/O thread 1 state: wait Windows aio (read thread)
I/O thread 2 state: wait Windows aio (read thread)
I/O thread 3 state: wait Windows aio (read thread)
I/O thread 4 state: wait Windows aio (read thread)
I/O thread 5 state: wait Windows aio (write thread)
I/O thread 6 state: wait Windows aio (write thread)
I/O thread 7 state: wait Windows aio (write thread)
I/O thread 8 state: wait Windows aio (write thread)

Pending normal aio reads:  [0, 0, 0, 0]
Pending normal aio writes: [0, 0, 0, 0]

Pending flushes:
  log: 0
  buffer pool: 0

OS file reads  : 1213
OS file writes : 2894
OS file fsyncs : 1403


-------------------------------------
INSERT BUFFER AND ADAPTIVE HASH INDEX
-------------------------------------
Ibuf: size 1, free list len 0, seg size 2, 0 merges

Hash table size 34679


---
LOG
---
Log sequence number          7927516281
Log buffer assigned up to    7927516281
Log buffer completed up to   7927516281
Log written up to            7927516281
Log flushed up to            7927516281
Last checkpoint at           7927516281


----------------------
BUFFER POOL AND MEMORY
----------------------
Buffer pool size        8191
Free buffers            6753
Database pages          1429
Old database pages       507
Modified db pages         0


--------------
ROW OPERATIONS
--------------
Number of rows inserted 62
Number of rows updated  10
Number of rows deleted   0
Number of rows read  40059


============================
END OF INNODB MONITOR OUTPUT
============================

```

</details>

---

## 장애 전파 과정

### 1. t1 <-> t2 race condition 발생, innoDB가 dead lock을 감지하여 t1 rollback

InnoDB 판단

```text
// db
*** WE ROLL BACK TRANSACTION (1)
```

- 순환 대기(deadlock)를 감지
- 비용이 낮다고 판단한 `Transaction 1`을 롤백

---

### 2. 연쇄 효과: 커넥션 풀 고갈
- t1의 희생(롤백)으로 인해 t2만 수행되며 데드락이 해제되었음
- 이후 트랜잭션은 pool을 점유한 채 대기하다가 T1 rollback 이후 정상 수행
- t3, t4.. 부터는 duplicate key 예외 발생 (정상적으로 unieque constraints를 적용받는 중)
- 이후 9991개의 트랜잭션이 처리되었음
```text
// server
java.sql.SQLIntegrityConstraintViolationException: Duplicate entry '1-1' for key 'post_like.UKpmmko3h7yonaqhy5gxvnmdeue' // 의도된 예외
```

### 3. 연쇄 효과: deadlock 처리 동안 지연되며 일부 트랜잭션은 타임아웃 발생
- 30초의 타임아웃 동안 connection pool 할당(waiting 상태 돌입)조차 받지 못한 트랜잭션은 요청 자체가 실패됨

```text
// server
java.sql.SQLTransientConnectionException:
HikariPool-1 - Connection is not available,
request timed out after 30002ms
(total=10, active=10, idle=0)

// db
LIST OF TRANSACTIONS FOR EACH SESSION:
---TRANSACTION 283709979839384, not started
0 lock struct(s), heap size 1128, 0 row lock(s)

---TRANSACTION 283709979838608, not started
0 lock struct(s), heap size 1128, 0 row lock(s)

...
```

## CLI 실행 결과

```text
Total Requests : 10000
Success        : 9991
Fail           : 9
Elapsed(ms)    : 49708
RPS            : 201
```

- 실패 요청 수는 **매 실행마다 달라짐** (데드락 처리 시간 + 커넥션 풀 상태에 따라 비결정적)
- 총 응답시간도 약 50초로 증가 (ch01의 ping 요청에서는 약 10초, db-write 비용을 감안하더라도 비대해짐)
---

  ## 인사이트
  언뜻 논리적으로는 문제없어 보이는 코드도, 같은 유저가 동시에 요청을 하는 비정상적인 경우로 문제가 발생했다. 응답 지연과 더불어 InnoDB 내부의 롤백 동작을 유발하여 예상치 못한 동작과 정합성 문제를 일으킬 수 있었다. 이를 막기위해 서버 로직에서 대안을 마련하는 것보다는 이전 레이어에서 트래픽 자체를 차단하고 정보를 수집하여 밴을하는 방법이 보편적이라고 한다. 그럼에도, 현재 코드 자체도 너무 단순한 형태를 띄고있다고 생각했다. 때문에 좀더 고도화하고 다시 공격하려 한다.
</details>
공격으로 DB 장애(deadlock) 유도에 성공합니다. 서버로 전파되는 과정을 관찰하고 공격 방지 체계의 당위성을 확립합니다.

---
</br>

Server

**[server](./server-java/)**

Attacker(CLI)

**[attacker](./attacker-java/cli/attack/)**