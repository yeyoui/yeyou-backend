---
--- Generated by teamSeckillOrder.lua
--- Created by lhy.
--- DateTime: 3/20/2023 1:20 PM
--- 完成秒杀并且将信息交给消息队列异步处理
---

--1.参数信息
--用户ID
local userId=ARGV[1];
--队伍ID
local teamId=ARGV[2];
-- 订单
local orderId=ARGV[3];

--返回信息
--0 异步下单成功
--1 库存不足
--2 用户重复下单

--2.数据KEY常量
--库存KEY
local stockKey="teamSeckill:stock:" .. teamId
--用户下单KEY
local orderKey="teamSeckill:order" .. teamId

--3.执行业务
--无库存信息
if(redis.call("exists",stockKey)==0) then
    return 1;
end
--3.1库存是否充足
if(tonumber(redis.call("get",stockKey))<=0) then
    -- 库存不足返回1
    return 1;
end
--3.2用户是否重复下单
if((redis.call("sismember",orderKey,userId))==1) then
    -- 重复下单返回2
    return 2;
end

--3.3库存-1
redis.call("incrby",stockKey,-1)
--3.4对应用户下订单
redis.call("sadd",orderKey,userId)
--3.5订单发送到消息队列
redis.call("xadd","stream.teamSeckillOrder","*","userId",userId,"teamId",teamId,"orderId",orderId)
--异步下单成功返回0
return 0;
