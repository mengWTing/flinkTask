package cn.ffcs.is.mss.analyzer.utils;

import java.io.Serializable;

/**
 * @Auther chenwei
 * @Description
 * @Date: Created in 2017/10/18 下午11:13
 * @Modified By
 */
public class Constants implements Serializable {
    private static final long serialVersionUID = -3772904329037147357L;

    //----------------------------------------------------------------------------------------------
    //配置文件名分组
    /**
     * Druid的配置
     */
    public static final String DRUID_CONFIG = "DRUID_CONFIG";
    /**
     * 定时任务的配置
     */
    public static final String QUARTZ_CONFIG = "QUARTZ_CONFIG";
    /**
     * HDFS的配置
     */
    public static final String HDFS_CONFIG = "HDFS_CONFIG";

    //----------------------------------------------------------------------------------------------
    //Druid配置
    /**
     * Druid的host
     */
    public static final String DRUID_HOST_PORT = "DRUID_HOST_PORT";
    /**
     * Druid的MSS表名
     */
    public static final String DRUID_OPERATION_TABLE_NAME = "DRUID_OPERATION_TABLE_NAME";
    /**
     * Druid的五元组表名
     */
    public static final String DRUID_QUINTET_TABLE_NAME = "DRUID_QUINTET_TABLE_NAME";
    /**
     * Druid的ARP表名
     */
    public static final String DRUID_ARP_TABLE_NAME = "DRUID_ARP_TABLE_NAME";
    /**
     * Druid的DNS表名
     */
    public static final String DRUID_DNS_TABLE_NAME = "DRUID_DNS_TABLE_NAME";
    /**
     * Druid的MAIL表名
     */
    public static final String DRUID_MAIL_TABLE_NAME = "DRUID_MAIL_TABLE_NAME";


    /**
     * Druid时间戳格式
     */
    public static final String DRUID_TIME_FORMAT = "DRUID_TIME_FORMAT";
    /**
     * Druid数据开始的时间
     */
    public static final String DRUID_DATA_START_TIMESTAMP = "DRUID_DATA_START_TIMESTAMP";
    /**
     * 业务话单延迟时间
     */
    public static final String OPERATION_FILE_DELAY_TIME = "OPERATION_FILE_DELAY_TIME";
    /**
     * 五元组话单延迟时间
     */
    public static final String QUINTET_FILE_DELAY_TIME = "QUINTET_FILE_DELAY_TIME";
    /**
     * ARP话单延迟时间
     */
    public static final String ARP_FILE_DELAY_TIME = "ARP_FILE_DELAY_TIME";
    /**
     * DNS话单延迟时间
     */
    public static final String DNS_FILE_DELAY_TIME = "DNS_FILE_DELAY_TIME";
    /**
     * MAIL话单延迟时间
     */
    public static final String DRUID_MAIL_DELAY_TIME = "DRUID_MAIL_DELAY_TIME";

    //----------------------------------------------------------------------------------------------
    //HDFS的配置
    /**
     * HDFS的路径
     */
    public static final String HDFS_BASIC_PATH = "HDFS_BASIC_PATH";

    //----------------------------------------------------------------------------------------------
    //定时任务启动表达式
    /**
     * 整体用户回归
     */
    public static final String QUARTZ_ALL_USER_REGRESSION_CRON = "ALL_USER_REGRESSION_CRON";
    /**
     * 单系统回归
     */
    public static final String QUARTZ_SINGLE_SYSTEM_REGRESSION_CRON = "SINGLE_SYSTEM_REGRESSION_CRON";
    /**
     * 系统IP的PORT回归
     */
    public static final String QUARTZ_IP_PORT_REGRESSION_CRON = "IP_PORT_REGRESSION_CRON";
    /**
     * 系统IP的PORT回归告警
     */
    public static final String QUARTZ_IP_PORT_WARN_CRON = "IP_PORT_WARN_CRON";
    /**
     * 同一时间多地登录告警
     */
    public static final String QUARTZ_SAME_TIME_DIFFERENT_PLACE_WARN_CRON = "SAME_TIME_DIFFERENT_PLACE_WARN_CRON";
    /**
     * 同一时间多地登录维护
     */
    public static final String QUARTZ_SAME_TIME_DIFFERENT_PLACE_MAINTENANCE_CRON = "SAME_TIME_DIFFERENT_PLACE_MAINTENANCE_CRON";
    /**
     * 地点系统聚类任务
     */
    public static final String QUARTZ_PLACE_SYSTEM_CLUSTER_CRON = "PLACE_SYSTEM_CLUSTER_CRON";
    /**
     * 登录地-IP维护任务
     */
    public static final String QUARTZ_PLACE_IP_MAINTENANCE_CRON = "PLACE_IP_MAINTENANCE_CRON";
    /**
     * 登录地-IP维护任务
     */
    public static final String QUARTZ_SYSTEM_HOST_MAINTENANCE_CRON = "SYSTEM_HOST_MAINTENANCE_CRON";
    /**
     * 常用登录系统
     */
    public static final String QUARTZ_USED_SYSTEM_WARN_CRON = "USED_SYSTEM_WARN_CRON";
    /**
     * 常用登录地
     */
    public static final String QUARTZ_USED_PLACE_WARN_CRON = "USED_PLACE_WARN_CRON";
    /**
     * 系统端口
     */
    public static final String QUARTZ_SYSTEM_PORT_WARN_CRON = "SYSTEM_PORT_WARN_CRON";
    /**
     * IP互访
     */
    public static final String QUARTZ_IP_VISIT_WARN_CRON = "IP_VISIT_WARN_CRON";
    /**
     * 向安管平台发送告警
     */
    public static final String QUARTZ_ALERT_STATISTICS_WARN_CRON = "ALERT_STATISTICS_WARN_CRON";

    //----------------------------------------------------------------------------------------------
    //各任务所使用的参数
    //----------------------------------------------------------------------------------------------
    /**
     * 整体用户回归
     */
    public static final String ALL_USER_REGRESSION_JOB = "ALL_USER_REGRESSION_JOB";
    /**
     * 整体用户回归的范围
     */
    public static final String ALL_USER_REGRESSION_POINT = "ALL_USER_REGRESSION_POINT";
    //----------------------------------------------------------------------------------------------
    /**
     * 单系统回归
     */
    public static final String SINGLE_SYSTEM_REGRESSION_JOB = "SINGLE_SYSTEM_REGRESSION_JOB";
    /**
     * 单系统回归的范围
     */
    public static final String SINGLE_SYSTEM_REGRESSION_POINT = "SINGLE_SYSTEM_REGRESSION_POINT";

    //----------------------------------------------------------------------------------------------
    /**
     * 系统IP的PORT回归
     */
    public static final String IP_PORT_REGRESSION_JOB = "IP_PORT_REGRESSION_JOB";
    /**
     * IPPORT回归的范围
     */
    public static final String IP_PORT_REGRESSION_POINT = "IP_PORT_REGRESSION_POINT";

    //----------------------------------------------------------------------------------------------
    /**
     * 系统IP的PORT回归告警
     */
    public static final String IP_PORT_WARN_JOB = "IP_PORT_WARN_JOB";
    //默认告警等级配置
    public static final String IP_PORT_WARN_JOB_DEFAULT_WARN_LEVEL_STR = "DEFAULT_WARN_LEVEL_STR";
    //默认最小链接次数
    public static final String IP_PORT_WARN_JOB_DEFAULT_IGNORE_CONNCOUNT = "DEFAULT_IGNORE_CONNCOUNT";


    //----------------------------------------------------------------------------------------------
    /**
     * 同一时间多地登录告警
     */
    public static final String SAME_TIME_DIFFERENT_PLACE_WARN_JOB = "SAME_TIME_DIFFERENT_PLACE_WARN_JOB";
    /**
     * 不同城市允许同时登录时间差
     */
    public static final String SAME_TIME_DIFFERENT_PLACE_WARN_JOB_DELTA_TIMESTAMP = "DELTA_TIMESTAMP";
    /**
     * 相同城市允许同时登录时间差
     */
    public static final String SAME_TIME_DIFFERENT_PLACE_WARN_JOB_CITY_DELTA_TIMESTAMP = "CITY_DELTA_TIMESTAMP";
    /**
     * 不产生告警的用户名
     */
    public static final String SAME_TIME_DIFFERENT_PLACE_WARN_JOB_WHITE_LIST_USERNAME = "WHITE_LIST_USERNAME";
    /**
     * 不产生告警的地点
     */
    public static final String SAME_TIME_DIFFERENT_PLACE_WARN_JOB_WHITE_LIST_PLACE = "WHITE_LIST_PLACE";

    //----------------------------------------------------------------------------------------------
    /**
     * 同一时间多地登录维护
     */
    public static final String SAME_TIME_DIFFERENT_PLACE_MAINTENANCE_JOB = "SAME_TIME_DIFFERENT_PLACE_MAINTENANCE_JOB";

    //----------------------------------------------------------------------------------------------
    /**
     * 地点系统聚类任务
     */
    public static final String PLACE_SYSTEM_CLUSTER_JOB = "PLACE_SYSTEM_CLUSTER_JOB";

    //----------------------------------------------------------------------------------------------
    /**
     * 登录地-IP维护
     */
    public static final String PLACE_IP_MAINTENANCE_JOB = "PLACE_IP_MAINTENANCE_JOB";
    /**
     * 登录地-IP文件在HDFS上的名字
     */
    public static final String PLACE_IP_FILE_NAME_HDFS = "PLACE_IP_FILE_NAME_HDFS";
    /**
     * 登录地-IP文件在HDFS上的路径
     */
    public static final String PLACE_IP_FILE_PATH_HDFS = "PLACE_IP_FILE_PATH_HDFS";

    //----------------------------------------------------------------------------------------------
    /**
     * 登录系统-HOST维护
     */
    public static final String SYSTEM_HOST_MAINTENANCE_JOB = "SYSTEM_HOST_MAINTENANCE_JOB";
    /**
     * 登录系统-HOST文件在HDFS上的名字
     */
    public static final String SYSTEM_HOST_FILE_NAME_HDFS = "SYSTEM_HOST_FILE_NAME_HDFS";
    /**
     * 登录系统-HOST文件在HDFS上的路径
     */
    public static final String SYSTEM_HOST_FILE_PATH_HDFS = "SYSTEM_HOST_FILE_PATH_HDFS";

    //----------------------------------------------------------------------------------------------
    /**
     * 不在常用登录系统
     */
    public static final String USED_SYSTEM_JOB = "USED_SYSTEM_JOB";
    /**
     * 不产生告警的用户名
     */
    public static final String USED_SYSTEM_JOB_WHITE_LIST_USERNAME = "WHITE_LIST_USERNAME";
    /**
     * 计算任务的线程数
     */
    public static final String USED_SYSTEM_JOB_CALCULATE_THREADS = "CALCULATE_THREADS";
    /**
     * 默认出告警的占比差值
     */
    public static final String USED_SYSTEM_JOB_DEFAULT_DELTA_PROPORTION = "DEFAULT_DELTA_PROPORTION";
    /**
     * 默认的迭代次数
     */
    public static final String USED_SYSTEM_JOB_DEFAULT_ISOLATION_FOREST_T = "DEFAULT_ISOLATION_FOREST_T";
    /**
     * 默认出告警的最小连接次数
     */
    public static final String USED_SYSTEM_JOB_DEFAULT_IGNORE_CONNCOUNT = "DEFAULT_IGNORE_CONNCOUNT";
    /**
     * 占比显示的格式
     */
    public static final String USED_SYSTEM_JOB_PROPORTION_DECIMAL_FORMAT = "PROPORTION_DECIMAL_FORMAT";

    //----------------------------------------------------------------------------------------------
    /**
     * 不在常用登录地
     */
    public static final String USED_PlACE_JOB = "USED_PlACE_JOB";

    //----------------------------------------------------------------------------------------------
    /**
     * 系统端口
     */
    public static final String SYSTEM_PORT_JOB = "SYSTEM_PORT_JOB";
    /**
     * 计算任务的线程数
     */
    public static final String SYSTEM_PORT_JOB_CALCULATE_THREADS = "CALCULATE_THREADS";

    //----------------------------------------------------------------------------------------------
    /**
     * IP互访
     */
    public static final String IP_VISIT_JOB = "IP_VISIT_JOB";
    public static final String IP_VISIT_JOB_INITIAL_CAPACITY = "IP_VISIT_JOB_INITIAL_CAPACITY";

    //----------------------------------------------------------------------------------------------
    /**
     * Flink通用配置
     */
    public static final String FLINK_COMMON_CONFIG = "FLINK_COMMON_CONFIG";
    /**
     * kafka的zookeeper地址
     */
    public static final String KAFKA_ZOOKEEPER_CONNECT = "KAFKA_ZOOKEEPER_CONNECT";
    /**
     * kafka的服务地址
     */
    public static final String KAFKA_BOOTSTRAP_SERVERS = "KAFKA_BOOTSTRAP_SERVERS";
    /**
     * tranquility的zookeeper地址
     */
    public static final String TRANQUILITY_ZK_CONNECT = "TRANQUILITY_ZK_CONNECT";
    /**
     * flink的group.id
     */
    public static final String GROUP_ID = "GROUP_ID";
    /**
     * druid的broker节点
     */
    public static final String DRUID_BROKER_HOST_PORT = "DRUID_BROKER_HOST_PORT";
    /**
     * 日期配置文件的路径
     */
    public static final String DATE_CONFIG_PATH = "DATE_CONFIG_PATH";
    /**
     * c3p0连接池配置文件路径
     */
    public static final String c3p0_CONFIG_PATH = "c3p0_CONFIG_PATH";
    /**
     * 使用的文件系统本地文件:{file:///} hdfs:{hdfs://hadoop01:9000}
     */
    public static final String FILE_SYSTEM_TYPE = "FILE_SYSTEM_TYPE";
    /**
     * redis连接池配置文件路径
     */
    public static final String REDIS_CONFIG_PATH = "REDIS_CONFIG_PATH";
    /**
     * package redis连接池配置文件路径
     */
    public static final String REDIS_PACKAGE_PROPERTIES = "REDIS_PACKAGE_PROPERTIES";
    //时间戳的格式化
    public static final String TIME_STAMP_FORMAT = "TIME_STAMP_FORMAT";
    //时间的格式化
    public static final String DATE_FORMAT = "DATE_FORMAT";

    //----------------------------------------------------------------------------------------------
    /**
     * 使用flink将ARP话单写入druid的配置
     */
    public static final String ARP_FLINK_TO_DRUID_CONFIG = "ARP_FLINK_TO_DRUID_CONFIG";
    /**
     * ARP话单的topic
     */
    public static final String ARP_TOPIC = "ARP_TOPIC";
    /**
     * 使用flink将ARP话单写入druid任务的job.name
     */
    public static final String ARP_JOB_NAME = "ARP_JOB_NAME";
    /**
     * checkpointing的间隔
     */
    public static final String ARP_CHECKPOINT_INTERVAL = "ARP_CHECKPOINT_INTERVAL";
    /**
     * 话单处理的并行度
     */
    public static final String ARP_DEAL_PARALLELISM = "ARP_DEAL_PARALLELISM";
    /**
     * kafka Source的名字
     */
    public static final String ARP_KAFKA_SOURCE_NAME = "ARP_KAFKA_SOURCE_NAME";
    /**
     * tranquility sink的名字
     */
    public static final String ARP_TRANQUILITY_SINK_NAME = "ARP_TRANQUILITY_SINK_NAME";
    /**
     * 写入kafka的topic
     */
    public static final String ARP_TO_KAFKA_TOPIC = "ARP_TO_KAFKA_TOPIC";
    /**
     * kakfa sink的名字
     */
    public static final String ARP_KAFKA_SINK_NAME = "ARP_KAFKA_SINK_NAME";

    //----------------------------------------------------------------------------------------------
    /**
     * 使用flink将DNS话单写入druid的配置
     */
    public static final String DNS_FLINK_TO_DRUID_CONFIG = "DNS_FLINK_TO_DRUID_CONFIG";
    /**
     * DNS话单的topic
     */
    public static final String DNS_TOPIC = "DNS_TOPIC";
    /**
     * 使用flink将ARP话单写入druid任务的job.name
     */
    public static final String DNS_JOB_NAME = "DNS_JOB_NAME";
    /**
     * checkpointing的间隔
     */
    public static final String DNS_CHECKPOINT_INTERVAL = "DNS_CHECKPOINT_INTERVAL";
    /**
     * 话单处理的并行度
     */
    public static final String DNS_DEAL_PARALLELISM = "DNS_DEAL_PARALLELISM";
    /**
     * kafka Source的名字
     */
    public static final String DNS_KAFKA_SOURCE_NAME = "DNS_KAFKA_SOURCE_NAME";
    /**
     * tranquility sink的名字
     */
    public static final String DNS_TRANQUILITY_SINK_NAME = "DNS_TRANQUILITY_SINK_NAME";
    /**
     * 写入kafka的topic
     */
    public static final String DNS_TO_KAFKA_TOPIC = "DNS_TO_KAFKA_TOPIC";
    /**
     * kakfa sink的名字
     */
    public static final String DNS_KAFKA_SINK_NAME = "DNS_KAFKA_SINK_NAME";

    //----------------------------------------------------------------------------------------------
    /**
     * 使用flink将业务话单写入druid的配置
     */
    public static final String OPERATION_FLINK_TO_DRUID_CONFIG = "OPERATION_FLINK_TO_DRUID_CONFIG";
    /**
     * ip-地点关联文件路径
     */
    public static final String OPERATION_PLACE_PATH = "OPERATION_PLACE_PATH";
    /**
     * host-系统名关联文件路径
     */
    public static final String OPERATION_SYSTEM_PATH = "OPERATION_SYSTEM_PATH";
    /**
     * 用户名-常用登录地关联文件路径
     */
    public static final String OPERATION_USEDPLACE_PATH = "OPERATION_USEDPLACE_PATH";
    /**
     * 业务话单的topic
     */
    public static final String OPERATION_TOPIC = "OPERATION_TOPIC";
    /**
     * 使用flink将ARP话单写入druid任务的job.name
     */
    public static final String OPERATION_JOB_NAME = "OPERATION_JOB_NAME";
    /**
     * checkpointing的间隔
     */
    public static final String OPERATION_CHECKPOINT_INTERVAL = "OPERATION_CHECKPOINT_INTERVAL";
    /**
     * 话单处理的并行度
     */
    public static final String OPERATION_DEAL_PARALLELISM = "OPERATION_DEAL_PARALLELISM";
    /**
     * kafka Source的名字
     */
    public static final String OPERATION_KAFKA_SOURCE_NAME = "OPERATION_KAFKA_SOURCE_NAME";
    /**
     * tranquility sink的名字
     */
    public static final String OPERATION_TRANQUILITY_SINK_NAME = "OPERATION_TRANQUILITY_SINK_NAME";
    /**
     * 写入kafka的topic
     */
    public static final String OPERATION_TO_KAFKA_TOPIC = "OPERATION_TO_KAFKA_TOPIC";
    /**
     * kakfa sink的名字
     */
    public static final String OPERATION_KAFKA_SINK_NAME = "OPERATION_KAFKA_SINK_NAME";

    //----------------------------------------------------------------------------------------------
    /**
     * 使用flink将五元组话单写入druid的配置
     */
    public static final String QUINTET_FLINK_TO_DRUID_CONFIG = "QUINTET_FLINK_TO_DRUID_CONFIG";
    /**
     * 五元组话单的topic
     */
    public static final String QUINTET_TOPIC = "QUINTET_TOPIC";
    /**
     * 使用flink将ARP话单写入druid任务的job.name
     */
    public static final String QUINTET_JOB_NAME = "QUINTET_JOB_NAME";
    /**
     * checkpointing的间隔
     */
    public static final String QUINTET_CHECKPOINT_INTERVAL = "QUINTET_CHECKPOINT_INTERVAL";
    /**
     * 话单处理的并行度
     */
    public static final String QUINTET_DEAL_PARALLELISM = "QUINTET_DEAL_PARALLELISM";
    /**
     * kafka Source的名字
     */
    public static final String QUINTET_KAFKA_SOURCE_NAME = "QUINTET_KAFKA_SOURCE_NAME";
    /**
     * tranquility sink的名字
     */
    public static final String QUINTET_TRANQUILITY_SINK_NAME = "QUINTET_TRANQUILITY_SINK_NAME";
    /**
     * 写入kafka的topic
     */
    public static final String QUINTET_TO_KAFKA_TOPIC = "QUINTET_TO_KAFKA_TOPIC";
    /**
     * kakfa sink的名字
     */
    public static final String QUINTET_KAFKA_SINK_NAME = "QUINTET_KAFKA_SINK_NAME";


    //----------------------------------------------------------------------------------------------
    //使用flink将邮件日志写入druid
    public static final String MAIL_FLINK_TO_DRUID_CONFIG = "MAIL_FLINK_TO_DRUID_CONFIG";
    public static final String MAIL_TOPIC = "MAIL_TOPIC";
    public static final String MAIL_END_TO_END_KAFKA_SERVER = "MAIL_END_TO_END_KAFKA_SERVER";
    public static final String MAIL_JOB_NAME = "MAIL_JOB_NAME";
    public static final String MAIL_CHECKPOINT_INTERVAL = "MAIL_CHECKPOINT_INTERVAL";
    public static final String MAIL_DEAL_PARALLELISM = "MAIL_DEAL_PARALLELISM";
    public static final String MAIL_KAFKA_SOURCE_PARALLELISM = "MAIL_KAFKA_SOURCE_PARALLELISM";
    public static final String MAIL_KAFKA_SOURCE_NAME = "MAIL_KAFKA_SOURCE_NAME";
    public static final String MAIL_KAFKA_SOURCE_GROUPID = "MAIL_KAFKA_SOURCE_GROUPID";
    public static final String MAIL_KAFKA_PRODUCE_GROUPID = "MAIL_KAFKA_PRODUCE_GROUPID";
    public static final String MAIL_TRANQUILITY_SINK_NAME = "MAIL_TRANQUILITY_SINK_NAME";
    public static final String MAIL_TO_KAFKA_TOPIC = "MAIL_TO_KAFKA_TOPIC";
    public static final String MAIL_KAFKA_SINK_NAME = "MAIL_KAFKA_SINK_NAME";


    //----------------------------------------------------------------------------------------------
    /**
     * 使用Flink实现ip互访的配置
     */
    public static final String FLINK_IP_VISIT_CONFIG = "FLINK_IP_VISIT_CONFIG";
    public static final String IP_VISIT_SOURCE_IP_COUNT_INTERVAL_TIME = "IP_VISIT_SOURCE_IP_COUNT_INTERVAL_TIME";
    public static final String IP_VISIT_SOURCE_IP_INTERVAL_TIME = "IP_VISIT_SOURCE_IP_INTERVAL_TIME";
    public static final String IP_VISIT_SOURCE_IP_COUNT_NUMBER_OF_SUBTREE = "IP_VISIT_SOURCE_IP_COUNT_NUMBER_OF_SUBTREE";
    public static final String IP_VISIT_SOURCE_IP_NUMBER_OF_SUBTREE = "IP_VISIT_SOURCE_IP_NUMBER_OF_SUBTREE";
    public static final String IP_VISIT_SOURCE_IP_COUNT_ITERS = "IP_VISIT_SOURCE_IP_COUNT_ITERS";
    public static final String IP_VISIT_SOURCE_IP_ITERS = "IP_VISIT_SOURCE_IP_ITERS";
    public static final String IP_VISIT_SOURCE_IP_COUNT_SAMPLE_SIZE = "IP_VISIT_SOURCE_IP_COUNT_SAMPLE_SIZE";
    public static final String IP_VISIT_SOURCE_IP_SAMPLE_SIZE = "IP_VISIT_SOURCE_IP_SAMPLE_SIZE";
    public static final String IP_VISIT_JOB_NAME = "IP_VISIT_JOB_NAME";
    public static final String IP_VISIT_CHECKPOINT_INTERVAL = "IP_VISIT_CHECKPOINT_INTERVAL";
    public static final String IP_VISIT_GROUP_ID = "IP_VISIT_GROUP_ID";
    public static final String IP_VISIT_KAFKA_SOURCE_NAME = "IP_VISIT_KAFKA_SOURCE_NAME";
    public static final String IP_VISIT_SQL_SINK_NAME = "IP_VISIT_SQL_SINK_NAME";
    public static final String IP_VISIT_KAFKA_SOURCE_PARALLELISM = "IP_VISIT_KAFKA_SOURCE_PARALLELISM";
    public static final String IP_VISIT_DEAL_PARALLELISM = "IP_VISIT_DEAL_PARALLELISM";
    public static final String IP_VISIT_QUERY_PARALLELISM = "IP_VISIT_QUERY_PARALLELISM";
    public static final String IP_VISIT_SQL_SINK_PARALLELISM = "IP_VISIT_SQL_SINK_PARALLELISM";
    public static final String IP_VISIT_TCP_HIGH_RISK_PATH = "IP_VISIT_TCP_HIGH_RISK_PATH";
    public static final String IP_VISIT_ALERT_KAFKA_SINK_TOPIC = "IP_VISIT_ALERT_KAFKA_SINK_TOPIC";
    public static final String IP_VISIT_ALERT_KAFKA_SINK_NAME = "IP_VISIT_ALERT_KAFKA_SINK_NAME";

    //----------------------------------------------------------------------------------------------
    /**
     * 使用Flink实现同一时间多地登录的配置
     */
    public static final String FLINK_SAME_TIME_DIFFERENT_PLACE_CONFIG = "FLINK_SAME_TIME_DIFFERENT_PLACE_CONFIG";
    public static final String SAME_TIME_DIFFERENT_PLACE_JOB_NAME = "SAME_TIME_DIFFERENT_PLACE_JOB_NAME";
    public static final String SAME_TIME_DIFFERENT_PLACE_CHECKPOINT_INTERVAL = "SAME_TIME_DIFFERENT_PLACE_CHECKPOINT_INTERVAL";
    public static final String SAME_TIME_DIFFERENT_PLACE_GROUP_ID = "SAME_TIME_DIFFERENT_PLACE_GROUP_ID";
    public static final String SAME_TIME_DIFFERENT_PLACE_KAFKA_SOURCE_NAME = "SAME_TIME_DIFFERENT_PLACE_KAFKA_SOURCE_NAME";
    public static final String SAME_TIME_DIFFERENT_PLACE_SQL_SINK_NAME = "SAME_TIME_DIFFERENT_PLACE_SQL_SINK_NAME";
    public static final String SAME_TIME_DIFFERENT_PLACE_KAFKA_SOURCE_PARALLELISM = "SAME_TIME_DIFFERENT_PLACE_KAFKA_SOURCE_PARALLELISM";
    public static final String SAME_TIME_DIFFERENT_PLACE_DEAL_PARALLELISM = "SAME_TIME_DIFFERENT_PLACE_DEAL_PARALLELISM";
    public static final String SAME_TIME_DIFFERENT_PLACE_SQL_SINK_PARALLELISM = "SAME_TIME_DIFFERENT_PLACE_SQL_SINK_PARALLELISM";
    public static final String SAME_TIME_DIFFERENT_PROVINCE_DELTA_TIMESTAMP = "SAME_TIME_DIFFERENT_PROVINCE_DELTA_TIMESTAMP";
    public static final String SAME_TIME_DIFFERENT_CITY_DELTA_TIMESTAMP = "SAME_TIME_DIFFERENT_CITY_DELTA_TIMESTAMP";
    public static final String SAME_TIME_DIFFERENT_KAFKA_SINK_TOPIC = "SAME_TIME_DIFFERENT_KAFKA_SINK_TOPIC";
    public static final String SAME_TIME_DIFFERENT_KAFKA_SINK_NAME = "SAME_TIME_DIFFERENT_KAFKA_SINK_NAME";
    public static final String SAME_TIME_DIFFERENT_KAFKA_SINK_PARALLELISM = "SAME_TIME_DIFFERENT_KAFKA_SINK_PARALLELISM";


    //----------------------------------------------------------------------------------------------
    /**
     * 使用Flink实现回归的配置
     */
    public static final String FLINK_REGRESSION_CONFIG = "FLINK_REGRESSION_CONFIG";
    public static final String REGRESSION_JOB_NAME = "REGRESSION_JOB_NAME";
    public static final String REGRESSION_CHECKPOINT_INTERVAL = "REGRESSION_CHECKPOINT_INTERVAL";
    public static final String REGRESSION_GROUP_ID = "REGRESSION_GROUP_ID";
    public static final String REGRESSION_KAFKA_SOURCE_NAME = "REGRESSION_KAFKA_SOURCE_NAME";
    public static final String REGRESSION_ALL_USER_SQL_SINK_NAME = "REGRESSION_ALL_USER_SQL_SINK_NAME";
    public static final String REGRESSION_SINGLE_SYSTEM_SQL_SINK_NAME = "REGRESSION_SINGLE_SYSTEM_SQL_SINK_NAME";
    public static final String REGRESSION_SINGLE_PROVINCE_SQL_SINK_NAME = "REGRESSION_SINGLE_PROVINCE_SQL_SINK_NAME";
    public static final String REGRESSION_KAFKA_SOURCE_PARALLELISM = "REGRESSION_KAFKA_SOURCE_PARALLELISM";
    public static final String REGRESSION_ALL_USER_REGRESSION_PARALLELISM = "REGRESSION_ALL_USER_REGRESSION_PARALLELISM";
    public static final String REGRESSION_SINGLE_PROVINCE_REGRESSION_PARALLELISM = "REGRESSION_SINGLE_PROVINCE_REGRESSION_PARALLELISM";
    public static final String REGRESSION_SINGLE_SYSTEM_REGRESSION_PARALLELISM = "REGRESSION_SINGLE_SYSTEM_REGRESSION_PARALLELISM";
    public static final String REGRESSION_SQL_SINK_PARALLELISM = "REGRESSION_SQL_SINK_PARALLELISM";
    public static final String REGRESSION_ALL_USER_REGRESSION_K = "REGRESSION_ALL_USER_REGRESSION_K";
    public static final String REGRESSION_ALL_USER_REGRESSION_SCOPE_OF_REGRESSION = "REGRESSION_ALL_USER_REGRESSION_SCOPE_OF_REGRESSION";
    public static final String REGRESSION_ALL_USER_REGRESSION_SCOPE_OF_WARN = "REGRESSION_ALL_USER_REGRESSION_SCOPE_OF_WARN";
    public static final String REGRESSION_ALL_USER_REGRESSION_WARN_LEVEL = "REGRESSION_ALL_USER_REGRESSION_WARN_LEVEL";
    public static final String REGRESSION_SINGLE_PROVINCE_REGRESSION_K = "REGRESSION_SINGLE_PROVINCE_REGRESSION_K";
    public static final String REGRESSION_SINGLE_PROVINCE_REGRESSION_SCOPE_OF_REGRESSION = "REGRESSION_SINGLE_PROVINCE_REGRESSION_SCOPE_OF_REGRESSION";
    public static final String REGRESSION_SINGLE_PROVINCE_REGRESSION_SCOPE_OF_WARN = "REGRESSION_SINGLE_PROVINCE_REGRESSION_SCOPE_OF_WARN";
    public static final String REGRESSION_SINGLE_PROVINCE_REGRESSION_WARN_LEVEL = "REGRESSION_SINGLE_PROVINCE_REGRESSION_WARN_LEVEL";
    public static final String REGRESSION_SINGLE_SYSTEM_REGRESSION_K = "REGRESSION_SINGLE_SYSTEM_REGRESSION_K";
    public static final String REGRESSION_SINGLE_SYSTEM_REGRESSION_SCOPE_OF_REGRESSION = "REGRESSION_SINGLE_SYSTEM_REGRESSION_SCOPE_OF_REGRESSION";
    public static final String REGRESSION_SINGLE_SYSTEM_REGRESSION_SCOPE_OF_WARN = "REGRESSION_SINGLE_SYSTEM_REGRESSION_SCOPE_OF_WARN";
    public static final String REGRESSION_SINGLE_SYSTEM_REGRESSION_WARN_LEVEL = "REGRESSION_SINGLE_SYSTEM_REGRESSION_WARN_LEVEL";

