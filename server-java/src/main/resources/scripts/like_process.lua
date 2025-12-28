-- KEYS[1]: 유저별 상태 Hash (post:like:status:{postId})
-- KEYS[2]: 포스트별 카운트 Hash (post:like:count)
-- KEYS[3]: 변경사항이 있는 postId
-- ARGV[1]: userId
-- ARGV[2]: newStatus (0 or 1)
-- ARGV[3]: postId

local oldStatus = tonumber(redis.call('HGET', KEYS[1], ARGV[1]) or "0")
local newStatus = tonumber(ARGV[2])

-- 1. XOR 연산으로 변화(diff) 감지 (같으면 0, 다르면 1)
local diff = bit.bxor(oldStatus, newStatus)

-- 2. diff가 0이면 상태 변화 없음 (충돌)
if diff == 0 then
    return -1
end

-- 3. diff가 1일 때만 실행.
-- delta 계산: newStatus가 1이면 +1, 0이면 -1
local delta = (newStatus * 2) - 1

redis.call('HSET', KEYS[1], ARGV[1], newStatus)
redis.call('HINCRBY', KEYS[2], ARGV[3], delta)
redis.call('SADD', KEYS[3], ARGV[3])

return newStatus
