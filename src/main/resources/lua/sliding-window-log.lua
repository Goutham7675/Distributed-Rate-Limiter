local limit = tonumber(ARGV[1])
local window_ms = tonumber(ARGV[2])
local now = tonumber(ARGV[3])
local member = ARGV[4]
local consume = tonumber(ARGV[5])

redis.call("ZREMRANGEBYSCORE", KEYS[1], 0, now - window_ms)
local current = tonumber(redis.call("ZCARD", KEYS[1]))

if current < limit then
    if consume == 1 then
        redis.call("ZADD", KEYS[1], now, member)
        redis.call("PEXPIRE", KEYS[1], window_ms * 2)
    end

    local remaining = limit - current - 1
    if remaining < 0 then
        remaining = 0
    end

    return {1, remaining, 0}
end

local oldest = redis.call("ZRANGE", KEYS[1], 0, 0, "WITHSCORES")
local retry_after_ms = window_ms

if oldest[2] then
    retry_after_ms = window_ms - (now - tonumber(oldest[2]))
end

if retry_after_ms < 0 then
    retry_after_ms = 0
end

return {0, 0, retry_after_ms}