    //----------------------------------------------------------------------------------------------
    /**
     * 使用Flink实现常用登录系统的配置
     */
    public static final String FLINK_USED_SYSTEM_CONFIG = "FLINK_USED_SYSTEM_CONFIG";
    public static final String USED_SYSTEM_JOB_NAME = "USED_SYSTEM_JOB_NAME";
    public static final String USED_SYSTEM_CHECKPOINT_INTERVAL = "USED_SYSTEM_CHECKPOINT_INTERVAL";
    public static final String USED_SYSTEM_GROUP_ID = "USED_SYSTEM_GROUP_ID";
    public static final String USED_SYSTEM_KAFKA_SOURCE_NAME = "USED_SYSTEM_KAFKA_SOURCE_NAME";
    public static final String USED_SYSTEM_SQL_SINK_NAME = "USED_SYSTEM_SQL_SINK_NAME";
    public static final String USED_SYSTEM_KAFKA_SOURCE_PARALLELISM = "USED_SYSTEM_KAFKA_SOURCE_PARALLELISM";
    public static final String USED_SYSTEM_DEAL_PARALLELISM = "USED_SYSTEM_DEAL_PARALLELISM";
    public static final String USED_SYSTEM_SQL_SINK_PARALLELISM = "USED_SYSTEM_SQL_SINK_PARALLELISM";
    public static final String USED_SYSTEM_DELTA_PER = "USED_SYSTEM_DELTA_PER";
    public static final String USED_SYSTEM_SINK_PARALLELISM = "USED_SYSTEM_SINK_PARALLELISM";

    //----------------------------------------------------------------------------------------------
    /**
     * 使用Flink实现常用登录地的配置
     */
    public static final String FLINK_USED_PLACE_CONFIG = "FLINK_USED_PLACE_CONFIG";
    public static final String USED_PLACE_JOB_NAME = "USED_PLACE_JOB_NAME";
    public static final String USED_PLACE_CHECKPOINT_INTERVAL = "USED_PLACE_CHECKPOINT_INTERVAL";
    public static final String USED_PLACE_GROUP_ID = "USED_PLACE_GROUP_ID";
    public static final String USED_PLACE_KAFKA_SOURCE_NAME = "USED_PLACE_KAFKA_SOURCE_NAME";
    public static final String USED_PLACE_SQL_SINK_NAME = "USED_PLACE_SQL_SINK_NAME";
    public static final String USED_PLACE_KAFKA_SOURCE_PARALLELISM = "USED_PLACE_KAFKA_SOURCE_PARALLELISM";
    public static final String USED_PLACE_DEAL_PARALLELISM = "USED_PLACE_DEAL_PARALLELISM";
    public static final String USED_PLACE_SQL_SINK_PARALLELISM = "USED_PLACE_SQL_SINK_PARALLELISM";
    public static final String USED_PLACE_CLUSTER_DESCRIBE_PATH = "USED_PLACE_CLUSTER_DESCRIBE_PATH";
    public static final String USED_PLACE_CLUSTER_ID_PATH = "USED_PLACE_CLUSTER_ID_PATH";
    public static final String USED_PLACE_SINK_PARALLELISM = "USED_PLACE_SINK_PARALLELISM";


    //----------------------------------------------------------------------------------------------
    //使用地检测写入风控
    public static final String ALERT_USED_PLACE_CONFIG = "ALERT_USED_PLACE_CONFIG";
    public static final String ALERT_USED_PLACE_JOB_NAME = "ALERT_USED_PLACE_JOB_NAME";
    public static final String ALERT_USED_PLACE_KAFKA_SOURCE_NAME = "ALERT_USED_PLACE_KAFKA_SOURCE_NAME";
    public static final String ALERT_USED_PLACE_KAFKA_SINK_NAME = "ALERT_USED_PLACE_KAFKA_SINK_NAME";
    public static final String ALERT_USED_PLACE_KAFKA_SOURCE_PARALLELISM = "ALERT_USED_PLACE_KAFKA_SOURCE_PARALLELISM";
    public static final String ALERT_USED_PLACE_DEAL_PARALLELISM = "ALERT_USED_PLACE_DEAL_PARALLELISM";
    public static final String ALERT_USED_PLACE_KAFKA_SOURCE_TOPIC = "ALERT_USED_PLACE_KAFKA_SOURCE_TOPIC";
    public static final String ALERT_USED_PLACE_KAFKA_SINK_PARALLELISM = "ALERT_USED_PLACE_KAFKA_SINK_PARALLELISM";
    public static final String ALERT_USED_PLACE_GROUP_ID = "ALERT_USED_PLACE_GROUP_ID";
    public static final String ALERT_USED_PLACE_WINDOW_SIZE = "ALERT_USED_PLACE_WINDOW_SIZE";
    public static final String ALERT_USED_PLACE_SLIDE_SIZE = "ALERT_USED_PLACE_SLIDE_SIZE";
    public static final String ALERT_USED_PLACE_ALERT_NAME = "ALERT_USED_PLACE_ALERT_NAME";
    public static final String ALERT_USED_PLACE_ALERT_TYPE = "ALERT_USED_PLACE_ALERT_TYPE";
    public static final String ALERT_USED_PLACE_REGION = "ALERT_USED_PLACE_REGION";
    public static final String ALERT_USED_PLACE_BUSINESS = "ALERT_USED_PLACE_BUSINESS";
    public static final String ALERT_USED_PLACE_DOMAIN = "ALERT_USED_PLACE_DOMAIN";
    public static final String ALERT_USED_PLACE_IP = "ALERT_USED_PLACE_IP";
    public static final String ALERT_USED_PLACE_DEVICE = "ALERT_USED_PLACE_DEVICE";
    public static final String ALERT_USED_PLACE_RULE_ID = "ALERT_USED_PLACE_RULE_ID";
    public static final String ALERT_USED_PLACE_ALERT_TIMESTAMP_FORMAT = "ALERT_USED_PLACE_ALERT_TIMESTAMP_FORMAT";
    public static final String ALERT_USED_PLACE_ASSEMBLY = "ALERT_USED_PLACE_ASSEMBLY";
    public static final String ALERT_USED_PLACE_CHECKPOINT_INTERVAL = "ALERT_USED_PLACE_CHECKPOINT_INTERVAL";

    //----------------------------------------------------------------------------------------------
    /**
     * 使用Flink实现用户行为分析的配置
     */
    public static final String FLINK_USER_BEHAVIOR_ANALYZER_CONFIG = "FLINK_USER_BEHAVIOR_ANALYZER_CONFIG";
    /**
     * 用户行为分析实时检测任务名
     */
    public static final String USER_BEHAVIOR_ANALYZER_STREAM_JOB_NAME = "USER_BEHAVIOR_ANALYZER_STREAM_JOB_NAME";
    /**
     * 用户行为分析批处理任务名
     */
    public static final String USER_BEHAVIOR_ANALYZER_BATCH_JOB_NAME = "USER_BEHAVIOR_ANALYZER_BATCH_JOB_NAME";
    /**
     * 用户行为分析checkpoint间隔
     */
    public static final String USER_BEHAVIOR_ANALYZER_CHECKPOINT_INTERVAL = "USER_BEHAVIOR_ANALYZER_CHECKPOINT_INTERVAL";
    /**
     * 用户行为分析group id
     */
    public static final String USER_BEHAVIOR_ANALYZER_GROUP_ID = "USER_BEHAVIOR_ANALYZER_GROUP_ID";
    /**
     * 用户行为分析kafka source名
     */
    public static final String USER_BEHAVIOR_ANALYZER_KAFKA_SOURCE_NAME = "USER_BEHAVIOR_ANALYZER_KAFKA_SOURCE_NAME";
    /**
     * 用户行为分析sql sink名
     */
    public static final String USER_BEHAVIOR_ANALYZER_SQL_SINK_NAME = "USER_BEHAVIOR_ANALYZER_SQL_SINK_NAME";
    /**
     * 用户行为分析phoenix sink名
     */
    public static final String USER_BEHAVIOR_ANALYZER_PHOENIX_SINK_NAME = "USER_BEHAVIOR_ANALYZER_PHOENIX_SINK_NAME";
    /**
     * 用户行为分析更新总分sink名
     */
    public static final String USER_BEHAVIOR_ANALYZER_TOTAL_SCORE_SINK_NAME = "USER_BEHAVIOR_ANALYZER_TOTAL_SCORE_SINK_NAME";
    /**
     * 用户行为分析 kafka source 并行度
     */
    public static final String USER_BEHAVIOR_ANALYZER_KAFKA_SOURCE_PARALLELISM = "USER_BEHAVIOR_ANALYZER_KAFKA_SOURCE_PARALLELISM";
    /**
     * 用户行为分析 处理  并行度
     */
    public static final String USER_BEHAVIOR_ANALYZER_DEAL_PARALLELISM = "USER_BEHAVIOR_ANALYZER_DEAL_PARALLELISM";
    /**
     * 用户行为分析 sql sink 并行度
     */
    public static final String USER_BEHAVIOR_ANALYZER_SQL_SINK_PARALLELISM = "USER_BEHAVIOR_ANALYZER_SQL_SINK_PARALLELISM";
    /**
     * 用户行为分析 更新总分并行度
     */
    public static final String USER_BEHAVIOR_ANALYZER_TOTAL_SCORE_SINK_PARALLELISM = "USER_BEHAVIOR_ANALYZER_TOTAL_SCORE_SINK_PARALLELISM";
    /**
     * 用户行为分析 phoenix sink 并行度
     */
    public static final String USER_BEHAVIOR_ANALYZER_PHOENIX_SINK_PARALLELISM = "USER_BEHAVIOR_ANALYZER_PHOENIX_SINK_PARALLELISM";
    /**
     * 用户行为分析批处理读取原始文件并行度
     */
    public static final String USER_BEHAVIOR_ANALYZER_BATCH_READ_RAW_PARALLELISM = "USER_BEHAVIOR_ANALYZER_BATCH_READ_RAW_PARALLELISM";
    /**
     * 用户行为分析批处理读取临时文件并行度
     */
    public static final String USER_BEHAVIOR_ANALYZER_BATCH_READ_TMP_PARALLELISM = "USER_BEHAVIOR_ANALYZER_BATCH_READ_TMP_PARALLELISM";
    /**
     * 用户行为分析批处理写入临时文件并行度
     */
    public static final String USER_BEHAVIOR_ANALYZER_BATCH_WRITE_TMP_PARALLELISM = "USER_BEHAVIOR_ANALYZER_BATCH_WRITE_TMP_PARALLELISM";
    /**
     * 用户行为分析批处理处理并行度
     */
    public static final String USER_BEHAVIOR_ANALYZER_BATCH_DEAL_PARALLELISM = "USER_BEHAVIOR_ANALYZER_BATCH_DEAL_PARALLELISM";
    /**
     * 用户行为分析判断是否长时间未访问的时间阈值
     */
    public static final String USER_BEHAVIOR_ANALYZER_NOT_LOGIN_FOR_A_LONG_TIME_WARN_TIME_THRESHOLD = "USER_BEHAVIOR_ANALYZER_NOT_LOGIN_FOR_A_LONG_TIME_WARN_TIME_THRESHOLD";
    /**
     * 用户行为分析判断是否在非常用时间访问的时间概率阈值
     */
    public static final String USER_BEHAVIOR_ANALYZER_NOT_USED_TIME_LOGIN_WARN_PROBABILITY_THRESHOLD = "USER_BEHAVIOR_ANALYZER_NOT_USED_TIME_LOGIN_WARN_PROBABILITY_THRESHOLD";
    /**
     * 用户行为分析判断是否是机器人访问的源ip文件路径
     */
    public static final String USER_BEHAVIOR_ANALYZER_ROBOT_WARN_SOURCE_IP_PATH = "USER_BEHAVIOR_ANALYZER_ROBOT_WARN_SOURCE_IP_PATH";
    /**
     * 用户行为分析判断是否是异地登录时，人力编码和登录地的对应关系
     */
    public static final String USER_BEHAVIOR_ANALYZER_REMOTE_LOGIN_WARN_PLACE_PATH = "USER_BEHAVIOR_ANALYZER_REMOTE_LOGIN_WARN_PLACE_PATH";
    /**
     * 用户行为分析判断是否频繁切换登录地时登录地阈值
     */
    public static final String USER_BEHAVIOR_ANALYZER_FREQUENTLY_SWITCH_LOGIN_LOCATIONS_WARN_PLACE_THRESHOLD = "USER_BEHAVIOR_ANALYZER_FREQUENTLY_SWITCH_LOGIN_LOCATIONS_WARN_PLACE_THRESHOLD";
    /**
     * 用户行为分析判断是否频繁切换登录地时保存登录地的时间范围
     */
    public static final String USER_BEHAVIOR_ANALYZER_FREQUENTLY_SWITCH_LOGIN_LOCATIONS_WARN_TIME_RANGE = "USER_BEHAVIOR_ANALYZER_FREQUENTLY_SWITCH_LOGIN_LOCATIONS_WARN_TIME_RANGE";
    /**
     * 用户行为分析计算http状态码时参考的数据范围
     */
    public static final String USER_BEHAVIOR_ANALYZER_HTTP_STATUS_REFERENCE_TIME_RANGE = "USER_BEHAVIOR_ANALYZER_HTTP_STATUS_REFERENCE_TIME_RANGE";
    /**
     * 用户行为分析session时间
     */
    public static final String USER_BEHAVIOR_ANALYZER_SESSION_TIME = "USER_BEHAVIOR_ANALYZER_SESSION_TIME";
    /**
     * 用户行为分析计算时间kde时的bandwidth
     */
    public static final String USER_BEHAVIOR_ANALYZER_TIME_KDE_BANDWIDTH = "USER_BEHAVIOR_ANALYZER_TIME_KDE_BANDWIDTH";
    /**
     * 用户行为分析计算系统-时间kde时的bandwidth
     */
    public static final String USER_BEHAVIOR_ANALYZER_SYSTEM_TIME_KDE_BANDWIDTH = "USER_BEHAVIOR_ANALYZER_SYSTEM_TIME_KDE_BANDWIDTH";
    /**
     * 用户行为分析计算操作系统-时间kde时的bandwidth
     */
    public static final String USER_BEHAVIOR_ANALYZER_OPERATION_SYSTEM_TIME_KDE_BANDWIDTH = "USER_BEHAVIOR_ANALYZER_OPERATION_SYSTEM_TIME_KDE_BANDWIDTH";
    /**
     * 用户行为分析计算浏览器-时间kde时的bandwidth
     */
    public static final String USER_BEHAVIOR_ANALYZER_BROWSER_TIME_KDE_BANDWIDTH = "USER_BEHAVIOR_ANALYZER_BROWSER_TIME_KDE_BANDWIDTH";
    /**
     * 用户行为分析计算源IP-时间kde时的bandwidth
     */
    public static final String USER_BEHAVIOR_ANALYZER_SOURCE_IP_TIME_KDE_BANDWIDTH = "USER_BEHAVIOR_ANALYZER_SOURCE_IP_TIME_KDE_BANDWIDTH";
    /**
     * 用户行为分析计算源IP kde时的bandwidth
     */
    public static final String USER_BEHAVIOR_ANALYZER_SOURCE_IP_KDE_BANDWIDTH = "USER_BEHAVIOR_ANALYZER_SOURCE_IP_KDE_BANDWIDTH";
    /**
     * 用户行为分析phoenix driver class
     */
    public static final String USER_BEHAVIOR_ANALYZER_PHOENIX_DRIVER_CLASS = "USER_BEHAVIOR_ANALYZER_PHOENIX_DRIVER_CLASS";
    /**
     * 用户行为分析插入hbase sql
     */
    public static final String USER_BEHAVIOR_ANALYZER_UPSERT_HBASE_SQL = "USER_BEHAVIOR_ANALYZER_UPSERT_HBASE_SQL";
    /**
     * 用户行为分析phoenix url
     */
    public static final String USER_BEHAVIOR_ANALYZER_PHOENIX_URL = "USER_BEHAVIOR_ANALYZER_PHOENIX_URL";
    /**
     * 用户行为分析参数分类服务host
     */
    public static final String USER_BEHAVIOR_ANALYZER_PARAM_CLASSIFICATION_SERVER_HOST = "USER_BEHAVIOR_ANALYZER_PARAM_CLASSIFICATION_SERVER_HOST";
    /**
     * 用户行为分析参数分类服务port
     */
    public static final String USER_BEHAVIOR_ANALYZER_PARAM_CLASSIFICATION_SERVER_PORT = "USER_BEHAVIOR_ANALYZER_PARAM_CLASSIFICATION_SERVER_PORT";
    /**
     * 用户行为分析参数分类服务netty连接池大小
     */
    public static final String USER_BEHAVIOR_ANALYZER_PARAM_CLASSIFICATION_NETTY_POOL_SIZE = "USER_BEHAVIOR_ANALYZER_PARAM_CLASSIFICATION_NETTY_POOL_SIZE";
    /**
     * 用户行为分析参数分类服务netty连接timeout
     */
    public static final String USER_BEHAVIOR_ANALYZER_PARAM_CLASSIFICATION_NETTY_POOL_TIMEOUT = "USER_BEHAVIOR_ANALYZER_PARAM_CLASSIFICATION_NETTY_POOL_TIMEOUT";
    /**
     * 用户行为分析忽略的后缀名
     */
    public static final String USER_BEHAVIOR_ANALYZER_IGNORE_SUFFIX = "USER_BEHAVIOR_ANALYZER_IGNORE_SUFFIX";
    /**
     * 用户行为分析忽略的源IP
     */
    public static final String USER_BEHAVIOR_ANALYZER_IGNORE_SOURCE_IP = "USER_BEHAVIOR_ANALYZER_IGNORE_SOURCE_IP";
    /**
     * 用户行为分析关注的用户列表文件路径
     */
    public static final String USER_BEHAVIOR_ANALYZER_FOLLOW_USERNAME_PATH = "USER_BEHAVIOR_ANALYZER_FOLLOW_USERNAME_PATH";
    /**
     * 用户行为分析总分在redis的key
     */
    public static final String USER_BEHAVIOR_ANALYZER_TOTAL_SCORE_REDIS_KEY_FORMAT = "USER_BEHAVIOR_ANALYZER_TOTAL_SCORE_REDIS_KEY_FORMAT";
    /**
     * 用户行为分析总次数在redis的key
     */
    public static final String USER_BEHAVIOR_ANALYZER_TOTAL_COUNT_REDIS_KEY_FORMAT = "USER_BEHAVIOR_ANALYZER_TOTAL_COUNT_REDIS_KEY_FORMAT";
    /**
     * 用户行为分析时间概率在redis的key
     */
    public static final String USER_BEHAVIOR_ANALYZER_TIME_PROBABILITY_REDIS_KEY_FORMAT = "USER_BEHAVIOR_ANALYZER_TIME_PROBABILITY_REDIS_KEY_FORMAT";
    /**
     * 用户行为分析系统时间概率在redis的key
     */
    public static final String USER_BEHAVIOR_ANALYZER_SYSTEM_TIME_PROBABILITY_REDIS_KEY_FORMAT = "USER_BEHAVIOR_ANALYZER_SYSTEM_TIME_PROBABILITY_REDIS_KEY_FORMAT";
    /**
     * 用户行为分析操作系统时间概率在redis的key
     */
    public static final String USER_BEHAVIOR_ANALYZER_OPERATION_SYSTEM_TIME_PROBABILITY_REDIS_KEY_FORMAT = "USER_BEHAVIOR_ANALYZER_OPERATION_SYSTEM_TIME_PROBABILITY_REDIS_KEY_FORMAT";
    /**
     * 用户行为分析浏览器时间概率在redis的key
     */
    public static final String USER_BEHAVIOR_ANALYZER_BROWSER_TIME_PROBABILITY_REDIS_KEY_FORMAT = "USER_BEHAVIOR_ANALYZER_BROWSER_TIME_PROBABILITY_REDIS_KEY_FORMAT";
    /**
     * 用户行为分析源IP时间概率在redis的key
     */
    public static final String USER_BEHAVIOR_ANALYZER_SOURCE_IP_TIME_PROBABILITY_REDIS_KEY_FORMAT = "USER_BEHAVIOR_ANALYZER_SOURCE_IP_TIME_PROBABILITY_REDIS_KEY_FORMAT";
    /**
     * 用户行为分析源IP概率在redis的key
     */
    public static final String USER_BEHAVIOR_ANALYZER_SOURCE_IP_PROBABILITY_REDIS_KEY_FORMAT = "USER_BEHAVIOR_ANALYZER_SOURCE_IP_PROBABILITY_REDIS_KEY_FORMAT";
    /**
     * 用户行为分析单位时间内访问的系统个数在redis的key
     */
    public static final String USER_BEHAVIOR_ANALYZER_SYSTEM_COUNT_REDIS_KEY_FORMAT = "USER_BEHAVIOR_ANALYZER_SYSTEM_COUNT_REDIS_KEY_FORMAT";
    /**
     * 用户行为分析最近一段时间内的HTTP 状态码在redis的key
     */
    public static final String USER_BEHAVIOR_ANALYZER_HTTP_STATUS_REDIS_KEY_FORMAT = "USER_BEHAVIOR_ANALYZER_HTTP_STATUS_REDIS_KEY_FORMAT";
    /**
     * 用户行为分析URL参数在redis的key
     */
    public static final String USER_BEHAVIOR_ANALYZER_URL_PARAM_REDIS_KEY_FORMAT = "USER_BEHAVIOR_ANALYZER_URL_PARAM_REDIS_KEY_FORMAT";
    /**
     * 用户行为分析源ip使用次数在redis中的key
     */
    public static final String USER_BEHAVIOR_ANALYZER_SOURCE_IP_REDIS_KEY_FORMAT = "USER_BEHAVIOR_ANALYZER_SOURCE_IP_REDIS_KEY_FORMAT";
    /**
     * 用户行为分析访问系统次数在redis中的key
     */
    public static final String USER_BEHAVIOR_ANALYZER_SYSTEM_REDIS_KEY_FORMAT = "USER_BEHAVIOR_ANALYZER_SYSTEM_REDIS_KEY_FORMAT";
    /**
     * 用户行为分析源操作系统使用次数在redis中的key
     */
    public static final String USER_BEHAVIOR_ANALYZER_OPERATION_SYSTEM_REDIS_KEY_FORMAT = "USER_BEHAVIOR_ANALYZER_OPERATION_SYSTEM_REDIS_KEY_FORMAT";
    /**
     * 用户行为分析浏览器使用次数在redis中的key
     */
    public static final String USER_BEHAVIOR_ANALYZER_BROWSER_REDIS_KEY_FORMAT = "USER_BEHAVIOR_ANALYZER_BROWSER_REDIS_KEY_FORMAT";
    /**
     * 用户行为分析最后一次访问信息在redis的key
     */
    public static final String USER_BEHAVIOR_ANALYZER_LAST_VISIT_INFORMATION_REDIS_KEY_FORMAT = "USER_BEHAVIOR_ANALYZER_LAST_VISIT_INFORMATION_REDIS_KEY_FORMAT";
    /**
     * 用户行为分析最后一次访问信息在redis的key 最后一次访问时间的field
     */
    public static final String USER_BEHAVIOR_ANALYZER_LAST_VISIT_INFORMATION_REDIS_FIELD_FORMAT_LAST_VISIT_TIMESTAMP = "USER_BEHAVIOR_ANALYZER_LAST_VISIT_INFORMATION_REDIS_FIELD_FORMAT_LAST_VISIT_TIMESTAMP";
    /**
     * 用户行为分析最后一次访问信息在redis的key 最后一次访问地点的field
     */
    public static final String USER_BEHAVIOR_ANALYZER_LAST_VISIT_INFORMATION_REDIS_FIELD_FORMAT_LAST_VISIT_PLACE = "USER_BEHAVIOR_ANALYZER_LAST_VISIT_INFORMATION_REDIS_FIELD_FORMAT_LAST_VISIT_PLACE";
    /**
     * 用户行为分析最后一次访问信息在redis的key 最后一次访问系统的field
     */
    public static final String USER_BEHAVIOR_ANALYZER_LAST_VISIT_INFORMATION_REDIS_FIELD_FORMAT_LAST_VISIT_SYSTEM = "USER_BEHAVIOR_ANALYZER_LAST_VISIT_INFORMATION_REDIS_FIELD_FORMAT_LAST_VISIT_SYSTEM";
    /**
     * 用户行为分析最后一次访问信息在redis的key 评分的field
     */
    public static final String USER_BEHAVIOR_ANALYZER_LAST_VISIT_INFORMATION_REDIS_FIELD_FORMAT_SCORE = "USER_BEHAVIOR_ANALYZER_LAST_VISIT_INFORMATION_REDIS_FIELD_FORMAT_SCORE";
    /**
     * 用户行为分析最后一次访问信息在redis的key 访问次数的field
     */
    public static final String USER_BEHAVIOR_ANALYZER_LAST_VISIT_INFORMATION_REDIS_FIELD_FORMAT_COUNT = "USER_BEHAVIOR_ANALYZER_LAST_VISIT_INFORMATION_REDIS_FIELD_FORMAT_COUNT";
    /**
     * 用户行为分析最后一次访问信息在redis的key 地点的field
     */
    public static final String USER_BEHAVIOR_ANALYZER_LAST_VISIT_INFORMATION_REDIS_FIELD_FORMAT_PLACE = "USER_BEHAVIOR_ANALYZER_LAST_VISIT_INFORMATION_REDIS_FIELD_FORMAT_PLACE";
    /**
     * 用户行为分析最后一次访问信息在redis的key 用户状态的field
     */
    public static final String USER_BEHAVIOR_ANALYZER_LAST_VISIT_INFORMATION_REDIS_FIELD_FORMAT_STATUS = "USER_BEHAVIOR_ANALYZER_LAST_VISIT_INFORMATION_REDIS_FIELD_FORMAT_STATUS";
    /**
     * 用户行为分析最后一次访问信息在redis的key 用户描述的field
     */
    public static final String USER_BEHAVIOR_ANALYZER_LAST_VISIT_INFORMATION_REDIS_FIELD_FORMAT_PERSONAS = "USER_BEHAVIOR_ANALYZER_LAST_VISIT_INFORMATION_REDIS_FIELD_FORMAT_PERSONAS";
    /**
     * 用户行为分析double保留位数
     */
    public static final String USER_BEHAVIOR_ANALYZER_DOUBLE_AFTER_POINT_LENGTH = "USER_BEHAVIOR_ANALYZER_DOUBLE_AFTER_POINT_LENGTH";
    /**
     * 用户行为分析原始数据路径
     */
    public static final String USER_BEHAVIOR_ANALYZER_ALL_DATA_PATH = "USER_BEHAVIOR_ANALYZER_ALL_DATA_PATH";
    /**
     * 用户行为分析原始数据日期格式
     */
    public static final String USER_BEHAVIOR_ANALYZER_ALL_DATA_DATE_FORMAT = "USER_BEHAVIOR_ANALYZER_ALL_DATA_DATE_FORMAT";
    /**
     * 用户行为分析中间结果路径
     */
    public static final String USER_BEHAVIOR_ANALYZER_TMP_DATA_PATH = "USER_BEHAVIOR_ANALYZER_TMP_DATA_PATH";
    /**
     * 用户行为分析数据范围
     */
    public static final String USER_BEHAVIOR_ANALYZER_DATA_TIME_RANGE = "USER_BEHAVIOR_ANALYZER_DATA_TIME_RANGE";

    //----------------------------------------------------------------------------------------------
    /**
     * 使用Flink实现运维人员下载的配置
     */
    public static final String FLINK_OPERATION_PERSONNEL_DOWNLOAD_CONFIG = "FLINK_OPERATION_PERSONNEL_DOWNLOAD_CONFIG";
    public static final String OPERATION_PERSONNEL_DOWNLOAD_JOB_NAME = "OPERATION_PERSONNEL_DOWNLOAD_JOB_NAME";
    public static final String OPERATION_PERSONNEL_DOWNLOAD_CHECKPOINT_INTERVAL = "OPERATION_PERSONNEL_DOWNLOAD_CHECKPOINT_INTERVAL";
    public static final String OPERATION_PERSONNEL_DOWNLOAD_GROUP_ID = "OPERATION_PERSONNEL_DOWNLOAD_GROUP_ID";
    public static final String OPERATION_PERSONNEL_DOWNLOAD_KAFKA_SOURCE_NAME = "OPERATION_PERSONNEL_DOWNLOAD_KAFKA_SOURCE_NAME";
    public static final String OPERATION_PERSONNEL_DOWNLOAD_SQL_SINK_NAME = "OPERATION_PERSONNEL_DOWNLOAD_SQL_SINK_NAME";
    public static final String OPERATION_PERSONNEL_DOWNLOAD_KAFKA_SINK_NAME = "OPERATION_PERSONNEL_DOWNLOAD_KAFKA_SINK_NAME";
    public static final String OPERATION_PERSONNEL_DOWNLOAD_KAFKA_SOURCE_PARALLELISM = "OPERATION_PERSONNEL_DOWNLOAD_KAFKA_SOURCE_PARALLELISM";
    public static final String OPERATION_PERSONNEL_DOWNLOAD_DEAL_PARALLELISM = "OPERATION_PERSONNEL_DOWNLOAD_DEAL_PARALLELISM";
    public static final String OPERATION_PERSONNEL_DOWNLOAD_SQL_SINK_PARALLELISM = "OPERATION_PERSONNEL_DOWNLOAD_SQL_SINK_PARALLELISM";
    public static final String OPERATION_PERSONNEL_DOWNLOAD_KAFKA_SINK_PARALLELISM = "OPERATION_PERSONNEL_DOWNLOAD_KAFKA_SINK_PARALLELISM";
    public static final String OPERATION_PERSONNEL_DOWNLOAD_OPERATION_PERSONNEL_PATH = "OPERATION_PERSONNEL_DOWNLOAD_OPERATION_PERSONNEL_PATH";
    public static final String OPERATION_PERSONNEL_DOWNLOAD_ALERT_KAFKA_SINK_TOPIC = "OPERATION_PERSONNEL_DOWNLOAD_ALERT_KAFKA_SINK_TOPIC";

    //----------------------------------------------------------------------------------------------
    /**
     * 使用Flink实现下载次数异常的配置
     */
    public static final String FLINK_DOWNLOAD_COUNT_CONFIG = "FLINK_DOWNLOAD_COUNT_CONFIG";
    public static final String DOWNLOAD_COUNT_JOB_NAME = "DOWNLOAD_COUNT_JOB_NAME";
    public static final String DOWNLOAD_COUNT_CHECKPOINT_INTERVAL = "DOWNLOAD_COUNT_CHECKPOINT_INTERVAL";
    public static final String DOWNLOAD_COUNT_DRUID_SOURCE_NAME = "DOWNLOAD_COUNT_DRUID_SOURCE_NAME";
    public static final String DOWNLOAD_COUNT_SQL_SINK_NAME = "DOWNLOAD_COUNT_SQL_SINK_NAME";
    public static final String DOWNLOAD_COUNT_KAFKA_SINK_NAME = "DOWNLOAD_COUNT_KAFKA_SINK_NAME";
    public static final String DOWNLOAD_COUNT_DRUID_SOURCE_PARALLELISM = "DOWNLOAD_COUNT_DRUID_SOURCE_PARALLELISM";
    public static final String DOWNLOAD_COUNT_DEAL_PARALLELISM = "DOWNLOAD_COUNT_DEAL_PARALLELISM";
    public static final String DOWNLOAD_COUNT_SQL_SINK_PARALLELISM = "DOWNLOAD_COUNT_SQL_SINK_PARALLELISM";
    public static final String DOWNLOAD_COUNT_KAFKA_SINK_PARALLELISM = "DOWNLOAD_COUNT_KAFKA_SINK_PARALLELISM";
    public static final String DOWNLOAD_COUNT_ALERT_KAFKA_SINK_TOPIC = "DOWNLOAD_COUNT_ALERT_KAFKA_SINK_TOPIC";

