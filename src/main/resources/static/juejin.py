# -*- coding: utf-8 -*-

import requests
import datetime
import redis

# 连接redis
pool = redis.ConnectionPool(host="192.168.2.144", port=6379, password="", max_connections=1024,db=0)
con = redis.Redis(connection_pool=pool)

url = 'https://api.juejin.cn/recommend_api/v1/article/recommend_all_feed'
headers = {
           "user-agent":"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.106 Safari/537.36",
           }
data = {"id_type": 2, "client_type": 2608, "sort_type": 3, "cursor": "0", "limit": 20}
response = requests.request("post", url,json=data, headers=headers).json()

date = datetime.datetime.now().strftime("%Y%m%d")
i = 0
for video in response['data']:
    if ('article_info' in video['item_info']):
        title = video['item_info']['article_info']['title']                         # 标题
        hot = video['item_info']['article_info']['digg_count']                      # 热度
        article_id = video['item_info']['article_info']['article_id']               # 文章ID
        link = 'https://juejin.cn/post/{}'.format(article_id)                       # 文章url

        # 写入redis
        i = i + 1
        mid = "title$$" + title + '##' + "link$$" + link + '##' + "hot$$" + str(hot)
        con.zadd('juejin_{}'.format(date), {mid: i})

        # 读取redis
        # title = con.zrange('36kr_20201211', 0, -1, desc=False)
        # print (title)
        # break