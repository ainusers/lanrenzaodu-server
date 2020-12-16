# -*- coding: utf-8 -*-

import requests
import datetime
import redis


# 连接redis
pool = redis.ConnectionPool(host="192.168.2.155", port=6379, password="", max_connections=1024,db=0)
con = redis.Redis(connection_pool=pool)

url = 'https://www.acfun.cn/rest/pc-direct/rank/channel?channelId=&subChannelId=&rankLimit=30&rankPeriod=DAY'
headers = {
           "user-agent":"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.106 Safari/537.36",
           }
response = requests.get(url,headers=headers).json()

date = datetime.datetime.now().strftime("%Y%m%d")

i = 0
for video in response['rankList']:
    title = video['title']                          # 标题
    hot = video['viewCount']                        # 热度
    link = video['picShareUrl']                     # 文章url

    # 写入redis
    i = i + 1
    mid = "title$$" + title + '##' + "link$$" + link + '##' + "hot$$" + str(hot)
    con.zadd('azhan_{}'.format(date), {mid: i})

    # 读取redis
    # title = con.zrange('36kr_20201211', 0, -1, desc=False)
    # print (title)
    # break