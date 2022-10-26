package cn.ffcs.is.mss.analyzer.utils.druid.entity;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * @Auther chenwei
 * @Description
 * @Date: Created in 2017/10/19 下午7:03
 * @Modified By
 */
public enum Dimension implements Serializable {

    //常用登录地
    usedPlace,
    //是否包含Sql
    containSql,
    //目的ip
    destinationIp,
    //目的port
    destinationPort,
    //host
    host,
    //http状态
    httpStatus,
    //是否异地登录
    isRemote,
    //登录地
    loginPlace,
    //登录系统
    loginSystem,
    //操作
    operate,
    //协议
    protocol,
    //源ip
    sourceIp,
    //源port
    sourcePort,
    //url
    url,
    //用户名
    userName,
    //协议2
    protocol2,
    //协议
    state,
    //源Mac
    sourceMac,
    //目的Mac
    destinationMac,
    //响应码
    responseCode,
    //查询的域名
    queryDomainName,
    //查询结果
    queryResult,
    //DNS响应码
    replyCode,
    //Answer rrs=0为解析错误
    answerRrs,
    //协议ID
    protocolId,
    //VLAN号，第一层vlan
    vlanId,
    //采集分段标识(探针号)
    probeId,
    //连接是否成功
    isSucceed,
    //是否是下载
    isDownload,
    //下载文件名
    downFileName,
    //下载是否成功
    isDownSuccess,
    //解回包类型
    packageType,
    //解回包数据信息
    packageValue,
    //告警库目的ip
    alertDestIp,
    //风险类型
    alertName;


    /**
     * @Auther chenwei
     * @Description 获取DimensionsSet
     * @Date: Created in 2018/5/29 17:29
     * @param dimensions
     * @return
     */
    public static Set<String> getDimensionsSet(Dimension ... dimensions){
        Set<String> dimensionsSet = new HashSet<>();
        for (Dimension dimension: dimensions){
            dimensionsSet.add(dimension.toString());

        }
        return dimensionsSet;
    }


}
