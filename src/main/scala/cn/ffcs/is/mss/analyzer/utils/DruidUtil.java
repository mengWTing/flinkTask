package cn.ffcs.is.mss.analyzer.utils;

import cn.ffcs.is.mss.analyzer.utils.druid.DDataDriver;
import cn.ffcs.is.mss.analyzer.utils.druid.entity.Entity;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Auther chenwei
 * @Description 查询druid的工具类
 * @Date: Created in 2017/10/18 下午12:54
 * @Modified By
 */
public class DruidUtil implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(DruidUtil.class);
    private static final long serialVersionUID = -7211372708995494819L;

    /**
     * 时间格式
     */
    public static String timeFormat = null;
    /**
     * druid 数据开始时间
     */
    public static long dateStartTimeStamp = 0L;
    /**
     * broker集合
     */
    public static Set<Broker> druidHostPortSet = null;


    /**
     * @Auther chenwei
     * @Description 设置broker
     * @Date: Created in 2018/5/29 18:30
     */
    public static void setDruidHostPortSet(String line) {
        druidHostPortSet = new HashSet<>();
        String[] brokers = line.split("\\|", -1);
        for (String broker : brokers) {
            String[] value = broker.split(":", -1);
            try {
                druidHostPortSet.add(new Broker(value[0], Integer.parseInt(value[1])));
            } catch (Exception e) {
//                logger.error("A Exception occurred", e);
            }
        }
    }

    /**
     * @Auther chenwei
     * @Description 查询druid的方法
     * @Date: Created in 2017/10/19 下午6:43
     */
    public static List<Map<String, String>> query(Entity entity) {

        //获取查询的json串
        String queryJson = Entity.getQueryJson(entity);
        //返回结果查询结果
        return getQueryResult(entity, queryJson);
    }

    /**
     * @Auther chenwei
     * @Description 查询并解析结果
     * @Date: Created in 2017/10/19 下午6:43
     */
    private static List<Map<String, String>> getQueryResult(Entity entity, String queryJson) {

//        logger.info("query json is {}", queryJson);
        //查询druid数据
        String line = queryDeal(druidHostPortSet, queryJson);
//        logger.info("query result is {}", line);
        List<Map<String, String>> list = null;
        if (line.length() > 0) {
            list = Entity.parseQueryResult(entity, line);
        }
        return list;
    }

    /**
     * @Auther chenwei
     * @Description 根据查询串查询druid，使用map保存多个broker服务，可以在某个broker挂掉时也可以查询
     * @Date: Created in 2018/5/15 18:05
     */
    private static String queryDeal(Set<Broker> druidHostPortSet, String queryJson) {

        for (Broker broker : druidHostPortSet) {

            try {
                //获取druid连接
                DDataDriver driver = new DDataDriver(broker.host, broker.port);
//                logger.info("query broker {}:{}", broker.host, broker.port);
                //查询druid
                String line = driver.getBroker().fireQuery(queryJson);
                if (line != null) {
                    return line;
                }
            } catch (Exception e) {
//                logger.error("A Exception occurred", e);
            }

        }

//        logger.warn("Not configured druid broker");
        return "[]";
    }

    /**
     * 保存kafka broker的host和port
     */
    static class Broker {

        String host;
        int port;

        Broker(String host, int port) {
            this.host = host;
            this.port = port;
        }

        @Override
        public String toString() {
            return host + ":" + port;
        }

        @Override
        public int hashCode() {
            return this.toString().hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return toString().equals(obj.toString());
        }
    }

    public static String getTimeFormat() {
        return timeFormat;
    }

    public static void setTimeFormat(String timeFormat) {
        DruidUtil.timeFormat = timeFormat;
    }

    public static long getDateStartTimeStamp() {
        return dateStartTimeStamp;
    }

    public static void setDateStartTimeStamp(long dateStartTimeStamp) {
        DruidUtil.dateStartTimeStamp = dateStartTimeStamp;
    }

}
