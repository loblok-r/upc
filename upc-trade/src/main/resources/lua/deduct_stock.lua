-- KEYS[1] = 库存 key (e.g., "flash:stock:123")
-- ARGV[1] = 用户ID (可选，用于防重)
-- ARGV[2] = 活动ID (可选)

local stock = tonumber(redis.call('GET', KEYS[1]))
if stock == nil or stock <= 0 then
    return 0  -- 库存不足或未初始化
end

-- 扣减库存
redis.call('DECR', KEYS[1])
return stock - 1