    //----------------------------------------------------------------------------------------------
    /**
     * 使用Flink实现sql注入告警的配置
     */
    public static final String FLINK_SQL_INJECTION_CONFIG = "FLINK_SQL_INJECTION_CONFIG";
    public static final String SQL_INJECTION_JOB_NAME = "SQL_INJECTION_JOB_NAME";
    public static final String SQL_INJECTION_CHECKPOINT_INTERVAL = "SQL_INJECTION_CHECKPOINT_INTERVAL";
    public static final String SQL_INJECTION_GROUP_ID = "SQL_INJECTION_GROUP_ID";
    public static final String SQL_INJECTION_KAFKA_SOURCE_NAME = "SQL_INJECTION_KAFKA_SOURCE_NAME";
    public static final String SQL_INJECTION_SQL_SINK_NAME = "SQL_INJECTION_SQL_SINK_NAME";
    public static final String SQL_INJECTION_KAFKA_SINK_NAME = "SQL_INJECTION_KAFKA_SINK_NAME";
    public static final String SQL_INJECTION_KAFKA_SOURCE_PARALLELISM = "SQL_INJECTION_KAFKA_SOURCE_PARALLELISM";
    public static final String SQL_INJECTION_DEAL_PARALLELISM = "SQL_INJECTION_DEAL_PARALLELISM";
    public static final String SQL_INJECTION_SQL_SINK_PARALLELISM = "SQL_INJECTION_SQL_SINK_PARALLELISM";
    public static final String SQL_INJECTION_KAFKA_SINK_PARALLELISM = "SQL_INJECTION_KAFKA_SINK_PARALLELISM";
    public static final String SQL_INJECTION_KAFKA_SINK_TOPIC = "SQL_INJECTION_KAFKA_SINK_TOPIC";
    public static final String SQL_INJECTION_RULE_PATH = "SQL_INJECTION_RULE_PATH";
    public static final String SQL_INJECTION_GROUP_SPLIT = "SQL_INJECTION_GROUP_SPLIT";
    public static final String SQL_INJECTION_KV_SPLIT = "SQL_INJECTION_KV_SPLIT";

    //----------------------------------------------------------------------------------------------
    /**
     * 使用Flink实现xss告警的配置
     */
    public static final String FLINK_XSS_INJECTION_CONFIG = "FLINK_XSS_INJECTION_CONFIG";
    public static final String XSS_INJECTION_JOB_NAME = "XSS_INJECTION_JOB_NAME";
    public static final String XSS_INJECTION_CHECKPOINT_INTERVAL = "XSS_INJECTION_CHECKPOINT_INTERVAL";
    public static final String XSS_INJECTION_GROUP_ID = "XSS_INJECTION_GROUP_ID";
    public static final String XSS_INJECTION_KAFKA_SOURCE_NAME = "XSS_INJECTION_KAFKA_SOURCE_NAME";
    public static final String XSS_INJECTION_SQL_SINK_NAME = "XSS_INJECTION_SQL_SINK_NAME";
    public static final String XSS_INJECTION_KAFKA_SINK_NAME = "XSS_INJECTION_KAFKA_SINK_NAME";
    public static final String XSS_INJECTION_KAFKA_SOURCE_PARALLELISM = "XSS_INJECTION_KAFKA_SOURCE_PARALLELISM";
    public static final String XSS_INJECTION_DEAL_PARALLELISM = "XSS_INJECTION_DEAL_PARALLELISM";
    public static final String XSS_INJECTION_SQL_SINK_PARALLELISM = "XSS_INJECTION_SQL_SINK_PARALLELISM";
    public static final String XSS_INJECTION_KAFKA_SINK_PARALLELISM = "XSS_INJECTION_KAFKA_SINK_PARALLELISM";
    public static final String XSS_INJECTION_KAFKA_SINK_TOPIC = "XSS_INJECTION_KAFKA_SINK_TOPIC";
    public static final String XSS_INJECTION_HDFS_RULE_PATH = "XSS_INJECTION_HDFS_RULE_PATH";
    public static final String XSS_INJECTION_RULE_PATH = "XSS_INJECTION_RULE_PATH";
    public static final String XSS_INJECTION_GROUP_SPLIT = "XSS_INJECTION_GROUP_SPLIT";
    public static final String XSS_INJECTION_KV_SPLIT = "XSS_INJECTION_KV_SPLIT";

    //----------------------------------------------------------------------------------------------
    /**
     * 向安管平台发送告警配置
     */
    public static final String FLINK_ALERT_STATISTICS_CONFIG = "FLINK_ALERT_STATISTICS_CONFIG";
    public static final String ALERT_STATISTICS_JOB_NAME = "ALERT_STATISTICS_JOB_NAME";
    public static final String ALERT_STATISTICS_CHECKPOINT_INTERVAL = "ALERT_STATISTICS_CHECKPOINT_INTERVAL";
    public static final String ALERT_STATISTICS_GROUP_ID = "ALERT_STATISTICS_GROUP_ID";
    public static final String ALERT_STATISTICS_KAFKA_SOURCE_NAME = "ALERT_STATISTICS_KAFKA_SOURCE_NAME";
    public static final String ALERT_STATISTICS_ALERT_SINK_NAME = "ALERT_STATISTICS_ALERT_SINK_NAME";
    public static final String ALERT_STATISTICS_KAFKA_SOURCE_PARALLELISM = "ALERT_STATISTICS_KAFKA_SOURCE_PARALLELISM";
    public static final String ALERT_STATISTICS_ALERT_SINK_PARALLELISM = "ALERT_STATISTICS_ALERT_SINK_PARALLELISM";
    public static final String ALERT_STATISTICS_KAFKA_TOPIC = "ALERT_STATISTICS_KAFKA_TOPIC";
    public static final String ALERT_STATISTICS_ALERT_URL = "ALERT_STATISTICS_ALERT_URL";
    public static final String ALERT_STATISTICS_NAMESPACE_URI = "ALERT_STATISTICS_NAMESPACE_URI";
    public static final String ALERT_STATISTICS_LOCAL_PART = "ALERT_STATISTICS_LOCAL_PART";
    public static final String ALERT_STATISTICS_SUBSOC_ID = "ALERT_STATISTICS_SUBSOC_ID";
    public static final String ALERT_STATISTICS_KEY = "ALERT_STATISTICS_KEY";
    public static final String ALERT_STATISTICS_INTERFACE_TYPE = "ALERT_STATISTICS_INTERFACE_TYPE";
    public static final String ALERT_STATISTICS_TIMESTAMP_FORMAT = "ALERT_STATISTICS_TIMESTAMP_FORMAT";
    public static final String ALERT_STATISTICS_WATERMARK = "ALERT_STATISTICS_WATERMARK";
    public static final String ALERT_STATISTICS_WINDOW_SIZE = "ALERT_STATISTICS_WINDOW_SIZE";
    public static final String ALERT_STATISTICS_SLIDE_SIZE = "ALERT_STATISTICS_SLIDE_SIZE";
    public static final String ALERT_STATISTICS_TO_END_TO_END_KAFKA_BROKER_LIST = "ALERT_STATISTICS_TO_END_TO_END_KAFKA_BROKER_LIST";
    public static final String ALERT_STATISTICS_TO_END_TO_END_KAFKA_TOPIC = "ALERT_STATISTICS_TO_END_TO_END_KAFKA_TOPIC";
    public static final String ALERT_STATISTICS_TO_RISK_CONTROL_KAFKA_BROKER_LIST = "ALERT_STATISTICS_TO_RISK_CONTROL_KAFKA_BROKER_LIST";
    public static final String ALERT_STATISTICS_TO_RISK_CONTROL_KAFKA_TOPIC = "ALERT_STATISTICS_TO_RISK_CONTROL_KAFKA_TOPIC";


    //----------------------------------------------------------------------------------------------
    /**
     * 向安管平台发送IP_VISIT告警配置
     */
    public static final String FLINK_ALERT_STATISTICS_IP_VISIT_CONFIG = "FLINK_ALERT_STATISTICS_IP_VISIT_CONFIG";
    public static final String ALERT_STATISTICS_IP_VISIT_JOB_NAME = "ALERT_STATISTICS_IP_VISIT_JOB_NAME";
    public static final String ALERT_STATISTICS_IP_VISIT_CHECKPOINT_INTERVAL = "ALERT_STATISTICS_IP_VISIT_CHECKPOINT_INTERVAL";
    public static final String ALERT_STATISTICS_IP_VISIT_GROUP_ID = "ALERT_STATISTICS_IP_VISIT_GROUP_ID";
    public static final String ALERT_STATISTICS_IP_VISIT_KAFKA_SOURCE_NAME = "ALERT_STATISTICS_IP_VISIT_KAFKA_SOURCE_NAME";
    public static final String ALERT_STATISTICS_IP_VISIT_KAFKA_SINK_NAME = "ALERT_STATISTICS_IP_VISIT_KAFKA_SINK_NAME";
    public static final String ALERT_STATISTICS_IP_VISIT_KAFKA_SOURCE_PARALLELISM = "ALERT_STATISTICS_IP_VISIT_KAFKA_SOURCE_PARALLELISM";
    public static final String ALERT_STATISTICS_IP_VISIT_DEAL_PARALLELISM = "ALERT_STATISTICS_IP_VISIT_DEAL_PARALLELISM";
    public static final String ALERT_STATISTICS_IP_VISIT_KAFKA_SINK_PARALLELISM = "ALERT_STATISTICS_IP_VISIT_KAFKA_SINK_PARALLELISM";
    public static final String ALERT_STATISTICS_IP_VISIT_WATERMARK = "ALERT_STATISTICS_IP_VISIT_WATERMARK";
    public static final String ALERT_STATISTICS_IP_VISIT_WINDOW_SIZE = "ALERT_STATISTICS_IP_VISIT_WINDOW_SIZE";
    public static final String ALERT_STATISTICS_IP_VISIT_SLIDE_SIZE = "ALERT_STATISTICS_IP_VISIT_SLIDE_SIZE";
    public static final String ALERT_STATISTICS_IP_VISIT_ALERT_NAME = "ALERT_STATISTICS_IP_VISIT_ALERT_NAME";
    public static final String ALERT_STATISTICS_IP_VISIT_ALERT_TIMESTAMP_FORMAT = "ALERT_STATISTICS_IP_VISIT_ALERT_TIMESTAMP_FORMAT";
    public static final String ALERT_STATISTICS_IP_VISIT_ALERT_TYPE = "ALERT_STATISTICS_IP_VISIT_ALERT_TYPE";
    public static final String ALERT_STATISTICS_IP_VISIT_ALERT_REGION = "ALERT_STATISTICS_IP_VISIT_ALERT_REGION";
    public static final String ALERT_STATISTICS_IP_VISIT_ALERT_BUSINESS = "ALERT_STATISTICS_IP_VISIT_ALERT_BUSINESS";
    public static final String ALERT_STATISTICS_IP_VISIT_ALERT_DOMAIN = "ALERT_STATISTICS_IP_VISIT_ALERT_DOMAIN";
    public static final String ALERT_STATISTICS_IP_VISIT_ALERT_IP = "ALERT_STATISTICS_IP_VISIT_ALERT_IP";
    public static final String ALERT_STATISTICS_IP_VISIT_ALERT_DEVICE = "ALERT_STATISTICS_IP_VISIT_ALERT_DEVICE";
    public static final String ALERT_STATISTICS_IP_VISIT_ALERT_RULE_ID = "ALERT_STATISTICS_IP_VISIT_ALERT_RULE_ID";
    public static final String ALERT_STATISTICS_IP_VISIT_ALERT_ASSEMBLY = "ALERT_STATISTICS_IP_VISIT_ALERT_ASSEMBLY";

    //----------------------------------------------------------------------------------------------
    /**
     * 向安管平台发送运维人员下载告警配置
     */
    public static final String FLINK_ALERT_STATISTICS_OPERATION_PERSONNEL_DOWNLOAD_CONFIG = "FLINK_ALERT_STATISTICS_OPERATION_PERSONNEL_DOWNLOAD_CONFIG";
    public static final String ALERT_STATISTICS_OPERATION_PERSONNEL_DOWNLOAD_JOB_NAME = "ALERT_STATISTICS_OPERATION_PERSONNEL_DOWNLOAD_JOB_NAME";
    public static final String ALERT_STATISTICS_OPERATION_PERSONNEL_DOWNLOAD_CHECKPOINT_INTERVAL = "ALERT_STATISTICS_OPERATION_PERSONNEL_DOWNLOAD_CHECKPOINT_INTERVAL";
    public static final String ALERT_STATISTICS_OPERATION_PERSONNEL_DOWNLOAD_GROUP_ID = "ALERT_STATISTICS_OPERATION_PERSONNEL_DOWNLOAD_GROUP_ID";
    public static final String ALERT_STATISTICS_OPERATION_PERSONNEL_DOWNLOAD_KAFKA_SOURCE_NAME = "ALERT_STATISTICS_OPERATION_PERSONNEL_DOWNLOAD_KAFKA_SOURCE_NAME";
    public static final String ALERT_STATISTICS_OPERATION_PERSONNEL_DOWNLOAD_KAFKA_SINK_NAME = "ALERT_STATISTICS_OPERATION_PERSONNEL_DOWNLOAD_KAFKA_SINK_NAME";
    public static final String ALERT_STATISTICS_OPERATION_PERSONNEL_DOWNLOAD_KAFKA_SOURCE_PARALLELISM = "ALERT_STATISTICS_OPERATION_PERSONNEL_DOWNLOAD_KAFKA_SOURCE_PARALLELISM";
    public static final String ALERT_STATISTICS_OPERATION_PERSONNEL_DOWNLOAD_DEAL_PARALLELISM = "ALERT_STATISTICS_OPERATION_PERSONNEL_DOWNLOAD_DEAL_PARALLELISM";
    public static final String ALERT_STATISTICS_OPERATION_PERSONNEL_DOWNLOAD_KAFKA_SINK_PARALLELISM = "ALERT_STATISTICS_OPERATION_PERSONNEL_DOWNLOAD_KAFKA_SINK_PARALLELISM";
    public static final String ALERT_STATISTICS_OPERATION_PERSONNEL_DOWNLOAD_WATERMARK = "ALERT_STATISTICS_OPERATION_PERSONNEL_DOWNLOAD_WATERMARK";
    public static final String ALERT_STATISTICS_OPERATION_PERSONNEL_DOWNLOAD_WINDOW_SIZE = "ALERT_STATISTICS_OPERATION_PERSONNEL_DOWNLOAD_WINDOW_SIZE";
    public static final String ALERT_STATISTICS_OPERATION_PERSONNEL_DOWNLOAD_SLIDE_SIZE = "ALERT_STATISTICS_OPERATION_PERSONNEL_DOWNLOAD_SLIDE_SIZE";
    public static final String ALERT_STATISTICS_OPERATION_PERSONNEL_DOWNLOAD_ALERT_NAME = "ALERT_STATISTICS_OPERATION_PERSONNEL_DOWNLOAD_ALERT_NAME";
    public static final String ALERT_STATISTICS_OPERATION_PERSONNEL_DOWNLOAD_ALERT_TIMESTAMP_FORMAT = "ALERT_STATISTICS_OPERATION_PERSONNEL_DOWNLOAD_ALERT_TIMESTAMP_FORMAT";
    public static final String ALERT_STATISTICS_OPERATION_PERSONNEL_DOWNLOAD_ALERT_TYPE = "ALERT_STATISTICS_OPERATION_PERSONNEL_DOWNLOAD_ALERT_TYPE";
    public static final String ALERT_STATISTICS_OPERATION_PERSONNEL_DOWNLOAD_ALERT_REGION = "ALERT_STATISTICS_OPERATION_PERSONNEL_DOWNLOAD_ALERT_REGION";
    public static final String ALERT_STATISTICS_OPERATION_PERSONNEL_DOWNLOAD_ALERT_BUSINESS = "ALERT_STATISTICS_OPERATION_PERSONNEL_DOWNLOAD_ALERT_BUSINESS";
    public static final String ALERT_STATISTICS_OPERATION_PERSONNEL_DOWNLOAD_ALERT_DOMAIN = "ALERT_STATISTICS_OPERATION_PERSONNEL_DOWNLOAD_ALERT_DOMAIN";
    public static final String ALERT_STATISTICS_OPERATION_PERSONNEL_DOWNLOAD_ALERT_IP = "ALERT_STATISTICS_OPERATION_PERSONNEL_DOWNLOAD_ALERT_IP";
    public static final String ALERT_STATISTICS_OPERATION_PERSONNEL_DOWNLOAD_ALERT_DEVICE = "ALERT_STATISTICS_OPERATION_PERSONNEL_DOWNLOAD_ALERT_DEVICE";
    public static final String ALERT_STATISTICS_OPERATION_PERSONNEL_DOWNLOAD_ALERT_RULE_ID = "ALERT_STATISTICS_OPERATION_PERSONNEL_DOWNLOAD_ALERT_RULE_ID";
    public static final String ALERT_STATISTICS_OPERATION_PERSONNEL_DOWNLOAD_ALERT_ASSEMBLY = "ALERT_STATISTICS_OPERATION_PERSONNEL_DOWNLOAD_ALERT_ASSEMBLY";

    //----------------------------------------------------------------------------------------------
    /**
     * 向安管平台发送运维人员下载告警配置
     */
    public static final String FLINK_ALERT_STATISTICS_DOWNLOAD_COUNT_CONFIG = "FLINK_ALERT_STATISTICS_DOWNLOAD_COUNT_CONFIG";
    public static final String ALERT_STATISTICS_DOWNLOAD_COUNT_JOB_NAME = "ALERT_STATISTICS_DOWNLOAD_COUNT_JOB_NAME";
    public static final String ALERT_STATISTICS_DOWNLOAD_COUNT_CHECKPOINT_INTERVAL = "ALERT_STATISTICS_DOWNLOAD_COUNT_CHECKPOINT_INTERVAL";
    public static final String ALERT_STATISTICS_DOWNLOAD_COUNT_GROUP_ID = "ALERT_STATISTICS_DOWNLOAD_COUNT_GROUP_ID";
    public static final String ALERT_STATISTICS_DOWNLOAD_COUNT_KAFKA_SOURCE_NAME = "ALERT_STATISTICS_DOWNLOAD_COUNT_KAFKA_SOURCE_NAME";
    public static final String ALERT_STATISTICS_DOWNLOAD_COUNT_KAFKA_SINK_NAME = "ALERT_STATISTICS_DOWNLOAD_COUNT_KAFKA_SINK_NAME";
    public static final String ALERT_STATISTICS_DOWNLOAD_COUNT_KAFKA_SOURCE_PARALLELISM = "ALERT_STATISTICS_DOWNLOAD_COUNT_KAFKA_SOURCE_PARALLELISM";
    public static final String ALERT_STATISTICS_DOWNLOAD_COUNT_DEAL_PARALLELISM = "ALERT_STATISTICS_DOWNLOAD_COUNT_DEAL_PARALLELISM";
    public static final String ALERT_STATISTICS_DOWNLOAD_COUNT_KAFKA_SINK_PARALLELISM = "ALERT_STATISTICS_DOWNLOAD_COUNT_KAFKA_SINK_PARALLELISM";
    public static final String ALERT_STATISTICS_DOWNLOAD_COUNT_WATERMARK = "ALERT_STATISTICS_DOWNLOAD_COUNT_WATERMARK";
    public static final String ALERT_STATISTICS_DOWNLOAD_COUNT_WINDOW_SIZE = "ALERT_STATISTICS_DOWNLOAD_COUNT_WINDOW_SIZE";
    public static final String ALERT_STATISTICS_DOWNLOAD_COUNT_SLIDE_SIZE = "ALERT_STATISTICS_DOWNLOAD_COUNT_SLIDE_SIZE";
    public static final String ALERT_STATISTICS_DOWNLOAD_COUNT_ALERT_NAME = "ALERT_STATISTICS_DOWNLOAD_COUNT_ALERT_NAME";
    public static final String ALERT_STATISTICS_DOWNLOAD_COUNT_ALERT_TIMESTAMP_FORMAT = "ALERT_STATISTICS_DOWNLOAD_COUNT_ALERT_TIMESTAMP_FORMAT";
    public static final String ALERT_STATISTICS_DOWNLOAD_COUNT_ALERT_TYPE = "ALERT_STATISTICS_DOWNLOAD_COUNT_ALERT_TYPE";
    public static final String ALERT_STATISTICS_DOWNLOAD_COUNT_ALERT_REGION = "ALERT_STATISTICS_DOWNLOAD_COUNT_ALERT_REGION";
    public static final String ALERT_STATISTICS_DOWNLOAD_COUNT_ALERT_BUSINESS = "ALERT_STATISTICS_DOWNLOAD_COUNT_ALERT_BUSINESS";
    public static final String ALERT_STATISTICS_DOWNLOAD_COUNT_ALERT_DOMAIN = "ALERT_STATISTICS_DOWNLOAD_COUNT_ALERT_DOMAIN";
    public static final String ALERT_STATISTICS_DOWNLOAD_COUNT_ALERT_IP = "ALERT_STATISTICS_DOWNLOAD_COUNT_ALERT_IP";
    public static final String ALERT_STATISTICS_DOWNLOAD_COUNT_ALERT_DEVICE = "ALERT_STATISTICS_DOWNLOAD_COUNT_ALERT_DEVICE";
    public static final String ALERT_STATISTICS_DOWNLOAD_COUNT_ALERT_RULE_ID = "ALERT_STATISTICS_DOWNLOAD_COUNT_ALERT_RULE_ID";
    public static final String ALERT_STATISTICS_DOWNLOAD_COUNT_ALERT_ASSEMBLY = "ALERT_STATISTICS_DOWNLOAD_COUNT_ALERT_ASSEMBLY";

    //----------------------------------------------------------------------------------------------
    /**
     * 向安管平台发送sql注入告警配置
     */
    public static final String FLINK_ALERT_STATISTICS_SQL_INJECTION_CONFIG = "FLINK_ALERT_STATISTICS_SQL_INJECTION_CONFIG";
    public static final String ALERT_STATISTICS_SQL_INJECTION_JOB_NAME = "ALERT_STATISTICS_SQL_INJECTION_JOB_NAME";
    public static final String ALERT_STATISTICS_SQL_INJECTION_CHECKPOINT_INTERVAL = "ALERT_STATISTICS_SQL_INJECTION_CHECKPOINT_INTERVAL";
    public static final String ALERT_STATISTICS_SQL_INJECTION_GROUP_ID = "ALERT_STATISTICS_SQL_INJECTION_GROUP_ID";
    public static final String ALERT_STATISTICS_SQL_INJECTION_KAFKA_SOURCE_NAME = "ALERT_STATISTICS_SQL_INJECTION_KAFKA_SOURCE_NAME";
    public static final String ALERT_STATISTICS_SQL_INJECTION_KAFKA_SINK_NAME = "ALERT_STATISTICS_SQL_INJECTION_KAFKA_SINK_NAME";
    public static final String ALERT_STATISTICS_SQL_INJECTION_KAFKA_SOURCE_PARALLELISM = "ALERT_STATISTICS_SQL_INJECTION_KAFKA_SOURCE_PARALLELISM";
    public static final String ALERT_STATISTICS_SQL_INJECTION_DEAL_PARALLELISM = "ALERT_STATISTICS_SQL_INJECTION_DEAL_PARALLELISM";
    public static final String ALERT_STATISTICS_SQL_INJECTION_KAFKA_SINK_PARALLELISM = "ALERT_STATISTICS_SQL_INJECTION_KAFKA_SINK_PARALLELISM";
    public static final String ALERT_STATISTICS_SQL_INJECTION_WATERMARK = "ALERT_STATISTICS_SQL_INJECTION_WATERMARK";
    public static final String ALERT_STATISTICS_SQL_INJECTION_WINDOW_SIZE = "ALERT_STATISTICS_SQL_INJECTION_WINDOW_SIZE";
    public static final String ALERT_STATISTICS_SQL_INJECTION_SLIDE_SIZE = "ALERT_STATISTICS_SQL_INJECTION_SLIDE_SIZE";
    public static final String ALERT_STATISTICS_SQL_INJECTION_ALERT_NAME = "ALERT_STATISTICS_SQL_INJECTION_ALERT_NAME";
    public static final String ALERT_STATISTICS_SQL_INJECTION_ALERT_TIMESTAMP_FORMAT = "ALERT_STATISTICS_SQL_INJECTION_ALERT_TIMESTAMP_FORMAT";
    public static final String ALERT_STATISTICS_SQL_INJECTION_ALERT_TYPE = "ALERT_STATISTICS_SQL_INJECTION_ALERT_TYPE";
    public static final String ALERT_STATISTICS_SQL_INJECTION_ALERT_REGION = "ALERT_STATISTICS_SQL_INJECTION_ALERT_REGION";
    public static final String ALERT_STATISTICS_SQL_INJECTION_ALERT_BUSINESS = "ALERT_STATISTICS_SQL_INJECTION_ALERT_BUSINESS";
    public static final String ALERT_STATISTICS_SQL_INJECTION_ALERT_DOMAIN = "ALERT_STATISTICS_SQL_INJECTION_ALERT_DOMAIN";
    public static final String ALERT_STATISTICS_SQL_INJECTION_ALERT_IP = "ALERT_STATISTICS_SQL_INJECTION_ALERT_IP";
    public static final String ALERT_STATISTICS_SQL_INJECTION_ALERT_DEVICE = "ALERT_STATISTICS_SQL_INJECTION_ALERT_DEVICE";
    public static final String ALERT_STATISTICS_SQL_INJECTION_ALERT_RULE_ID = "ALERT_STATISTICS_SQL_INJECTION_ALERT_RULE_ID";
    public static final String ALERT_STATISTICS_SQL_INJECTION_ALERT_ASSEMBLY = "ALERT_STATISTICS_SQL_INJECTION_ALERT_ASSEMBLY";

    //----------------------------------------------------------------------------------------------
    /**
     * 向安管平台发送xss告警配置
     */
    public static final String FLINK_ALERT_STATISTICS_XSS_INJECTION_CONFIG = "FLINK_ALERT_STATISTICS_XSS_INJECTION_CONFIG";
    public static final String ALERT_STATISTICS_XSS_INJECTION_JOB_NAME = "ALERT_STATISTICS_XSS_INJECTION_JOB_NAME";
    public static final String ALERT_STATISTICS_XSS_INJECTION_CHECKPOINT_INTERVAL = "ALERT_STATISTICS_XSS_INJECTION_CHECKPOINT_INTERVAL";
    public static final String ALERT_STATISTICS_XSS_INJECTION_GROUP_ID = "ALERT_STATISTICS_XSS_INJECTION_GROUP_ID";
    public static final String ALERT_STATISTICS_XSS_INJECTION_KAFKA_SOURCE_NAME = "ALERT_STATISTICS_XSS_INJECTION_KAFKA_SOURCE_NAME";
    public static final String ALERT_STATISTICS_XSS_INJECTION_KAFKA_SINK_NAME = "ALERT_STATISTICS_XSS_INJECTION_KAFKA_SINK_NAME";
    public static final String ALERT_STATISTICS_XSS_INJECTION_KAFKA_SOURCE_PARALLELISM = "ALERT_STATISTICS_XSS_INJECTION_KAFKA_SOURCE_PARALLELISM";
    public static final String ALERT_STATISTICS_XSS_INJECTION_DEAL_PARALLELISM = "ALERT_STATISTICS_XSS_INJECTION_DEAL_PARALLELISM";
    public static final String ALERT_STATISTICS_XSS_INJECTION_KAFKA_SINK_PARALLELISM = "ALERT_STATISTICS_XSS_INJECTION_KAFKA_SINK_PARALLELISM";
    public static final String ALERT_STATISTICS_XSS_INJECTION_WATERMARK = "ALERT_STATISTICS_XSS_INJECTION_WATERMARK";
    public static final String ALERT_STATISTICS_XSS_INJECTION_WINDOW_SIZE = "ALERT_STATISTICS_XSS_INJECTION_WINDOW_SIZE";
    public static final String ALERT_STATISTICS_XSS_INJECTION_SLIDE_SIZE = "ALERT_STATISTICS_XSS_INJECTION_SLIDE_SIZE";
    public static final String ALERT_STATISTICS_XSS_INJECTION_ALERT_NAME = "ALERT_STATISTICS_XSS_INJECTION_ALERT_NAME";
    public static final String ALERT_STATISTICS_XSS_INJECTION_ALERT_TIMESTAMP_FORMAT = "ALERT_STATISTICS_XSS_INJECTION_ALERT_TIMESTAMP_FORMAT";
    public static final String ALERT_STATISTICS_XSS_INJECTION_ALERT_TYPE = "ALERT_STATISTICS_XSS_INJECTION_ALERT_TYPE";
    public static final String ALERT_STATISTICS_XSS_INJECTION_ALERT_REGION = "ALERT_STATISTICS_XSS_INJECTION_ALERT_REGION";
    public static final String ALERT_STATISTICS_XSS_INJECTION_ALERT_BUSINESS = "ALERT_STATISTICS_XSS_INJECTION_ALERT_BUSINESS";
    public static final String ALERT_STATISTICS_XSS_INJECTION_ALERT_DOMAIN = "ALERT_STATISTICS_XSS_INJECTION_ALERT_DOMAIN";
    public static final String ALERT_STATISTICS_XSS_INJECTION_ALERT_IP = "ALERT_STATISTICS_XSS_INJECTION_ALERT_IP";
    public static final String ALERT_STATISTICS_XSS_INJECTION_ALERT_DEVICE = "ALERT_STATISTICS_XSS_INJECTION_ALERT_DEVICE";
    public static final String ALERT_STATISTICS_XSS_INJECTION_ALERT_RULE_ID = "ALERT_STATISTICS_XSS_INJECTION_ALERT_RULE_ID";
    public static final String ALERT_STATISTICS_XSS_INJECTION_ALERT_ASSEMBLY = "ALERT_STATISTICS_XSS_INJECTION_ALERT_ASSEMBLY";

