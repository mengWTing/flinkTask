package cn.ffcs.is.mss.analyzer.utils.druid.entity;

import java.io.Serializable;

/**
 * @Auther chenwei
 * @Description
 * @Date: Created in 2018/1/8 11:27
 * @Modified By
 */
public enum Metrics implements Serializable {
    //上行包数
    inputPackets,
    //下行包数
    outputPackets,
    //总包数
    packets,
    //上行流量
    inputOctets,
    //下行流量
    outputOctets,
    //总流量
    octets,
    //连接次数
    connCount;

    @Override
    public String toString() {
        return super.toString();
    }
}
