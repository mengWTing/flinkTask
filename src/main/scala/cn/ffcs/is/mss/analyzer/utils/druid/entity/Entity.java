package cn.ffcs.is.mss.analyzer.utils.druid.entity;

import cn.ffcs.is.mss.analyzer.utils.druid.DruidTable;
import cn.ffcs.is.mss.analyzer.utils.druid.entity.Aggregation.AggregationType;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

/**
 * @Auther chenwei
 * @Description 查询druid业务话单的类
 * @Date: Created in 2017/10/19 上午12:18
 * @Modified By
 */
public class Entity implements Serializable {

//    private static final Logger logger = LoggerFactory.getLogger(Entity.class);
    public static final String timestamp = "timestamp";
    //having语句
    private JSONObject having = null;
    //限制条件
    private JSONObject limitSpec = null;
    //过滤条件
    private JSONObject filter = null;
    //聚合列
    private Set<String> dimensionsSet = null;
    //聚合指标
    private Set<JSONObject> aggregationsSet = null;
    //表名
    private String tableName;
    //聚合粒度
    private JSONObject granularity;
    //查询开始时间
    private String[] startTimeStr;
    //查询结束时间
    private String[] endTimeStr;

    public Entity(){

    }

    /**
     * @Auther chenwei
     * @Description 获取过滤条件
     * @Date: Created in 2018/1/8 14:20
     * @return
     */
    public JSONObject getFilter() {
        return filter;
    }

    /**
     * @Auther chenwei
     * @Description 设置过滤条件
     * @Date: Created in 2018/1/8 14:20
     * @param filter
     */
    public void setFilter(JSONObject filter) {
        this.filter = filter;
    }

    /**
     * @Auther chenwei
     * @Description 获取聚合条件
     * @Date: Created in 2018/1/8 14:20
     * @return
     */
    public Set<String> getDimensionsSet() {
        return dimensionsSet;
    }

    /**
     * @Auther chenwei
     * @Description 设置聚合条件
     * @Date: Created in 2018/1/8 14:20
     * @param dimensionsSet
     */
    public void setDimensionsSet(Set<String> dimensionsSet) {
        this.dimensionsSet = dimensionsSet;
    }

    /**
     * @Auther chenwei
     * @Description 获取聚合指标
     * @Date: Created in 2018/1/8 14:20
     * @return
     */
    public Set<JSONObject> getAggregationsSet() {
        return aggregationsSet;
    }

    /**
     * @Auther chenwei
     * @Description 设置聚合指标
     * @Date: Created in 2018/1/8 14:20
     * @param aggregationsSet
     */
    public void setAggregationsSet(Set<JSONObject> aggregationsSet) {
        this.aggregationsSet = aggregationsSet;
    }

    /**
     * @Auther chenwei
     * @Description 获取表名
     * @Date: Created in 2018/1/8 14:20
     * @return
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * @Auther chenwei
     * @Description 设置表名
     * @Date: Created in 2018/1/8 14:20
     * @param tableName
     */
    public void setTableName(DruidTable tableName) {
        this.tableName = tableName.tableName;
    }

    /**
     * @Auther chenwei
     * @Description 设置表名
     * @Date: Created in 2018/1/8 14:20
     * @param tableName
     */
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    /**
     * @Auther chenwei
     * @Description 获取having条件
     * @Date: Created in 2018/1/8 14:20
     * @return
     */
    public JSONObject getHaving() {
        return having;
    }

    /**
     * @Auther chenwei
     * @Description 设置having条件
     * @Date: Created in 2018/1/8 14:20
     * @param having
     */
    public void setHaving(JSONObject having) {
        this.having = having;
    }

    /**
     * @Auther chenwei
     * @Description 获取限制条件
     * @Date: Created in 2018/1/8 14:20
     * @return
     */
    public JSONObject getLimitSpec() {
        return limitSpec;
    }

    /**
     * @Auther chenwei
     * @Description 设置限制条件
     * @Date: Created in 2018/1/8 14:20
     * @param limitSpec
     */
    public void setLimitSpec(JSONObject limitSpec) {
        this.limitSpec = limitSpec;
    }

    /**
     * @Auther chenwei
     * @Description 获取聚合粒度
     * @Date: Created in 2018/1/8 14:20
     * @return
     */
    public JSONObject getGranularity(){
        return granularity;
    }

    /**
     * @Auther chenwei
     * @Description 设置聚合粒度
     * @Date: Created in 2018/1/8 14:20
     * @param granularity
     */
    public void setGranularity(JSONObject granularity){
        this.granularity = granularity;
    }

    /**
     * @Auther chenwei
     * @Description 获取查询开始时间
     * @Date: Created in 2018/1/8 14:20
     * @return
     */
    public String[] getStartTimeStr(){
        return startTimeStr;
    }

    /**
     * @Auther chenwei
     * @Description 设置查询开始时间
     * @Date: Created in 2018/1/8 14:20
     * @param startTimeStrs
     */
    public void setStartTimeStr(String[] startTimeStrs){
        this.startTimeStr = startTimeStrs;
    }

    /**
     * @Auther chenwei
     * @Description 设置查询开始时间
     * @Date: Created in 2018/1/8 14:20
     * @param startTimeStr
     */
    public void setStartTimeStr(String startTimeStr){
        this.startTimeStr = transform(startTimeStr);
    }

    /**
     * @Auther chenwei
     * @Description 设置查询开始时间
     * @Date: Created in 2018/1/8 14:20
     * @param timeStamp
     */
    public void setStartTimeStr(long timeStamp){
        this.startTimeStr = transform(timeStamp);
    }

    /**
     * @Auther chenwei
     * @Description 获取查询结束时间
     * @Date: Created in 2018/1/8 14:20
     * @return
     */
    public String[] getEndTimeStr(){
        return endTimeStr;
    }

