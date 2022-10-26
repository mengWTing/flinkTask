package cn.ffcs.is.mss.analyzer.utils.druid.entity;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import org.json.JSONObject;

/**
 * @Auther chenwei
 * @Description
 * @Date: Created in 2017/10/19 下午7:08
 * @Modified By
 */
public enum Aggregation implements Serializable {

    //连接次数
    connCount(Metrics.connCount,Metrics.connCount, AggregationType.longSum),
    //上行流量
    inputOctets(Metrics.inputOctets,Metrics.inputOctets, AggregationType.longSum),
    //总流量
    octets(Metrics.octets,Metrics.octets, AggregationType.longSum),
    //下行流量
    outputOctets(Metrics.outputOctets,Metrics.outputOctets, AggregationType.longSum),
    //上行包数
    inputPackets(Metrics.inputPackets,Metrics.inputPackets, AggregationType.longSum),
    //总包数
    packets(Metrics.packets,Metrics.packets, AggregationType.longSum),
    //下行包数
    outputPackets(Metrics.outputPackets,Metrics.outputPackets, AggregationType.longSum),
    //用户数
    userNameCount(Dimension.userName,Dimension.userName, AggregationType.thetaSketch),
    //源ip数
    sourceIpCount(Dimension.sourceIp,Dimension.sourceIp, AggregationType.thetaSketch),
    //源port数
    sourcePortCount(Dimension.sourcePort,Dimension.sourcePort, AggregationType.thetaSketch),
    //目的ip数
    destinationIpCount(Dimension.destinationIp,Dimension.destinationIp, AggregationType.thetaSketch),
    // TODO:  对String类型是数据的聚合   //下载文件名集合
//    downLoadFileName(Dimension.downFileName,Dimension.downFileName, AggregationType.stringAny,1024),
    //目的port数
    destinationPortCount(Dimension.destinationPort,Dimension.destinationPort, AggregationType.thetaSketch);

    private String name;
    private String fieldName;
    private String aggregationType;
//    private Integer maxStringBytes;

    /**
     * @Auther chenwei
     * @Description 根据聚合列，聚合列名，参数类型设置name，fieldName，aggregationType属性
     * @Date: Created in 2018/2/6 10:56
     * @param name
     * @param fieldName
     * @param aggregationType
     */
    Aggregation(Dimension name,Dimension fieldName,AggregationType aggregationType){

        this.aggregationType = aggregationType.toString();
        if (aggregationType == AggregationType.thetaSketch) {
            this.name = name + "Count";
        }else {
            this.name = name.toString();
        }
        this.fieldName = fieldName.toString();
    }

    /**
     * @Auther chenwei
     * @Description 根据指标列，指标列名，参数类型设置name，fieldName，aggregationType属性
     * @Date: Created in 2018/2/6 10:57
     * @param name
     * @param fieldName
     * @param aggregationType
     */
    Aggregation(Metrics name,Metrics fieldName,AggregationType aggregationType){

        this.aggregationType = aggregationType.toString();
        if (aggregationType == AggregationType.thetaSketch) {
            this.name = name + "Count";
        }else {
            this.name = name.toString();
        }
        this.fieldName = fieldName.toString();
    }

    // TODO:  对String类型是数据的聚合
//    Aggregation(Dimension name, Dimension fieldName, AggregationType aggregationType, Integer maxStringBytes) {
//
//        this.aggregationType = aggregationType.toString();
//        if (aggregationType == AggregationType.thetaSketch) {
//            this.name = name + "Count";
//        } else if (aggregationType == AggregationType.longSum) {
//
//            this.name = name.toString();
//        } else {
//            this.name = name.toString();
//        }
//        this.fieldName = fieldName.toString();
//        this.maxStringBytes = maxStringBytes;
//    }


    /**
     * @Auther chenwei
     * @Description 根据Aggregation对象获取aggregation的Json串
     * @Date: Created in 2018/2/6 10:58
     * @param aggregation
     * @return
     */
    public static JSONObject getAggregation(Aggregation aggregation){

        JSONObject aggregationJsonObject = new JSONObject();

        aggregationJsonObject.put("name", aggregation.name);
        aggregationJsonObject.put("fieldName", aggregation.fieldName);
        aggregationJsonObject.put("type", aggregation.aggregationType);

        return aggregationJsonObject;
    }

    @Override
    public String toString() {

        return name;
    }


    /**
     * @Auther chenwei
     * @Description 获取AggregationsSet
     * @Date: Created in 2018/5/29 17:31
     * @param aggregations
     * @return
     */
    public static Set<JSONObject> getAggregationsSet(Aggregation ... aggregations){

        Set<JSONObject> aggregationsSet = new HashSet<>();
        for (Aggregation aggregation : aggregations){
            aggregationsSet.add(getAggregation(aggregation));

        }

        return aggregationsSet;

    }

    public enum AggregationType{
        //计算去重后的个数
        thetaSketch,
        //计算加起来的个数
        longSum;
//        TODO:  对String类型是数据的聚合
//        //计算字符串的总和
//        stringAny

        @Override
        public String toString() {
            return super.toString();
        }
    }
}
