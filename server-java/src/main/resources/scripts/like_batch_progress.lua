-- [[ KEYS ]]
-- KEYS[1]: post-like:batch:meta:last-time
-- KEYS[2]: post-like:batch:meta:last-seq
-- KEYS[3]: post-like:batch:queue

-- [[ ARGV ]]
-- ARGV[1]: server_time (currentTimeMillis)
-- ARGV[2]: batch_size

local redis_last_time = tonumber(redis.call('GET', KEYS[1]) or 0)
local redis_last_seq = tonumber(redis.call('GET', KEYS[2]) or 0)
local server_time = tonumber(ARGV[1])
local batch_size = tonumber(ARGV[2])

local resolved_time = server_time

if resolved_time < redis_last_time then
    -- 시간 역전 발생 시 레디스 시각을 기준으로 하고   seq 1 증가
    resolved_time = redis_last_time
    redis_last_seq = (redis_last_seq + 1) % 1048576
    if redis_last_seq == 0 then resolved_time = resolved_time + 1 end
    -- 시간 충돌 발생 시 seq 1 증가
elseif resolved_time == redis_last_time then
    redis_last_seq = (redis_last_seq + 1) % 1048576
    if redis_last_seq == 0 then resolved_time = resolved_time + 1 end
else
    -- 문제가 없다면 seq 0부터 시작
    redis_last_seq = 0
end

-- 메타 데이터(last_time, last_seq) 레디스에 저장
-- 지수 표기법 방지를 위해 포맷팅하여 저장
local resolved_time_str = string.format("%.0f", resolved_time)
redis.call('SET', KEYS[1], resolved_time_str)
redis.call('SET', KEYS[2], redis_last_seq)

-- 산술 연산 (resolved_time * 1048576)은 2^53을 넘어 정밀도가 파괴되므로 16진수 결합 사용
-- 44비트 시간(11자리) + 20비트 seq(5자리) = 64비트 ID (16진수 문자열)
local batch_seq = string.format("%011x%05x", resolved_time, redis_last_seq)
local snapshot_key = "post-like:batch:snapshot:" .. batch_seq
local status_key = "post-like:batch:status:" .. batch_seq

local snapshot_len = 0

-- 원자적 Bulk Pop 및 스냅샷 저장
-- 단일 post_id 에서 pairs(user_id, status)를 최대한 뽑음
-- 현재 post_id에서 need 만큼 뽑을 수 있다면 need만큼 뽑음
-- 현재 post_id에서 need 만큼 뽑을 수 없다면 post_id에서 다 뽑고 다음 post_id 탐색
-- post_id 목록이 비었다면 리턴
while snapshot_len < batch_size do
    local post_id = redis.call('SPOP', KEYS[3])
    if not post_id then break end

    local hash_key = "post-like:realtime:status:" .. post_id
    local need = batch_size - snapshot_len

    local hscan_res = redis.call('HSCAN', hash_key, 0, 'COUNT', need)
    local pairs = hscan_res[2]

    if #pairs > 0 then
        local move_items = {}
        for i = 1, #pairs, 2 do
            local user_id = pairs[i]
            local status = pairs[i+1]
            -- Lua Table에 데이터를 임시 적재
            table.insert(move_items, post_id .. ":" .. user_id .. ":" .. status)
            -- 원본 삭제 (Pop)
            redis.call('HDEL', hash_key, user_id)
        end

        -- unpack을 사용하여 move_items 테이블의 모든 요소를 가변 인자로 전달
        -- redis.call('SADD', snapshot_key, 'item1', 'item2', ...) 와 동일한 효과
        redis.call('SADD', snapshot_key, unpack(move_items))
        snapshot_len = snapshot_len + (#pairs / 2)
    end

    -- 해당 해시에 데이터가 남아있을 경우 대기열 재등록, 비었을 경우 명시적 삭제
    if redis.call('HLEN', hash_key) > 0 then
        redis.call('SADD', KEYS[3], post_id)
    else
        redis.call('DEL', hash_key)
    end
end

-- 청크 구성에 성공했으므로 배치 대기 중으로 설정
if snapshot_len > 0 then
    redis.call('SET', status_key, "PENDING")
    -- 사고 대비 만료 시간 설정
    redis.call('EXPIRE', snapshot_key, 3600)
end

-- 최종 반환: 서버는 이 batch_seq를 받아 실제 데이터를 SMEMBERS로 읽어감
return {batch_seq, snapshot_len}