    /**
     * @Auther chenwei
     * @Description 设置查询结束时间
     * @Date: Created in 2018/1/8 14:20
     * @param endTimeStrs
     */
    public void setEndTimeStr(String[] endTimeStrs){
        this.endTimeStr = endTimeStrs;
    }

    /**
     * @Auther chenwei
     * @Description 设置查询结束时间
     * @Date: Created in 2018/1/8 14:20
     * @param endTimeStr
     */
    public void setEndTimeStr(String endTimeStr){
        this.endTimeStr = transform(endTimeStr);
    }

    /**
     * @Auther chenwei
     * @Description 设置查询结束时间
     * @Date: Created in 2018/1/8 14:20
     * @param timeStamp
     */
    public void setEndTimeStr(long timeStamp){
        this.endTimeStr = transform(timeStamp);
    }

    /**
     * @Auther chenwei
     * @Description 获取查询druidjson串的方法
     * @Date: Created in 2017/10/19 下午6:43
     * @param entity
     * @return
     */
    public static String getQueryJson(Entity entity){
        return entity.toString();
    }

    /**
     * @Auther chenwei
     * @Description 解析jsonArray的方法
     * @Date: Created in 2017/10/23 下午3:16
     * @param entity
     * @param queryResult
     * @return
     */
    public static List<Map<String,String>> parseQueryResult(Entity entity, String queryResult){

        JSONArray jsonArray = new JSONArray(queryResult);

        //新建list保存结果
        List<Map<String,String>> list = new ArrayList<>(jsonArray.length());

        try {
            for (int i = 0; i < jsonArray.length(); i++) {

                //新建map保存结果
                Map<String, String> map = new HashMap<>();

                //获取jsonObject
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                //获取时间戳
                Long timeStamp = new DateTime(jsonObject.getString(timestamp), DateTimeZone.forID("+08:00")).getMillis();
                map.put(timestamp, timeStamp.toString());

                //获取eventJsonObject
                JSONObject eventJsonObject = jsonObject.getJSONObject("event");

                //遍历dimensionsSet，获取对应的维度值
                Set<String> dimensionsSet = entity.getDimensionsSet();
                for (String dimension : dimensionsSet) {

                    String value = eventJsonObject.get(dimension).toString();

                        map.put(dimension, value);



                }

                //遍历aggregationsSet,获取对应的指标值
                Set<JSONObject> aggregationsSet = entity.getAggregationsSet();
                for (JSONObject aggregation : aggregationsSet) {
                    //获取指标名
                    String name = aggregation.getString("name");
                    //判断返回类型
                    AggregationType aggregationType = AggregationType
                        .valueOf(aggregation.getString("type"));

                    switch (aggregationType) {
                        case longSum: {
                            Long value = eventJsonObject.getLong(name);
                            map.put(name, value.toString());
                            break;
                        }
                        case thetaSketch: {
                            Double value = eventJsonObject.getDouble(name);
                            map.put(name, value.toString());
                            break;
                        }
//                        case stringAny: {
//                            String value = eventJsonObject.getString(name);
//                            map.put(name, value);
//                            break;
//                        }

                        default: {
                            break;
                        }
                    }


                }

                list.add(map);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public String toString() {
        //json对象
        JSONObject jsonObject = new JSONObject();
        try {
            //查询类型
            jsonObject.put("queryType", "groupBy");
            //表名
            jsonObject.put("dataSource", tableName);

            //过滤条件
            if (filter != null) {
                jsonObject.put("filter", filter);
            }

            //按值过滤条件
            if (having != null) {
                jsonObject.put("having", having);
            }

            //TopN
            if (limitSpec != null) {
                jsonObject.put("limitSpec", limitSpec);
            }

            jsonObject.put("granularity", granularity);

            //聚合列
            JSONArray dimensionsJsonArray = new JSONArray();
            for (String dimension : dimensionsSet) {

                dimensionsJsonArray.put(dimension);
            }
            jsonObject.put("dimensions", dimensionsJsonArray);

            //查询指标
            JSONArray aggregationsJsonArray = new JSONArray();
            for (JSONObject aggregationJsonogject : aggregationsSet) {
                aggregationsJsonArray.put(aggregationJsonogject);
            }
            jsonObject.put("aggregations", aggregationsJsonArray);

            //时间范围
            JSONArray intervalsJSONArray = new JSONArray();
            for (int i = 0; i < startTimeStr.length; i++) {
                if ((startTimeStr[i] != null && startTimeStr[i].length() > 0) && (
                    endTimeStr[i] != null
                        && endTimeStr[i].length() > 0)) {

                    intervalsJSONArray.put(startTimeStr[i] + "/" + endTimeStr[i]);
                    jsonObject.put("intervals", intervalsJSONArray);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }


    /**
     * @Auther chenwei
     * @Description 根据时间戳获取查询时间
     * @Date: Created in 2018/6/1 10:08
     * @param timeStamp
     * @return
     */
    public static String getQueryTimeStr(long timeStamp){

        return new DateTime(timeStamp, DateTimeZone.forID("+08:00")).toString();
    }

    /**
     * @Auther chenwei
     * @Description 将查询的时间转换成字符串数组
     * @Date: Created in 2018/1/8 11:20
     * @param timeStamp
     * @return
     */
    public static String[] transform(long timeStamp){

        return transform(new DateTime(timeStamp, DateTimeZone.forID("+08:00")).toString());
    }

    /**
     * @Auther chenwei
     * @Description 将查询的时间从字符串转换成字符串数组
     * @Date: Created in 2018/1/8 11:20
     * @param string
     * @return
     */
    public static String[] transform(String string){

        return new String[]{string};
    }
}
