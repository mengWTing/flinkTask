package cn.ffcs.is.mss.analyzer.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.URI;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Auther chenwei
 * @Description
 * @Date: Created in 2018/4/19 15:08
 * @Modified By
 */
public class DateUtil implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(DateUtil.class);
    private static final long serialVersionUID = -6578092815623714119L;
    private static Map<String, Integer> dateHashMap;
    private static Map<Long, Integer> dateTreeMap;

    /**
     * 根据配置文件路径初始化
     */
    public static void ini(String fileSystemType, String path) {
        dateHashMap = new HashMap<>();
        dateTreeMap = new HashMap<>();
        setDate(fileSystemType, path);
    }

    /**
     * 根据日期信息初始化
     */
    public static void ini(Map<String, Integer> hashMap) {

        dateHashMap = hashMap;

        dateTreeMap = new TreeMap<>(Comparator.naturalOrder());

        for (Map.Entry<String, Integer> entry : dateHashMap.entrySet()) {
            String[] values = entry.getKey().split("-", -1);
            if (values.length == 3) {
                dateTreeMap
                    .put(new DateTime(Integer.parseInt(values[0]), Integer.parseInt(values[1]),
                        Integer.parseInt(values[2]), 0, 0, 0)
                        .getMillis(), entry.getValue());
            }
        }

    }

    public static int getFlag(String date) {
        return dateHashMap.get(date);
    }

    public static int getFlag(long timeStamp) {

        String key = new DateTime(timeStamp, DateTimeZone.forID("+08:00")).toString()
            .substring(0, 10);

        return dateHashMap.getOrDefault(key, -1);
    }

    public static Map<Long, Integer> getDateTreeMap() {
        return dateTreeMap;
    }


    private static void setDate(String fileSystemType, String path) {

        FileSystem fileSystem = null;
        FSDataInputStream fsDataInputStream = null;
        BufferedReader bufferedReader = null;
        try {

            fileSystem = FileSystem.get(URI.create(fileSystemType), new Configuration());
            fsDataInputStream = fileSystem.open(new Path(path));
            bufferedReader = new BufferedReader(new InputStreamReader(fsDataInputStream));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] values = line.split("\\|", -1);
                if (values.length == 2) {

                    int dateType = Integer.parseInt(values[1]);
                    long timestamp = dateStrToTimestamp(values[0]);
                    if (timestamp > 0) {
                        dateHashMap.put(values[0], dateType);
                        dateTreeMap.put(timestamp, dateType);
                    }
                }
            }
        } catch (IOException e) {
            logger.error("A Exception occurred", e);
        } finally {

            if (bufferedReader != null) {

                try {
                    // bufferedReader.close();
                } catch (Exception e) {
                    logger.error("A Exception occurred", e);
                }
            }

            if (fsDataInputStream != null) {

                try {
                    // fsDataInputStream.close();
                } catch (Exception e) {
                    logger.error("A Exception occurred", e);
                }
            }

            if (fileSystem != null) {

                try {
                    // fileSystem.close();
                } catch (Exception e) {
                    logger.error("A Exception occurred", e);
                }
            }

        }

    }

    /**
     * 将date字符串转化为时间戳
     */
    private static long dateStrToTimestamp(String dateStr) {
        String[] values = dateStr.split("-", -1);
        long timestamp = 0;
        try {
            timestamp = new DateTime(Integer.parseInt(values[0]), Integer.parseInt(values[1]),
                Integer.parseInt(values[2]), 0, 0, 0).getMillis();
        } catch (Exception e) {
            logger.error("dateStr is {}", dateStr);
            logger.error("A Exception occurred", e);
        }

        return timestamp;
    }
}