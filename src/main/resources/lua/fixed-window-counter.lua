local limit = tonumber(ARGV[1])
local window_ms = tonumber(ARGV[2])
local now = tonumber(ARGV[3])
local consume = tonumber(ARGV[4])

local window_start = now - (now % window_ms)
local key = KEYS[1] .. ":" .. window_start
local current = tonumber(redis.call("GET", key) or "0")

if current < limit then
    if consume == 1 then
        current = redis.call("INCR", key)
        redis.call("PEXPIRE", key, window_ms + 1000)
        return {1, math.max(limit - current, 0), 0}
    end

    return {1, math.max(limit - current - 1, 0), 0}
end

local retry_after_ms = window_ms - (now % window_ms)
if retry_after_ms < 0 then
    retry_after_ms = 0
end

return {0, 0, retry_after_ms}
