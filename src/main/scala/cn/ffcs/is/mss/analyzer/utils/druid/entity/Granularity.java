package cn.ffcs.is.mss.analyzer.utils.druid.entity;

import java.io.Serializable;
import org.json.JSONObject;

/**
 * @Auther chenwei
 * @Description
 * @Date: Created in 2018/1/8 15:08
 * @Modified By
 */
public enum Granularity implements Serializable {

    //全部聚合
    all,
    //不聚合
    none,
    //1秒
    second,
    //1分钟
    minute,
    //1刻钟
    fifteen_minute,
    //3刻钟
    thirty_minute,
    //1小时
    hour,
    //1天
    day,
    //1周
    week,
    //1月
    month,
    //1季度
    quarter,
    //1年
    year,
    //ISO-8601时间格式下的秒
    periodSecond(Period.SECOND,TimeZoneType.AsiaShanghai,"period"),
    //ISO-8601时间格式下的分钟
    periodMinute(Period.MINUTE,TimeZoneType.AsiaShanghai,"period"),
    //ISO-8601时间格式下的小时
    periodHour(Period.HOUR,TimeZoneType.AsiaShanghai,"period"),
    //ISO-8601时间格式下的天
    periodDay(Period.DAY,TimeZoneType.AsiaShanghai,"period"),
    //ISO-8601时间格式下的周
    periodWeek(Period.WEEK,TimeZoneType.AsiaShanghai,"period"),
    //ISO-8601时间格式下的月
    periodMonth(Period.MONTH,TimeZoneType.AsiaShanghai,"period"),
    //ISO-8601时间格式下的年
    periodYear(Period.YEAR,TimeZoneType.AsiaShanghai,"period");

    private Period period;
    private TimeZoneType timeZoneType;
    private String typeName;
    Granularity(){

    }

    Granularity(Period period,TimeZoneType timeZoneType,String typeName){
        this.period = period;
        this.timeZoneType = timeZoneType;
        this.typeName = typeName;
    }



    @Override
    public String toString() {
        return super.toString();
    }

    /**
     * @Auther chenwei
     * @Description 设置聚合粒度
     * @Date: Created in 2018/2/6 11:30
     * @param granularity
     * @return
     */
    public static JSONObject getGranularity(Granularity granularity){

        return getGranularity(granularity,null, TimeZoneType.AsiaShanghai);

    }

    /**
     * @Auther chenwei
     * @Description 设置聚合粒度
     * @Date: Created in 2018/2/6 11:30
     * @param granularity
     * @param num
     * @return
     */
    public static JSONObject getGranularity(Granularity granularity,Integer num){

        return getGranularity(granularity,num, TimeZoneType.AsiaShanghai);

    }

    /**
     * @Auther chenwei
     * @Description 设置聚合粒度
     * @Date: Created in 2018/2/6 11:30
     * @param granularity
     * @param num
     * @param timeZoneType
     * @return
     */
    public static JSONObject getGranularity(Granularity granularity,Integer num,TimeZoneType timeZoneType){

        JSONObject granularityJsonObject = new JSONObject();

        if (granularity.timeZoneType!= null && granularity.period != null && granularity.typeName != null){
            granularityJsonObject.put("timeZone", timeZoneType.timeZone);
            granularityJsonObject.put("period", Period.getPeriod(granularity.period,num));
            granularityJsonObject.put("type", granularity.typeName);
        }else {
            granularityJsonObject.put("type", granularity.toString());
        }
        return granularityJsonObject;

    }


    /**
     * 时区
     */
    public enum TimeZoneType{

        AsiaShanghai("Asia/Shanghai");

        public final String timeZone;

        TimeZoneType(String timeZone){
            this.timeZone = timeZone;
        }
    }

    public enum Period {
        //秒
        SECOND("PT","S"),
        //分钟
        MINUTE("PT","M"),
        //小时
        HOUR("PT","H"),
        //天
        DAY("P","D"),
        //周
        WEEK("P","W"),
        //月
        MONTH("P","M"),
        //年
        YEAR("P","Y");

        private String type;
        private String period;


        Period(String type,String period){
            this.type = type;
            this.period = period;
        }

        public static String getPeriod(Period period,int number){

            return period.type + number + period.period;

        }


    }

}
