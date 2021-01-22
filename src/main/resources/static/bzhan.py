# -*- coding: utf-8 -*-

import requests
from lxml import etree
import datetime
import redis


# 连接redis
pool = redis.ConnectionPool(host="192.168.2.144", port=6379, password="", max_connections=1024,db=0)
con = redis.Redis(connection_pool=pool)

# 组装请求
url = 'https://www.bilibili.com/v/popular/rank/all'
headers = {
           "user-agent":"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.106 Safari/537.36",
           }
response = requests.get(url,headers=headers)
date = datetime.datetime.now().strftime("%Y%m%d")

# 提取数据
selector = etree.HTML(response.text)
lists = selector.xpath("//div[@class='rank-list-wrap']/ul[@class='rank-list']/li")
i = 0
for video in lists:
    title = video.xpath("./div[@class='content']/div[@class='info']/a/text()")
    hot = video.xpath("./div[@class='content']/div[@class='info']/div[@class='pts']/div/text()")
    uri = video.xpath("./div[@class='content']/div[@class='info']/a/@href")
    link = 'https:{}'.format(uri[0])

    # 写入redis
    i = i + 1
    mid = "title$$" + title[0] + '##' + "link$$" + link + '##' + "hot$$" + hot[0]
    con.zadd('bzhan_{}'.format(date), {mid: i})

    # 读取redis
    # title = con.zrange('36kr_20201211', 0, -1, desc=False)
    # print (title)
    # break