    //----------------------------------------------------------------------------------------------
    /**
     * 获取TopN异常用户
     */
    public static final String FLINK_ABNORMAL_USER_CONFIG = "FLINK_ABNORMAL_USER_CONFIG";
    public static final String ABNORMAL_USER_JOB_NAME = "ABNORMAL_USER_JOB_NAME";
    public static final String ABNORMAL_USER_CHECKPOINT_INTERVAL = "ABNORMAL_USER_CHECKPOINT_INTERVAL";
    public static final String ABNORMAL_USER_GROUP_ID = "ABNORMAL_USER_GROUP_ID";
    public static final String ABNORMAL_USER_KAFKA_SOURCE_NAME = "ABNORMAL_USER_KAFKA_SOURCE_NAME";
    public static final String ABNORMAL_USER_MYSQL_SINK_NAME = "ABNORMAL_USER_MYSQL_SINK_NAME";
    public static final String ABNORMAL_USER_KAFKA_SOURCE_PARALLELISM = "ABNORMAL_USER_KAFKA_SOURCE_PARALLELISM";
    public static final String ABNORMAL_USER_DEAL_PARALLELISM = "ABNORMAL_USER_DEAL_PARALLELISM";
    public static final String ABNORMAL_USER_MYSQL_SINK_PARALLELISM = "ABNORMAL_USER_MYSQL_SINK_PARALLELISM";
    public static final String ABNORMAL_USER_WATERMARK = "ABNORMAL_USER_WATERMARK";
    public static final String ABNORMAL_USER_WINDOW_SIZE = "ABNORMAL_USER_WINDOW_SIZE";
    public static final String ABNORMAL_USER_SLIDE_SIZE = "ABNORMAL_USER_SLIDE_SIZE";
    public static final String ABNORMAL_USER_TOP_K = "ABNORMAL_USER_TOP_K";
    public static final String ABNORMAL_USER_KAFKA_SINK_TOPIC = "ABNORMAL_USER_KAFKA_SINK_TOPIC";
    public static final String ABNORMAL_USER_KAFKA_SINK_NAME = "ABNORMAL_USER_KAFKA_SINK_NAME";
    public static final String ABNORMAL_USER_KAFKA_SINK_PARALLELISM = "ABNORMAL_USER_KAFKA_SINK_PARALLELISM";

    //----------------------------------------------------------------------------------------------
    /**
     * 获取TopN异常系统
     */
    public static final String FLINK_ABNORMAL_SYSTEM_CONFIG = "FLINK_ABNORMAL_SYSTEM_CONFIG";
    public static final String ABNORMAL_SYSTEM_JOB_NAME = "ABNORMAL_SYSTEM_JOB_NAME";
    public static final String ABNORMAL_SYSTEM_CHECKPOINT_INTERVAL = "ABNORMAL_SYSTEM_CHECKPOINT_INTERVAL";
    public static final String ABNORMAL_SYSTEM_GROUP_ID = "ABNORMAL_SYSTEM_GROUP_ID";
    public static final String ABNORMAL_SYSTEM_KAFKA_SOURCE_NAME = "ABNORMAL_SYSTEM_KAFKA_SOURCE_NAME";
    public static final String ABNORMAL_SYSTEM_MYSQL_SINK_NAME = "ABNORMAL_SYSTEM_MYSQL_SINK_NAME";
    public static final String ABNORMAL_SYSTEM_KAFKA_SOURCE_PARALLELISM = "ABNORMAL_SYSTEM_KAFKA_SOURCE_PARALLELISM";
    public static final String ABNORMAL_SYSTEM_DEAL_PARALLELISM = "ABNORMAL_SYSTEM_DEAL_PARALLELISM";
    public static final String ABNORMAL_SYSTEM_MYSQL_SINK_PARALLELISM = "ABNORMAL_SYSTEM_MYSQL_SINK_PARALLELISM";
    public static final String ABNORMAL_SYSTEM_WATERMARK = "ABNORMAL_SYSTEM_WATERMARK";
    public static final String ABNORMAL_SYSTEM_WINDOW_SIZE = "ABNORMAL_SYSTEM_WINDOW_SIZE";
    public static final String ABNORMAL_SYSTEM_SLIDE_SIZE = "ABNORMAL_SYSTEM_SLIDE_SIZE";
    public static final String ABNORMAL_SYSTEM_TOP_K = "ABNORMAL_SYSTEM_TOP_K";
    public static final String ABNORMAL_SYSTEM_KAFKA_SINK_TOPIC = "ABNORMAL_SYSTEM_KAFKA_SINK_TOPIC";
    public static final String ABNORMAL_SYSTEM_KAFKA_SINK_NAME = "ABNORMAL_SYSTEM_KAFKA_SINK_NAME";
    public static final String ABNORMAL_SYSTEM_KAFKA_SINK_PARALLELISM = "ABNORMAL_SYSTEM_KAFKA_SINK_PARALLELISM";

    //----------------------------------------------------------------------------------------------
    /**
     * 状态异常用户
     */
    public static final String FLINK_ABNORMAL_STATUS_USER_CONFIG = "FLINK_ABNORMAL_STATUS_USER_CONFIG";
    public static final String ABNORMAL_STATUS_USER_JOB_NAME = "ABNORMAL_STATUS_USER_JOB_NAME";
    public static final String ABNORMAL_STATUS_USER_CHECKPOINT_INTERVAL = "ABNORMAL_STATUS_USER_CHECKPOINT_INTERVAL";
    public static final String ABNORMAL_STATUS_USER_GROUP_ID = "ABNORMAL_STATUS_USER_GROUP_ID";
    public static final String ABNORMAL_STATUS_USER_KAFKA_SOURCE_NAME = "ABNORMAL_STATUS_USER_KAFKA_SOURCE_NAME";
    public static final String ABNORMAL_STATUS_USER_SQL_SINK_NAME = "ABNORMAL_STATUS_USER_SQL_SINK_NAME";
    public static final String ABNORMAL_STATUS_USER_KAFKA_SINK_NAME = "ABNORMAL_STATUS_USER_KAFKA_SINK_NAME";
    public static final String ABNORMAL_STATUS_USER_KAFKA_SOURCE_PARALLELISM = "ABNORMAL_STATUS_USER_KAFKA_SOURCE_PARALLELISM";
    public static final String ABNORMAL_STATUS_USER_DEAL_PARALLELISM = "ABNORMAL_STATUS_USER_DEAL_PARALLELISM";
    public static final String ABNORMAL_STATUS_USER_SQL_SINK_PARALLELISM = "ABNORMAL_STATUS_USER_SQL_SINK_PARALLELISM";
    public static final String ABNORMAL_STATUS_USER_KAFKA_SINK_PARALLELISM = "ABNORMAL_STATUS_USER_KAFKA_SINK_PARALLELISM";
    public static final String ABNORMAL_STATUS_USER_ALERT_KAFKA_SINK_TOPIC = "ABNORMAL_STATUS_USER_ALERT_KAFKA_SINK_TOPIC";
    public static final String ABNORMAL_STATUS_USER_USER_STATUS_REDIS_KEY = "ABNORMAL_STATUS_USER_USER_STATUS_REDIS_KEY";
    public static final String ABNORMAL_STATUS_USER_CHANNEL_NAME = "ABNORMAL_STATUS_USER_CHANNEL_NAME";

    //----------------------------------------------------------------------------------------------
    //爬虫用户检测
    public static final String FLINK_CRAWLER_DETECT_CONFIG = "FLINK_CRAWLER_DETECT_CONFIG";
    public static final String CRAWLER_DETECT_JOB_NAME = "CRAWLER_DETECT_JOB_NAME";
    public static final String CRAWLER_DETECT_CHECKPOINT_INTERVAL = "CRAWLER_DETECT_CHECKPOINT_INTERVAL";
    public static final String CRAWLER_DETECT_GROUP_ID = "CRAWLER_DETECT_GROUP_ID";
    public static final String CRAWLER_DETECT_KAFKA_SOURCE_NAME = "CRAWLER_DETECT_KAFKA_SOURCE_NAME";
    public static final String CRAWLER_DETECT_KAFKA_SOURCE_PARALLELISM = "CRAWLER_DETECT_KAFKA_SOURCE_PARALLELISM";
    public static final String CRAWLER_DETECT_SQL_SINK_NAME = "CRAWLER_DETECT_SQL_SINK_NAME";
    public static final String CRAWLER_DETECT_ONLINE_TIME_ALLOW = "CRAWLER_DETECT_ONLINE_TIME_ALLOW";
    public static final String CRAWLER_DETECT_ONLINE_TIME_DECIDE = "CRAWLER_DETECT_ONLINE_TIME_DECIDE";
    public static final String CRAWLER_DETECT_DEAL_PARALLELISM = "CRAWLER_DETECT_DEAL_PARALLELISM";
    public static final String CRAWLER_DETECT_SINK_PARALLELISM = "CRAWLER_DETECT_SINK_PARALLELISM";
    public static final String CRAWLER_DETECT_FILTER_KEY = "CRAWLER_DETECT_FILTER_KEY";
    public static final String CRAWLER_DETECT_ROBOT_URL = "CRAWLER_DETECT_ROBOT_URL";
    public static final String CRAWLER_DETECT_KAFKA_SINK_TOPIC = "CRAWLER_DETECT_KAFKA_SINK_TOPIC";
    public static final String CRAWLER_DETECT_KAFKA_SINK_NAME = "CRAWLER_DETECT_KAFKA_SINK_NAME";
    public static final String CRAWLER_DETECT_KAFKA_SINK_PARALLELISM = "CRAWLER_DETECT_KAFKA_SINK_PARALLELISM";


    //----------------------------------------------------------------------------------------------
    //WebShell检测
    public static final String SCAN_DETECT_CONFIG = "SCAN_DETECT_CONFIG";
    public static final String SCAN_DETECT_JOB_NAME = "SCAN_DETECT_JOB_NAME";
    public static final String SCAN_DETECT_ALARM_SINK_NAME = "SCAN_DETECT_ALARM_SINK_NAME";
    public static final String SCAN_DETECT_DEAL_PARALLELISM = "SCAN_DETECT_DEAL_PARALLELISM";
    public static final String SCAN_DETECT_SINK_PARALLELISM = "SCAN_DETECT_SINK_PARALLELISM";
    public static final String SCAN_DETECT_RANDOM_SIZE = "SCAN_DETECT_RANDOM_SIZE";
    public static final String SCAN_DETECT_TIME_INTERVAL = "SCAN_DETECT_TIME_INTERVAL";
    public static final String SCAN_DETECT_USER_COUNT_THRESHOLD = "SCAN_DETECT_USER_COUNT_THRESHOLD";
    public static final String SCAN_DETECT_URL_COUNT_THRESHOLD = "SCAN_DETECT_URL_COUNT_THRESHOLD";
    public static final String SCAN_DETECT_TOTAL_THRESHOLD = "SCAN_DETECT_TOTAL_THRESHOLD";
    public static final String SCAN_DETECT_DATA_PATH = "SCAN_DETECT_DATA_PATH";
    public static final String SCAN_DETECT_FILTER_KEYS = "SCAN_DETECT_FILTER_KEYS";
    public static final String SCAN_DETECT_FAIL_SCAN_DATA_PATH = "SCAN_DETECT_FAIL_SCAN_DATA_PATH";
    public static final String SCAN_DETECT_HISTORY_DATA_FILE = "SCAN_DETECT_HISTORY_DATA_FILE";
    public static final String SCAN_DETECT_HISTORY_DATA_NEW_FILE = "SCAN_DETECT_HISTORY_DATA_NEW_FILE";
    public static final String SCAN_DETECT_DAY_LONG = "SCAN_DETECT_DAY_LONG";
    public static final String SCAN_DETECT_WEB_LEN_THRESHOLD = "SCAN_DETECT_WEB_LEN_THRESHOLD";
    public static final String SCAN_DETECT_KAFKA_SINK_PARALLELISM = "SCAN_DETECT_KAFKA_SINK_PARALLELISM";
    public static final String SCAN_DETECT_KAFKA_SINK_TOPIC = "SCAN_DETECT_KAFKA_SINK_TOPIC";
    public static final String SCAN_DETECT_WHITE_LIST = "SCAN_DETECT_WHITE_LIST";

    //----------------------------------------------------------------------------------------------
    /**
     * 目录遍历检测
     */
    public static final String DIRECTORY_TRAVERSAL_CONFIG = "DIRECTORY_TRAVERSAL_CONFIG";
    /**
     * 目录遍历检测任务名
     */
    public static final String DIRECTORY_TRAVERSAL_JOB_NAME = "DIRECTORY_TRAVERSAL_JOB_NAME";
    /**
     * 目录遍历检测checkpoint间隔
     */
    public static final String DIRECTORY_TRAVERSAL_CHECKPOINT_INTERVAL = "DIRECTORY_TRAVERSAL_CHECKPOINT_INTERVAL";
    /**
     * 目录遍历检测group id
     */
    public static final String DIRECTORY_TRAVERSAL_GROUP_ID = "DIRECTORY_TRAVERSAL_GROUP_ID";
    /**
     * 目录遍历检测 kafka source name
     */
    public static final String DIRECTORY_TRAVERSAL_KAFKA_SOURCE_NAME = "DIRECTORY_TRAVERSAL_KAFKA_SOURCE_NAME";
    /**
     * 目录遍历检测 sql sink name
     */
    public static final String DIRECTORY_TRAVERSAL_SQL_SINK_NAME = "DIRECTORY_TRAVERSAL_SQL_SINK_NAME";
    /**
     * 目录遍历检测 kafka sink name
     */
    public static final String DIRECTORY_TRAVERSAL_KAFKA_SINK_NAME = "DIRECTORY_TRAVERSAL_KAFKA_SINK_NAME";
    /**
     * 目录遍历检测 kafka source 并行度
     */
    public static final String DIRECTORY_TRAVERSAL_KAFKA_SOURCE_PARALLELISM = "DIRECTORY_TRAVERSAL_KAFKA_SOURCE_PARALLELISM";
    /**
     * 目录遍历检测处理并行度
     */
    public static final String DIRECTORY_TRAVERSAL_DEAL_PARALLELISM = "DIRECTORY_TRAVERSAL_DEAL_PARALLELISM";
    /**
     * 目录遍历检测 sql sink并行度
     */
    public static final String DIRECTORY_TRAVERSAL_SQL_SINK_PARALLELISM = "DIRECTORY_TRAVERSAL_SQL_SINK_PARALLELISM";
    /**
     * 目录遍历检测 kafka sink并行度
     */
    public static final String DIRECTORY_TRAVERSAL_KAFKA_SINK_PARALLELISM = "DIRECTORY_TRAVERSAL_KAFKA_SINK_PARALLELISM";
    /**
     * 目录遍历检测 kafka sink topic
     */
    public static final String DIRECTORY_TRAVERSAL_KAFKA_SINK_TOPIC = "DIRECTORY_TRAVERSAL_KAFKA_SINK_TOPIC";
    /**
     * 目录遍历检测 训练数据集的路径
     */
    public static final String DIRECTORY_TRAVERSAL_TRAIN_DATA_PATH = "DIRECTORY_TRAVERSAL_TRAIN_DATA_PATH";

    //holtWinters回归预测
    public static final String FLINK_HOLT_WINTERS_REGRESSION_CONFIG = "FLINK_HOLT_WINTERS_REGRESSION_CONFIG";
    public static final String FLINK_HOLT_WINTERS_REGRESSION_JOB_NAME = "FLINK_HOLT_WINTERS_REGRESSION_JOB_NAME";
    public static final String FLINK_HOLT_WINTERS_REGRESSION_KAFKA_NAME = "FLINK_HOLT_WINTERS_REGRESSION_KAFKA_NAME";
    public static final String FLINK_HOLT_WINTERS_REGRESSION_SINK_NAME = "FLINK_HOLT_WINTERS_REGRESSION_SINK_NAME";
    public static final String FLINK_HOLT_WINTERS_REGRESSION_KAFKA_SOURCE_PARALLELISM = "FLINK_HOLT_WINTERS_REGRESSION_KAFKA_SOURCE_PARALLELISM";
    public static final String FLINK_HOLT_WINTERS_REGRESSION_DEAL_PARALLELISM = "FLINK_HOLT_WINTERS_REGRESSION_DEAL_PARALLELISM";
    public static final String FLINK_HOLT_WINTERS_REGRESSION_MYSQL_SINK_PARALLELISM = "FLINK_HOLT_WINTERS_REGRESSION_MYSQL_SINK_PARALLELISM";
    public static final String FLINK_HOLT_WINTERS_REGRESSION_ALL_GROUP_ID = "FLINK_HOLT_WINTERS_REGRESSION_ALL_GROUP_ID";
    public static final String FLINK_HOLT_WINTERS_REGRESSION_SYSTEM_GROUP_ID = "FLINK_HOLT_WINTERS_REGRESSION_SYSTEM_GROUP_ID";
    public static final String FLINK_HOLT_WINTERS_REGRESSION_PROVINCE_GROUP_ID = "FLINK_HOLT_WINTERS_REGRESSION_PROVINCE_GROUP_ID";
    public static final String FLINK_HOLT_WINTERS_REGRESSION_ALPHA = "FLINK_HOLT_WINTERS_REGRESSION_ALPHA";
    public static final String FLINK_HOLT_WINTERS_REGRESSION_BETA = "FLINK_HOLT_WINTERS_REGRESSION_BETA";
    public static final String FLINK_HOLT_WINTERS_REGRESSION_GAMMA = "FLINK_HOLT_WINTERS_REGRESSION_GAMMA";
    public static final String FLINK_HOLT_WINTERS_REGRESSION_PERIOD = "FLINK_HOLT_WINTERS_REGRESSION_PERIOD";
    public static final String FLINK_HOLT_WINTERS_REGRESSION_HISTORY_LEN = "FLINK_HOLT_WINTERS_REGRESSION_HISTORY_LEN";
    public static final String FLINK_HOLT_WINTERS_REGRESSION_ALL_USER_REGRESSION_SCOPE_OF_WARN = "FLINK_HOLT_WINTERS_REGRESSION_ALL_USER_REGRESSION_SCOPE_OF_WARN";
    public static final String FLINK_HOLT_WINTERS_REGRESSION_ALL_USER_REGRESSION_WARN_LEVEL = "FLINK_HOLT_WINTERS_REGRESSION_ALL_USER_REGRESSION_WARN_LEVEL";

    //----------------------------------------------------------------------------------------------
    //向安管平台发送目录遍历告警配置
    public static final String FLINK_ALERT_STATISTICS_DIRECTORY_TRAVERSAL_CONFIG = "FLINK_ALERT_STATISTICS_DIRECTORY_TRAVERSAL_CONFIG";
    public static final String ALERT_STATISTICS_DIRECTORY_TRAVERSAL_JOB_NAME = "ALERT_STATISTICS_DIRECTORY_TRAVERSAL_JOB_NAME";
    public static final String ALERT_STATISTICS_DIRECTORY_TRAVERSAL_CHECKPOINT_INTERVAL = "ALERT_STATISTICS_DIRECTORY_TRAVERSAL_CHECKPOINT_INTERVAL";
    public static final String ALERT_STATISTICS_DIRECTORY_TRAVERSAL_GROUP_ID = "ALERT_STATISTICS_DIRECTORY_TRAVERSAL_GROUP_ID";
    public static final String ALERT_STATISTICS_DIRECTORY_TRAVERSAL_KAFKA_SOURCE_NAME = "ALERT_STATISTICS_DIRECTORY_TRAVERSAL_KAFKA_SOURCE_NAME";
    public static final String ALERT_STATISTICS_DIRECTORY_TRAVERSAL_KAFKA_SINK_NAME = "ALERT_STATISTICS_DIRECTORY_TRAVERSAL_KAFKA_SINK_NAME";
    public static final String ALERT_STATISTICS_DIRECTORY_TRAVERSAL_KAFKA_SOURCE_PARALLELISM = "ALERT_STATISTICS_DIRECTORY_TRAVERSAL_KAFKA_SOURCE_PARALLELISM";
    public static final String ALERT_STATISTICS_DIRECTORY_TRAVERSAL_DEAL_PARALLELISM = "ALERT_STATISTICS_DIRECTORY_TRAVERSAL_DEAL_PARALLELISM";
    public static final String ALERT_STATISTICS_DIRECTORY_TRAVERSAL_KAFKA_SINK_PARALLELSIM = "ALERT_STATISTICS_DIRECTORY_TRAVERSAL_KAFKA_SINK_PARALLELSIM";
    public static final String ALERT_STATISTICS_DIRECTORY_TRAVERSAL_WINDOW_SIZE = "ALERT_STATISTICS_DIRECTORY_TRAVERSAL_WINDOW_SIZE";
    public static final String ALERT_STATISTICS_DIRECTORY_TRAVERSAL_SLIDE_SIZE = "ALERT_STATISTICS_DIRECTORY_TRAVERSAL_SLIDE_SIZE";
    public static final String ALERT_STATISTICS_DIRECTORY_TRAVERSAL_ALERT_NAME = "ALERT_STATISTICS_DIRECTORY_TRAVERSAL_ALERT_NAME";
    public static final String ALERT_STATISTICS_DIRECTORY_TRAVERSAL_ALERT_TIMESTAMP_FORMAT = "ALERT_STATISTICS_DIRECTORY_TRAVERSAL_ALERT_TIMESTAMP_FORMAT";
    public static final String ALERT_STATISTICS_DIRECTORY_TRAVERSAL_ALERT_TYPE = "ALERT_STATISTICS_DIRECTORY_TRAVERSAL_ALERT_TYPE";
    public static final String ALERT_STATISTICS_DIRECTORY_TRAVERSAL_ALERT_REGION = "ALERT_STATISTICS_DIRECTORY_TRAVERSAL_ALERT_REGION";
    public static final String ALERT_STATISTICS_DIRECTORY_TRAVERSAL_ALERT_BUSINESS = "ALERT_STATISTICS_DIRECTORY_TRAVERSAL_ALERT_BUSINESS";
    public static final String ALERT_STATISTICS_DIRECTORY_TRAVERSAL_ALERT_DOMAIN = "ALERT_STATISTICS_DIRECTORY_TRAVERSAL_ALERT_DOMAIN";
    public static final String ALERT_STATISTICS_DIRECTORY_TRAVERSAL_ALERT_IP = "ALERT_STATISTICS_DIRECTORY_TRAVERSAL_ALERT_IP";
    public static final String ALERT_STATISTICS_DIRECTORY_TRAVERSAL_ALERT_DEVICE = "ALERT_STATISTICS_DIRECTORY_TRAVERSAL_ALERT_DEVICE";
    public static final String ALERT_STATISTICS_DIRECTORY_TRAVERSAL_ALERT_RULE_ID = "ALERT_STATISTICS_DIRECTORY_TRAVERSAL_ALERT_RULE_ID";
    public static final String ALERT_STATISTICS_DIRECTORY_TRAVERSAL_ASSEMBLY = "ALERT_STATISTICS_DIRECTORY_TRAVERSAL_ASSEMBLY";


    //----------------------------------------------------------------------------------------------
    //向安管平台发送webshell告警配置
    public static final String FLINK_ALERT_STATISTICS_WEBSHELL_CONFIG = "FLINK_ALERT_STATISTICS_WEBSHELL_CONFIG";
    public static final String ALERT_STATISTICS_WEBSHELL_JOB_NAME = "ALERT_STATISTICS_WEBSHELL_JOB_NAME";
    public static final String ALERT_STATISTICS_WEBSHELL_CHECKPOINT_INTERVAL = "ALERT_STATISTICS_WEBSHELL_CHECKPOINT_INTERVAL";
    public static final String ALERT_STATISTICS_WEBSHELL_GROUP_ID = "ALERT_STATISTICS_WEBSHELL_GROUP_ID";
    public static final String ALERT_STATISTICS_WEBSHELL_KAFKA_SOURCE_NAME = "ALERT_STATISTICS_WEBSHELL_KAFKA_SOURCE_NAME";
    public static final String ALERT_STATISTICS_WEBSHELL_KAFKA_SINK_NAME = "ALERT_STATISTICS_WEBSHELL_KAFKA_SINK_NAME";
    public static final String ALERT_STATISTICS_WEBSHELL_KAFKA_SOURCE_PARALLELISM = "ALERT_STATISTICS_WEBSHELL_KAFKA_SOURCE_PARALLELISM";
    public static final String ALERT_STATISTICS_WEBSHELL_DEAL_PARALLELISM = "ALERT_STATISTICS_WEBSHELL_DEAL_PARALLELISM";
    public static final String ALERT_STATISTICS_WEBSHELL_KAFKA_SINK_PARALLELSIM = "ALERT_STATISTICS_WEBSHELL_KAFKA_SINK_PARALLELSIM";
    public static final String ALERT_STATISTICS_WEBSHELL_WINDOW_SIZE = "ALERT_STATISTICS_WEBSHELL_WINDOW_SIZE";
    public static final String ALERT_STATISTICS_WEBSHELL_SLIDE_SIZE = "ALERT_STATISTICS_WEBSHELL_SLIDE_SIZE";
    public static final String ALERT_STATISTICS_WEBSHELL_ALERT_NAME = "ALERT_STATISTICS_WEBSHELL_ALERT_NAME";
    public static final String ALERT_STATISTICS_WEBSHELL_ALERT_TIMESTAMP_FORMAT = "ALERT_STATISTICS_WEBSHELL_ALERT_TIMESTAMP_FORMAT";
    public static final String ALERT_STATISTICS_WEBSHELL_ALERT_TYPE = "ALERT_STATISTICS_WEBSHELL_ALERT_TYPE";
    public static final String ALERT_STATISTICS_WEBSHELL_ALERT_REGION = "ALERT_STATISTICS_WEBSHELL_ALERT_REGION";
    public static final String ALERT_STATISTICS_WEBSHELL_ALERT_BUSINESS = "ALERT_STATISTICS_WEBSHELL_ALERT_BUSINESS";
    public static final String ALERT_STATISTICS_WEBSHELL_ALERT_DOMAIN = "ALERT_STATISTICS_WEBSHELL_ALERT_DOMAIN";
    public static final String ALERT_STATISTICS_WEBSHELL_ALERT_IP = "ALERT_STATISTICS_WEBSHELL_ALERT_IP";
    public static final String ALERT_STATISTICS_WEBSHELL_ALERT_DEVICE = "ALERT_STATISTICS_WEBSHELL_ALERT_DEVICE";
    public static final String ALERT_STATISTICS_WEBSHELL_ALERT_RULE_ID = "ALERT_STATISTICS_WEBSHELL_ALERT_RULE_ID";
    public static final String ALERT_STATISTICS_WEBSHELL_ASSEMBLY = "ALERT_STATISTICS_WEBSHELL_ASSEMBLY";


    //----------------------------------------------------------------------------------------------
    //Land攻击检测
    public static final String FLINK_DDOS_LAND_DETECT_CONFIG = "FLINK_DDOS_LAND_DETECT_CONFIG";
    public static final String DDOS_LAND_DETECT_JOB_NAME = "DDOS_LAND_DETECT_JOB_NAME";
    public static final String DDOS_LAND_DETECT_CHECKPOINT_INTERVAL = "DDOS_LAND_DETECT_CHECKPOINT_INTERVAL";
    public static final String DDOS_LAND_DETECT_GROUP_ID = "DDOS_LAND_DETECT_GROUP_ID";
    public static final String DDOS_LAND_DETECT_KAFKA_SOURCE_NAME = "DDOS_LAND_DETECT_KAFKA_SOURCE_NAME";
    public static final String DDOS_LAND_DETECT_SQL_SINK_NAME = "DDOS_LAND_DETECT_SQL_SINK_NAME";
    public static final String DDOS_LAND_DETECT_KAFKA_SINK_NAME = "DDOS_LAND_DETECT_KAFKA_SINK_NAME";
    public static final String DDOS_LAND_DETECT_KAFKA_SOURCE_PARALLELISM = "DDOS_LAND_DETECT_KAFKA_SOURCE_PARALLELISM";
    public static final String DDOS_LAND_DETECT_DEAL_PARALLELISM = "DDOS_LAND_DETECT_DEAL_PARALLELISM";
    public static final String DDOS_LAND_DETECT_SQL_SINK_PARALLELISM = "DDOS_LAND_DETECT_SQL_SINK_PARALLELISM";
    public static final String DDOS_LAND_DETECT_KAFKA_SINK_PARALLELISM = "DDOS_LAND_DETECT_KAFKA_SINK_PARALLELISM";
    public static final String DDOS_LAND_DETECT_KAFKA_SINK_TOPIC = "DDOS_LAND_DETECT_KAFKA_SINK_TOPIC";


    //----------------------------------------------------------------------------------------------
    //SSDP攻击检测
    public static final String FLINK_DDOS_SSDP_DETECT_CONFIG = "FLINK_DDOS_SSDP_DETECT_CONFIG";
    public static final String DDOS_SSDP_DETECT_JOB_NAME = "DDOS_SSDP_DETECT_JOB_NAME";
    public static final String DDOS_SSDP_DETECT_CHECKPOINT_INTERVAL = "DDOS_SSDP_DETECT_CHECKPOINT_INTERVAL";
    public static final String DDOS_SSDP_DETECT_GROUP_ID = "DDOS_SSDP_DETECT_GROUP_ID";
    public static final String DDOS_SSDP_DETECT_KAFKA_SOURCE_NAME = "DDOS_SSDP_DETECT_KAFKA_SOURCE_NAME";
    public static final String DDOS_SSDP_DETECT_THRESHOLD = "DDOS_SSDP_DETECT_THRESHOLD";
    public static final String DDOS_SSDP_DETECT_SQL_SINK_NAME = "DDOS_SSDP_DETECT_SQL_SINK_NAME";
    public static final String DDOS_SSDP_DETECT_KAFKA_SINK_NAME = "DDOS_SSDP_DETECT_KAFKA_SINK_NAME";
    public static final String DDOS_SSDP_DETECT_KAFKA_SOURCE_PARALLELISM = "DDOS_SSDP_DETECT_KAFKA_SOURCE_PARALLELISM";
    public static final String DDOS_SSDP_DETECT_DEAL_PARALLELISM = "DDOS_SSDP_DETECT_DEAL_PARALLELISM";
    public static final String DDOS_SSDP_DETECT_SQL_SINK_PARALLELISM = "DDOS_SSDP_DETECT_SQL_SINK_PARALLELISM";
    public static final String DDOS_SSDP_DETECT_KAFKA_SINK_PARALLELISM = "DDOS_SSDP_DETECT_KAFKA_SINK_PARALLELISM";
    public static final String DDOS_SSDP_DETECT_KAFKA_SINK_TOPIC = "DDOS_SSDP_DETECT_KAFKA_SINK_TOPIC";


    //----------------------------------------------------------------------------------------------
    //HttpGet攻击检测
    public static final String FLINK_DDOS_HTTP_GET_DETECT_CONFIG = "FLINK_DDOS_HTTP_GET_DETECT_CONFIG";
    public static final String DDOS_HTTP_GET_DETECT_JOB_NAME = "DDOS_HTTP_GET_DETECT_JOB_NAME";
    public static final String DDOS_HTTP_GET_DETECT_CHECKPOINT_INTERVAL = "DDOS_HTTP_GET_DETECT_CHECKPOINT_INTERVAL";
    public static final String DDOS_HTTP_GET_DETECT_GROUP_ID = "DDOS_HTTP_GET_DETECT_GROUP_ID";
    public static final String DDOS_HTTP_GET_DETECT_KAFKA_SOURCE_NAME = "DDOS_HTTP_GET_DETECT_KAFKA_SOURCE_NAME";
    public static final String DDOS_HTTP_GET_DETECT_THRESHOLD = "DDOS_HTTP_GET_DETECT_THRESHOLD";
    public static final String DDOS_HTTP_GET_DETECT_SQL_SINK_NAME = "DDOS_HTTP_GET_DETECT_SQL_SINK_NAME";
    public static final String DDOS_HTTP_GET_DETECT_KAFKA_SINK_NAME = "DDOS_HTTP_GET_DETECT_KAFKA_SINK_NAME";
    public static final String DDOS_HTTP_GET_DETECT_KAFKA_SOURCE_PARALLELISM = "DDOS_HTTP_GET_DETECT_KAFKA_SOURCE_PARALLELISM";
    public static final String DDOS_HTTP_GET_DETECT_DEAL_PARALLELISM = "DDOS_HTTP_GET_DETECT_DEAL_PARALLELISM";
    public static final String DDOS_HTTP_GET_DETECT_SQL_SINK_PARALLELISM = "DDOS_HTTP_GET_DETECT_SQL_SINK_PARALLELISM";
    public static final String DDOS_HTTP_GET_DETECT_KAFKA_SINK_PARALLELISM = "DDOS_HTTP_GET_DETECT_KAFKA_SINK_PARALLELISM";
    public static final String DDOS_HTTP_GET_DETECT_KAFKA_SINK_TOPIC = "DDOS_HTTP_GET_DETECT_KAFKA_SINK_TOPIC";


