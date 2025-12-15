

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

> 목적: 모든 스레드가 동일한 시작 시점에서 요청을 보내도록 보장
> 

---

### 5.2 burst = true (Burst Mode)

1. `threads` 개수만큼 플랫폼 스레드를 생성
2. 스레드 적재와 동시에 대기 없이 요청 전송
3. 각 스레드는 `requestsPerThread` 만큼 즉시 요청 수행
4. 전체 과정을 **10ms delay** 후 반복

> 특징:
> 
> - 요청이 매우 짧은 시간에 집중적으로 발생
> - 실제 공격 트래픽에 가까운 패턴

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

현재로써는 rate limiter의 당위성이 부족하기 때문에, 먼저 고비용 작업(network I/O, DB wrihte) API를 추가하고 장애 발생시 rate Limiter을 구성하는 방향으로 확장한다.
</details>