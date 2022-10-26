package cn.ffcs.is.mss.analyzer.utils;

import cn.ffcs.is.mss.analyzer.utils.druid.DruidTable;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

/**
 * @Auther chenwei
 * @Description 时间操作工具类
 * @Date: Created in 2017/10/19 下午7:48
 * @Modified By
 */
public class TimeUtil {

    /**
     * 一秒有多少毫秒
     */
    public static final long SECOND_MILLISECOND = 1000;
    /**
     * 一分有多少毫秒
     */
    public static final long MINUTE_MILLISECOND = SECOND_MILLISECOND * 60;
    /**
     * 一小时有多少毫秒
     */
    public static final long HOUR_MILLISECOND = MINUTE_MILLISECOND * 60;
    /**
     * 一天有多少毫秒
     */
    public static final long DAY_MILLISECOND = HOUR_MILLISECOND * 24;


    /**
     * @param todayTimeStamp
     * @param prvTimeStamp
     * @return
     * @Auther chenwei
     * @Description 计算两个时间相差的天数
     * @Date: Created in 2018/2/6 10:30
     */
    public static int getBetweenDayNumber(long todayTimeStamp, long prvTimeStamp) {
        return (int) ((getDayStartTime(todayTimeStamp) - getDayStartTime(prvTimeStamp)) / (TimeUtil.DAY_MILLISECOND));
    }


    /**
     * @param druid_table
     * @return
     * @Auther chenwei
     * @Description 获取处理话单时使用的系统时间
     * @Date: Created in 2018/5/16 14:52
     */
    public static long getSystemTime(DruidTable druid_table) {

        long timestamp = System.currentTimeMillis();

        timestamp = timestamp - druid_table.tableDelayTime;

        return timestamp;

    }


    /**
     * @param druid_table
     * @return
     * @Auther chenwei
     * @Description 获取话单延迟时间
     * @Date: Created in 2017/12/8 19:44
     */
    public static long getFileDelayTime(DruidTable druid_table) {

        return druid_table.tableDelayTime;
    }

    /**
     * @param timeStamp
     * @return
     * @Auther chenwei
     * @Description 根据时间戳获取指定的时间格式
     * @Date: Created in 2018/1/9 11:18
     */
    public static String getTimeStr(long timeStamp) {
        return new DateTime(timeStamp, DateTimeZone.forID(DruidUtil.timeFormat)).toString();

    }


    /**
     * @param timeStamp
     * @param REGRESSION_POINT
     * @return
     * @Auther chenwei
     * @Description 计算回归查询开始时间的方法
     * @Date: Created in 2017/10/23 下午9:20
     */
    public static long getRegressionStartTime(long timeStamp, int REGRESSION_POINT) {


        long startTimeStamp = timeStamp - MINUTE_MILLISECOND * REGRESSION_POINT;
        return startTimeStamp / MINUTE_MILLISECOND * MINUTE_MILLISECOND;
    }


    /**
     * @param timeStamp
     * @return
     * @Auther chenwei
     * @Description 获取当天0点0分0秒0毫秒的时间戳
     * @Date: Created in 2017/10/19 下午7:48
     */
    public static long getDayStartTime(long timeStamp) {
        return ((timeStamp + TimeUtil.HOUR_MILLISECOND * 8) / TimeUtil.DAY_MILLISECOND * TimeUtil.DAY_MILLISECOND - TimeUtil.HOUR_MILLISECOND * 8);
    }


    /**
     * @param timeStamp
     * @return
     * @Auther chenwei
     * @Description 获取当小时0分0秒0毫秒的时间戳
     * @Date: Created in 2017/10/19 下午7:49
     */
    public static long getHourStartTime(long timeStamp) {
        return timeStamp / HOUR_MILLISECOND * HOUR_MILLISECOND;
    }

    /**
     * @param timeStamp
     * @return
     * @Auther chenwei
     * @Description 查询当分钟开始的时间戳
     * @Date: Created in 2017/11/1 下午6:11
     */
    public static long getMinuteStartTime(long timeStamp) {
        return timeStamp / MINUTE_MILLISECOND * MINUTE_MILLISECOND;
    }


    /**
     * @param timeStamp
     * @return
     * @Auther chenwei
     * @Description 根据时间获取index
     * @Date: Created in 2017/11/22 15:21
     */
    public static int getIndex(long timeStamp) {

        long indexTime = getMinuteStartTime(timeStamp) + MINUTE_MILLISECOND;

        long startTimeStamp = getDayStartTime(indexTime);

        long duringTime = indexTime - startTimeStamp;

        return (int) duringTime / 1000 / 60;
    }

    //获取当前日期为第几周
    //        val calendar = Calendar.getInstance
    //        calendar.setTimeInMillis(attackBehaviorModel.alertTime)
    //        (calendar.get(Calendar.WEEK_OF_YEAR) + "|" + attackBehaviorModel.userName, ArrayBuffer(attackBehaviorModel))


}