    //----------------------------------------------------------------------------------------------
    //DDOS攻击发送到风控平台
    public static final String FLINK_ALERT_STATISTICS_DDOS_CONFIG = "FLINK_ALERT_STATISTICS_DDOS_CONFIG";
    public static final String ALERT_STATISTICS_DDOS_JOB_NAME = "ALERT_STATISTICS_DDOS_JOB_NAME";
    public static final String ALERT_STATISTICS_DDOS_CHECKPOINT_INTERVAL = "ALERT_STATISTICS_DDOS_CHECKPOINT_INTERVAL";
    public static final String ALERT_STATISTICS_DDOS_GROUP_ID = "ALERT_STATISTICS_DDOS_GROUP_ID";
    public static final String ALERT_STATISTICS_DDOS_KAFKA_SOURCE_NAME = "ALERT_STATISTICS_DDOS_KAFKA_SOURCE_NAME";
    public static final String ALERT_STATISTICS_DDOS_KAFKA_SOURCE_TOPIC = "ALERT_STATISTICS_DDOS_KAFKA_SOURCE_TOPIC";
    public static final String ALERT_STATISTICS_DDOS_KAFKA_SINK_NAME = "ALERT_STATISTICS_DDOS_KAFKA_SINK_NAME";
    public static final String ALERT_STATISTICS_DDOS_KAFKA_SOURCE_PARALLELISM = "ALERT_STATISTICS_DDOS_KAFKA_SOURCE_PARALLELISM";
    public static final String ALERT_STATISTICS_DDOS_DEAL_PARALLELISM = "ALERT_STATISTICS_DDOS_DEAL_PARALLELISM";
    public static final String ALERT_STATISTICS_DDOS_KAFKA_SINK_PARALLELISM = "ALERT_STATISTICS_DDOS_KAFKA_SINK_PARALLELISM";
    public static final String ALERT_STATISTICS_DDOS_WINDOW_SIZE = "ALERT_STATISTICS_DDOS_WINDOW_SIZE";
    public static final String ALERT_STATISTICS_DDOS_SLIDE_SIZE = "ALERT_STATISTICS_DDOS_SLIDE_SIZE";
    public static final String ALERT_STATISTICS_DDOS_ALERT_NAME = "ALERT_STATISTICS_DDOS_ALERT_NAME";
    public static final String ALERT_STATISTICS_DDOS_ALERT_TIMESTAMP_FORMAT = "ALERT_STATISTICS_DDOS_ALERT_TIMESTAMP_FORMAT";
    public static final String ALERT_STATISTICS_DDOS_ALERT_TYPE = "ALERT_STATISTICS_DDOS_ALERT_TYPE";
    public static final String ALERT_STATISTICS_DDOS_REGION = "ALERT_STATISTICS_DDOS_REGION";
    public static final String ALERT_STATISTICS_DDOS_BUSINESS = "ALERT_STATISTICS_DDOS_BUSINESS";
    public static final String ALERT_STATISTICS_DDOS_DOMAIN = "ALERT_STATISTICS_DDOS_DOMAIN";
    public static final String ALERT_STATISTICS_DDOS_IP = "ALERT_STATISTICS_DDOS_IP";
    public static final String ALERT_STATISTICS_DDOS_DEVICE = "ALERT_STATISTICS_DDOS_DEVICE";
    public static final String ALERT_STATISTICS_DDOS_RULE_ID = "ALERT_STATISTICS_DDOS_RULE_ID";
    public static final String ALERT_STATISTICS_DDOS_ASSEMBLY = "ALERT_STATISTICS_DDOS_ASSEMBLY";


    //----------------------------------------------------------------------------------------------
    //异常流量分析
    public static final String FLINK_ABNORMAL_FLOW_CONFIG = "FLINK_ABNORMAL_FLOW_CONFIG";
    public static final String FLINK_ABNORMAL_FLOW_JOB_NAME = "FLINK_ABNORMAL_FLOW_JOB_NAME";
    public static final String FLINK_ABNORMAL_FLOW_CHECKPOINT_INTERVAL = "FLINK_ABNORMAL_FLOW_CHECKPOINT_INTERVAL";
    public static final String FLINK_ABNORMAL_FLOW_GROUP_ID = "FLINK_ABNORMAL_FLOW_GROUP_ID";
    public static final String FLINK_ABNORMAL_FLOW_KAFKA_SOURCE_NAME = "FLINK_ABNORMAL_FLOW_KAFKA_SOURCE_NAME";
    public static final String FLINK_ABNORMAL_FLOW_KAFKA_SOURCE_PARALLELISM = "FLINK_ABNORMAL_FLOW_KAFKA_SOURCE_PARALLELISM";
    public static final String FLINK_ABNORMAL_FLOW_DEAL_PARALLELISM = "FLINK_ABNORMAL_FLOW_DEAL_PARALLELISM";
    public static final String FLINK_ABNORMAL_FLOW_KAFKA_SINK_NAME = "FLINK_ABNORMAL_FLOW_KAFKA_SINK_NAME";
    public static final String FLINK_ABNORMAL_FLOW_KAFKA_SINK_PARALLELISM = "FLINK_ABNORMAL_FLOW_KAFKA_SINK_PARALLELISM";
    public static final String FLINK_ABNORMAL_FLOW_KAFKA_SINK_TOPIC = "FLINK_ABNORMAL_FLOW_KAFKA_SINK_TOPIC";
    public static final String FLINK_ABNORMAL_FLOW_SQL_SINK_NAME = "FLINK_ABNORMAL_FLOW_SQL_SINK_NAME";
    public static final String FLINK_ABNORMAL_FLOW_SINK_PARALLELISM = "FLINK_ABNORMAL_FLOW_SINK_PARALLELISM";
    public static final String FLINK_ABNORMAL_FLOW_SQL_SINK_PARALLELISM = "FLINK_ABNORMAL_FLOW_SQL_SINK_PARALLELISM";
    public static final String FLINK_ABNORMAL_FLOW_STATISTICS_LENGTH = "FLINK_ABNORMAL_FLOW_STATISTICS_LENGTH";
    public static final String FLINK_ABNORMAL_FLOW_THRESHOLD_RATIO = "FLINK_ABNORMAL_FLOW_THRESHOLD_RATIO";
    public static final String FLINK_ABNORMAL_FLOW_BASE_COUNT = "FLINK_ABNORMAL_FLOW_BASE_COUNT";


    //----------------------------------------------------------------------------------------------
    //异常流量告警发送到风控平台
    public static final String FLINK_ALERT_STATISTICS_ABNORMAL_FLOW_CONFIG = "FLINK_ALERT_STATISTICS_ABNORMAL_FLOW_CONFIG";
    public static final String ALERT_STATISTICS_ABNORMAL_FLOW_JOB_NAME = "ALERT_STATISTICS_ABNORMAL_FLOW_JOB_NAME";
    public static final String ALERT_STATISTICS_ABNORMAL_FLOW_CHECKPOINT_INTERVAL = "ALERT_STATISTICS_ABNORMAL_FLOW_CHECKPOINT_INTERVAL";
    public static final String ALERT_STATISTICS_ABNORMAL_FLOW_GROUP_ID = "ALERT_STATISTICS_ABNORMAL_FLOW_GROUP_ID";
    public static final String ALERT_STATISTICS_ABNORMAL_FLOW_KAFKA_SOURCE_NAME = "ALERT_STATISTICS_ABNORMAL_FLOW_KAFKA_SOURCE_NAME";
    public static final String ALERT_STATISTICS_ABNORMAL_FLOW_KAFKA_SINK_NAME = "ALERT_STATISTICS_ABNORMAL_FLOW_KAFKA_SINK_NAME";
    public static final String ALERT_STATISTICS_ABNORMAL_FLOW_KAFKA_SOURCE_PARALLELISM = "ALERT_STATISTICS_ABNORMAL_FLOW_KAFKA_SOURCE_PARALLELISM";
    public static final String ALERT_STATISTICS_ABNORMAL_FLOW_DEAL_PARALLELISM = "ALERT_STATISTICS_ABNORMAL_FLOW_DEAL_PARALLELISM";
    public static final String ALERT_STATISTICS_ABNORMAL_FLOW_KAFKA_SINK_PARALLELISM = "ALERT_STATISTICS_ABNORMAL_FLOW_KAFKA_SINK_PARALLELISM";
    public static final String ALERT_STATISTICS_ABNORMAL_FLOW_WINDOW_SIZE = "ALERT_STATISTICS_ABNORMAL_FLOW_WINDOW_SIZE";
    public static final String ALERT_STATISTICS_ABNORMAL_FLOW_SLIDE_SIZE = "ALERT_STATISTICS_ABNORMAL_FLOW_SLIDE_SIZE";
    public static final String ALERT_STATISTICS_ABNORMAL_FLOW_ALERT_NAME = "ALERT_STATISTICS_ABNORMAL_FLOW_ALERT_NAME";
    public static final String ALERT_STATISTICS_ABNORMAL_FLOW_ALERT_TIMESTAMP_FORMAT = "ALERT_STATISTICS_ABNORMAL_FLOW_ALERT_TIMESTAMP_FORMAT";
    public static final String ALERT_STATISTICS_ABNORMAL_FLOW_ALERT_TYPE = "ALERT_STATISTICS_ABNORMAL_FLOW_ALERT_TYPE";
    public static final String ALERT_STATISTICS_ABNORMAL_FLOW_REGION = "ALERT_STATISTICS_ABNORMAL_FLOW_REGION";
    public static final String ALERT_STATISTICS_ABNORMAL_FLOW_BUSINESS = "ALERT_STATISTICS_ABNORMAL_FLOW_BUSINESS";
    public static final String ALERT_STATISTICS_ABNORMAL_FLOW_DOMAIN = "ALERT_STATISTICS_ABNORMAL_FLOW_DOMAIN";
    public static final String ALERT_STATISTICS_ABNORMAL_FLOW_IP = "ALERT_STATISTICS_ABNORMAL_FLOW_IP";
    public static final String ALERT_STATISTICS_ABNORMAL_FLOW_DEVICE = "ALERT_STATISTICS_ABNORMAL_FLOW_DEVICE";
    public static final String ALERT_STATISTICS_ABNORMAL_FLOW_RULE_ID = "ALERT_STATISTICS_ABNORMAL_FLOW_RULE_ID";
    public static final String ALERT_STATISTICS_ABNORMAL_FLOW_ASSEMBLY = "ALERT_STATISTICS_ABNORMAL_FLOW_ASSEMBLY";


    //----------------------------------------------------------------------------------------------
    //检测用户是否为爬虫行为 发送到风控平台
    public static final String ALERT_CRAWLER_DETECT_CONFIG = "ALERT_CRAWLER_DETECT_CONFIG";
    public static final String ALERT_CRAWLER_DETECT_JOB_NAME = "ALERT_CRAWLER_DETECT_JOB_NAME";
    public static final String ALERT_CRAWLER_DETECT_CHECKPOINT_INTERVAL = "ALERT_CRAWLER_DETECT_CHECKPOINT_INTERVAL";
    public static final String ALERT_CRAWLER_DETECT_GROUP_ID = "ALERT_CRAWLER_DETECT_GROUP_ID";
    public static final String ALERT_CRAWLER_DETECT_KAFKA_SOURCE_NAME = "ALERT_CRAWLER_DETECT_KAFKA_SOURCE_NAME";
    public static final String ALERT_CRAWLER_DETECT_KAFKA_SOURCE_TOPIC = "ALERT_CRAWLER_DETECT_KAFKA_SOURCE_TOPIC";
    public static final String ALERT_CRAWLER_DETECT_KAFKA_SINK_NAME = "ALERT_CRAWLER_DETECT_KAFKA_SINK_NAME";
    public static final String ALERT_CRAWLER_DETECT_KAFKA_SOURCE_PARALLELISM = "ALERT_CRAWLER_DETECT_KAFKA_SOURCE_PARALLELISM";
    public static final String ALERT_CRAWLER_DETECT_DEAL_PARALLELISM = "ALERT_CRAWLER_DETECT_DEAL_PARALLELISM";
    public static final String ALERT_CRAWLER_DETECT_KAFKA_SINK_PARALLELISM = "ALERT_CRAWLER_DETECT_KAFKA_SINK_PARALLELISM";
    public static final String ALERT_CRAWLER_DETECT_ALERT_NAME = "ALERT_CRAWLER_DETECT_ALERT_NAME";
    public static final String ALERT_CRAWLER_DETECT_ALERT_TIMESTAMP_FORMAT = "ALERT_CRAWLER_DETECT_ALERT_TIMESTAMP_FORMAT";
    public static final String ALERT_CRAWLER_DETECT_ALERT_TYPE = "ALERT_CRAWLER_DETECT_ALERT_TYPE";
    public static final String ALERT_CRAWLER_DETECT_REGION = "ALERT_CRAWLER_DETECT_REGION";
    public static final String ALERT_CRAWLER_DETECT_BUSINESS = "ALERT_CRAWLER_DETECT_BUSINESS";
    public static final String ALERT_CRAWLER_DETECT_DOMAIN = "ALERT_CRAWLER_DETECT_DOMAIN";
    public static final String ALERT_CRAWLER_DETECT_IP = "ALERT_CRAWLER_DETECT_IP";
    public static final String ALERT_CRAWLER_DETECT_DEVICE = "ALERT_CRAWLER_DETECT_DEVICE";
    public static final String ALERT_CRAWLER_DETECT_RULE_ID = "ALERT_CRAWLER_DETECT_RULE_ID";
    public static final String ALERT_CRAWLER_DETECT_ASSEMBLY = "ALERT_CRAWLER_DETECT_ASSEMBLY";


    //----------------------------------------------------------------------------------------------
    //异常状态的用户告警 发送到风控平台
    public static final String ALERT_ABNORMAL_STATUS_USER_CONFIG = "ALERT_ABNORMAL_STATUS_USER_CONFIG";
    public static final String ALERT_ABNORMAL_STATUS_USER_JOB_NAME = "ALERT_ABNORMAL_STATUS_USER_JOB_NAME";
    public static final String ALERT_ABNORMAL_STATUS_USER_CHECKPOINT_INTERVAL = "ALERT_ABNORMAL_STATUS_USER_CHECKPOINT_INTERVAL";
    public static final String ALERT_ABNORMAL_STATUS_USER_GROUP_ID = "ALERT_ABNORMAL_STATUS_USER_GROUP_ID";
    public static final String ALERT_ABNORMAL_STATUS_USER_KAFKA_SOURCE_NAME = "ALERT_ABNORMAL_STATUS_USER_KAFKA_SOURCE_NAME";
    public static final String ALERT_ABNORMAL_STATUS_USER_KAFKA_SOURCE_TOPIC = "ALERT_ABNORMAL_STATUS_USER_KAFKA_SOURCE_TOPIC";
    public static final String ALERT_ABNORMAL_STATUS_USER_KAFKA_SINK_NAME = "ALERT_ABNORMAL_STATUS_USER_KAFKA_SINK_NAME";
    public static final String ALERT_ABNORMAL_STATUS_USER_KAFKA_SOURCE_PARALLELISM = "ALERT_ABNORMAL_STATUS_USER_KAFKA_SOURCE_PARALLELISM";
    public static final String ALERT_ABNORMAL_STATUS_USER_DEAL_PARALLELISM = "ALERT_ABNORMAL_STATUS_USER_DEAL_PARALLELISM";
    public static final String ALERT_ABNORMAL_STATUS_USER_KAFKA_SINK_PARALLELISM = "ALERT_ABNORMAL_STATUS_USER_KAFKA_SINK_PARALLELISM";
    public static final String ALERT_ABNORMAL_STATUS_USER_ALERT_NAME = "ALERT_ABNORMAL_STATUS_USER_ALERT_NAME";
    public static final String ALERT_ABNORMAL_STATUS_USER_ALERT_TIMESTAMP_FORMAT = "ALERT_ABNORMAL_STATUS_USER_ALERT_TIMESTAMP_FORMAT";
    public static final String ALERT_ABNORMAL_STATUS_USER_ALERT_TYPE = "ALERT_ABNORMAL_STATUS_USER_ALERT_TYPE";
    public static final String ALERT_ABNORMAL_STATUS_USER_REGION = "ALERT_ABNORMAL_STATUS_USER_REGION";
    public static final String ALERT_ABNORMAL_STATUS_USER_BUSINESS = "ALERT_ABNORMAL_STATUS_USER_BUSINESS";
    public static final String ALERT_ABNORMAL_STATUS_USER_DOMAIN = "ALERT_ABNORMAL_STATUS_USER_DOMAIN";
    public static final String ALERT_ABNORMAL_STATUS_USER_IP = "ALERT_ABNORMAL_STATUS_USER_IP";
    public static final String ALERT_ABNORMAL_STATUS_USER_DEVICE = "ALERT_ABNORMAL_STATUS_USER_DEVICE";
    public static final String ALERT_ABNORMAL_STATUS_USER_RULE_ID = "ALERT_ABNORMAL_STATUS_USER_RULE_ID";
    public static final String ALERT_ABNORMAL_STATUS_USER_ASSEMBLY = "ALERT_ABNORMAL_STATUS_USER_ASSEMBLY";


    //----------------------------------------------------------------------------------------------
    //将 同一时间多地登录告警 放入风控
    public static final String ALERT_SAME_TIME_DIFFERENT_PLACE_WARN_CONFIG = "ALERT_SAME_TIME_DIFFERENT_PLACE_WARN_CONFIG";
    public static final String ALERT_SAME_TIME_DIFFERENT_PLACE_WARN_JOB_NAME = "ALERT_SAME_TIME_DIFFERENT_PLACE_WARN_JOB_NAME";
    public static final String ALERT_SAME_TIME_DIFFERENT_PLACE_WARN_CHECKPOINT_INTERVAL = "ALERT_SAME_TIME_DIFFERENT_PLACE_WARN_CHECKPOINT_INTERVAL";
    public static final String ALERT_SAME_TIME_DIFFERENT_PLACE_WARN_GROUP_ID = "ALERT_SAME_TIME_DIFFERENT_PLACE_WARN_GROUP_ID";
    public static final String ALERT_SAME_TIME_DIFFERENT_PLACE_WARN_KAFKA_SOURCE_NAME = "ALERT_SAME_TIME_DIFFERENT_PLACE_WARN_KAFKA_SOURCE_NAME";
    public static final String ALERT_SAME_TIME_DIFFERENT_PLACE_WARN_KAFKA_SOURCE_TOPIC = "ALERT_SAME_TIME_DIFFERENT_PLACE_WARN_KAFKA_SOURCE_TOPIC";
    public static final String ALERT_SAME_TIME_DIFFERENT_PLACE_WARN_KAFKA_SINK_NAME = "ALERT_SAME_TIME_DIFFERENT_PLACE_WARN_KAFKA_SINK_NAME";
    public static final String ALERT_SAME_TIME_DIFFERENT_PLACE_WARN_KAFKA_SOURCE_PARALLELISM = "ALERT_SAME_TIME_DIFFERENT_PLACE_WARN_KAFKA_SOURCE_PARALLELISM";
    public static final String ALERT_SAME_TIME_DIFFERENT_PLACE_WARN_DEAL_PARALLELISM = "ALERT_SAME_TIME_DIFFERENT_PLACE_WARN_DEAL_PARALLELISM";
    public static final String ALERT_SAME_TIME_DIFFERENT_PLACE_WARN_KAFKA_SINK_PARALLELISM = "ALERT_SAME_TIME_DIFFERENT_PLACE_WARN_KAFKA_SINK_PARALLELISM";
    public static final String ALERT_SAME_TIME_DIFFERENT_PLACE_WARN_ALERT_NAME = "ALERT_SAME_TIME_DIFFERENT_PLACE_WARN_ALERT_NAME";
    public static final String ALERT_SAME_TIME_DIFFERENT_PLACE_WARN_ALERT_TIMESTAMP_FORMAT = "ALERT_SAME_TIME_DIFFERENT_PLACE_WARN_ALERT_TIMESTAMP_FORMAT";
    public static final String ALERT_SAME_TIME_DIFFERENT_PLACE_WARN_ALERT_TYPE = "ALERT_SAME_TIME_DIFFERENT_PLACE_WARN_ALERT_TYPE";
    public static final String ALERT_SAME_TIME_DIFFERENT_PLACE_WARN_REGION = "ALERT_SAME_TIME_DIFFERENT_PLACE_WARN_REGION";
    public static final String ALERT_SAME_TIME_DIFFERENT_PLACE_WARN_BUSINESS = "ALERT_SAME_TIME_DIFFERENT_PLACE_WARN_BUSINESS";
    public static final String ALERT_SAME_TIME_DIFFERENT_PLACE_WARN_DOMAIN = "ALERT_SAME_TIME_DIFFERENT_PLACE_WARN_DOMAIN";
    public static final String ALERT_SAME_TIME_DIFFERENT_PLACE_WARN_IP = "ALERT_SAME_TIME_DIFFERENT_PLACE_WARN_IP";
    public static final String ALERT_SAME_TIME_DIFFERENT_PLACE_WARN_DEVICE = "ALERT_SAME_TIME_DIFFERENT_PLACE_WARN_DEVICE";
    public static final String ALERT_SAME_TIME_DIFFERENT_PLACE_WARN_RULE_ID = "ALERT_SAME_TIME_DIFFERENT_PLACE_WARN_RULE_ID";
    public static final String ALERT_SAME_TIME_DIFFERENT_PLACE_WARN_ASSEMBLY = "ALERT_SAME_TIME_DIFFERENT_PLACE_WARN_ASSEMBLY";


    //----------------------------------------------------------------------------------------------
    //读取 kelai_dns topic 解析成json 串
    public static final String DNS_KELAI_FLINK_TO_KAFKA_CONFIG = "DNS_KELAI_FLINK_TO_KAFKA_CONFIG";
    public static final String DNS_KELAI_JOB_NAME = "DNS_KELAI_JOB_NAME";
    public static final String DNS_KELAI_CHECKPOINT_INTERVAL = "DNS_KELAI_CHECKPOINT_INTERVAL";
    public static final String DNS_KELAI_GROUP_ID = "DNS_KELAI_GROUP_ID";
    public static final String DNS_KELAI_KAFKA_SOURCE_NAME = "DNS_KELAI_KAFKA_SOURCE_NAME";
    public static final String DNS_KELAI_KAFKA_SOURCE_PARALLELISM = "DNS_KELAI_KAFKA_SOURCE_PARALLELISM";
    public static final String DNS_KELAI_KAFKA_SOURCE_TOPIC = "DNS_KELAI_KAFKA_SOURCE_TOPIC";
    public static final String DNS_KELAI_KAFKA_SINK_NAME = "DNS_KELAI_KAFKA_SINK_NAME";
    public static final String DNS_KELAI_KAFKA_SINK_PARALLELISM = "DNS_KELAI_KAFKA_SINK_PARALLELISM";
    public static final String DNS_KELAI_KAFKA_SINK_TOPIC = "DNS_KELAI_KAFKA_SINK_TOPIC";
    public static final String DNS_KELAI_DEAL_PARALLELISM = "DNS_KELAI_DEAL_PARALLELISM";
    public static final String DNS_KELAI_LENGTH = "DNS_KELAI_LENGTH";


    //----------------------------------------------------------------------------------------------
    //读取 kelai_option topic 解析成json 串
    public static final String OPERATION_KELAI_FLINK_TO_KAFKA_CONFIG = "OPERATION_KELAI_FLINK_TO_KAFKA_CONFIG";
    public static final String OPERATION_KELAI_JOB_NAME = "OPERATION_KELAI_JOB_NAME";
    public static final String OPERATION_KELAI_CHECKPOINT_INTERVAL = "OPERATION_KELAI_CHECKPOINT_INTERVAL";
    public static final String OPERATION_KELAI_GROUP_ID = "OPERATION_KELAI_GROUP_ID";
    public static final String OPERATION_KELAI_KAFKA_SOURCE_NAME = "OPERATION_KELAI_KAFKA_SOURCE_NAME";
    public static final String OPERATION_KELAI_KAFKA_SOURCE_PARALLELISM = "OPERATION_KELAI_KAFKA_SOURCE_PARALLELISM";
    public static final String OPERATION_KELAI_KAFKA_SOURCE_TOPIC = "OPERATION_KELAI_KAFKA_SOURCE_TOPIC";
    public static final String OPERATION_KELAI_KAFKA_SINK_NAME = "OPERATION_KELAI_KAFKA_SINK_NAME";
    public static final String OPERATION_KELAI_KAFKA_SINK_PARALLELISM = "OPERATION_KELAI_KAFKA_SINK_PARALLELISM";
    public static final String OPERATION_KELAI_KAFKA_SINK_TOPIC = "OPERATION_KELAI_KAFKA_SINK_TOPIC";
    public static final String OPERATION_KELAI_DEAL_PARALLELISM = "OPERATION_KELAI_DEAL_PARALLELISM";
    public static final String OPERATION_KELAI_LENGTH = "OPERATION_KELAI_LENGTH";


    //----------------------------------------------------------------------------------------------
    //使用Flink将科莱五元组话单写入Kafka
    public static final String QUINTET_KELAI_FLINK_TO_KAFKA_CONFIG = "QUINTET_KELAI_FLINK_TO_KAFKA_CONFIG";
    public static final String QUINTET_KELAI_JOB_NAME = "QUINTET_KELAI_JOB_NAME";
    public static final String QUINTET_KELAI_CHECKPOINT_INTERVAL = "QUINTET_KELAI_CHECKPOINT_INTERVAL";
    public static final String QUINTET_KELAI_GROUP_ID = "QUINTET_KELAI_GROUP_ID";
    public static final String QUINTET_KELAI_KAFKA_SOURCE_NAME = "QUINTET_KELAI_KAFKA_SOURCE_NAME";
    public static final String QUINTET_KELAI_KAFKA_SOURCE_PARALLELISM = "QUINTET_KELAI_KAFKA_SOURCE_PARALLELISM";
    public static final String QUINTET_KELAI_KAFKA_SOURCE_TOPIC = "QUINTET_KELAI_KAFKA_SOURCE_TOPIC";
    public static final String QUINTET_KELAI_KAFKA_SINK_NAME = "QUINTET_KELAI_KAFKA_SINK_NAME";
    public static final String QUINTET_KELAI_KAFKA_SINK_PARALLELISM = "QUINTET_KELAI_KAFKA_SINK_PARALLELISM";
    public static final String QUINTET_KELAI_KAFKA_SINK_TOPIC = "QUINTET_KELAI_KAFKA_SINK_TOPIC";
    public static final String QUINTET_KELAI_DEAL_PARALLELISM = "QUINTET_KELAI_DEAL_PARALLELISM";
    public static final String QUINTET_KELAI_LENGTH = "QUINTET_KELAI_LENGTH";


    //----------------------------------------------------------------------------------------------
    //异常流量分析
    public static final String FLINK_ALERT_STATISTICS_ABNORMAL_USER_CONFIG = "FLINK_ALERT_STATISTICS_ABNORMAL_USER_CONFIG";
    public static final String ALERT_STATISTICS_ABNORMAL_USER_JOB_NAME = "ALERT_STATISTICS_ABNORMAL_USER_JOB_NAME";
    public static final String ALERT_STATISTICS_ABNORMAL_USER_CHECKPOINT_INTERVAL = "ALERT_STATISTICS_ABNORMAL_USER_CHECKPOINT_INTERVAL";
    public static final String ALERT_STATISTICS_ABNORMAL_USER_GROUP_ID = "ALERT_STATISTICS_ABNORMAL_USER_GROUP_ID";
    public static final String ALERT_STATISTICS_ABNORMAL_USER_KAFKA_SOURCE_NAME = "ALERT_STATISTICS_ABNORMAL_USER_KAFKA_SOURCE_NAME";
    public static final String ALERT_STATISTICS_ABNORMAL_USER_KAFKA_SOURCE_TOPIC = "ALERT_STATISTICS_ABNORMAL_USER_KAFKA_SOURCE_TOPIC";
    public static final String ALERT_STATISTICS_ABNORMAL_USER_KAFKA_SINK_NAME = "ALERT_STATISTICS_ABNORMAL_USER_KAFKA_SINK_NAME";
    public static final String ALERT_STATISTICS_ABNORMAL_USER_KAFKA_SOURCE_PARALLELISM = "ALERT_STATISTICS_ABNORMAL_USER_KAFKA_SOURCE_PARALLELISM";
    public static final String ALERT_STATISTICS_ABNORMAL_USER_DEAL_PARALLELISM = "ALERT_STATISTICS_ABNORMAL_USER_DEAL_PARALLELISM";
    public static final String ALERT_STATISTICS_ABNORMAL_USER_KAFKA_SINK_PARALLELISM = "ALERT_STATISTICS_ABNORMAL_USER_KAFKA_SINK_PARALLELISM";
    public static final String ALERT_STATISTICS_ABNORMAL_USER_WINDOW_SIZE = "ALERT_STATISTICS_ABNORMAL_USER_WINDOW_SIZE";
    public static final String ALERT_STATISTICS_ABNORMAL_USER_SLIDE_SIZE = "ALERT_STATISTICS_ABNORMAL_USER_SLIDE_SIZE";
    public static final String ALERT_STATISTICS_ABNORMAL_USER_ALERT_NAME = "ALERT_STATISTICS_ABNORMAL_USER_ALERT_NAME";
    public static final String ALERT_STATISTICS_ABNORMAL_USER_ALERT_TIMESTAMP_FORMAT = "ALERT_STATISTICS_ABNORMAL_USER_ALERT_TIMESTAMP_FORMAT";
    public static final String ALERT_STATISTICS_ABNORMAL_USER_ALERT_TYPE = "ALERT_STATISTICS_ABNORMAL_USER_ALERT_TYPE";
    public static final String ALERT_STATISTICS_ABNORMAL_USER_ALERT_REGION = "ALERT_STATISTICS_ABNORMAL_USER_ALERT_REGION";
    public static final String ALERT_STATISTICS_ABNORMAL_USER_ALERT_BUSINESS = "ALERT_STATISTICS_ABNORMAL_USER_ALERT_BUSINESS";
    public static final String ALERT_STATISTICS_ABNORMAL_USER_ALERT_DOMAIN = "ALERT_STATISTICS_ABNORMAL_USER_ALERT_DOMAIN";
    public static final String ALERT_STATISTICS_ABNORMAL_USER_ALERT_IP = "ALERT_STATISTICS_ABNORMAL_USER_ALERT_IP";
    public static final String ALERT_STATISTICS_ABNORMAL_USER_ALERT_DEVICE = "ALERT_STATISTICS_ABNORMAL_USER_ALERT_DEVICE";
    public static final String ALERT_STATISTICS_ABNORMAL_USER_ALERT_RULE_ID = "ALERT_STATISTICS_ABNORMAL_USER_ALERT_RULE_ID";
    public static final String ALERT_STATISTICS_ABNORMAL_USER_ALERT_ASSEMBLY = "ALERT_STATISTICS_ABNORMAL_USER_ALERT_ASSEMBLY";


