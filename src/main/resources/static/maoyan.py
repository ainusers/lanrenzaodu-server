# -*- coding: utf-8 -*-

import requests
import redis
import datetime
import sys
reload(sys)
sys.setdefaultencoding('utf8')

# 连接redis
pool = redis.ConnectionPool(host="192.168.2.155", port=6379, password="", max_connections=1024,db=0)
con = redis.Redis(connection_pool=pool)

url = 'http://piaofang.maoyan.com/dashboard-ajax/movie?orderType=0&uuid=1764023371ac8-00ca742d431e3b-930346c-1fa400-1764023371ac8&riskLevel=71&optimusCode=10&_token=eJyNj08LgkAQxb%2FLnBfd1fVPCx6EIAw6JNYlPGxqq4Su6CJF9N0bSQ%2FdgoH35sfjMfOCISlBUAJTNYAAZlHLBwJmBMF8Gnh%2BELp%2BwDmB4peFbkjgOpy3IC5s41DiUZbPJEXwJYyGNCer5%2BgdjjOnEgxBbUwvbLtvpL7JTlmt1E%2FZWYVu7VKO9VXLobRbPTUVXvRPGLC6zbAa9b6oXNSs%2BwHfxL6xUR26av%2FITiqJ452K02MUwfsD271OBw%3D%3D'
headers = {
           "user-agent":"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.106 Safari/537.36",
           }
response = requests.get(url=url, headers=headers).json()

date = datetime.datetime.now().strftime("%Y%m%d")

i = 0
for video in response['movieList']['list']:
    title = video['movieInfo']['movieName']
    online = video['movieInfo']['releaseInfo']
    boxCount = video['showCount']
    boxPer = video['splitBoxRate']

    if('' == online):
        online = '未公布'

    # 写入redis
    i = i + 1
    mid = "title$$" + title + '##' + "online$$" + online + '##' + "count$$" + str(boxCount) + '##' + "per$$" + boxPer
    con.zadd('maoyan_{}'.format(date), {mid: i})

    # 读取redis
    # title = con.zrange('36kr_20201211', 0, -1, desc=False)
    # print (title)
    # break