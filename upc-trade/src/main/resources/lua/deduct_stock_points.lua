-- KEYS[1]: flash:stock:{saleId}
-- KEYS[2]: user:points:{userId}
-- KEYS[3]: flash:user:{userId}:{saleId}  （可选：防重复）
-- ARGV[1]: requiredPoints （商品所需积分）
-- ARGV[2]: stockDeduct    （通常为 1）
-- ARGV[3]: ttlSeconds     （防重 key 过期时间，如 3600）

-- 1. 检查是否已抢过（可选）
if redis.call('EXISTS', KEYS[3]) == 1 then
    return -2  -- 已参与，禁止重复
end

-- 2. 检查商品库存
local stock = redis.call('GET', KEYS[1])
if not stock or tonumber(stock) < tonumber(ARGV[2]) then
    return 0  -- 库存不足
end

-- 3. 检查用户积分
local points = redis.call('GET', KEYS[2])
if not points or tonumber(points) < tonumber(ARGV[1]) then
    return -1 -- 积分不足
end

-- 4. 扣减库存
redis.call('DECRBY', KEYS[1], ARGV[2])

-- 5. 扣减积分
redis.call('DECRBY', KEYS[2], ARGV[1])

-- 6. 标记已参与（可选）
redis.call('SET', KEYS[3], '1', 'EX', ARGV[3])

return 1  -- 成功