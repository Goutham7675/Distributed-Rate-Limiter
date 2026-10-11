local capacity = tonumber(ARGV[1])
local refill_tokens = tonumber(ARGV[2])
local interval_ms = tonumber(ARGV[3])
local now = tonumber(ARGV[4])
local requested = tonumber(ARGV[5])
local consume = tonumber(ARGV[6])

local data = redis.call("HMGET", KEYS[1], "tokens", "last_refill")
local tokens = tonumber(data[1])
local last_refill = tonumber(data[2])

if tokens == nil then
    tokens = capacity
end

if last_refill == nil then
    last_refill = now
end

if now > last_refill then
    local elapsed = now - last_refill
    local replenished = (elapsed * refill_tokens) / interval_ms
    tokens = math.min(capacity, tokens + replenished)
    last_refill = now
end

local allowed = 0
local remaining = math.floor(tokens)
local retry_after_ms = 0

if tokens >= requested then
    allowed = 1
    remaining = math.floor(tokens - requested)

    if consume == 1 then
        tokens = tokens - requested
        redis.call("HSET", KEYS[1], "tokens", tokens, "last_refill", last_refill)

        local ttl_ms = math.ceil((capacity / refill_tokens) * interval_ms * 2)
        if ttl_ms < interval_ms then
            ttl_ms = interval_ms
        end

        redis.call("PEXPIRE", KEYS[1], ttl_ms)
    end
else
    local deficit = requested - tokens
    retry_after_ms = math.ceil((deficit / refill_tokens) * interval_ms)
end

if retry_after_ms < 0 then
    retry_after_ms = 0
end

return {allowed, remaining, retry_after_ms}
