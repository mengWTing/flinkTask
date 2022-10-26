#话单类型
1. 业务话单
>
|编号|字段名|字段说明|最大长度|备注|
|:---:|:---:|:---:|:---:|:---:|
|1|user_name|用户名|变长|取值为form item里的username或是cookie里的loginName|
|2|DestinationIP|用户访问的目标ipv4或ipv6地址|变长||
|3|DestinationPort|用户访问的目标端口号|变长||
|4|sourceIP|用户访问目标时，使用的ipv4或ipv6|变长||
|5|sourcePort|用户访问目标时使用的端口号|变长||
|6|Host|域名|变长||
|7|URL|用户访问的目标网站的URL|变长||
|8|cmd/act（先空着）|用户具体的操作|变长|由URL进行判断，还未解析|
|9|refere|链接源信息|变长||
|10|Use-Agent|Use Agent信息|变长||
|11|request.arrivaltime|每个请求包的时间点|变长||
|12|acctinputoctets|发给用户的业务字节数|变长||
|13|acctoutputoctets|用户发出的业务字节数|变长||
|14|httpstatus|http状态码|变长||
|15|last.Response.arrivaltime|网页最后1个Response消息时间点|变长|对应业务感知的Lastpacktime|
|16|first.request.arrivaltime|网页第1个GET请求时间点网页|变长|对应业务感知的ActionTime|
|17|Content-Length/Chunk Size|网页大小|变长|对应业务感知的ContentLen|
|18|first.Response.arrivaltime|终端收到目标服务器响应的第一个数据响应包（第一个respone包）的时间点|变长|对应业务感知的FirstResptime|
|19|[SYN].arrivaltime|终端向网页发起连接请求的时间点|变长|对应业务感知的SYNtime|
|20|[ACK].arrivaltime|TCP三次握手的[ACK]的时间点|变长|对应业务感知的ACKtime|
|21|[FIN.ACk].arrivaltime|终端向服务器端发送[FIN.ACk]的时间点|变长|对应业务感知的LastAcktime|
|22|[DNS.Request].arrivaltime|DNS查询请求时间点|变长|对应业务感知的DNS的QueryTime|
|23|[DNS.response].arrivaltime|DNS查询响应时间点|变长|对应业务感知的DNS的ResponseTime|
|24|protocol|协议类型	|变长||
|25|Sql|URL或者form item是否包含sql语句	|Int|是为1否为0|
|26|isDownload|是否是下载行为|Int|（1-是、0-否)|
|27|IsDownSuccess|是否下载成功|Int|（1-成功、0-失败）|
|28|downFileSize|下载的文件大小|变长||
|29|downFilename|下载的文件名|变长||
|30|访问类型：get，post|访问类型：get，post|int|1:get 0:post
|31|form value|form值，get请求为空|变长|

2. 五元组话单
>
|编号|字段名|字段说明|最大长度|备注|
|:---:|:---:|:---:|:---:|:---:|
|1|sourceIP|源IP|||
|2|sourcePort|源端口|||
|3|sourceMac|源MAC|||
|4|destinationIP|目的IP|||
|5|destinationPort|目的PORT|||
|6|destinationMac|目的MAC|||
|7|protocolId|协议类型|||
|8|vlanId|VLAN号，第一层vlan|||
|9|probeId|采集分段标识(探针号)|||
|10|inputOctets|流入字节数|||
|11|outputOctets|流出字节数|||
|12|inputPacket|流入包数|||
|13|outputPacket|流出包数|||
|14|inputRetransPacket|流入重传包数|||
|15|outputRetransPacket|流出重传包数|||
|16|inputRetransDelay|流入重传时延(总时延)-ms|||
|17|outputRetransDelay|流出重传时延(总时延)-ms|||
|18|inputRest|流入RESET数量|||
|19|outputRest|流出RESET数量|||
|20|isSucceed|连接是否成功（0-成功、1-失败、2-分割）|||
|21|inputSmallPacket|流入小包数：根据配置规定判断小包字节数|||
|22|outputSmallPacket|流出小包数：根据配置规定判断小包字节数|||
|23|inputMediumPacket|流入中包数：根据配置规定判断中包字节数|||
|24|outputMediumPacket|流出中包数：根据配置规定判断中包字节数|||
|25|inputLargePacket|流入大包数：根据配置规定判断大包字节数|||
|26|outputLargePacket|流出大包数；根据配置规定判断大包字节数|||
|27|inputZeroWindow|流入零窗口数|||
|28|outputZeroWindow|流出零窗口数|||
|29|startTime|开始时间戳-ms|||
|30|finishTime|结束时间戳-ms|||
|31|synTime|SYN时间戳-ms|||
|32|synAckTime|SYN ACK时间戳-ms|||
|33|ackTime|ACK时间戳-ms|||
|34|ttl|Time to live|||
|35|tcpOrUdp|TCP协议或UDP协议（1：TCP、0：UDP）|||

3. Arp话单
>
|编号|字段名|字段说明|最大长度|备注|
|:---:|:---:|:---:|:---:|:---:|
|1|StartTime|时间||毫秒ms|
|2|源mac||||
|3|源ip||||
|4|目的mac||||
|5|目的ip||||
|6|响应情况|||0超时,1正常|
|7|响应时间|||超时时为0|
|8|writetime|话单解析时间

4. Dns话单
>
|编号|字段名|字段说明|最大长度|备注|
|:---:|:---:|:---:|:---:|:---:|
|1|QueryDomainName|请求查询的DNS域名|64byte||
|2|QueryResult|DNS的解析结果|可变长|取查询响应中的解析结果，对于存在多个解析结果的情况，使用分号分隔|
|3|ReplyCode|DNS响应码|1byte||
|4|QueryTime|请求时间戳|8byte|UTC时间，从1970/1/1 00:00:00开始到当前的毫秒数。|
|5|ResponseTime|响应时间戳|8byte|UTC时间，从1970/1/1 00:00:00开始到当前的毫秒数。|
|6|RequestNumber|DNS的请求次数|1 byte|同一条DNS请求次数重传次数|
|7|ResponseNumber|响应数目|1 byte|DNS响应包中的资源记录数，指请求查询的结果|
|8|Answer rrs|Answer rrs=0为解析错误|||
|9|writetime|话单解析时间|||
