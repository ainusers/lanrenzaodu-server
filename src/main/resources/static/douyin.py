# -*- coding: utf-8 -*-

import requests
import datetime
import redis


# 连接redis
pool = redis.ConnectionPool(host="192.168.2.155", port=6379, password="", max_connections=1024,db=0)
con = redis.Redis(connection_pool=pool)

url = 'https://creator.douyin.com/aweme/v1/creator/data/billboard/?billboard_type=1'
headers = {
           "user-agent":"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.106 Safari/537.36",
           "referer": "https://creator.douyin.com/billboard/hot_aweme",
           }
response = requests.get(url=url, headers=headers).json()

date = datetime.datetime.now().strftime("%Y%m%d")
i = 0
for video in response['billboard_data']:
    link = video['extra_list'][0]['link']          # 分享页链接
    title = video['title']                         # 视频标题
    hot = video['value']                           # 当前热度

    # 写入redis
    i = i + 1
    mid = "title$$" + title + '##' + "link$$" + link + '##' + "hot$$" +hot
    con.zadd('douyin_{}'.format(date), {mid: i})

    # 读取redis
    # title = con.zrange('36kr_20201211', 0, -1, desc=False)
    # print (title)
    # break