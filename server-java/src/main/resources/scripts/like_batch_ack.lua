-- [[ KEYS ]]
-- KEYS[1]: post-like:batch:snapshot:{batchSeq}
-- KEYS[2]: post-like:batch:status:{batchSeq}

-- 스냅샷 데이터 삭제
redis.call('DEL', KEYS[1])

-- 상태를 SUCCESS로 전이시키고 10분간 유지
redis.call('SET', KEYS[2], 'SUCCESS')
redis.call('EXPIRE', KEYS[2], 600)

return true
