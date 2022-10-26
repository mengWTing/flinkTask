package cn.ffcs.is.mss.analyzer.utils;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import java.beans.PropertyVetoException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @Auther chenwei
 * @Description 使用c3p0连接池管理sql连接
 * @Date: Created in 2017/10/16 上午10:43
 * @Modified By
 */

public class C3P0Util implements Serializable {

  private static final Logger logger = LoggerFactory.getLogger(C3P0Util.class);
    private static final long serialVersionUID = 4262503779214581572L;
    private static ComboPooledDataSource cpds;

    public static String database = null;
    static {
        //这是oracle数据库
        // cpds = new ComboPooledDataSource("oracle");
        // 这是mysql数据库
        cpds = new ComboPooledDataSource("mysql");
    }

    public static void ini(Properties properties) {
        cpds = new ComboPooledDataSource("mysql");

        // cpds.setDescription();
        // 设置driverClass
        try {
            cpds.setDriverClass(
                properties.getProperty("c3p0.driverClass", "com.mysql.jdbc.Driver").trim());
        } catch (PropertyVetoException e) {
//            logger.error("A Exception occurred", e);
        }
        // 设置jdbcUrl
        cpds.setJdbcUrl(properties.getProperty("c3p0.jdbcUrl", "").trim());
        // 设置用户名
        cpds.setUser(properties.getProperty("c3p0.user", "").trim());
        // 设置密码
        cpds.setPassword(properties.getProperty("c3p0.password", "").trim());
        // 配置当连接池所有连接用完时应用程序getConnection的等待时间。为0则无限等待直至有其他连接释放或者创建新的连接，
        // 不为0则当时间到的时候如果仍没有获得连接，则会抛出SQLException。其实就acquireRetryAttempts*acquireRetryDelay。default : 0（与上面两个，有重复，选择其中两个都行）
        cpds.setCheckoutTimeout(
            Integer.parseInt(properties.getProperty("c3p0.checkoutTimeout", "0").trim()));
        // 连接池在无空闲连接可用时一次性创建的新数据库连接数,default : 3（建议使用）
        cpds.setAcquireIncrement(
            Integer.parseInt(properties.getProperty("c3p0.acquireIncrement", "3").trim()));
        // 连接池在获得新连接失败时重试的次数，如果小于等于0则无限重试直至连接获得成功。default : 30（建议使用）
        cpds.setAcquireRetryAttempts(
            Integer.parseInt(properties.getProperty("c3p0.acquireRetryAttempts", "30").trim()));
        // 连接池在获得新连接时的间隔时间。default : 1000 单位ms（建议使用）
        cpds.setAcquireRetryDelay(
            Integer.parseInt(properties.getProperty("c3p0.acquireRetryDelay", "1000").trim()));
        // 连接池在回收数据库连接时是否自动提交事务。如果为false，则会回滚未提交的事务，如果为true，则会自动提交事务。default : false（不建议使用）
        cpds.setAutoCommitOnClose(
            Boolean.parseBoolean(properties.getProperty("c3p0.autoCommitOnClose", "false").trim()));
        // 测试连接使用的类名称
        // cpds.setConnectionTesterClassName() ;
        // 配置一个表名，连接池根据这个表名用自己的测试sql语句在这个空表上测试数据库连接,这个表只能由c3p0来使用，用户不能操作。default : null（不建议使用）
        cpds.setAutomaticTestTable(
            properties.getProperty("c3p0.automaticTestTable", "null").trim());
        // 这个配置强烈不建议为true。default : false（不建议使用）
        // cpds.setForceIgnoreUnresolvedTransactions();
        // 用来配置测试空闲连接的间隔时间。测试方式还是上面的两种之一，可以用来解决MySQL8小时断开连接的问题。
        // 因为它保证连接池会每隔一定时间对空闲连接进行一次测试，从而保证有效的空闲连接能每隔一定时间访问一次数据库，将
        // 于MySQL8小时无会话的状态打破。为0则不测试。default : 0(建议使用)
        cpds.setIdleConnectionTestPeriod(
            Integer.parseInt(properties.getProperty("c3p0.idleConnectionTestPeriod", "0").trim()));
        // 连接池初始化时创建的连接数,default : 3（建议使用）
        cpds.setInitialPoolSize(
            Integer.parseInt(properties.getProperty("c3p0.initialPoolSize", "3").trim()));
        // 连接的最大空闲时间，如果超过这个时间，某个数据库连接还没有被使用，则会断开掉这个连接。如果为0，则永远不会断开连接,即回收此连接。default : 0 单位 s（建议使用）
        cpds.setMaxIdleTime(
            Integer.parseInt(properties.getProperty("c3p0.maxIdleTime", "0").trim()));
        // 连接池中拥有的最大连接数，如果获得新连接时会使连接总数超过这个  值则不会再获取新连接，而是等待其他连接释放，所以这个值有可能会设计地很大,default : 15（建议使用）
        cpds.setMaxPoolSize(
            Integer.parseInt(properties.getProperty("c3p0.maxPoolSize", "15").trim()));

        // 连接池为数据源缓存的PreparedStatement的总数。由于PreparedStatement属于单个Connection,
        // 所以这个数量应该根据应用中平均连接数乘以每个连接的平均PreparedStatement来计算。同时maxStatementsPerConnection的配置无效。default : 0（不建议使用）
        // cpds.setMaxStatements();
        // 连接池为数据源单个Connection缓存的PreparedStatement数，这个配置比maxStatements更有意义，
        // 因为它缓存的服务对象是单个数据连接，如果设置的好，肯定是可以提高性能的。为0的时候不缓存。default : 0（看情况而论）
        // cpds.setMaxStatementsPerConnection();
        // 连接池保持的最小连接数,default : 3（建议使用）
        cpds.setMinPoolSize(
            Integer.parseInt(properties.getProperty("c3p0.minPoolSize", "3").trim()));
        // 设置覆盖默认用户
        // cpds.setOverrideDefaultUser();
        // 设置覆盖默认密码
        // cpds.setOverrideDefaultPassword();
        // 用户修改系统配置参数执行前最多等待的秒数.默认为300;
        // cpds.setPropertyCycle();
        // true表示pool向数据库请求连接失败后标记整个pool为block并close，就算后端数据库恢复正常也不进行重连，客户端对pool的请求都拒绝掉。
        // false表示不会标记 pool为block，新的请求都会尝试去数据库请求connection。默认为false。
        // 因此，如果想让数据库和网络故障恢复之后，pool能继续请求正常资源必须把此项配置设为false
        cpds.setBreakAfterAcquireFailure(Boolean
            .parseBoolean(properties.getProperty("c3p0.breakAfterAcquireFailure", "false").trim()));
        // 性能消耗大。如果为true，在每次getConnection的时候都会测试，为了提高性能,尽量不要用。default : false（不建议使用）
        // cpds.setTestConnectionOnCheckout();
        // 如果为true，则在close的时候测试连接的有效性。default : false（不建议使用）
        cpds.setTestConnectionOnCheckin(Boolean
            .parseBoolean(properties.getProperty("c3p0.testConnectionOnCheckin", "false").trim()));
        // 使用传统的反射代理
        // cpds.setUsesTraditionalReflectiveProxies();
        // 与上面的automaticTestTable二者只能选一。自己实现一条SQL   检测语句。default : null（建议使用）
        // cpds.setPreferredTestQuery();
        // cpds.setUserOverridesAsString();
        // cpds.setMaxAdministrativeTaskTime();
        // 这个配置主要是为了快速减轻连接池的负载，比如连接池中连接数因为某次数据访问高峰导致创建了很多数据连接，但是后面的时间段需要的数据库连接数很少，
        // 需要快速释放，必须小于maxIdleTime。其实这个没必要 配置，maxIdleTime已经配置了。default : 0 单位 s（不建议使用）
        // cpds.setMaxIdleTimeExcessConnections();
        // 配置连接的生存时间，超过这个时间的连接将由连接池自动断开丢弃掉。当然正在使用的连接不会马上断开，而是等待它close再断开。
        // 配置为0的时候则不会对连接的生存时间进行限制。 default : 0 单位 s（不建议使用）
        cpds.setMaxConnectionAge(
            Integer.parseInt(properties.getProperty("c3p0.maxConnectionAge", "0").trim()));
        // 用来定制Connection的管理，比如在Connection acquire 的时候设定Connection的隔离级别，或者在Connection丢弃的时候进行资源关闭，
        // 就可以通过继承一个AbstractConnectionCustomizer来实现相关方法，配置的时候使用全类名。有点类似监听器的作用。default : null（不建议使用）
        // cpds.setConnectionCustomizerClassName();
        // 为0的时候要求所有的Connection在应用程序中必须关闭。如果不为0，则强制在设定的时间到达后回收Connection，所以必须小心设置，
        // 保证在回收之前所有数据库操作都能够完成。这种限制减少Connection未关闭情况的不是很适用。建议手动关闭。default : 0 单位 s（不建议使用）
        cpds.setUnreturnedConnectionTimeout(Integer
            .parseInt(properties.getProperty("c3p0.unreturnedConnectionTimeout", "0").trim()));
        // 如果为true并且unreturnedConnectionTimeout设为大于0的值，当所有被getConnection出去的连接unreturnedConnectionTimeout时间到的时候，
        // 就会打印出堆栈信息。只能在debug模式下适用，因为打印堆栈信息会减慢getConnection的速度 default : false（不建议使用）
        cpds.setDebugUnreturnedConnectionStackTraces(Boolean.parseBoolean(
            properties.getProperty("c3p0.debugUnreturnedConnectionStackTraces", "false").trim()));
        // 能是被用来说明C3P0类能从什么地方下载，如果C3P0数据源没有被本地安装，它能是被客户端从JNDI数据源作为引用进行索引。
        // cpds.setFactoryClassLocation();

        // 设置数据库名
        database = properties.getProperty("c3p0.database");

    }



    /**
     * @Auther chenwei
     * @Description 从连接池获取数据库连接
     * @Date: Created in 2017/11/21 18:13
     * @return
     */
    public static Connection getConnection() {
        try {
            return cpds.getConnection();
        } catch (SQLException e) {
//            logger.error("A Exception occurred", e);
            return null;
        }
    }

    /**
     * @Auther chenwei
     * @Description 关闭数据库连接，放回连接池
     * @Date: Created in 2017/11/21 18:13
     * @param conn
     * @param pst
     * @param rs
     */
    public static void close(Connection conn, PreparedStatement pst, ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
//                logger.error("A Exception occurred", e);
            }
        }
        if (pst != null) {
            try {
                pst.close();
            } catch (SQLException e) {
//                logger.error("A Exception occurred", e);
            }
        }

        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
//                logger.error("A Exception occurred", e);
            }
        }
    }
}