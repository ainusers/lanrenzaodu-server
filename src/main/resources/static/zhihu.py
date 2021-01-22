# -*- coding: utf-8 -*-

import requests
from lxml import etree
import datetime
import redis
import sys
reload(sys)
sys.setdefaultencoding('utf8')

# 组装请求
url = 'https://www.zhihu.com/hot'
headers = {
            "user-agent":"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.106 Safari/537.36",
            "cookie":'_zap=844834c6-3f2e-4ea8-addb-076475a678b8; d_c0="AGDRQCkCQxGPTr5KQCAczC0xL91HPIJ5ZYk=|1589334636"; _ga=GA1.2.1936531613.1589334638; _xsrf=83bd2bf3-b4fc-4508-9e8e-fb95ed2b216c; Hm_lvt_98beee57fd2ef70ccdd5ca52b9740c49=1607492186,1607492276,1607497783,1607503071; capsion_ticket="2|1:0|10:1607503135|14:capsion_ticket|44:ZWE5MzY5M2NiZjFlNGI5ODk0ZTEzZDM0YjQ0MTkxODA=|2c24aa310a3a67756e6bb14d59bf0f25e0bbfc5a8d9c504d6e8d6c6040cb673d"; z_c0="2|1:0|10:1607503136|4:z_c0|92:Mi4xaGZOTEF3QUFBQUFBWU5GQUtRSkRFU1lBQUFCZ0FsVk5JTnU5WUFCYVB3SFpJaFFQYXJYQ010UFJINHJXVFFacUl3|cd209c196d36b90f0d37948d804ededfdf0e07610a0de7ab9f86a471e9d83490"; tst=h; tshl=; Hm_lpvt_98beee57fd2ef70ccdd5ca52b9740c49=1607503825; KLBRSID=4843ceb2c0de43091e0ff7c22eadca8c|1607503826|1607501787; SESSIONID=v0httXvo7oB2KgBAWmP4uJ6pImhA9agDiINea5y3gCW; JOID=U10RBki4HrtEd07Ib7w4JwBbSI17yi_SNgESvwGNdfh3OiuOL2-RbB9xS8hoday-Vuf0iI6iHkarSJBkrMnJr9M=; osd=VVwSAEO-H7hCfEjJbLozIQFYToZ9yyzUPQcTvAeGc_l0PCCILmyXZxlwSM5jc629UOzyiY2kFUCqS5ZvqsjKqdg='
           }
response = requests.get(url,headers=headers)
date = datetime.datetime.now().strftime("%Y%m%d")
times = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")

# 连接redis
pool = redis.ConnectionPool(host="192.168.2.144", port=6379, password="", max_connections=1024,db=0)
con = redis.Redis(connection_pool=pool)

# 提取数据
selector = etree.HTML(response.text)
lists = selector.xpath("//div[@class='HotList-list']/section[@class='HotItem']")
i = 0
for video in lists:
    title = video.xpath("./div[@class='HotItem-content']/a/@title")
    hot = video.xpath("./div[@class='HotItem-content']/div[@class='HotItem-metrics HotItem-metrics--bottom']/text()")
    link = video.xpath("./div[@class='HotItem-content']/a/@href")

    if (len(hot) == 0):
        hot = video.xpath("./div[@class='HotItem-content']/div[@class='HotItem-metrics']/text()")

    # 写入redis
    i = i + 1
    mid = "title$$" + title[0] + '##' + "link$$" + link[0] + '##' + "hot$$" + str(hot[0]).replace('热度','').replace(" ","")
    con.zadd('zhihu_{}'.format(date), {mid: i})

    # 读取redis
    # title = con.zrange('36kr_20201211', 0, -1, desc=False)
    # print (title)
    # break