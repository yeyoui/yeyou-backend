---
--- Generated by Luanalysis
--- Created by lhy.
--- DateTime: 3/20/2023 2:34 PM
---

--redis.call("xadd","stream.teamSeckillOrder","*","userId","default","teamId","default","orderId","default")
redis.call("XGROUP","CREATE","stream.teamSeckillOrder","TeamSeckillC1","$","MKSTREAM")

return 1;

