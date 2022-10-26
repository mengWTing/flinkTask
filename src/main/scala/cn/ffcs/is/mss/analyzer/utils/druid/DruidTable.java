package cn.ffcs.is.mss.analyzer.utils.druid;

import cn.ffcs.is.mss.analyzer.utils.Constants;

import java.io.Serializable;
import java.util.Map;

/**
 * @Auther chenwei
 * @Description
 * @Date: Created in 2018/5/16 13:55
 * @Modified By
 */
public enum DruidTable implements Serializable {

    ARP(Constants.DRUID_ARP_TABLE_NAME, Constants.ARP_FILE_DELAY_TIME),
    DNS(Constants.DRUID_DNS_TABLE_NAME, Constants.DNS_FILE_DELAY_TIME),
    OPERATION(Constants.DRUID_OPERATION_TABLE_NAME, Constants.OPERATION_FILE_DELAY_TIME),
    QUINTET(Constants.DRUID_QUINTET_TABLE_NAME, Constants.QUINTET_FILE_DELAY_TIME),
    MAIL(Constants.DRUID_MAIL_TABLE_NAME, Constants.DRUID_MAIL_DELAY_TIME);


    public String tableNameKey;
    public String tableName;
    public String tableDelayKey;
    public long tableDelayTime;

    DruidTable(String tableNameKey, String tableDelayKey) {
        this.tableNameKey = tableNameKey;
        this.tableDelayKey = tableDelayKey;
    }


    /**
     * @param druidConfigMap
     * @Auther chenwei
     * @Description 设置表名
     * @Date: Created in 2018/5/16 14:04
     */
    public static void ini(Map<String, Object> druidConfigMap) {

        for (DruidTable druidTableName : DruidTable.values()) {
            druidTableName.tableName = druidConfigMap.getOrDefault(druidTableName.tableNameKey, new Object()).toString();
        }

        for (DruidTable druidTableName : DruidTable.values()) {
            druidTableName.tableDelayTime = Long.parseLong(druidConfigMap.getOrDefault(druidTableName.tableDelayKey, new Object()).toString());
        }

    }


}