    //----------------------------------------------------------------------------------------------
    //异常流量分析
    public static final String FLINK_ALERT_STATISTICS_ABNORMAL_SYSTEM_CONFIG = "FLINK_ALERT_STATISTICS_ABNORMAL_SYSTEM_CONFIG";
    public static final String ALERT_STATISTICS_ABNORMAL_SYSTEM_JOB_NAME = "ALERT_STATISTICS_ABNORMAL_SYSTEM_JOB_NAME";
    public static final String ALERT_STATISTICS_ABNORMAL_SYSTEM_CHECKPOINT_INTERVAL = "ALERT_STATISTICS_ABNORMAL_SYSTEM_CHECKPOINT_INTERVAL";
    public static final String ALERT_STATISTICS_ABNORMAL_SYSTEM_GROUP_ID = "ALERT_STATISTICS_ABNORMAL_SYSTEM_GROUP_ID";
    public static final String ALERT_STATISTICS_ABNORMAL_SYSTEM_KAFKA_SOURCE_NAME = "ALERT_STATISTICS_ABNORMAL_SYSTEM_KAFKA_SOURCE_NAME";
    public static final String ALERT_STATISTICS_ABNORMAL_SYSTEM_KAFKA_SOURCE_TOPIC = "ALERT_STATISTICS_ABNORMAL_SYSTEM_KAFKA_SOURCE_TOPIC";
    public static final String ALERT_STATISTICS_ABNORMAL_SYSTEM_KAFKA_SINK_NAME = "ALERT_STATISTICS_ABNORMAL_SYSTEM_KAFKA_SINK_NAME";
    public static final String ALERT_STATISTICS_ABNORMAL_SYSTEM_KAFKA_SOURCE_PARALLELISM = "ALERT_STATISTICS_ABNORMAL_SYSTEM_KAFKA_SOURCE_PARALLELISM";
    public static final String ALERT_STATISTICS_ABNORMAL_SYSTEM_DEAL_PARALLELISM = "ALERT_STATISTICS_ABNORMAL_SYSTEM_DEAL_PARALLELISM";
    public static final String ALERT_STATISTICS_ABNORMAL_SYSTEM_KAFKA_SINK_PARALLELISM = "ALERT_STATISTICS_ABNORMAL_SYSTEM_KAFKA_SINK_PARALLELISM";
    public static final String ALERT_STATISTICS_ABNORMAL_SYSTEM_WINDOW_SIZE = "ALERT_STATISTICS_ABNORMAL_SYSTEM_WINDOW_SIZE";
    public static final String ALERT_STATISTICS_ABNORMAL_SYSTEM_SLIDE_SIZE = "ALERT_STATISTICS_ABNORMAL_SYSTEM_SLIDE_SIZE";
    public static final String ALERT_STATISTICS_ABNORMAL_SYSTEM_ALERT_NAME = "ALERT_STATISTICS_ABNORMAL_SYSTEM_ALERT_NAME";
    public static final String ALERT_STATISTICS_ABNORMAL_SYSTEM_ALERT_TIMESTAMP_FORMAT = "ALERT_STATISTICS_ABNORMAL_SYSTEM_ALERT_TIMESTAMP_FORMAT";
    public static final String ALERT_STATISTICS_ABNORMAL_SYSTEM_ALERT_TYPE = "ALERT_STATISTICS_ABNORMAL_SYSTEM_ALERT_TYPE";
    public static final String ALERT_STATISTICS_ABNORMAL_SYSTEM_ALERT_REGION = "ALERT_STATISTICS_ABNORMAL_SYSTEM_ALERT_REGION";
    public static final String ALERT_STATISTICS_ABNORMAL_SYSTEM_ALERT_BUSINESS = "ALERT_STATISTICS_ABNORMAL_SYSTEM_ALERT_BUSINESS";
    public static final String ALERT_STATISTICS_ABNORMAL_SYSTEM_ALERT_DOMAIN = "ALERT_STATISTICS_ABNORMAL_SYSTEM_ALERT_DOMAIN";
    public static final String ALERT_STATISTICS_ABNORMAL_SYSTEM_ALERT_IP = "ALERT_STATISTICS_ABNORMAL_SYSTEM_ALERT_IP";
    public static final String ALERT_STATISTICS_ABNORMAL_SYSTEM_ALERT_DEVICE = "ALERT_STATISTICS_ABNORMAL_SYSTEM_ALERT_DEVICE";
    public static final String ALERT_STATISTICS_ABNORMAL_SYSTEM_ALERT_RULE_ID = "ALERT_STATISTICS_ABNORMAL_SYSTEM_ALERT_RULE_ID";
    public static final String ALERT_STATISTICS_ABNORMAL_SYSTEM_ALERT_ASSEMBLY = "ALERT_STATISTICS_ABNORMAL_SYSTEM_ALERT_ASSEMBLY";


    //----------------------------------------------------------------------------------------------
    //慢速扫描
    public static final String LOW_VELOCITY_SCAN_CONFIG = "LOW_VELOCITY_SCAN_CONFIG";
    public static final String LOW_VELOCITY_SCAN_JOB_NAME = "LOW_VELOCITY_SCAN_JOB_NAME";
    public static final String LOW_VELOCITY_SCAN_GROUP_ID = "LOW_VELOCITY_SCAN_GROUP_ID";
    public static final String LOW_VELOCITY_SCAN_CHECKPOINT_INTERVAL = "LOW_VELOCITY_SCAN_CHECKPOINT_INTERVAL";
    public static final String LOW_VELOCITY_SCAN_SOURCE_PARALLELISM = "LOW_VELOCITY_SCAN_SOURCE_PARALLELISM";
    public static final String LOW_VELOCITY_SCAN_SINK_PARALLELISM = "LOW_VELOCITY_SCAN_SINK_PARALLELISM";
    public static final String LOW_VELOCITY_SCAN_DEAL_PARALLELISM = "LOW_VELOCITY_SCAN_DEAL_PARALLELISM";
    public static final String LOW_VELOCITY_SCAN_KAFKA_SOURCE_TOPIC = "LOW_VELOCITY_SCAN_KAFKA_SOURCE_TOPIC";
    public static final String LOW_VELOCITY_SCAN_KAFKA_SINK_TOPIC = "LOW_VELOCITY_SCAN_KAFKA_SINK_TOPIC";
    public static final String LOW_VELOCITY_SCAN_KAFKA_SINK_NAME = "LOW_VELOCITY_SCAN_KAFKA_SINK_NAME";
    public static final String LOW_VELOCITY_SCAN_KAFKA_SINK_PARALLELISM = "LOW_VELOCITY_SCAN_KAFKA_SINK_PARALLELISM";


    //水平扫描
    public static final String LOW_VELOCITY_SCAN_LEVEL_STATISTICS_TIMEWINDOW = "LOW_VELOCITY_SCAN_LEVEL_STATISTICS_TIMEWINDOW";
    public static final String LOW_VELOCITY_SCAN_LEVEL_FILTER_SIZE = "LOW_VELOCITY_SCAN_LEVEL_FILTER_SIZE";
    public static final String LOW_VELOCITY_SCAN_LEVEL_STATISTICS_TIMESTEP_SIZE_MAX = "LOW_VELOCITY_SCAN_LEVEL_STATISTICS_TIMESTEP_SIZE_MAX";
    public static final String LOW_VELOCITY_SCAN_LEVEL_COMENTROPY = "LOW_VELOCITY_SCAN_LEVEL_COMENTROPY";
    public static final String LOW_VELOCITY_SCAN_LEVEL_INPUTOCTES_PROBABILITY = "LOW_VELOCITY_SCAN_LEVEL_INPUTOCTES_PROBABILITY";
    public static final String LOW_VELOCITY_SCAN_LEVEL_STATISTICS_TIMEWINDOW_COUNT = "LOW_VELOCITY_SCAN_LEVEL_STATISTICS_TIMEWINDOW_COUNT";
    //垂直
    public static final String LOW_VELOCITY_SCAN_VERTICAL_STATISTICS_TIMEWINDOW = "LOW_VELOCITY_SCAN_VERTICAL_STATISTICS_TIMEWINDOW";
    public static final String LOW_VELOCITY_SCAN_VERTICAL_FILTER_SIZE = "LOW_VELOCITY_SCAN_VERTICAL_FILTER_SIZE";
    public static final String LOW_VELOCITY_SCAN_VERTICAL_STATISTICS_TIMESTEP_SIZE_MAX = "LOW_VELOCITY_SCAN_VERTICAL_STATISTICS_TIMESTEP_SIZE_MAX";
    public static final String LOW_VELOCITY_SCAN_VERTICAL_COMENTROPY = "LOW_VELOCITY_SCAN_VERTICAL_STATISTICS_TIMESTEP_SIZE_MAX";
    public static final String LOW_VELOCITY_SCAN_VERTICAL_INPUTOCTES_PROBABILITY = "LOW_VELOCITY_SCAN_VERTICAL_INPUTOCTES_PROBABILITY";
    public static final String LOW_VELOCITY_SCAN_VERTICAL_STATISTICS_TIMEWINDOW_COUNT = "LOW_VELOCITY_SCAN_VERTICAL_STATISTICS_TIMEWINDOW_COUNT";


    //----------------------------------------------------------------------------------------------
    //主动外联检测
    public static final String ACTIVE_OUTREACH_ANALYZE_CONFIG = "ACTIVE_OUTREACH_ANALYZE_CONFIG";
    public static final String ACTIVE_OUTREACH_ANALYZE_JOB_NAME = "ACTIVE_OUTREACH_ANALYZE_JOB_NAME";
    public static final String ACTIVE_OUTREACH_ANALYZE_GROUP_ID = "ACTIVE_OUTREACH_ANALYZE_GROUP_ID";
    public static final String ACTIVE_OUTREACH_ANALYZE_CHECKPOINT_INTERVAL = "ACTIVE_OUTREACH_ANALYZE_CHECKPOINT_INTERVAL";
    public static final String ACTIVE_OUTREACH_ANALYZE_SOURCE_PARALLELISM = "ACTIVE_OUTREACH_ANALYZE_SOURCE_PARALLELISM";
    public static final String ACTIVE_OUTREACH_ANALYZE_SINK_PARALLELISM = "ACTIVE_OUTREACH_ANALYZE_SINK_PARALLELISM";
    public static final String ACTIVE_OUTREACH_ANALYZE_DEAL_PARALLELISM = "ACTIVE_OUTREACH_ANALYZE_DEAL_PARALLELISM";
    public static final String ACTIVE_OUTREACH_ANALYZE_KAFKA_SOURCE_TOPIC = "ACTIVE_OUTREACH_ANALYZE_KAFKA_SOURCE_TOPIC";
    public static final String ACTIVE_OUTREACH_ANALYZE_KAFKA_SINK_TOPIC = "ACTIVE_OUTREACH_ANALYZE_KAFKA_SINK_TOPIC";
    public static final String ACTIVE_OUTREACH_ANALYZE_PORT_WHITE_LIST = "ACTIVE_OUTREACH_ANALYZE_PORT_WHITE_LIST";
    public static final String ACTIVE_OUTREACH_ANALYZE_IP_WHITE_LIST = "ACTIVE_OUTREACH_ANALYZE_IP_WHITE_LIST";
    public static final String ACTIVE_OUTREACH_ANALYZE_FLOW_MIN_VALUE = "ACTIVE_OUTREACH_ANALYZE_FLOW_MIN_VALUE";
    public static final String ACTIVE_OUTREACH_ANALYZE_SPECIAL_IP = "ACTIVE_OUTREACH_ANALYZE_SPECIAL_IP";
    public static final String ACTIVE_OUTREACH_ANALYZE_KAFKA_SINK_NAME = "ACTIVE_OUTREACH_ANALYZE_KAFKA_SINK_NAME";
    public static final String ACTIVE_OUTREACH_ANALYZE_KAFKA_SINK_PARALLELISM = "ACTIVE_OUTREACH_ANALYZE_KAFKA_SINK_PARALLELISM";
    public static final String ACTIVE_OUTREACH_ANALYZE_OFFICE_IP = "ACTIVE_OUTREACH_ANALYZE_OFFICE_IP";

    //----------------------------------------------------------------------------------------------
    //慢速扫描告警写入风控
    public static final String ALERT_LOW_VELOCITY_SCAN_CONFIG = "ALERT_LOW_VELOCITY_SCAN_CONFIG";
    public static final String ALERT_LOW_VELOCITY_SCAN_JOB_NAME = "ALERT_LOW_VELOCITY_SCAN_JOB_NAME";
    public static final String ALERT_LOW_VELOCITY_SCAN_KAFKA_SOURCE_NAME = "ALERT_LOW_VELOCITY_SCAN_KAFKA_SOURCE_NAME";
    public static final String ALERT_LOW_VELOCITY_SCAN_KAFKA_SINK_NAME = "ALERT_LOW_VELOCITY_SCAN_KAFKA_SINK_NAME";
    public static final String ALERT_LOW_VELOCITY_SCAN_KAFKA_SOURCE_PARALLELISM = "ALERT_LOW_VELOCITY_SCAN_KAFKA_SOURCE_PARALLELISM";
    public static final String ALERT_LOW_VELOCITY_SCAN_DEAL_PARALLELISM = "ALERT_LOW_VELOCITY_SCAN_DEAL_PARALLELISM";
    public static final String ALERT_LOW_VELOCITY_SCAN_KAFKA_SINK_PARALLELISM = "ALERT_LOW_VELOCITY_SCAN_KAFKA_SINK_PARALLELISM";
    public static final String ALERT_LOW_VELOCITY_SCAN_GROUP_ID = "ALERT_LOW_VELOCITY_SCAN_GROUP_ID";
    public static final String ALERT_LOW_VELOCITY_SCAN_KAFKA_SOURCE_TOPIC = "ALERT_LOW_VELOCITY_SCAN_KAFKA_SOURCE_TOPIC";
    public static final String ALERT_LOW_VELOCITY_SCAN_WINDOW_SIZE = "ALERT_LOW_VELOCITY_SCAN_WINDOW_SIZE";
    public static final String ALERT_LOW_VELOCITY_SCAN_SLIDE_SIZE = "ALERT_LOW_VELOCITY_SCAN_SLIDE_SIZE";
    public static final String ALERT_LOW_VELOCITY_SCAN_ALERT_NAME = "ALERT_LOW_VELOCITY_SCAN_ALERT_NAME";
    public static final String ALERT_LOW_VELOCITY_SCAN_ALERT_TYPE = "ALERT_LOW_VELOCITY_SCAN_ALERT_TYPE";
    public static final String ALERT_LOW_VELOCITY_SCAN_REGION = "ALERT_LOW_VELOCITY_SCAN_REGION";
    public static final String ALERT_LOW_VELOCITY_SCAN_BUSINESS = "ALERT_LOW_VELOCITY_SCAN_BUSINESS";
    public static final String ALERT_LOW_VELOCITY_SCAN_DOMAIN = "ALERT_LOW_VELOCITY_SCAN_DOMAIN";
    public static final String ALERT_LOW_VELOCITY_SCAN_IP = "ALERT_LOW_VELOCITY_SCAN_IP";
    public static final String ALERT_LOW_VELOCITY_SCAN_DEVICE = "ALERT_LOW_VELOCITY_SCAN_DEVICE";
    public static final String ALERT_LOW_VELOCITY_SCAN_RULE_ID = "ALERT_LOW_VELOCITY_SCAN_RULE_ID";
    public static final String ALERT_LOW_VELOCITY_SCAN_ALERT_TIMESTAMP_FORMAT = "ALERT_LOW_VELOCITY_SCAN_ALERT_TIMESTAMP_FORMAT";
    public static final String ALERT_LOW_VELOCITY_SCAN_ASSEMBLY = "ALERT_LOW_VELOCITY_SCAN_ASSEMBLY";
    public static final String ALERT_LOW_VELOCITY_SCAN_CHECKPOINT_INTERVAL = "ALERT_LOW_VELOCITY_SCAN_CHECKPOINT_INTERVAL";


    //----------------------------------------------------------------------------------------------
    //主动外联告警写入风控
    public static final String ALERT_ACTIVE_OUTREACH_ANALYZE_CONFIG = "ALERT_ACTIVE_OUTREACH_ANALYZE_CONFIG";
    public static final String ALERT_ACTIVE_OUTREACH_ANALYZE_JOB_NAME = "ALERT_ACTIVE_OUTREACH_ANALYZE_JOB_NAME";
    public static final String ALERT_ACTIVE_OUTREACH_ANALYZE_KAFKA_SOURCE_NAME = "ALERT_ACTIVE_OUTREACH_ANALYZE_KAFKA_SOURCE_NAME";
    public static final String ALERT_ACTIVE_OUTREACH_ANALYZE_KAFKA_SINK_NAME = "ALERT_ACTIVE_OUTREACH_ANALYZE_KAFKA_SINK_NAME";
    public static final String ALERT_ACTIVE_OUTREACH_ANALYZE_KAFKA_SOURCE_PARALLELISM = "ALERT_ACTIVE_OUTREACH_ANALYZE_KAFKA_SOURCE_PARALLELISM";
    public static final String ALERT_ACTIVE_OUTREACH_ANALYZE_DEAL_PARALLELISM = "ALERT_ACTIVE_OUTREACH_ANALYZE_DEAL_PARALLELISM";
    public static final String ALERT_ACTIVE_OUTREACH_ANALYZE_KAFKA_SINK_PARALLELISM = "ALERT_ACTIVE_OUTREACH_ANALYZE_KAFKA_SINK_PARALLELISM";
    public static final String ALERT_ACTIVE_OUTREACH_ANALYZE_GROUP_ID = "ALERT_ACTIVE_OUTREACH_ANALYZE_GROUP_ID";
    public static final String ALERT_ACTIVE_OUTREACH_ANALYZE_KAFKA_SOURCE_TOPIC = "ALERT_ACTIVE_OUTREACH_ANALYZE_KAFKA_SOURCE_TOPIC";
    public static final String ALERT_ACTIVE_OUTREACH_ANALYZE_WINDOW_SIZE = "ALERT_ACTIVE_OUTREACH_ANALYZE_WINDOW_SIZE";
    public static final String ALERT_ACTIVE_OUTREACH_ANALYZE_SLIDE_SIZE = "ALERT_ACTIVE_OUTREACH_ANALYZE_SLIDE_SIZE";
    public static final String ALERT_ACTIVE_OUTREACH_ANALYZE_ALERT_NAME = "ALERT_ACTIVE_OUTREACH_ANALYZE_ALERT_NAME";
    public static final String ALERT_ACTIVE_OUTREACH_ANALYZE_ALERT_TYPE = "ALERT_ACTIVE_OUTREACH_ANALYZE_ALERT_TYPE";
    public static final String ALERT_ACTIVE_OUTREACH_ANALYZE_REGION = "ALERT_ACTIVE_OUTREACH_ANALYZE_REGION";
    public static final String ALERT_ACTIVE_OUTREACH_ANALYZE_BUSINESS = "ALERT_ACTIVE_OUTREACH_ANALYZE_BUSINESS";
    public static final String ALERT_ACTIVE_OUTREACH_ANALYZE_DOMAIN = "ALERT_ACTIVE_OUTREACH_ANALYZE_DOMAIN";
    public static final String ALERT_ACTIVE_OUTREACH_ANALYZE_IP = "ALERT_ACTIVE_OUTREACH_ANALYZE_IP";
    public static final String ALERT_ACTIVE_OUTREACH_ANALYZE_DEVICE = "ALERT_ACTIVE_OUTREACH_ANALYZE_DEVICE";
    public static final String ALERT_ACTIVE_OUTREACH_ANALYZE_RULE_ID = "ALERT_ACTIVE_OUTREACH_ANALYZE_RULE_ID";
    public static final String ALERT_ACTIVE_OUTREACH_ANALYZE_ALERT_TIMESTAMP_FORMAT = "ALERT_ACTIVE_OUTREACH_ANALYZE_ALERT_TIMESTAMP_FORMAT";
    public static final String ALERT_ACTIVE_OUTREACH_ANALYZE_ASSEMBLY = "ALERT_ACTIVE_OUTREACH_ANALYZE_ASSEMBLY";
    public static final String ALERT_ACTIVE_OUTREACH_ANALYZE_CHECKPOINT_INTERVAL = "ALERT_ACTIVE_OUTREACH_ANALYZE_CHECKPOINT_INTERVAL";


    //----------------------------------------------------------------------------------------------
    //DNS隐秘隧道检测
    public static final String DNS_TUNNENL_SERVICE_CONFIG = "DNS_TUNNENL_SERVICE_CONFIG";
    public static final String DNS_TUNNENL_SERVICE_JOB_NAME = "DNS_TUNNENL_SERVICE_JOB_NAME";
    public static final String DNS_TUNNENL_SERVICE_GROUP_ID = "DNS_TUNNENL_SERVICE_GROUP_ID";
    public static final String DNS_TUNNENL_SERVICE_CHECKPOINT_INTERVAL = "DNS_TUNNENL_SERVICE_CHECKPOINT_INTERVAL";
    public static final String DNS_TUNNENL_SERVICE_SOURCE_PARALLELISM = "DNS_TUNNENL_SERVICE_SOURCE_PARALLELISM";
    public static final String DNS_TUNNENL_SERVICE_SINK_PARALLELISM = "DNS_TUNNENL_SERVICE_SINK_PARALLELISM";
    public static final String DNS_TUNNENL_SERVICE_DEAL_PARALLELISM = "DNS_TUNNENL_SERVICE_DEAL_PARALLELISM";
    public static final String DNS_TUNNENL_SERVICE_KAFKA_SOURCE_TOPIC = "DNS_TUNNENL_SERVICE_KAFKA_SOURCE_TOPIC";
    public static final String DNS_TUNNENL_SERVICE_KAFKA_SINK_TOPIC = "DNS_TUNNENL_SERVICE_KAFKA_SINK_TOPIC";
    public static final String DNS_TUNNENL_SERVICE_TRAIN_DATA_PATH = "DNS_TUNNENL_SERVICE_TRAIN_DATA_PATH";
    public static final String DNS_TUNNENL_SERVICE_WHITE_LIST = "DNS_TUNNENL_SERVICE_WHITE_LIST";
    public static final String DNS_TUNNENL_SERVICE_SVM_PUN_FACTOR = "DNS_TUNNENL_SERVICE_SVM_PUN_FACTOR";
    public static final String DNS_TUNNENL_SERVICE_SVM_TOL_LIMIT = "DNS_TUNNENL_SERVICE_SVM_TOL_LIMIT";
    public static final String DNS_TUNNENL_SERVICE_SVM_MAXPASSES = "DNS_TUNNENL_SERVICE_SVM_MAXPASSES";
    public static final String DNS_TUNNENL_SERVICE_SVM_VERIFY_DATA = "DNS_TUNNENL_SERVICE_SVM_VERIFY_DATA";
    public static final String DNS_TUNNENL_SERVICE_SVM_MODEL_ACCURACY = "DNS_TUNNENL_SERVICE_SVM_MODEL_ACCURACY";


    //----------------------------------------------------------------------------------------------
    //Dns隐秘隧道检测写入风控
    public static final String ALERT_DNS_TUNNEL_SERVICE_CONFIG = "ALERT_DNS_TUNNEL_SERVICE_CONFIG";
    public static final String ALERT_DNS_TUNNEL_SERVICE_JOB_NAME = "ALERT_DNS_TUNNEL_SERVICE_JOB_NAME";
    public static final String ALERT_DNS_TUNNEL_SERVICE_KAFKA_SOURCE_NAME = "ALERT_DNS_TUNNEL_SERVICE_KAFKA_SOURCE_NAME";
    public static final String ALERT_DNS_TUNNEL_SERVICE_KAFKA_SINK_NAME = "ALERT_DNS_TUNNEL_SERVICE_KAFKA_SINK_NAME";
    public static final String ALERT_DNS_TUNNEL_SERVICE_KAFKA_SOURCE_PARALLELISM = "ALERT_DNS_TUNNEL_SERVICE_KAFKA_SOURCE_PARALLELISM";
    public static final String ALERT_DNS_TUNNEL_SERVICE_DEAL_PARALLELISM = "ALERT_DNS_TUNNEL_SERVICE_DEAL_PARALLELISM";
    public static final String ALERT_DNS_TUNNEL_SERVICE_KAFKA_SOURCE_TOPIC = "ALERT_DNS_TUNNEL_SERVICE_KAFKA_SOURCE_TOPIC";
    public static final String ALERT_DNS_TUNNEL_SERVICE_KAFKA_SINK_PARALLELISM = "ALERT_DNS_TUNNEL_SERVICE_KAFKA_SINK_PARALLELISM";
    public static final String ALERT_DNS_TUNNEL_SERVICE_GROUP_ID = "ALERT_DNS_TUNNEL_SERVICE_GROUP_ID";
    public static final String ALERT_DNS_TUNNEL_SERVICE_WINDOW_SIZE = "ALERT_DNS_TUNNEL_SERVICE_WINDOW_SIZE";
    public static final String ALERT_DNS_TUNNEL_SERVICE_SLIDE_SIZE = "ALERT_DNS_TUNNEL_SERVICE_SLIDE_SIZE";
    public static final String ALERT_DNS_TUNNEL_SERVICE_ALERT_NAME = "ALERT_DNS_TUNNEL_SERVICE_ALERT_NAME";
    public static final String ALERT_DNS_TUNNEL_SERVICE_ALERT_TYPE = "ALERT_DNS_TUNNEL_SERVICE_ALERT_TYPE";
    public static final String ALERT_DNS_TUNNEL_SERVICE_REGION = "ALERT_DNS_TUNNEL_SERVICE_REGION";
    public static final String ALERT_DNS_TUNNEL_SERVICE_BUSINESS = "ALERT_DNS_TUNNEL_SERVICE_BUSINESS";
    public static final String ALERT_DNS_TUNNEL_SERVICE_DOMAIN = "ALERT_DNS_TUNNEL_SERVICE_DOMAIN";
    public static final String ALERT_DNS_TUNNEL_SERVICE_IP = "ALERT_DNS_TUNNEL_SERVICE_IP";
    public static final String ALERT_DNS_TUNNEL_SERVICE_DEVICE = "ALERT_DNS_TUNNEL_SERVICE_DEVICE";
    public static final String ALERT_DNS_TUNNEL_SERVICE_RULE_ID = "ALERT_DNS_TUNNEL_SERVICE_RULE_ID";
    public static final String ALERT_DNS_TUNNEL_SERVICE_ALERT_TIMESTAMP_FORMAT = "ALERT_DNS_TUNNEL_SERVICE_ALERT_TIMESTAMP_FORMAT";
    public static final String ALERT_DNS_TUNNEL_SERVICE_ASSEMBLY = "ALERT_DNS_TUNNEL_SERVICE_ASSEMBLY";
    public static final String ALERT_DNS_TUNNEL_SERVICE_CHECKPOINT_INTERVAL = "ALERT_DNS_TUNNEL_SERVICE_CHECKPOINT_INTERVAL";

    //----------------------------------------------------------------------------------------------
    //领导邮箱登录异常检测(登录地点+访问方式组合)
    public static final String MAIL_LOGIN_ABNORMAL_CONFIG = "MAIL_LOGIN_ABNORMAL_CONFIG";
    public static final String MAIL_LOGIN_ABNORMAL_JOB_NAME = "MAIL_LOGIN_ABNORMAL_JOB_NAME";
    public static final String MAIL_LOGIN_ABNORMAL_CHECKPOINT_INTERVAL = "MAIL_LOGIN_ABNORMAL_CHECKPOINT_INTERVAL";
    public static final String MAIL_LOGIN_ABNORMAL_GROUP_ID = "MAIL_LOGIN_ABNORMAL_GROUP_ID";
    public static final String MAIL_LOGIN_ABNORMAL_KAFKA_SOURCE_NAME = "MAIL_LOGIN_ABNORMAL_KAFKA_SOURCE_NAME";
    public static final String MAIL_LOGIN_ABNORMAL_SQL_SINK_NAME = "MAIL_LOGIN_ABNORMAL_SQL_SINK_NAME";
    public static final String MAIL_LOGIN_ABNORMAL_KAFKA_SINK_NAME = "MAIL_LOGIN_ABNORMAL_KAFKA_SINK_NAME";
    public static final String MAIL_LOGIN_ABNORMAL_KAFKA_SOURCE_PARALLELISM = "MAIL_LOGIN_ABNORMAL_KAFKA_SOURCE_PARALLELISM";
    public static final String MAIL_LOGIN_ABNORMAL_DEAL_PARALLELISM = "MAIL_LOGIN_ABNORMAL_DEAL_PARALLELISM";
    public static final String MAIL_LOGIN_ABNORMAL_SQL_SINK_PARALLELISM = "MAIL_LOGIN_ABNORMAL_SQL_SINK_PARALLELISM";
    public static final String MAIL_LOGIN_ABNORMAL_KAFKA_SINK_PARALLELISM = "MAIL_LOGIN_ABNORMAL_KAFKA_SINK_PARALLELISM";
    public static final String MAIL_LOGIN_ABNORMAL_KAFKA_SINK_TOPIC = "MAIL_LOGIN_ABNORMAL_KAFKA_SINK_TOPIC";
    public static final String MAIL_LOGIN_ABNORMAL_LEADER_NAME_LIST = "MAIL_LOGIN_ABNORMAL_LEADER_NAME_LIST";
    public static final String MAIL_LOGIN_ABNORMAL_HISTORY_LENGTH = "MAIL_LOGIN_ABNORMAL_HISTORY_LENGTH";


    //----------------------------------------------------------------------------------------------
    //无用账号
    public static final String USELESS_ACCOUNT_CONFIG = "USELESS_ACCOUNT_CONFIG";
    public static final String USELESS_ACCOUNT_JOB_NAME = "USELESS_ACCOUNT_JOB_NAME";
    public static final String USELESS_ACCOUNT_CHECKPOINT_INTERVAL = "USELESS_ACCOUNT_CHECKPOINT_INTERVAL";
    public static final String USELESS_ACCOUNT_GROUP_ID = "USELESS_ACCOUNT_GROUP_ID";
    public static final String USELESS_ACCOUNT_KAFKA_SOURCE_NAME = "USELESS_ACCOUNT_KAFKA_SOURCE_NAME";
    public static final String USELESS_ACCOUNT_KAFKA_SINK_TOPIC = "USELESS_ACCOUNT_KAFKA_SINK_TOPIC";
    public static final String USELESS_ACCOUNT_KAFKA_SINK_NAME = "USELESS_ACCOUNT_KAFKA_SINK_NAME";
    public static final String USELESS_ACCOUNT_SQL_SINK_NAME = "USELESS_ACCOUNT_SQL_SINK_NAME";
    public static final String USELESS_ACCOUNT_SOURCE_PARALLELISM = "USELESS_ACCOUNT_SOURCE_PARALLELISM";
    public static final String USELESS_ACCOUNT_DEAL_PARALLELISM = "USELESS_ACCOUNT_DEAL_PARALLELISM";
    public static final String USELESS_ACCOUNT_SINK_PARALLELISM = "USELESS_ACCOUNT_SINK_PARALLELISM";
    public static final String USELESS_ACCOUNT_DECIDE_TIME_LENGTH = "USELESS_ACCOUNT_DECIDE_TIME_LENGTH";


