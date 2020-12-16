# -*- coding: utf-8 -*-

import requests
from lxml import etree
import datetime
import redis


# 连接redis
pool = redis.ConnectionPool(host="192.168.2.155", port=6379, password="", max_connections=1024,db=0)
con = redis.Redis(connection_pool=pool)

# 组装请求
url = 'http://top.baidu.com/buzz?b=1&c=513&fr=topbuzz_b1_c513'
headers = {
           "user-agent":"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.106 Safari/537.36",
           }
response = requests.get(url,headers=headers)
response.encoding = 'gbk'
date = datetime.datetime.now().strftime("%Y%m%d")

# 提取数据
selector = etree.HTML(response.text)
lists = selector.xpath("//table[@class='list-table']/tr")

i = 0
for video in lists:
    title = video.xpath("./td[@class='keyword']/a[@class='list-title']/text()")
    hot = video.xpath("./td[@class='last']/span/text()")
    link = video.xpath("./td[@class='keyword']/a[@class='list-title']/@href")

    if (len(title) != 0):

        # 写入redis
        i = i + 1
        mid = "title$$" + title[0] + '##' + "link$$" + link[0] + '##' + "hot$$" + hot[0]
        con.zadd('baidu_{}'.format(date), {mid: i})

        # 读取redis
        # title = con.zrange('36kr_20201211', 0, -1, desc=False)
        # print (title)
        # break
