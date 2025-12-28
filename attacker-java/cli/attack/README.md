
### 배치파일 경로(for windows)
**[scripts](./scripts/)**

## CLI 옵션
```text
- `--scenario` (string)
  - 필수
  - 공격 방식 정의 (enum에 정의된 시나리오만 가능)

- `--url` (string)
  - 필수
  - 공격 대상 HTTP 엔드포인트  

- `--threads` (int)
  - default 1
  - 동시 실행 스레드 수

- `--rpt` (int)
  - default 1
  - 단일 스레드 요청 횟수 (RPT, Request Per Thread)

- `--burst` (boolean)
  - default false
  - false: 스레드가 지연 없이 시작
  - true: 모든 스레드가 동시에 시작(요청 간 10ms 지연 적용)
```

## CLI 예시
### Linux / WSL / Bash

```bash
java -jar build/libs/attack.jar \
  --scenario ping \
  --url http://localhost:8080/api/ping \
  --threads 10 \
  --rpt 1000 \
  --burst true
```

---

### Windows (PowerShell / CMD)

```bash
java -jar build/libs/attack.jar ^
  --scenario ping ^
  --url http://localhost:8080/api/ping ^
  --threads 10 ^
  --rpt 1000 ^
  --burst true
```

---

## 결과 출력 형식
```text
- Total Requests
- Success
- Fail
- Elapsed(ms)
- RPS (total × 1000 / elapsed)
```