    //----------------------------------------------------------------------------------------------
    //无用账号告警写入风控
    public static final String ALERT_USELESS_ACCOUNT_CONFIG = "ALERT_USELESS_ACCOUNT_CONFIG";
    public static final String ALERT_USELESS_ACCOUNT_JOB_NAME = "ALERT_USELESS_ACCOUNT_JOB_NAME";
    public static final String ALERT_USELESS_ACCOUNT_KAFKA_SOURCE_NAME = "ALERT_USELESS_ACCOUNT_KAFKA_SOURCE_NAME";
    public static final String ALERT_USELESS_ACCOUNT_KAFKA_SINK_NAME = "ALERT_USELESS_ACCOUNT_KAFKA_SINK_NAME";
    public static final String ALERT_USELESS_ACCOUNT_KAFKA_SOURCE_PARALLELISM = "ALERT_USELESS_ACCOUNT_KAFKA_SOURCE_PARALLELISM";
    public static final String ALERT_USELESS_ACCOUNT_DEAL_PARALLELISM = "ALERT_USELESS_ACCOUNT_DEAL_PARALLELISM";
    public static final String ALERT_USELESS_ACCOUNT_KAFKA_SOURCE_TOPIC = "ALERT_USELESS_ACCOUNT_KAFKA_SOURCE_TOPIC";
    public static final String ALERT_USELESS_ACCOUNT_KAFKA_SINK_PARALLELISM = "ALERT_USELESS_ACCOUNT_KAFKA_SINK_PARALLELISM";
    public static final String ALERT_USELESS_ACCOUNT_GROUP_ID = "ALERT_USELESS_ACCOUNT_GROUP_ID";
    public static final String ALERT_USELESS_ACCOUNT_WINDOW_SIZE = "ALERT_USELESS_ACCOUNT_WINDOW_SIZE";
    public static final String ALERT_USELESS_ACCOUNT_SLIDE_SIZE = "ALERT_USELESS_ACCOUNT_SLIDE_SIZE";
    public static final String ALERT_USELESS_ACCOUNT_ALERT_NAME = "ALERT_USELESS_ACCOUNT_ALERT_NAME";
    public static final String ALERT_USELESS_ACCOUNT_ALERT_TYPE = "ALERT_USELESS_ACCOUNT_ALERT_TYPE";
    public static final String ALERT_USELESS_ACCOUNT_REGION = "ALERT_USELESS_ACCOUNT_REGION";
    public static final String ALERT_USELESS_ACCOUNT_BUSINESS = "ALERT_USELESS_ACCOUNT_BUSINESS";
    public static final String ALERT_USELESS_ACCOUNT_DOMAIN = "ALERT_USELESS_ACCOUNT_DOMAIN";
    public static final String ALERT_USELESS_ACCOUNT_IP = "ALERT_USELESS_ACCOUNT_IP";
    public static final String ALERT_USELESS_ACCOUNT_DEVICE = "ALERT_USELESS_ACCOUNT_DEVICE";
    public static final String ALERT_USELESS_ACCOUNT_RULE_ID = "ALERT_USELESS_ACCOUNT_RULE_ID";
    public static final String ALERT_USELESS_ACCOUNT_ALERT_TIMESTAMP_FORMAT = "ALERT_USELESS_ACCOUNT_ALERT_TIMESTAMP_FORMAT";
    public static final String ALERT_USELESS_ACCOUNT_ASSEMBLY = "ALERT_USELESS_ACCOUNT_ASSEMBLY";
    public static final String ALERT_USELESS_ACCOUNT_CHECKPOINT_INTERVAL = "ALERT_USELESS_ACCOUNT_CHECKPOINT_INTERVAL";


    //----------------------------------------------------------------------------------------------
    //解回包写入redis
    public static final String PACKAGE_ANALYZE_TO_REDIS_CONFIG = "PACKAGE_ANALYZE_TO_REDIS_CONFIG";
    public static final String PACKAGE_ANALYZE_TO_REDIS_JOB_NAME = "PACKAGE_ANALYZE_TO_REDIS_JOB_NAME";
    public static final String PACKAGE_ANALYZE_TO_REDIS_CHECKPOINT_INTERVAL = "PACKAGE_ANALYZE_TO_REDIS_CHECKPOINT_INTERVAL";
    public static final String PACKAGE_ANALYZE_TO_REDIS_GROUP_ID = "PACKAGE_ANALYZE_TO_REDIS_GROUP_ID";
    public static final String PACKAGE_ANALYZE_TO_REDIS_KAFKA_SOURCE_TOPIC = "PACKAGE_ANALYZE_TO_REDIS_KAFKA_SOURCE_TOPIC";
    public static final String PACKAGE_ANALYZE_TO_REDIS_SOURCE_PARALLELISM = "PACKAGE_ANALYZE_TO_REDIS_SOURCE_PARALLELISM";
    public static final String PACKAGE_ANALYZE_TO_REDIS_DEAL_PARALLELISM = "PACKAGE_ANALYZE_TO_REDIS_SOURCE_PARALLELISM";
    public static final String PACKAGE_ANALYZE_TO_REDIS_SINK_PARALLELISM = "PACKAGE_ANALYZE_TO_REDIS_SINK_PARALLELISM";
    public static final String PACKAGE_ANALYZE_TO_REDIS_DATA_COUNT = "PACKAGE_ANALYZE_TO_REDIS_DATA_COUNT";
    public static final String PACKAGE_ANALYZE_TO_REDIS_DATA_TTL = "PACKAGE_ANALYZE_TO_REDIS_DATA_TTL";


    //----------------------------------------------------------------------------------------------
    //SQL注入解回包
    public static final String SQL_INJECTION_VALIDITY_CONFIG = "SQL_INJECTION_VALIDITY_CONFIG";
    public static final String SQL_INJECTION_VALIDITY_JOB_NAME = "SQL_INJECTION_VALIDITY_JOB_NAME";
    public static final String SQL_INJECTION_VALIDITY_CHECKPOINT_INTERVAL = "SQL_INJECTION_VALIDITY_CHECKPOINT_INTERVAL";
    public static final String SQL_INJECTION_VALIDITY_GROUP_ID = "SQL_INJECTION_VALIDITY_GROUP_ID";
    public static final String SQL_INJECTION_VALIDITY_KAFKA_SOURCE_NAME = "SQL_INJECTION_VALIDITY_KAFKA_SOURCE_NAME";
    public static final String SQL_INJECTION_VALIDITY_SQL_SINK_NAME = "SQL_INJECTION_VALIDITY_SQL_SINK_NAME";
    public static final String SQL_INJECTION_VALIDITY_KAFKA_SINK_NAME = "SQL_INJECTION_VALIDITY_KAFKA_SINK_NAME";
    public static final String SQL_INJECTION_VALIDITY_KAFKA_SOURCE_PARALLELISM = "SQL_INJECTION_VALIDITY_KAFKA_SOURCE_PARALLELISM";
    public static final String SQL_INJECTION_VALIDITY_DEAL_PARALLELISM = "SQL_INJECTION_VALIDITY_DEAL_PARALLELISM";
    public static final String SQL_INJECTION_VALIDITY_SQL_SINK_PARALLELISM = "SQL_INJECTION_VALIDITY_SQL_SINK_PARALLELISM";
    public static final String SQL_INJECTION_VALIDITY_KAFKA_SINK_PARALLELISM = "SQL_INJECTION_VALIDITY_KAFKA_SINK_PARALLELISM";
    public static final String SQL_INJECTION_VALIDITY_KAFKA_SINK_TOPIC = "SQL_INJECTION_VALIDITY_KAFKA_SINK_TOPIC";
    public static final String SQL_INJECTION_VALIDITY_RULE_PATH = "SQL_INJECTION_VALIDITY_RULE_PATH";
    public static final String SQL_INJECTION_VALIDITY_GROUP_SPLIT = "SQL_INJECTION_VALIDITY_GROUP_SPLIT";
    public static final String SQL_INJECTION_VALIDITY_KV_SPLIT = "SQL_INJECTION_VALIDITY_KV_SPLIT";


    //----------------------------------------------------------------------------------------------
    //xss注入解回包
    public static final String XSS_INJECTION_VALIDITY_CONFIG = "XSS_INJECTION_VALIDITY_CONFIG";
    public static final String XSS_INJECTION_VALIDITY_JOB_NAME = "XSS_INJECTION_VALIDITY_JOB_NAME";
    public static final String XSS_INJECTION_VALIDITY_CHECKPOINT_INTERVAL = "XSS_INJECTION_VALIDITY_CHECKPOINT_INTERVAL";
    public static final String XSS_INJECTION_VALIDITY_GROUP_ID = "XSS_INJECTION_VALIDITY_GROUP_ID";
    public static final String XSS_INJECTION_VALIDITY_KAFKA_SOURCE_NAME = "XSS_INJECTION_VALIDITY_KAFKA_SOURCE_NAME";
    public static final String XSS_INJECTION_VALIDITY_SQL_SINK_NAME = "XSS_INJECTION_VALIDITY_SQL_SINK_NAME";
    public static final String XSS_INJECTION_VALIDITY_KAFKA_SINK_NAME = "XSS_INJECTION_VALIDITY_KAFKA_SINK_NAME";
    public static final String XSS_INJECTION_VALIDITY_KAFKA_SOURCE_PARALLELISM = "XSS_INJECTION_VALIDITY_KAFKA_SOURCE_PARALLELISM";
    public static final String XSS_INJECTION_VALIDITY_DEAL_PARALLELISM = "XSS_INJECTION_VALIDITY_DEAL_PARALLELISM";
    public static final String XSS_INJECTION_VALIDITY_SQL_SINK_PARALLELISM = "XSS_INJECTION_VALIDITY_SQL_SINK_PARALLELISM";
    public static final String XSS_INJECTION_VALIDITY_KAFKA_SINK_PARALLELISM = "XSS_INJECTION_VALIDITY_KAFKA_SINK_PARALLELISM";
    public static final String XSS_INJECTION_VALIDITY_KAFKA_SINK_TOPIC = "XSS_INJECTION_VALIDITY_KAFKA_SINK_TOPIC";
    public static final String XSS_INJECTION_VALIDITY_HDFS_RULE_PATH = "XSS_INJECTION_VALIDITY_HDFS_RULE_PATH";
    public static final String XSS_INJECTION_VALIDITY_RULE_PATH = "XSS_INJECTION_VALIDITY_RULE_PATH";
    public static final String XSS_INJECTION_VALIDITY_GROUP_SPLIT = "XSS_INJECTION_VALIDITY_GROUP_SPLIT";
    public static final String XSS_INJECTION_VALIDITY_KV_SPLIT = "XSS_INJECTION_VALIDITY_KV_SPLIT";


    //----------------------------------------------------------------------------------------------
    //路径遍历解回包
    public static final String DIRECTORY_TRAVERSAL_VALIDITY_CONFIG = "DIRECTORY_TRAVERSAL_VALIDITY_CONFIG";
    public static final String DIRECTORY_TRAVERSAL_VALIDITY_JOB_NAME = "DIRECTORY_TRAVERSAL_VALIDITY_JOB_NAME";
    public static final String DIRECTORY_TRAVERSAL_VALIDITY_CHECKPOINT_INTERVAL = "DIRECTORY_TRAVERSAL_VALIDITY_CHECKPOINT_INTERVAL";
    public static final String DIRECTORY_TRAVERSAL_VALIDITY_GROUP_ID = "DIRECTORY_TRAVERSAL_VALIDITY_GROUP_ID";
    public static final String DIRECTORY_TRAVERSAL_VALIDITY_KAFKA_SOURCE_NAME = "DIRECTORY_TRAVERSAL_VALIDITY_KAFKA_SOURCE_NAME";
    public static final String DIRECTORY_TRAVERSAL_VALIDITY_SQL_SINK_NAME = "DIRECTORY_TRAVERSAL_VALIDITY_SQL_SINK_NAME";
    public static final String DIRECTORY_TRAVERSAL_VALIDITY_KAFKA_SINK_NAME = "DIRECTORY_TRAVERSAL_VALIDITY_KAFKA_SINK_NAME";
    public static final String DIRECTORY_TRAVERSAL_VALIDITY_KAFKA_SOURCE_PARALLELISM = "DIRECTORY_TRAVERSAL_VALIDITY_KAFKA_SOURCE_PARALLELISM";
    public static final String DIRECTORY_TRAVERSAL_VALIDITY_DEAL_PARALLELISM = "DIRECTORY_TRAVERSAL_VALIDITY_DEAL_PARALLELISM";
    public static final String DIRECTORY_TRAVERSAL_VALIDITY_SQL_SINK_PARALLELISM = "DIRECTORY_TRAVERSAL_VALIDITY_SQL_SINK_PARALLELISM";
    public static final String DIRECTORY_TRAVERSAL_VALIDITY_KAFKA_SINK_PARALLELISM = "DIRECTORY_TRAVERSAL_VALIDITY_KAFKA_SINK_PARALLELISM";
    public static final String DIRECTORY_TRAVERSAL_VALIDITY_KAFKA_SINK_TOPIC = "DIRECTORY_TRAVERSAL_VALIDITY_KAFKA_SINK_TOPIC";
    public static final String DIRECTORY_TRAVERSAL_VALIDITY_TRAIN_DATA_PATH = "DIRECTORY_TRAVERSAL_VALIDITY_TRAIN_DATA_PATH";


    //----------------------------------------------------------------------------------------------
    //UA攻击尝试告警检测
    public static final String ATTACK_ATTEMPT_CONFIG = "ATTACK_ATTEMPT_CONFIG";
    public static final String ATTACK_ATTEMPT_JOB_NAME = "ATTACK_ATTEMPT_JOB_NAME";
    public static final String ATTACK_ATTEMPT_CHECKPOINT_INTERVAL = "ATTACK_ATTEMPT_CHECKPOINT_INTERVAL";
    public static final String ATTACK_ATTEMPT_GROUP_ID = "ATTACK_ATTEMPT_GROUP_ID";
    public static final String ATTACK_ATTEMPT_KAFKA_SOURCE_TOPIC = "ATTACK_ATTEMPT_KAFKA_SOURCE_TOPIC";
    public static final String ATTACK_ATTEMPT_SOURCE_PARALLELISM = "ATTACK_ATTEMPT_SOURCE_PARALLELISM";
    public static final String ATTACK_ATTEMPT_DEAL_PARALLELISM = "ATTACK_ATTEMPT_DEAL_PARALLELISM";
    public static final String ATTACK_ATTEMPT_SINK_PARALLELISM = "ATTACK_ATTEMPT_SINK_PARALLELISM";
    public static final String ATTACK_ATTEMPT_KEY_STRING = "ATTACK_ATTEMPT_KEY_STRING";
    public static final String ATTACK_ATTEMPT_ANALYZE_KAFKA_SINK_TOPIC = "ATTACK_ATTEMPT_ANALYZE_KAFKA_SINK_TOPIC";
    public static final String ATTACK_ATTEMPT_PYTHON_WHITE_LIST = "ATTACK_ATTEMPT_PYTHON_WHITE_LIST";


    //----------------------------------------------------------------------------------------------
    //UA攻击尝试告警写入风控
    public static final String ALERT_ATTACK_ATTEMPT_ANALYZE_CONFIG = "ALERT_ATTACK_ATTEMPT_ANALYZE_CONFIG";
    public static final String ALERT_ATTACK_ATTEMPT_ANALYZE_JOB_NAME = "ALERT_ATTACK_ATTEMPT_ANALYZE_JOB_NAME";
    public static final String ALERT_ATTACK_ATTEMPT_ANALYZE_KAFKA_SOURCE_NAME = "ALERT_ATTACK_ATTEMPT_ANALYZE_KAFKA_SOURCE_NAME";
    public static final String ALERT_ATTACK_ATTEMPT_ANALYZE_KAFKA_SINK_NAME = "ALERT_ATTACK_ATTEMPT_ANALYZE_KAFKA_SINK_NAME";
    public static final String ALERT_ATTACK_ATTEMPT_ANALYZE_KAFKA_SOURCE_PARALLELISM = "ALERT_ATTACK_ATTEMPT_ANALYZE_KAFKA_SOURCE_PARALLELISM";
    public static final String ALERT_ATTACK_ATTEMPT_ANALYZE_KAFKA_SOURCE_TOPIC = "ALERT_ATTACK_ATTEMPT_ANALYZE_KAFKA_SOURCE_TOPIC";
    public static final String ALERT_ATTACK_ATTEMPT_ANALYZE_DEAL_PARALLELISM = "ALERT_ATTACK_ATTEMPT_ANALYZE_DEAL_PARALLELISM";
    public static final String ALERT_ATTACK_ATTEMPT_ANALYZE_KAFKA_SINK_PARALLELISM = "ALERT_ATTACK_ATTEMPT_ANALYZE_KAFKA_SINK_PARALLELISM";
    public static final String ALERT_ATTACK_ATTEMPT_ANALYZE_GROUP_ID = "ALERT_ATTACK_ATTEMPT_ANALYZE_GROUP_ID";
    public static final String ALERT_ATTACK_ATTEMPT_ANALYZE_ALERT_NAME = "ALERT_ATTACK_ATTEMPT_ANALYZE_ALERT_NAME";
    public static final String ALERT_ATTACK_ATTEMPT_ANALYZE_ALERT_TYPE = "ALERT_ATTACK_ATTEMPT_ANALYZE_ALERT_TYPE";
    public static final String ALERT_ATTACK_ATTEMPT_ANALYZE_REGION = "ALERT_ATTACK_ATTEMPT_ANALYZE_REGION";
    public static final String ALERT_ATTACK_ATTEMPT_ANALYZE_BUSINESS = "ALERT_ATTACK_ATTEMPT_ANALYZE_BUSINESS";
    public static final String ALERT_ATTACK_ATTEMPT_ANALYZE_DOMAIN = "ALERT_ATTACK_ATTEMPT_ANALYZE_DOMAIN";
    public static final String ALERT_ATTACK_ATTEMPT_ANALYZE_IP = "ALERT_ATTACK_ATTEMPT_ANALYZE_IP";
    public static final String ALERT_ATTACK_ATTEMPT_ANALYZE_DEVICE = "ALERT_ATTACK_ATTEMPT_ANALYZE_DEVICE";
    public static final String ALERT_ATTACK_ATTEMPT_ANALYZE_RULE_ID = "ALERT_ATTACK_ATTEMPT_ANALYZE_RULE_ID";
    public static final String ALERT_ATTACK_ATTEMPT_ANALYZE_ALERT_TIMESTAMP_FORMAT = "ALERT_ATTACK_ATTEMPT_ANALYZE_ALERT_TIMESTAMP_FORMAT";
    public static final String ALERT_ATTACK_ATTEMPT_ANALYZE_ASSEMBLY = "ALERT_ATTACK_ATTEMPT_ANALYZE_ASSEMBLY";
    public static final String ALERT_ATTACK_ATTEMPT_ANALYZE_CHECKPOINT_INTERVAL = "ALERT_ATTACK_ATTEMPT_ANALYZE_CHECKPOINT_INTERVAL";


    //----------------------------------------------------------------------------------------------
    //OA系统公文（明文、密文）、通讯录的批量导出（包括运维批量导出和个人账号大量下载）
    public static final String OA_SYSTEM_CRAWLER_WARN_CONFIG = "OA_SYSTEM_CRAWLER_WARN_CONFIG";
    public static final String OA_SYSTEM_CRAWLER_WARN_JOB_NAME = "OA_SYSTEM_CRAWLER_WARN_JOB_NAME";
    public static final String OA_SYSTEM_CRAWLER_WARN_KAFKA_SOURCE_PARALLELISM = "OA_SYSTEM_CRAWLER_WARN_KAFKA_SOURCE_PARALLELISM";
    public static final String OA_SYSTEM_CRAWLER_WARN_DEAL_PARALLELISM = "OA_SYSTEM_CRAWLER_WARN_DEAL_PARALLELISM";
    public static final String OA_SYSTEM_CRAWLER_WARN_KAFKA_SINK_PARALLELISM = "OA_SYSTEM_CRAWLER_WARN_KAFKA_SINK_PARALLELISM";
    public static final String OA_SYSTEM_CRAWLER_WARN_GROUP_ID = "OA_SYSTEM_CRAWLER_WARN_GROUP_ID";
    public static final String OA_SYSTEM_CRAWLER_WARN_KAFKA_SINK_TOPIC = "OA_SYSTEM_CRAWLER_WARN_KAFKA_SINK_TOPIC";
    public static final String OA_SYSTEM_CRAWLER_WARN_CHECKPOINT_INTERVAL = "OA_SYSTEM_CRAWLER_WARN_CHECKPOINT_INTERVAL";
    public static final String OA_SYSTEM_CRAWLER_WARN_ONLINE_TIME_ALLOW = "OA_SYSTEM_CRAWLER_WARN_ONLINE_TIME_ALLOW";
    public static final String OA_SYSTEM_CRAWLER_WARN_ONLINE_TIME_DECIDE = "OA_SYSTEM_CRAWLER_WARN_ONLINE_TIME_DECIDE";
    public static final String OA_SYSTEM_CRAWLER_WARN_OFFICIAL_URL = "OA_SYSTEM_CRAWLER_WARN_OFFICIAL_URL";
    public static final String OA_SYSTEM_CRAWLER_WARN_ADDRESS_URL = "OA_SYSTEM_CRAWLER_WARN_ADDRESS_URL";
    public static final String OA_SYSTEM_CRAWLER_WARN_COUNT_ALLOW = "OA_SYSTEM_CRAWLER_WARN_COUNT_ALLOW";
    public static final String OPERATION_PERSONNEL_DOWNLOAD_OPERATION_PERSONNEL_PATH_OA = "OPERATION_PERSONNEL_DOWNLOAD_OPERATION_PERSONNEL_PATH_OA";

    //-----------------------------临时
    public static final String ALERT_STATISTICS_TO_RISK_CONTROL_KAFKA_BROKER_LIST_OUT = "ALERT_STATISTICS_TO_RISK_CONTROL_KAFKA_BROKER_LIST_OUT";
    public static final String ALERT_STATISTICS_TO_RISK_CONTROL_KAFKA_TOPIC_OUT = "ALERT_STATISTICS_TO_RISK_CONTROL_KAFKA_TOPIC_OUT";

    //----------------------------------------------------------------------------------------------
    //告警数据入druid
    public static final String WARNING_FLINK_TO_DRUID_CONFIG = "WARNING_FLINK_TO_DRUID_CONFIG";
    public static final String WARNING_PLACE_PATH = "WARNING_PLACE_PATH";
    public static final String WARNING_SYSTEM_PATH = "WARNING_SYSTEM_PATH";
    public static final String WARNING_USED_PLACE_PATH = "WARNING_USED_PLACE_PATH";
    public static final String WARNING_TOPIC = "WARNING_TOPIC";
    public static final String WARNING_JOB_NAME = "WARNING_JOB_NAME";
    public static final String WARNING_CHECKPOINT_INTERVAL = "WARNING_CHECKPOINT_INTERVAL";
    public static final String WARNING_DEAL_PARALLELISM = "WARNING_DEAL_PARALLELISM";
    public static final String WARNING_KAFKA_SOURCE_NAME = "WARNING_KAFKA_SOURCE_NAME";
    public static final String WARNING_TRANQUILITY_SINK_NAME = "WARNING_TRANQUILITY_SINK_NAME";
    public static final String WARNING_TO_KAFKA_TOPIC = "WARNING_TO_KAFKA_TOPIC";
    public static final String WARNING_KAFKA_SINK_NAME = "WARNING_KAFKA_SINK_NAME";
    public static final String WARNING_WARNING_TABLE_NAME = "WARNING_WARNING_TABLE_NAME";
    public static final String WARNING_REDIS_PROPERTIES = "WARNING_REDIS_PROPERTIES";


    //----------------------------------------------------------------------------------------------
    //未知风险
    public static final String UN_KNOW_RISK_WARN_CONFIG = "UN_KNOW_RISK_WARN_CONFIG";
    public static final String UN_KNOW_RISK_WARN_JOB_NAME = "UN_KNOW_RISK_WARN_JOB_NAME";
    public static final String UN_KNOW_RISK_WARN_KAFKA_SOURCE_PARALLELISM = "UN_KNOW_RISK_WARN_KAFKA_SOURCE_PARALLELISM";
    public static final String UN_KNOW_RISK_WARN_DEAL_PARALLELISM = "UN_KNOW_RISK_WARN_DEAL_PARALLELISM";
    public static final String UN_KNOW_RISK_WARN_KAFKA_SINK_PARALLELISM = "UN_KNOW_RISK_WARN_KAFKA_SINK_PARALLELISM";
    public static final String UN_KNOW_RISK_WARN_KAFKA_SINK_TOPIC = "UN_KNOW_RISK_WARN_KAFKA_SINK_TOPIC";
    public static final String UN_KNOW_RISK_WARN_CHECKPOINT_INTERVAL = "UN_KNOW_RISK_WARN_CHECKPOINT_INTERVAL";
    public static final String UN_KNOW_RISK_WARN_GROUP_ID = "UN_KNOW_RISK_WARN_GROUP_ID";
    public static final String UN_KNOW_RISK_WARN_DRUID_DATA_START_TIMESTAMP = "UN_KNOW_RISK_WARN_DRUID_DATA_START_TIMESTAMP";
    public static final String UN_KNOW_RISK_WARN_SIMILARITY = "UN_KNOW_RISK_WARN_SIMILARITY";
    public static final String UN_KNOW_RISK_WARN_CMD_SIMILARITY = "UN_KNOW_RISK_WARN_CMD_SIMILARITY";
    public static final String UN_KNOW_RISK_WARN_SAMPLE_PERIOD = "UN_KNOW_RISK_WARN_SAMPLE_PERIOD";
    public static final String UN_KNOW_RISK_WARN_UN_SIMILARITY = "UN_KNOW_RISK_WARN_UN_SIMILARITY";
    public static final String UN_KNOW_RISK_WARN_CMD_LIST = "UN_KNOW_RISK_WARN_CMD_LIST";


    //    ---------------------------------------------------------------------------------------------------
//    M域自动封堵
    public static final String M_AUTOMATION_POLICY_CONFIG = "M_AUTOMATION_POLICY_CONFIG";
    public static final String M_AUTOMATION_POLICY_JOB_NAME = "M_AUTOMATION_POLICY_JOB_NAME";
    public static final String M_AUTOMATION_POLICY_KAFKA_SOURCE_PARALLELISM = "M_AUTOMATION_POLICY_KAFKA_SOURCE_PARALLELISM";
    public static final String M_AUTOMATION_POLICY_DEAL_PARALLELISM = "M_AUTOMATION_POLICY_DEAL_PARALLELISM";
    public static final String M_AUTOMATION_POLICY_KAFKA_SINK_PARALLELISM = "M_AUTOMATION_POLICY_KAFKA_SINK_PARALLELISM";
    public static final String M_AUTOMATION_POLICY_KAFKA_SINK_TOPIC = "M_AUTOMATION_POLICY_KAFKA_SINK_TOPIC";
    public static final String M_AUTOMATION_POLICY_CHECKPOINT_INTERVAL = "M_AUTOMATION_POLICY_CHECKPOINT_INTERVAL";
    public static final String M_AUTOMATION_POLICY_GROUP_ID = "M_AUTOMATION_POLICY_GROUP_ID";
    public static final String M_AUTOMATION_POLICY_FALSE_ALERT_NAME = "M_AUTOMATION_POLICY_FALSE_ALERT_NAME";
    public static final String M_AUTOMATION_POLICY_TRUE_ALERT_NAME = "M_AUTOMATION_POLICY_TRUE_ALERT_NAME";
    public static final String M_AUTOMATION_POLICY_SYSTEM_KEY = "M_AUTOMATION_POLICY_SYSTEM_KEY";
    public static final String M_AUTOMATION_POLICY_C3P0 = "M_AUTOMATION_POLICY_C3P0";


    //    ---------------------------------------------------------------------------------------------------
//    攻击画像
    public static final String ATTACKER_BEHAVIOR_CONFIG = "ATTACKER_BEHAVIOR_CONFIG";
    public static final String ATTACKER_BEHAVIOR_JOB_NAME = "ATTACKER_BEHAVIOR_JOB_NAME";
    public static final String ATTACKER_BEHAVIOR_SOURCE_PARALLELISM = "ATTACKER_BEHAVIOR_SOURCE_PARALLELISM";
    public static final String ATTACKER_BEHAVIOR_DEAL_PARALLELISM = "ATTACKER_BEHAVIOR_DEAL_PARALLELISM";
    public static final String ATTACKER_BEHAVIOR_SINK_PARALLELISM = "ATTACKER_BEHAVIOR_SINK_PARALLELISM";
    public static final String ATTACKER_BEHAVIOR_GROUP_ID = "ATTACKER_BEHAVIOR_GROUP_ID";
    public static final String ATTACKER_BEHAVIOR_CHECKPOINT_INTERVAL = "ATTACKER_BEHAVIOR_CHECKPOINT_INTERVAL";
    public static final String ATTACKER_BEHAVIOR_SOURCE_TOPIC = "ATTACKER_BEHAVIOR_SOURCE_TOPIC";
    public static final String ATTACKER_BEHAVIOR_SINK_TOPIC = "ATTACKER_BEHAVIOR_SINK_TOPIC";
    public static final String ATTACKER_BEHAVIOR_ALERT_NAME_LINK = "ATTACKER_BEHAVIOR_ALERT_NAME_LINK";
    public static final String ATTACKER_BEHAVIOR_PERSONAL_INFO = "ATTACKER_BEHAVIOR_PERSONAL_INFO";

    //----------------------------------------------------------------------------------------------
    //OA系统公文（明文、密文）、通讯录的批量导出（包括运维批量导出和个人账号大量下载）  写入风控
    public static final String ALERT_OA_SYSTEM_CRAWLER_WARN_CONFIG = "ALERT_OA_SYSTEM_CRAWLER_WARN_CONFIG";
    public static final String ALERT_OA_SYSTEM_CRAWLER_WARN_JOB_NAME = "ALERT_OA_SYSTEM_CRAWLER_WARN_JOB_NAME";
    public static final String ALERT_OA_SYSTEM_CRAWLER_WARN_KAFKA_SOURCE_NAME = "ALERT_OA_SYSTEM_CRAWLER_WARN_KAFKA_SOURCE_NAME";
    public static final String ALERT_OA_SYSTEM_CRAWLER_WARN_KAFKA_SINK_NAME = "ALERT_OA_SYSTEM_CRAWLER_WARN_KAFKA_SINK_NAME";
    public static final String ALERT_OA_SYSTEM_CRAWLER_WARN_KAFKA_SOURCE_PARALLELISM = "ALERT_OA_SYSTEM_CRAWLER_WARN_KAFKA_SOURCE_PARALLELISM";
    public static final String ALERT_OA_SYSTEM_CRAWLER_WARN_KAFKA_SOURCE_TOPIC = "ALERT_OA_SYSTEM_CRAWLER_WARN_KAFKA_SOURCE_TOPIC";
    public static final String ALERT_OA_SYSTEM_CRAWLER_WARN_DEAL_PARALLELISM = "ALERT_OA_SYSTEM_CRAWLER_WARN_DEAL_PARALLELISM";
    public static final String ALERT_OA_SYSTEM_CRAWLER_WARN_KAFKA_SINK_PARALLELISM = "ALERT_OA_SYSTEM_CRAWLER_WARN_KAFKA_SINK_PARALLELISM";
    public static final String ALERT_OA_SYSTEM_CRAWLER_WARN_GROUP_ID = "ALERT_OA_SYSTEM_CRAWLER_WARN_GROUP_ID";
    public static final String ALERT_OA_SYSTEM_CRAWLER_WARN_ALERT_NAME = "ALERT_OA_SYSTEM_CRAWLER_WARN_ALERT_NAME";
    public static final String ALERT_OA_SYSTEM_CRAWLER_WARN_ALERT_TYPE = "ALERT_OA_SYSTEM_CRAWLER_WARN_ALERT_TYPE";
    public static final String ALERT_OA_SYSTEM_CRAWLER_WARN_REGION = "ALERT_OA_SYSTEM_CRAWLER_WARN_REGION";
    public static final String ALERT_OA_SYSTEM_CRAWLER_WARN_BUSINESS = "ALERT_OA_SYSTEM_CRAWLER_WARN_BUSINESS";
    public static final String ALERT_OA_SYSTEM_CRAWLER_WARN_DOMAIN = "ALERT_OA_SYSTEM_CRAWLER_WARN_DOMAIN";
    public static final String ALERT_OA_SYSTEM_CRAWLER_WARN_IP = "ALERT_OA_SYSTEM_CRAWLER_WARN_IP";
    public static final String ALERT_OA_SYSTEM_CRAWLER_WARN_DEVICE = "ALERT_OA_SYSTEM_CRAWLER_WARN_DEVICE";
    public static final String ALERT_OA_SYSTEM_CRAWLER_WARN_RULE_ID = "ALERT_OA_SYSTEM_CRAWLER_WARN_RULE_ID";
    public static final String ALERT_OA_SYSTEM_CRAWLER_WARN_ALERT_TIMESTAMP_FORMAT = "ALERT_OA_SYSTEM_CRAWLER_WARN_ALERT_TIMESTAMP_FORMAT";
    public static final String ALERT_OA_SYSTEM_CRAWLER_WARN_ASSEMBLY = "ALERT_OA_SYSTEM_CRAWLER_WARN_ASSEMBLY";
    public static final String ALERT_OA_SYSTEM_CRAWLER_WARN_CHECKPOINT_INTERVAL = "ALERT_OA_SYSTEM_CRAWLER_WARN_CHECKPOINT_INTERVAL";

