-- [[ KEYS ]]
-- KEYS[1]: post-like:realtime:status:{postId}
-- KEYS[2]: post-like:batch:queue

-- [[ ARGV ]]
-- ARGV[1]: user_id
-- ARGV[2]: new_status (0 or 1)
-- ARGV[3]: postId

local old_status = tonumber(redis.call('HGET', KEYS[1], ARGV[1]) or "0")
local new_status = tonumber(ARGV[2])

local diff = bit.bxor(old_status, new_status)

-- 충돌 발생 시 리턴
if diff == 0 then
    return -1
end

-- 정상적인 경우 유저 상태 갱신
redis.call('HSET', KEYS[1], ARGV[1], new_status)
-- 변경이 일어난 post_id 큐에 적재 (향후 배치에서 변경이 일어난 post_id 추적을 위함)
redis.call('SADD', KEYS[2], ARGV[3])

return new_status
