# -*- coding: utf-8 -*-

import redis

# 连接redis
pool = redis.ConnectionPool(host="192.168.2.144", port=6379, password="", max_connections=1024,db=0)
con = redis.Redis(connection_pool=pool)

# 格式化redis数据
con.flushall()