    //----------------------------------------------------------------------------------------------
    //未知风险写入风控
    public static final String ALERT_UN_KNOW_RISK_CONFIG = "ALERT_UN_KNOW_RISK_CONFIG";
    public static final String ALERT_UN_KNOW_RISK_JOB_NAME = "ALERT_UN_KNOW_RISK_JOB_NAME";
    public static final String ALERT_UN_KNOW_RISK_KAFKA_SOURCE_NAME = "ALERT_UN_KNOW_RISK_KAFKA_SOURCE_NAME";
    public static final String ALERT_UN_KNOW_RISK_KAFKA_SINK_NAME = "ALERT_UN_KNOW_RISK_KAFKA_SINK_NAME";
    public static final String ALERT_UN_KNOW_RISK_KAFKA_SOURCE_PARALLELISM = "ALERT_UN_KNOW_RISK_KAFKA_SOURCE_PARALLELISM";
    public static final String ALERT_UN_KNOW_RISK_KAFKA_SOURCE_TOPIC = "ALERT_UN_KNOW_RISK_KAFKA_SOURCE_TOPIC";
    public static final String ALERT_UN_KNOW_RISK_DEAL_PARALLELISM = "ALERT_UN_KNOW_RISK_DEAL_PARALLELISM";
    public static final String ALERT_UN_KNOW_RISK_KAFKA_SINK_PARALLELISM = "ALERT_UN_KNOW_RISK_KAFKA_SINK_PARALLELISM";
    public static final String ALERT_UN_KNOW_RISK_GROUP_ID = "ALERT_UN_KNOW_RISK_GROUP_ID";
    public static final String ALERT_UN_KNOW_RISK_ALERT_NAME = "ALERT_UN_KNOW_RISK_ALERT_NAME";
    public static final String ALERT_UN_KNOW_RISK_ALERT_TYPE = "ALERT_UN_KNOW_RISK_ALERT_TYPE";
    public static final String ALERT_UN_KNOW_RISK_REGION = "ALERT_UN_KNOW_RISK_REGION";
    public static final String ALERT_UN_KNOW_RISK_BUSINESS = "ALERT_UN_KNOW_RISK_BUSINESS";
    public static final String ALERT_UN_KNOW_RISK_DOMAIN = "ALERT_UN_KNOW_RISK_DOMAIN";
    public static final String ALERT_UN_KNOW_RISK_IP = "ALERT_UN_KNOW_RISK_IP";
    public static final String ALERT_UN_KNOW_RISK_DEVICE = "ALERT_UN_KNOW_RISK_DEVICE";
    public static final String ALERT_UN_KNOW_RISK_RULE_ID = "ALERT_UN_KNOW_RISK_RULE_ID";
    public static final String ALERT_UN_KNOW_RISK_ALERT_TIMESTAMP_FORMAT = "ALERT_UN_KNOW_RISK_ALERT_TIMESTAMP_FORMAT";
    public static final String ALERT_UN_KNOW_RISK_ASSEMBLY = "ALERT_UN_KNOW_RISK_ASSEMBLY";
    public static final String ALERT_UN_KNOW_RISK_CHECKPOINT_INTERVAL = "ALERT_UN_KNOW_RISK_CHECKPOINT_INTERVAL";

    //风险用户写入风控
    public static final String ALERT_ATTACKER_BEHAVIOR_CONFIG = "ALERT_ATTACKER_BEHAVIOR_CONFIG";
    public static final String ALERT_ATTACKER_BEHAVIOR_JOB_NAME = "ALERT_ATTACKER_BEHAVIOR_JOB_NAME";
    public static final String ALERT_ATTACKER_BEHAVIOR_KAFKA_SOURCE_NAME = "ALERT_ATTACKER_BEHAVIOR_KAFKA_SOURCE_NAME";
    public static final String ALERT_ATTACKER_BEHAVIOR_KAFKA_SINK_NAME = "ALERT_ATTACKER_BEHAVIOR_KAFKA_SINK_NAME";
    public static final String ALERT_ATTACKER_BEHAVIOR_KAFKA_SOURCE_PARALLELISM = "ALERT_ATTACKER_BEHAVIOR_KAFKA_SOURCE_PARALLELISM";
    public static final String ALERT_ATTACKER_BEHAVIOR_KAFKA_SOURCE_TOPIC = "ALERT_ATTACKER_BEHAVIOR_KAFKA_SOURCE_TOPIC";
    public static final String ALERT_ATTACKER_BEHAVIOR_DEAL_PARALLELISM = "ALERT_ATTACKER_BEHAVIOR_DEAL_PARALLELISM";
    public static final String ALERT_ATTACKER_BEHAVIOR_KAFKA_SINK_PARALLELISM = "ALERT_ATTACKER_BEHAVIOR_KAFKA_SINK_PARALLELISM";
    public static final String ALERT_ATTACKER_BEHAVIOR_GROUP_ID = "ALERT_ATTACKER_BEHAVIOR_GROUP_ID";
    public static final String ALERT_ATTACKER_BEHAVIOR_ALERT_NAME = "ALERT_ATTACKER_BEHAVIOR_ALERT_NAME";
    public static final String ALERT_ATTACKER_BEHAVIOR_ALERT_TYPE = "ALERT_ATTACKER_BEHAVIOR_ALERT_TYPE";
    public static final String ALERT_ATTACKER_BEHAVIOR_REGION = "ALERT_ATTACKER_BEHAVIOR_REGION";
    public static final String ALERT_ATTACKER_BEHAVIOR_BUSINESS = "ALERT_ATTACKER_BEHAVIOR_BUSINESS";
    public static final String ALERT_ATTACKER_BEHAVIOR_DOMAIN = "ALERT_ATTACKER_BEHAVIOR_DOMAIN";
    public static final String ALERT_ATTACKER_BEHAVIOR_IP = "ALERT_ATTACKER_BEHAVIOR_IP";
    public static final String ALERT_ATTACKER_BEHAVIOR_DEVICE = "ALERT_ATTACKER_BEHAVIOR_DEVICE";
    public static final String ALERT_ATTACKER_BEHAVIOR_RULE_ID = "ALERT_ATTACKER_BEHAVIOR_RULE_ID";
    public static final String ALERT_ATTACKER_BEHAVIOR_ALERT_TIMESTAMP_FORMAT = "ALERT_ATTACKER_BEHAVIOR_ALERT_TIMESTAMP_FORMAT";
    public static final String ALERT_ATTACKER_BEHAVIOR_ASSEMBLY = "ALERT_ATTACKER_BEHAVIOR_ASSEMBLY";
    public static final String ALERT_ATTACKER_BEHAVIOR_CHECKPOINT_INTERVAL = "ALERT_ATTACKER_BEHAVIOR_CHECKPOINT_INTERVAL";


    //渗透软件流量分析—webshell及命令执行流量检测
    public static final String PERMEATE_SOFTWARE_FLOW_ANALYSE_CONFIG = "PERMEATE_SOFTWARE_FLOW_ANALYSE_CONFIG";
    public static final String PERMEATE_SOFTWARE_FLOW_ANALYSE_JOB_NAME = "PERMEATE_SOFTWARE_FLOW_ANALYSE_JOB_NAME";
    public static final String PERMEATE_SOFTWARE_FLOW_ANALYSE_SOURCE_PARALLELISM = "PERMEATE_SOFTWARE_FLOW_ANALYSE_SOURCE_PARALLELISM";
    public static final String PERMEATE_SOFTWARE_FLOW_ANALYSE_DEAL_PARALLELISM = "PERMEATE_SOFTWARE_FLOW_ANALYSE_DEAL_PARALLELISM";
    public static final String PERMEATE_SOFTWARE_FLOW_ANALYSE_SINK_PARALLELISM = "PERMEATE_SOFTWARE_FLOW_ANALYSE_SINK_PARALLELISM";
    public static final String PERMEATE_SOFTWARE_FLOW_ANALYSE_GROUP_ID = "PERMEATE_SOFTWARE_FLOW_ANALYSE_GROUP_ID";
    public static final String PERMEATE_SOFTWARE_FLOW_ANALYSE_CHECKPOINT_INTERVAL = "PERMEATE_SOFTWARE_FLOW_ANALYSE_CHECKPOINT_INTERVAL";
    public static final String PERMEATE_SOFTWARE_FLOW_ANALYSE_SINK_TOPIC = "PERMEATE_SOFTWARE_FLOW_ANALYSE_SINK_TOPIC";
    public static final String PERMEATE_SOFTWARE_FLOW_ANALYSE_USER_AGENT_LIST = "PERMEATE_SOFTWARE_FLOW_ANALYSE_USER_AGENT_LIST";
    public static final String PERMEATE_SOFTWARE_FLOW_ANALYSE_ACCEPT = "PERMEATE_SOFTWARE_FLOW_ANALYSE_ACCEPT";
    public static final String PERMEATE_SOFTWARE_FLOW_ANALYSE_XFF_FLAG = "PERMEATE_SOFTWARE_FLOW_ANALYSE_XFF_FLAG";
    public static final String PERMEATE_SOFTWARE_FLOW_ANALYSE_TIME_WINDOW = "PERMEATE_SOFTWARE_FLOW_ANALYSE_TIME_WINDOW";
    public static final String PERMEATE_SOFTWARE_FLOW_ANALYSE_COOKIE_ICE = "PERMEATE_SOFTWARE_FLOW_ANALYSE_COOKIE_ICE";
    public static final String PERMEATE_SOFTWARE_FLOW_ANALYSE_URL_REGEX = "PERMEATE_SOFTWARE_FLOW_ANALYSE_URL_REGEX";


    //#反弹shell
    public static final String REBOUND_SHELL_WARN_CONFIG = "REBOUND_SHELL_WARN_CONFIG";
    public static final String REBOUND_SHELL_WARN_CONFIG_JOB_NAME = "REBOUND_SHELL_WARN_CONFIG_JOB_NAME";
    public static final String REBOUND_SHELL_WARN_CONFIG_SOURCE_PARALLELISM = "REBOUND_SHELL_WARN_CONFIG_SOURCE_PARALLELISM";
    public static final String REBOUND_SHELL_WARN_CONFIG_DEAL_PARALLELISM = "REBOUND_SHELL_WARN_CONFIG_DEAL_PARALLELISM";
    public static final String REBOUND_SHELL_WARN_CONFIG_SINK_PARALLELISM = "REBOUND_SHELL_WARN_CONFIG_SINK_PARALLELISM";
    public static final String REBOUND_SHELL_WARN_CONFIG_GROUP_ID = "REBOUND_SHELL_WARN_CONFIG_GROUP_ID";
    public static final String REBOUND_SHELL_WARN_CONFIG_CHECKPOINT_INTERVAL = "REBOUND_SHELL_WARN_CONFIG_CHECKPOINT_INTERVAL";
    public static final String REBOUND_SHELL_WARN_CONFIG_SOURCE_TOPIC = "REBOUND_SHELL_WARN_CONFIG_SOURCE_TOPIC";
    public static final String REBOUND_SHELL_WARN_CONFIG_SINK_TOPIC = "REBOUND_SHELL_WARN_CONFIG_SINK_TOPIC";
    public static final String REBOUND_SHELL_WARN_CONFIG_TIME_WINDOW = "REBOUND_SHELL_WARN_CONFIG_TIME_WINDOW";
    public static final String REBOUND_SHELL_WARN_CONFIG_REBOUND_ORDER_FLAG = "REBOUND_SHELL_WARN_CONFIG_REBOUND_ORDER_FLAG";


    //#Fscan工具Web漏洞、Tcp横向端口扫描、主机探测异常行为检测
    public static final String FSCAN_ABNORMAL_ANALYSE_CONFIG = "FSCAN_ABNORMAL_ANALYSE_CONFIG";
    public static final String FSCAN_ABNORMAL_ANALYSE_CONFIG_JOB_NAME = "FSCAN_ABNORMAL_ANALYSE_CONFIG_JOB_NAME";
    public static final String FSCAN_ABNORMAL_ANALYSE_CONFIG_CHECKPOINT_INTERVAL = "FSCAN_ABNORMAL_ANALYSE_CONFIG_CHECKPOINT_INTERVAL";
    public static final String FSCAN_ABNORMAL_ANALYSE_CONFIG_GROUP_ID = "FSCAN_ABNORMAL_ANALYSE_CONFIG_GROUP_ID";
    public static final String FSCAN_ABNORMAL_ANALYSE_CONFIG_WEB_LEAK_KAFKA_SOURCE_TOPIC = "FSCAN_ABNORMAL_ANALYSE_CONFIG_WEB_LEAK_KAFKA_SOURCE_TOPIC";
    public static final String FSCAN_ABNORMAL_ANALYSE_CONFIG_TCP_PORT_SCAN_KAFKA_SOURCE_TOPIC = "FSCAN_ABNORMAL_ANALYSE_CONFIG_TCP_PORT_SCAN_KAFKA_SOURCE_TOPIC";
    public static final String FSCAN_ABNORMAL_ANALYSE_CONFIG_KAFKA_SOURCE_NAME = "FSCAN_ABNORMAL_ANALYSE_CONFIG_KAFKA_SOURCE_NAME";
    public static final String FSCAN_ABNORMAL_ANALYSE_CONFIG_KAFKA_SINK_NAME = "FSCAN_ABNORMAL_ANALYSE_CONFIG_KAFKA_SINK_NAME";
    public static final String FSCAN_ABNORMAL_ANALYSE_CONFIG_SOURCE_PARALLELISM = "FSCAN_ABNORMAL_ANALYSE_CONFIG_SOURCE_PARALLELISM";
    public static final String FSCAN_ABNORMAL_ANALYSE_CONFIG_SINK_PARALLELISM = "FSCAN_ABNORMAL_ANALYSE_CONFIG_SINK_PARALLELISM";
    public static final String FSCAN_ABNORMAL_ANALYSE_CONFIG_DEAL_PARALLELISM = "FSCAN_ABNORMAL_ANALYSE_CONFIG_DEAL_PARALLELISM";
    public static final String FSCAN_ABNORMAL_ANALYSE_CONFIG_KAFKA_SINK_TOPIC = "FSCAN_ABNORMAL_ANALYSE_CONFIG_KAFKA_SINK_TOPIC";
    public static final String FSCAN_ABNORMAL_ANALYSE_CONFIG_SQL_SINK_NAME = "FSCAN_ABNORMAL_ANALYSE_CONFIG_SQL_SINK_NAME";
    public static final String FSCAN_ABNORMAL_ANALYSE_CONFIG_SQL_SINK_PARALLELISM = "FSCAN_ABNORMAL_ANALYSE_CONFIG_SQL_SINK_PARALLELISM";


    //#Fscan工具Tcp端口扫描异常行为检测
    public static final String FSCAN_ABNORMAL_ANALYSE_TCP_CONFIG = "FSCAN_ABNORMAL_ANALYSE_TCP_CONFIG";
    public static final String FSCAN_ABNORMAL_ANALYSE_TCP_CONFIG_JOB_NAME = "FSCAN_ABNORMAL_ANALYSE_TCP_CONFIG_JOB_NAME";
    public static final String FSCAN_ABNORMAL_ANALYSE_TCP_CONFIG_CHECKPOINT_INTERVAL = "FSCAN_ABNORMAL_ANALYSE_TCP_CONFIG_CHECKPOINT_INTERVAL";
    public static final String FSCAN_ABNORMAL_ANALYSE_TCP_CONFIG_GROUP_ID = "FSCAN_ABNORMAL_ANALYSE_TCP_CONFIG_GROUP_ID";
    public static final String FSCAN_ABNORMAL_ANALYSE_TCP_CONFIG_KAFKA_SOURCE_TOPIC = "FSCAN_ABNORMAL_ANALYSE_TCP_CONFIG_KAFKA_SOURCE_TOPIC";
    public static final String FSCAN_ABNORMAL_ANALYSE_TCP_CONFIG_KAFKA_SOURCE_NAME = "FSCAN_ABNORMAL_ANALYSE_TCP_CONFIG_KAFKA_SOURCE_NAME";
    public static final String FSCAN_ABNORMAL_ANALYSE_TCP_CONFIG_KAFKA_SINK_NAME = "FSCAN_ABNORMAL_ANALYSE_TCP_CONFIG_KAFKA_SINK_NAME";
    public static final String FSCAN_ABNORMAL_ANALYSE_TCP_CONFIG_SOURCE_PARALLELISM = "FSCAN_ABNORMAL_ANALYSE_TCP_CONFIG_SOURCE_PARALLELISM";
    public static final String FSCAN_ABNORMAL_ANALYSE_TCP_CONFIG_SINK_PARALLELISM = "FSCAN_ABNORMAL_ANALYSE_TCP_CONFIG_SINK_PARALLELISM";
    public static final String FSCAN_ABNORMAL_ANALYSE_TCP_CONFIG_DEAL_PARALLELISM = "FSCAN_ABNORMAL_ANALYSE_TCP_CONFIG_DEAL_PARALLELISM";
    public static final String FSCAN_ABNORMAL_ANALYSE_TCP_KAFKA_SINK_TOPIC = "FSCAN_ABNORMAL_ANALYSE_TCP_KAFKA_SINK_TOPIC";
    public static final String FSCAN_ABNORMAL_ANALYSE_TCP_CONFIG_SQL_SINK_NAME = "FSCAN_ABNORMAL_ANALYSE_TCP_CONFIG_SQL_SINK_NAME";
    public static final String FSCAN_ABNORMAL_ANALYSE_TCP_CONFIG_SQL_SINK_PARALLELISM = "FSCAN_ABNORMAL_ANALYSE_TCP_CONFIG_SQL_SINK_PARALLELISM";


    //#Fscan工具Web漏洞、主机存活探测异常行为检测
    public static final String FSCAN_ABNORMAL_ANALYSE_WEB_MASTER_CONFIG = "FSCAN_ABNORMAL_ANALYSE_WEB_MASTER_CONFIG";
    public static final String FSCAN_ABNORMAL_ANALYSE_WEB_MASTER_CONFIG_JOB_NAME = "FSCAN_ABNORMAL_ANALYSE_WEB_MASTER_CONFIG_JOB_NAME";
    public static final String FSCAN_ABNORMAL_ANALYSE_WEB_MASTER_CONFIG_CHECKPOINT_INTERVAL = "FSCAN_ABNORMAL_ANALYSE_WEB_MASTER_CONFIG_CHECKPOINT_INTERVAL";
    public static final String FSCAN_ABNORMAL_ANALYSE_WEB_MASTER_CONFIG_GROUP_ID = "FSCAN_ABNORMAL_ANALYSE_WEB_MASTER_CONFIG_GROUP_ID";
    public static final String FSCAN_ABNORMAL_ANALYSE_WEB_MASTER_CONFIG_KAFKA_SOURCE_TOPIC = "FSCAN_ABNORMAL_ANALYSE_WEB_MASTER_CONFIG_KAFKA_SOURCE_TOPIC";
    public static final String FSCAN_ABNORMAL_ANALYSE_WEB_MASTER_CONFIG_KAFKA_SOURCE_NAME = "FSCAN_ABNORMAL_ANALYSE_WEB_MASTER_CONFIG_KAFKA_SOURCE_NAME";
    public static final String FSCAN_ABNORMAL_ANALYSE_WEB_MASTER_CONFIG_KAFKA_SINK_NAME = "FSCAN_ABNORMAL_ANALYSE_WEB_MASTER_CONFIG_KAFKA_SINK_NAME";
    public static final String FSCAN_ABNORMAL_ANALYSE_WEB_MASTER_CONFIG_SOURCE_PARALLELISM = "FSCAN_ABNORMAL_ANALYSE_WEB_MASTER_CONFIG_SOURCE_PARALLELISM";
    public static final String FSCAN_ABNORMAL_ANALYSE_WEB_MASTER_CONFIG_SINK_PARALLELISM = "FSCAN_ABNORMAL_ANALYSE_WEB_MASTER_CONFIG_SINK_PARALLELISM";
    public static final String FSCAN_ABNORMAL_ANALYSE_WEB_MASTER_CONFIG_DEAL_PARALLELISM = "FSCAN_ABNORMAL_ANALYSE_WEB_MASTER_CONFIG_DEAL_PARALLELISM";
    public static final String FSCAN_ABNORMAL_ANALYSE_WEB_KAFKA_SINK_TOPIC = "FSCAN_ABNORMAL_ANALYSE_WEB_KAFKA_SINK_TOPIC";
    public static final String FSCAN_ABNORMAL_ANALYSE_MASTER_KAFKA_SINK_TOPIC = "FSCAN_ABNORMAL_ANALYSE_MASTER_KAFKA_SINK_TOPIC";
    public static final String FSCAN_ABNORMAL_ANALYSE_WEB_MASTER_CONFIG_SQL_SINK_NAME = "FSCAN_ABNORMAL_ANALYSE_WEB_MASTER_CONFIG_SQL_SINK_NAME";
    public static final String FSCAN_ABNORMAL_ANALYSE_WEB_MASTER_CONFIG_SQL_SINK_PARALLELISM = "FSCAN_ABNORMAL_ANALYSE_WEB_MASTER_CONFIG_SQL_SINK_PARALLELISM";


    //#蚁剑使用异常行为流量检测
    public static final String FLINK_ANT_SWORD_CONFIG = "FLINK_ANT_SWORD_CONFIG";
    public static final String FLINK_ANT_SWORD_CONFIG_JOB_NAME = "FLINK_ANT_SWORD_CONFIG_JOB_NAME";
    public static final String FLINK_ANT_SWORD_CONFIG_SOURCE_PARALLELISM = "FLINK_ANT_SWORD_CONFIG_SOURCE_PARALLELISM";
    public static final String FLINK_ANT_SWORD_CONFIG_DEAL_PARALLELISM = "FLINK_ANT_SWORD_CONFIG_DEAL_PARALLELISM";
    public static final String FLINK_ANT_SWORD_CONFIG_SINK_PARALLELISM = "FLINK_ANT_SWORD_CONFIG_SINK_PARALLELISM";
    public static final String FLINK_ANT_SWORD_CONFIG_GROUP_ID = "FLINK_ANT_SWORD_CONFIG_GROUP_ID";
    public static final String FLINK_ANT_SWORD_CONFIG_CHECKPOINT_INTERVAL = "FLINK_ANT_SWORD_CONFIG_CHECKPOINT_INTERVAL";
    public static final String FLINK_ANT_SWORD_CONFIG_KAFKA_SOURCE_TOPIC = "FLINK_ANT_SWORD_CONFIG_KAFKA_SOURCE_TOPIC";
    public static final String FLINK_ANT_SWORD_CONFIG_KAFKA_SOURCE_NAME = "FLINK_ANT_SWORD_CONFIG_KAFKA_SOURCE_NAME";
    public static final String FLINK_ANT_SWORD_CONFIG_KAFKA_SINK_TOPIC = "FLINK_ANT_SWORD_CONFIG_KAFKA_SINK_TOPIC";
    public static final String FLINK_ANT_SWORD_CONFIG_KAFKA_SINK_NAME = "FLINK_ANT_SWORD_CONFIG_KAFKA_SINK_NAME";


    //#新版本使用检测
    public static final String NEW_VERSION_TEST_CONFIG = "NEW_VERSION_TEST_CONFIG";
    public static final String NEW_VERSION_TEST_CONFIG_JOB_NAME = "NEW_VERSION_TEST_CONFIG_JOB_NAME";
    public static final String NEW_VERSION_TEST_CONFIG_CHECKPOINT_INTERVAL = "NEW_VERSION_TEST_CONFIG_CHECKPOINT_INTERVAL";
    public static final String NEW_VERSION_TEST_CONFIG_GROUP_ID = "NEW_VERSION_TEST_CONFIG_GROUP_ID";
    public static final String NEW_VERSION_TEST_CONFIG_KAFKA_SOURCE_TOPIC = "NEW_VERSION_TEST_CONFIG_KAFKA_SOURCE_TOPIC";
    public static final String NEW_VERSION_TEST_CONFIG_KAFKA_SOURCE_NAME = "NEW_VERSION_TEST_CONFIG_KAFKA_SOURCE_NAME";
    public static final String NEW_VERSION_TEST_CONFIG_KAFKA_SINK_NAME = "NEW_VERSION_TEST_CONFIG_KAFKA_SINK_NAME";
    public static final String NEW_VERSION_TEST_CONFIG_SOURCE_PARALLELISM = "NEW_VERSION_TEST_CONFIG_SOURCE_PARALLELISM";
    public static final String NEW_VERSION_TEST_CONFIG_SINK_PARALLELISM = "NEW_VERSION_TEST_CONFIG_SINK_PARALLELISM";
    public static final String NEW_VERSION_TEST_CONFIG_DEAL_PARALLELISM = "NEW_VERSION_TEST_CONFIG_DEAL_PARALLELISM";
    public static final String NEW_VERSION_TEST_CONFIG_KAFKA_SINK_TOPIC = "NEW_VERSION_TEST_CONFIG_KAFKA_SINK_TOPIC";
    public static final String NEW_VERSION_TEST_CONFIG_SQL_SINK_NAME = "NEW_VERSION_TEST_CONFIG_SQL_SINK_NAME";
    public static final String NEW_VERSION_TEST_CONFIG_SQL_SINK_PARALLELISM = "NEW_VERSION_TEST_CONFIG_SQL_SINK_PARALLELISM";

    public static final String TEST_UN_KNOW_RISK_WARN_CONFIG = "TEST_UN_KNOW_RISK_WARN_CONFIG";
    public static final String TEST_UN_KNOW_RISK_WARN_JOB_NAME = "TEST_UN_KNOW_RISK_WARN_JOB_NAME";
    public static final String TEST_UN_KNOW_RISK_WARN_GROUP_ID = "TEST_UN_KNOW_RISK_WARN_GROUP_ID";
    public static final String TEST_UN_KNOW_RISK_WARN_KAFKA_SINK_TOPIC = "TEST_UN_KNOW_RISK_WARN_KAFKA_SINK_TOPIC";
    public static final String TEST_UN_KNOW_RISK_WARN_WARNING_TOPIC = "TEST_UN_KNOW_RISK_WARN_WARNING_TOPIC";


    //test_合并
    public static final String TEST_PERMEATE_SOFTWARE_FLOW_ANALYSE_CONFIG = "TEST_PERMEATE_SOFTWARE_FLOW_ANALYSE_CONFIG";
    public static final String TEST_PERMEATE_SOFTWARE_FLOW_ANALYSE_JOB_NAME = "TEST_PERMEATE_SOFTWARE_FLOW_ANALYSE_JOB_NAME";
    public static final String TEST_PERMEATE_SOFTWARE_FLOW_ANALYSE_GROUP_ID = "TEST_PERMEATE_SOFTWARE_FLOW_ANALYSE_GROUP_ID";
    public static final String TEST_PERMEATE_SOFTWARE_FLOW_ANALYSE_SINK_TOPIC = "TEST_PERMEATE_SOFTWARE_FLOW_ANALYSE_SINK_TOPIC";


    //test_two_groupId
    public static final String LOCAL_TWO_GROUP_CONFIG = "LOCAL_TWO_GROUP_CONFIG";
    public static final String LOCAL_KAFKA_BOOTSTRAP_SERVERS = "LOCAL_KAFKA_BOOTSTRAP_SERVERS";
    public static final String LOCAL_DATA_GROUP_ID = "LOCAL_DATA_GROUP_ID";

    //堡垒机每日会话数
    public static final String BASTION_SESSION_DAY_COUNT_ANALYSE_CONFIG = "BASTION_SESSION_DAY_COUNT_ANALYSE_CONFIG";
    public static final String BASTION_SESSION_DAY_COUNT_ANALYSE_CONFIG_JOB_NAME = "BASTION_SESSION_DAY_COUNT_ANALYSE_CONFIG_JOB_NAME";
    public static final String BASTION_SESSION_DAY_COUNT_ANALYSE_CONFIG_CHECKPOINT_INTERVAL = "BASTION_SESSION_DAY_COUNT_ANALYSE_CONFIG_CHECKPOINT_INTERVAL";
    public static final String BASTION_SESSION_DAY_COUNT_ANALYSE_CONFIG_GROUP_ID = "BASTION_SESSION_DAY_COUNT_ANALYSE_CONFIG_GROUP_ID";
    public static final String BASTION_SESSION_DAY_COUNT_ANALYSE_CONFIG_KAFKA_SOURCE_TOPIC = "BASTION_SESSION_DAY_COUNT_ANALYSE_CONFIG_KAFKA_SOURCE_TOPIC";
    public static final String BASTION_SESSION_DAY_COUNT_ANALYSE_CONFIG_KAFKA_SOURCE_NAME = "BASTION_SESSION_DAY_COUNT_ANALYSE_CONFIG_KAFKA_SOURCE_NAME";
    public static final String BASTION_SESSION_DAY_COUNT_ANALYSE_CONFIG_KAFKA_SINK_NAME = "BASTION_SESSION_DAY_COUNT_ANALYSE_CONFIG_KAFKA_SINK_NAME";
    public static final String BASTION_SESSION_DAY_COUNT_ANALYSE_CONFIG_SOURCE_PARALLELISM = "BASTION_SESSION_DAY_COUNT_ANALYSE_CONFIG_SOURCE_PARALLELISM";
    public static final String BASTION_SESSION_DAY_COUNT_ANALYSE_CONFIG_SINK_PARALLELISM = "BASTION_SESSION_DAY_COUNT_ANALYSE_CONFIG_SINK_PARALLELISM";
    public static final String BASTION_SESSION_DAY_COUNT_ANALYSE_CONFIG_DEAL_PARALLELISM = "BASTION_SESSION_DAY_COUNT_ANALYSE_CONFIG_DEAL_PARALLELISM";
    public static final String BASTION_SESSION_DAY_COUNT_ANALYSE_CONFIG_KAFKA_SINK_TOPIC = "BASTION_SESSION_DAY_COUNT_ANALYSE_CONFIG_KAFKA_SINK_TOPIC";
    public static final String BASTION_SESSION_DAY_COUNT_ANALYSE_CONFIG_SQL_SINK_NAME = "BASTION_SESSION_DAY_COUNT_ANALYSE_CONFIG_SQL_SINK_NAME";
    public static final String BASTION_SESSION_DAY_COUNT_ANALYSE_CONFIG_SQL_SINK_PARALLELISM = "BASTION_SESSION_DAY_COUNT_ANALYSE_CONFIG_SQL_SINK_PARALLELISM";

    //streampark公共配置
    public static final String FLINK_STREAM_PARK_COMMON_CONFIG = "FLINK_STREAM_PARK_COMMON_CONFIG";
    public static final String KAFKA_BOOTSTRAP_SERVERS_TEST_ENV = "KAFKA_BOOTSTRAP_SERVERS_STREAM_PARK";


}
