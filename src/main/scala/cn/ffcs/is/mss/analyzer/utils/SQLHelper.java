package cn.ffcs.is.mss.analyzer.utils;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Auther chenwei
 * @Description 统一的sql操作类
 *              未实现线程同步，所以在使用时每个线程需要new新的对象
 *              使用c3p0连接池管理链接，所以使用前需要调用C3P0Util.ini初始化
 *
 * @Date: Created in 2017/10/16 上午10:43
 * @Modified By
 */
public class SQLHelper implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(SQLHelper.class);
    private static final long serialVersionUID = 7821871554246014602L;

    private Connection conn = null;
    private PreparedStatement ps = null;
    private ResultSet rs = null;
    public String database = null;

    /**
     * @Auther chenwei
     * @Date: Created in 2018/11/6 12:03
     * @Description
     * @Param []
     * @Return
     * @Other
     */
    public SQLHelper() {
        database = C3P0Util.database;
    }

    /**
     * @Auther chenwei
     * @Date: Created in 2018/11/6 12:04
     * @Description 执行sql的方法
     * @Param [sql]
     * @Return boolean 执行成功返回True 执行失败返回false
     * @Other
     */
    public boolean execute(String sql) {

        boolean isSucceed = false;
        if (sql == null || sql.length() == 0) {
            return false;
        }
        //获取数据库连接
        conn = C3P0Util.getConnection();

        try {
            if (conn != null) {
                ps = conn.prepareStatement(sql);
//                logger.info("prepare execute {}", sql);
                isSucceed = ps.execute(sql);
//                logger.info("execute {} isSucceed {}", sql, isSucceed);
            }
        } catch (Exception e) {
//            logger.error("A Exception occurred", e);
        } finally {
            C3P0Util.close(conn, ps, rs);
        }

        return isSucceed;
    }


    /**
     * @Auther chenwei
     * @Date: Created in 2018/12/27 17:18
     * @Description 根据sql查询数据库
     * @Param [sql]
     * @Return java.util.List<java.util.Map < java.lang.String , java.lang.Object>>
     * @Other
     */
    public List<Map<String, Object>> query(String sql) {

        List<Map<String, Object>> list = null;
        if (sql == null || sql.length() == 0) {
            return null;
        }
        //获取数据库连接
        conn = C3P0Util.getConnection();

        try {
            if (conn != null) {
                ps = conn.prepareStatement(sql);
//                logger.info("prepare execute {}", sql);
                rs = ps.executeQuery(sql);
                list = resultSetToList(rs);
//                logger.info("execute {} result size is {}", sql, list.size());
            }
        } catch (Exception e) {
//            logger.error("A Exception occurred", e);
        } finally {
            C3P0Util.close(conn, ps, rs);
        }

        return list;
    }

    /**
     * @Auther chenwei
     * @Date: Created in 2018/11/6 14:23
     * @Description 根据输入的sql查询数据库
     * @Param [sql, clazz]
     * @Return java.util.List<java.lang.Object>
     * @Other 返回所有的结果listlist
     */
    public List<Object> query(String sql, Class clazz) {
        return queryDeal(clazz, sql, null, false);
    }

    /**
     * @Auther chenwei
     * @Date: Created in 2018/11/6 14:24
     * @Description 查询一个表所有数据的方法
     * @Param [clazz]
     * @Return java.util.List<java.lang.Object>
     * @Other
     */
    public List<Object> query(Class clazz) {

        //获取该类的表名
        String tableName = null;
        try {
            Table table = (Table) clazz.getAnnotation(Table.class);
            tableName = table.name();
        } catch (Exception e) {
//            logger.error("A Exception occurred", e);
        }

        if (tableName == null) {
            return null;
        }

        //拼接查询sql
        String sql = " SELECT * FROM " + getDatabaseTableName(database, tableName);
        return queryDeal(clazz, sql, null, false);
    }


    /**
     * @Auther chenwei
     * @Date: Created in 2018/11/6 14:27
     * @Description 查询数据库, 如果onlyPrimaryKey为true则只根据主键查询, 否则根 据所有不为空的字段查询
     * @Param [object, onl yPrimaryKey]
     * @Return java.util.List<java.lang.Object>
     * @Other
     */
    public List<Object> query(Object object, boolean onlyPrimaryKey) {

        //根据对象获得类
        Class clazz = object.getClass();

        //获取该类的表名
        String tableName = null;

        try {
            Table table = (Table) clazz.getAnnotation(Table.class);
            tableName = table.name();
        } catch (Exception e) {
//            logger.error("A Exception occurred", e);
        }

        if (tableName == null) {
            return null;
        }

        //获取该类的所有字段
        Field[] fields = null;

        try {
            fields = clazz.getDeclaredFields();
        } catch (Exception e) {
//            logger.error("A Exception occurred", e);
        }

        if (fields == null) {
            return null;
        }

        //拼接Sql
        StringBuilder sqlStringBuilder = new StringBuilder(
            "SELECT * FROM " + getDatabaseTableName(database, tableName));
        StringBuilder whereStringBuilder = new StringBuilder();

        for (Field field : fields) {

            try {
                //使其可以访问私有字段
                field.setAccessible(true);
                //根据字段获取其get方法
                PropertyDescriptor propertyDescriptor = new PropertyDescriptor(field.getName(),
                    clazz);

                Method method = propertyDescriptor.getReadMethod();
                //获取其对应数据库的字段名
                Column column = method.getAnnotation(Column.class);
                //看其是否为数据库主键
                Id id = method.getAnnotation(Id.class);

                if (field.get(object) != null) {
                    if (!(onlyPrimaryKey && id == null)) {
                        whereStringBuilder.append(column.name());
                        whereStringBuilder.append(" = ? ");
                        whereStringBuilder.append("AND ");
                    }
                }
            } catch (Exception e) {
//                logger.error("A Exception occurred", e);
            }
        }

        //如果包含条件则拼接sql,如果不包含则不允许查询
        if (whereStringBuilder.length() > 4) {
            sqlStringBuilder.append(" WHERE ");
            whereStringBuilder.delete(whereStringBuilder.length() - 4, whereStringBuilder.length());
            sqlStringBuilder.append(whereStringBuilder);
            return queryDeal(clazz, sqlStringBuilder.toString(), object, onlyPrimaryKey);
        } else {
//            logger.info("execute {} is Failed no fields available", sqlStringBuilder.toString());
            return null;
        }
    }

    /**
     * @Auther chenwei
     * @Date: Created in 2018/11/6 14:27
     * @Description 根据where条件查询数据库
     * @Param [clazz, whereSql]
     * @Return java.util.List<java.lang.Object>
     * @Other
     */
    public List<Object> query(Class clazz, String whereSql) {

        try {
            //获取该类的数据库名和表名
            Table table = (Table) clazz.getAnnotation(Table.class);
            String tableName = table.name();

            //拼接Sql
            String sql = "SELECT * FROM " + getDatabaseTableName(database, tableName) + whereSql;

            return queryDeal(clazz, sql, null, false);
        } catch (Exception e) {
//            logger.error("A Exception occurred", e);
            return null;
        }
    }

    /**
     * @Auther chenwei
     * @Date: Created in 2018/11/6 14:28
     * @Description 处理查询数据库的方法
     * @Param [clazz, sql, object]
     * @Return java.util.List<java.lang.Object>
     * @Other
     */
    private List<Object> queryDeal(Class clazz, String sql, Object object, boolean onlyPrimaryKey) {

        //获取数据库连接
        conn = C3P0Util.getConnection();

        //新建一个动态数组用于保存结果
        List<Object> list = null;

        try {

            if (conn != null) {
                ps = conn.prepareStatement(sql);
//                logger.info("prepare execute {}", sql);

                if (object != null) {
                    ps = setObject(ps, object, onlyPrimaryKey);
                }

                //查询数据库
                rs = ps.executeQuery();
                //将结果转换为List
                if (clazz != null) {
                    list = resultSetToList(rs, clazz);
//                    logger.info("execute {} result size is {}", sql, list.size());
                }
            }

        } catch (Exception e) {
//            logger.error("A Exception occurred", e);
        } finally {
            C3P0Util.close(conn, ps, rs);
        }

        return list;
    }

    /**
     * @Auther chenwei
     * @Date: Created in 2018/11/6 14:41
     * @Description 根据主键批量更新数据
     * @Param [clazz, objects]
     * @Return boolean
     * @Other
     */
    public boolean update(Class clazz, List<Object> objects) {
        return updateDeal(clazz, objects);
    }

    /**
     * @Auther chenwei
     * @Date: Created in 2018/11/6 14:41
     * @Description 根据主键更新数据
     * @Param [object]
     * @Return boolean
     * @Other
     */
    public boolean update(Object object) {
        if (object == null) {
            return false;
        } else {
            List<Object> list = new ArrayList<>();
            //获取类型类
            Class clazz = object.getClass();
            list.add(object);
            return updateDeal(clazz, list);
        }

    }

    /**
     * @Auther chenwei
     * @Date: Created in 2018/11/6 14:44
     * @Description 根据条件批量更新
     * @Param [object, whereSql]
     * @Return boolean
     * @Other
     */
    public boolean update(Object object, String whereSql) {
        boolean isSucceed = false;
        if (object == null || whereSql == null || whereSql.length() == 0) {
            return false;
        }

        //获取连接
        conn = C3P0Util.getConnection();
        //根据对象获得类
        Class clazz = object.getClass();
        //获取该类的所有字段
        Field[] fields = null;
        try {
            fields = clazz.getDeclaredFields();
        } catch (Exception e) {
//            logger.error("A Exception occurred", e);
        }

        if (fields == null) {
            return false;
        }

        //获取该类的表名
        String tableName = null;

        try {
            Table table = (Table) clazz.getAnnotation(Table.class);
            tableName = table.name();
        } catch (Exception e) {
//            logger.error("A Exception occurred", e);
        }

        if (tableName == null) {
            return false;
        }

        StringBuilder sqlStringBuilder = new StringBuilder();
        //编辑插入sql
        sqlStringBuilder.append(" UPDATE ").append(getDatabaseTableName(database, tableName))
            .append(" SET ");
        for (Field field : fields) {

            try {
                //使其可以访问私有字段
                field.setAccessible(true);
                //根据字段获取起get方法
                PropertyDescriptor propertyDescriptor = new PropertyDescriptor(field.getName(),
                    clazz);
                Method method = propertyDescriptor.getReadMethod();

                //获取其对应数据库的字段名
                Column column = method.getAnnotation(Column.class);
                sqlStringBuilder.append(column.name());
                sqlStringBuilder.append(" = ? ,");

            } catch (Exception e) {
//                logger.error("A Exception occurred", e);
            }
        }

        sqlStringBuilder.delete(sqlStringBuilder.length() - 1, sqlStringBuilder.length());
        sqlStringBuilder.append(" WHERE ");
        sqlStringBuilder.append(whereSql);

        try {
            ps = conn.prepareStatement(sqlStringBuilder.toString());
//            logger.info("prepare execute {}", sqlStringBuilder.toString());

            int index = 1;
            for (Field field : fields) {

                //使其可以访问私有字段
                field.setAccessible(true);
                Object value = field.get(object);
                if (value != null) {
                    ps.setObject(index, value);
                    index++;
                }
            }

            int a = ps.executeUpdate();
            if (a > 0) {
                isSucceed = true;
            }
//            logger.info("execute {} result size is {}", sqlStringBuilder.toString(), a);
        } catch (Exception e) {
//            logger.error("A Exception occurred", e);
        } finally {
            C3P0Util.close(conn, ps, rs);
        }

        return isSucceed;
    }

    /**
     * @Auther chenwei
     * @Date: Created in 2018/11/6 14:45
     * @Description 执行更新数据库的方法
     * 防止误操作只允许针对主键的更新
     * @Param [clazz, objects]
     * @Return boolean
     * @Other
     */
    private boolean updateDeal(Class clazz, List<Object> objects) {

        boolean isSucceed = false;

        if (objects == null || objects.size() == 0) {
            return false;
        }

        //获取数据库连接
        conn = C3P0Util.getConnection();

        //获取该类的所有字段
        Field[] fields = null;
        try {
            fields = clazz.getDeclaredFields();
        } catch (Exception e) {
//            logger.error("A Exception occurred", e);
        }

        if (fields == null) {
            return false;
        }

        StringBuilder stringBuilder = new StringBuilder();
        StringBuilder whereStringBuilder = new StringBuilder();

        //获取该类的数据库名和表名
        String tableName = null;
        try {
            Table table = (Table) clazz.getAnnotation(Table.class);
            tableName = table.name();
        } catch (Exception e) {
//            logger.error("A Exception occurred", e);
        }

        if (tableName == null) {
            return false;
        }

        //拼接字符串
        stringBuilder.append(" UPDATE ").append(getDatabaseTableName(database, tableName))
            .append(" SET ");

        boolean[] isId = new boolean[fields.length];
        int valueNum = 0;
        for (int i = 0; i < fields.length; i++) {

            try {
                //使其可以访问私有字段
                fields[i].setAccessible(true);
                //根据字段获取其get方法
                PropertyDescriptor propertyDescriptor = new PropertyDescriptor(fields[i].getName(),
                    clazz);

                Method method = propertyDescriptor.getReadMethod();
                //获取其对应数据库的字段名
                Column column = method.getAnnotation(Column.class);
                //看其是否为数据库主键
                Id id = method.getAnnotation(Id.class);

                if (id != null) {

                    whereStringBuilder.append(column.name());
                    whereStringBuilder.append(" = ? ");
                    whereStringBuilder.append("AND ");
                    isId[i] = true;
                } else {

                    stringBuilder.append(column.name());
                    stringBuilder.append(" = ? ");
                    if (i != fields.length - 1) {
                        stringBuilder.append(" , ");
                    }
                    isId[i] = false;
                    valueNum++;
                }
            } catch (Exception e) {
//                logger.error("A Exception occurred", e);
            }
        }

        if (whereStringBuilder.length() > 0) {
            stringBuilder.append(" WHERE ");

            whereStringBuilder
                .delete(whereStringBuilder.length() - 4, whereStringBuilder.length());
            stringBuilder.append(whereStringBuilder.toString());
        } else {
//            logger.info("execute {} is Failed no fields available", stringBuilder.toString());
            return false;
        }

        try {

            int size = 0;
            ps = conn.prepareStatement(stringBuilder.toString());
//            logger.info(stringBuilder.toString());

            for (Object object : objects) {
                int iDnum = 0;
                for (int i = 0; i < fields.length; i++) {
                    if (isId[i]) {
                        fields[i].setAccessible(true);
                        ps.setObject((valueNum + iDnum + 1), fields[i].get(object));
                        iDnum++;
                    } else {
                        fields[i].setAccessible(true);
                        ps.setObject((i - iDnum + 1), fields[i].get(object));
                    }
                }

                ps.addBatch();
                size++;
                if (size % 1000 == 0) {
                    ps.executeBatch();
                }
            }

            int[] executeBatchResult = ps.executeBatch();

            // 记录执行成功和失败的个数
            isSucceed = true;
            long succeedCount = 0L;
            long failedCount = 0L;

            for (int executeResult : executeBatchResult) {
                if (executeResult == 0) {
                    failedCount++;
                } else {
                    succeedCount++;
                }
            }

            if (failedCount > 0) {
                isSucceed = false;
            }

//            logger.info("execute {} result size is {} succeedCount is {} failedCount is {}",
//                stringBuilder.toString(), executeBatchResult.length, succeedCount, failedCount);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            C3P0Util.close(conn, ps, rs);
        }

        return isSucceed;
    }

    /**
     * @Auther chenwei
     * @Date: Created in 2018/11/6 14:47
     * @Description 将对象其插入数据库的方法
     * @Param [object]
     * @Return boolean
     * @Other
     */
    public boolean insert(Object object) {
        if (object == null) {
            return false;
        } else {
            //获取类型类
            Class clazz = object.getClass();
            List<Object> objects = new ArrayList<>();
            objects.add(object);
            return insert(clazz, objects);
        }

    }

    /**
     * @Auther chenwei
     * @Date: Created in 2018/11/6 14:47
     * @Description 将批量对象其插入数据库的方法
     * @Param [clazz, objects]
     * @Return boolean
     * @Other
     */
    public boolean insert(Class clazz, List<Object> objects) {
        return insertDeal(clazz, objects);
    }

    /**
     * @Auther chenwei
     * @Date: Created in 2018/11/6 14:49
     * @Description 执行插入数据库的方法
     * @Param [clazz, objects]
     * @Return boolean
     * @Other
     */
    private boolean insertDeal(Class clazz, List<Object> objects) {

        boolean isSucceed = false;

        if (objects == null || objects.size() == 0) {
            return false;
        }

        //获取连接
        conn = C3P0Util.getConnection();
        //获取该类的所有字段
        Field[] fields = null;

        try {
            fields = clazz.getDeclaredFields();
        }catch (Exception e){
//            logger.error("A Exception occurred", e);
        }

        if (fields == null){
            return false;
        }

        StringBuilder sqlStringBuilder = new StringBuilder();
        StringBuilder sqlValueStringBuilder = new StringBuilder();

        //获取该类的数据库名和表名
        String tableName = null;

        try {
            Table table = (Table) clazz.getAnnotation(Table.class);
            tableName = table.name();
        }catch (Exception e){
//            logger.error("A Exception occurred", e);
        }

        if (tableName == null){
            return false;
        }


        //编辑插入sql
        sqlStringBuilder.append("INSERT INTO ").append(getDatabaseTableName(database, tableName))
            .append(" (");
        for (int i = 0; i < fields.length; i++) {

            try {
                //使其可以访问私有字段
                fields[i].setAccessible(true);
                //根据字段获取起get方法
                PropertyDescriptor propertyDescriptor = new PropertyDescriptor(fields[i].getName(),
                    clazz);

                Method method = propertyDescriptor.getReadMethod();

                //获取其对应数据库的字段名
                Column column = method.getAnnotation(Column.class);
                sqlStringBuilder.append(column.name());
                sqlValueStringBuilder.append("?");

                if (i != fields.length - 1) {
                    sqlStringBuilder.append(",");
                    sqlValueStringBuilder.append(",");
                }
            } catch (Exception e){
//                logger.error("A Exception occurred", e);
            }
        }

        sqlStringBuilder.append(") ").append("values (").append(sqlValueStringBuilder).append(")");

        try {
            ps = conn.prepareStatement(sqlStringBuilder.toString());
//            logger.info("prepare execute {}", sqlStringBuilder.toString());

            int size = 0;
            for (Object objectTemp : objects) {
                Class clazzTemp = objectTemp.getClass();
                Field[] fieldsTemp = clazzTemp.getDeclaredFields();

                for (int i = 0; i < fieldsTemp.length; i++) {
                    fieldsTemp[i].setAccessible(true);
                    ps.setObject(i + 1, fieldsTemp[i].get(objectTemp));
                }

                ps.addBatch();
                size++;
                if (size % 1000 == 0) {
                    ps.executeBatch();
                }

            }

            int[] executeBatchResult = ps.executeBatch();

            //记录插入成功和和插入失败的次数
            isSucceed = true;

            int succeedCount = 0;
            int failedCount = 0;

            for (int executeResult : executeBatchResult) {
                if (executeResult == 0) {
                    failedCount++;
                }else {
                    succeedCount++;
                }
            }

            if (failedCount > 0){
                isSucceed = false;
            }

//            logger.info("execute {} result size is {} succeedCount is {} failedCount is {}",
//                sqlStringBuilder.toString(), executeBatchResult.length, succeedCount, failedCount);

        } catch (Exception e) {
//            logger.error("A Exception occurred", e);
        } finally {
            C3P0Util.close(conn, ps, rs);
        }
        return isSucceed;

    }

    /**
     * @Auther chenwei
     * @Date: Created in 2018/11/6 14:51
     * @Description 根据条件删除数据表的方法
     * @Param [clazz, whereSql]
     * @Return boolean
     * @Other
     */
    public boolean delete(Class clazz, String whereSql) {

        if (whereSql == null || whereSql.length() <= 0) {
            return false;
        }

        try {
            //获取数据库名和表名
            Table table = (Table) clazz.getAnnotation(Table.class);
            String tableName = table.name();
            //拼接sql，根据where条件删除数据库信息
            String sql =
                "DELETE FROM " + getDatabaseTableName(database, tableName) + " WHERE " + whereSql;
            return deleteDeal(sql, null, false);

        }catch (Exception e){
//            logger.error("A Exception occurred", e);
        }
        return false;
    }


    /**
     * @Auther chenwei
     * @Description 根据主键删除数据库的数据
     * @Date: Created in 2017/10/17 上午12:06
     */
    public boolean delete(Object object, boolean onlyPrimaryKey) {

        //获取对象类型
        Class clazz = object.getClass();
        //获取所有字段
        Field[] fields = null;
        try {
            fields = clazz.getDeclaredFields();
        } catch (Exception e) {
//            logger.error("A Exception occurred", e);
        }

        if (fields == null) {
            return false;
        }

        //获取数据库名和表名
        String tableName = null;

        try {
            Table table = (Table) clazz.getAnnotation(Table.class);
            tableName = table.name();
        } catch (Exception e) {
//            logger.error("A Exception occurred", e);
        }

        if (tableName == null) {
            return false;
        }

        //拼接Sql
        StringBuilder sqlStringBuilder = new StringBuilder(
            "DELETE FROM " + getDatabaseTableName(database, tableName));
        StringBuilder whereStringBuilder = new StringBuilder();

        for (Field field : fields) {

            try {
                //使其可以访问私有字段
                field.setAccessible(true);
                //根据字段获取其get方法
                PropertyDescriptor propertyDescriptor = new PropertyDescriptor(field.getName(),
                    clazz);
                Method method = propertyDescriptor.getReadMethod();
                //获取其对应数据库的字段名
                Column column = method.getAnnotation(Column.class);
                //看其是否为数据库主键
                Id id = method.getAnnotation(Id.class);

                if (field.get(object) != null) {
                    if (!(onlyPrimaryKey && id == null)) {
                        whereStringBuilder.append(column.name());
                        whereStringBuilder.append(" = ? ");
                        whereStringBuilder.append(" and ");
                    }
                }
            } catch (Exception e) {
//                logger.error("A Exception occurred", e);
            }
        }

        //如果包含查询字段，则进行拼接并返回删除的结果。如果不包含则直接返回false
        if (whereStringBuilder.length() > 0) {
            sqlStringBuilder.append(" WHERE ");
            whereStringBuilder.delete(whereStringBuilder.length() - 5, whereStringBuilder.length());
            sqlStringBuilder.append(whereStringBuilder.toString());
            return deleteDeal(sqlStringBuilder.toString(), object, onlyPrimaryKey);

        } else {
//            logger.info("execute {} is Failed no fields available", sqlStringBuilder.toString());
            return false;
        }
    }

    /**
     * @Auther chenwei
     * @Date: Created in 2018/11/6 14:53
     * @Description 执行删除数据库的方法
     * @Param [sql, object]
     * @Return boolean
     * @Other
     */
    private boolean deleteDeal(String sql, Object object, boolean onlyPrimaryKey) {

        boolean isSucceed = false;

        //获取数据库连接
        conn = C3P0Util.getConnection();
        try {
            if (conn != null) {
                ps = conn.prepareStatement(sql);
//                logger.info("prepare execute {}", sql);
                if (object != null) {
                    ps = setObject(ps, object, onlyPrimaryKey);
                }

                int a = ps.executeUpdate();

                if (a > 0) {
                    isSucceed = true;
                }
//                logger.info("execute {} result size is {}", sql, a);
            }
        } catch (Exception e) {
//            logger.error("A Exception occurred", e);
        } finally {
            C3P0Util.close(conn, ps, rs);
        }
        return isSucceed;

    }

    /**
     * @Auther chenwei
     * @Date: Created in 2018/11/6 14:54
     * @Description 根据数据库名和表名拼接
     * @Param [database, tableName]
     * @Return java.lang.String
     * @Other
     */
    private static String getDatabaseTableName(String database, String tableName) {
        return " `" + database + "`.`" + tableName + "` ";
    }

    /**
     * @Auther chenwei
     * @Date: Created in 2018/11/6 14:55
     * @Description 将ResultSet转为List
     * @Param [rs]
     * @Return java.util.List<java.util.Map < java.lang.String, javlang.Object>>
     * @Other
     */
    private static List<Map<String, Object>> resultSetToList(ResultSet rs) {
        if (rs == null) {
            return null;
        }
        //得到结果集(rs)的结构信息，比如字段数、字段名等
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            ResultSetMetaData md = rs.getMetaData();
            //返回此 ResultSet 对象中的列数
            int columnCount = md.getColumnCount();

            Map<String, Object> rowData;

            while (rs.next()) {

                rowData = new HashMap<>(columnCount);

                for (int i = 1; i <= columnCount; i++) {

                    rowData.put(md.getColumnName(i), rs.getObject(i));
                }

                list.add(rowData);
            }
        } catch (Exception e) {
//            logger.error("A Exception occurred", e);
        }

        return list;
    }

    /**
     * @Auther chenwei
     * @Date: Created in 2018/11/6 14:56
     * @Description 将ResultSet转为List
     * @Param [rs, clazz]
     * @Return java.util.List<java.lang.Object>
     * @Other
     */
    private static List<Object> resultSetToList(ResultSet rs, Class clazz) {

        List<Object> arrayList = new ArrayList<>();

        try {
            //获取所有字段
            Field[] fields = clazz.getDeclaredFields();
            while (rs.next()) {
                arrayList.add(getObject(rs, clazz, fields));
            }
            rs.close();
        } catch (Exception e) {
//            logger.error("A Exception occurred", e);
        }

        return arrayList;

    }

    /**
     * @Auther chenwei
     * @Date: Created in 2018/11/6 14:56
     * @Description 根据查询到的ResultSet获取对象
     * @Param [rs, clazz, fields]
     * @Return java.lang.Object
     * @Other
     */
    private static Object getObject(ResultSet rs, Class clazz, Field[] fields) {
        Object object = null;
        try {
            //实例化类对象
            object = clazz.newInstance();
            for (Field field : fields) {
                field.setAccessible(true);
                //根据字段获取其get方法
                PropertyDescriptor propertyDescriptor = new PropertyDescriptor(field.getName(),
                    clazz);
                Method method = propertyDescriptor.getReadMethod();
                //获取其对应数据库的字段名
                Column column = method.getAnnotation(Column.class);
                field.set(object, rs.getObject(column.name()));
            }
        } catch (Exception e) {
//            logger.error("A Exception occurred", e);
        }

        return object;

    }

    /**
     * @Auther chenwei
     * @Date: Created in 2018/11/6 14:57
     * @Description 根据对象给PreparedStatement的占位符赋值
     * @Param [ps, object]
     * @Return java.sql.PreparedStatement
     * @Other
     */
    private static PreparedStatement setObject(PreparedStatement ps, Object object,
        boolean onlyPrimaryKey) {

        Class clazz = object.getClass();
        //获取所有字段
        Field[] fields = clazz.getDeclaredFields();

        int index = 1;
        for (Field field : fields) {

            try {
                //使其可以访问私有字段
                field.setAccessible(true);
                //根据字段获取其get方法
                PropertyDescriptor propertyDescriptor = new PropertyDescriptor(field.getName(),
                    clazz);
                Method method = propertyDescriptor.getReadMethod();
                //看其是否为数据库主键
                Id id = method.getAnnotation(Id.class);

                //如果字段不为空
                if (field.get(object) != null) {
                    //如果只设置主键值并且该字段是主键值则设置该字段值
                    if (!(onlyPrimaryKey && id == null)) {
                        ps.setObject(index, field.get(object));
                        index++;
                    }
                }

            } catch (Exception e) {
//                logger.error("A Exception occurred", e);
            }

        }

        return ps;
    }

    // /**
    //  * @Auther chenwei
    //  * @Description 在使用oracle数据库时，进行类型转换
    //  * @Date: Created in 2018/3/14 17:52
    //  */
    // private static Object getObject(Object object, Field field) {
    //
    //     Class fieldClazz = field.getType();
    //     if (object == null) {
    //         if (fieldClazz.equals(Integer.class)) {
    //             return 0;
    //         } else if (fieldClazz.equals(String.class)) {
    //             return "";
    //         } else if (fieldClazz.equals(Time.class)) {
    //             return new Time(System.currentTimeMillis());
    //         } else if (fieldClazz.equals(Long.class)) {
    //             return 0L;
    //         } else {
    //             System.out.println(field);
    //         }
    //     }
    //
    //     if (object != null) {
    //         Class objectClazz = object.getClass();
    //
    //         if (fieldClazz.equals(Time.class)) {
    //             Time time = null;
    //             if (objectClazz.equals(TIMESTAMP.class)) {
    //                 try {
    //                     time = ((TIMESTAMP) object).timeValue();
    //                 } catch (SQLException e) {
    //                     e.printStackTrace();
    //                 }
    //
    //             } else if (objectClazz.equals(DATE.class)) {
    //                 time = ((DATE) object).timeValue();
    //             } else if (objectClazz.equals(Time.class)) {
    //                 time = (Time) object;
    //             }
    //             return time;
    //
    //         } else if (fieldClazz.equals(Date.class)) {
    //             Date date = null;
    //             if (objectClazz.equals(TIMESTAMP.class)) {
    //                 try {
    //                     date = ((TIMESTAMP) object).dateValue();
    //                 } catch (SQLException e) {
    //                     e.printStackTrace();
    //                 }
    //
    //             } else if (objectClazz.equals(DATE.class)) {
    //                 date = ((DATE) object).dateValue();
    //             } else if (objectClazz.equals(Time.class)) {
    //                 date = new Date(((Time) object).getTime());
    //             } else if (objectClazz.equals(Timestamp.class)) {
    //                 date = new Date(((Timestamp) object).getTime());
    //             }
    //             return date;
    //         } else if (fieldClazz.equals(String.class)) {
    //             if (objectClazz.equals(CLOB.class)) {
    //                 try {
    //                     return ((CLOB) object).stringValue();
    //                 } catch (SQLException e) {
    //                     e.printStackTrace();
    //                 }
    //             }
    //             return object.toString();
    //         } else if (fieldClazz.equals(Long.class)) {
    //             if (objectClazz.equals(BigDecimal.class)) {
    //                 return ((BigDecimal) object).longValue();
    //             }
    //             return object;
    //         } else if (fieldClazz.equals(Double.class)) {
    //             if (objectClazz.equals(BigDecimal.class)) {
    //                 return ((BigDecimal) object).doubleValue();
    //             }
    //             return object;
    //         } else if (fieldClazz.equals(Integer.class)) {
    //             if (objectClazz.equals(BigDecimal.class)) {
    //                 return ((BigDecimal) object).intValue();
    //             }
    //             return object;
    //
    //         } else {
    //             return object;
    //         }
    //     } else {
    //         return null;
    //     }
    // }


    /**
     * @Auther chenwei
     * @Date: Created in 2018/11/6 14:59
     * @Description 获取数据库名
     * @Param []
     * @Return java.lang.String
     * @Other
     */
    public String getDatabase() {
        return database;
    }

    /**
     * @Auther chenwei
     * @Date: Created in 2018/11/6 14:59
     * @Description 设置数据库名
     * @Param [database]
     * @Return void
     * @Other
     */
    public void setDatabase(String database) {
        this.database = database;
    }

    /**
     * @Auther chenwei
     * @Date: Created in 2020/03/26 16:09
     * @Description 判断主键类型是否为自增主键
     * @Param [object]
     * @Return void
     * @Other
     */
    public boolean isGeneratedKey(Object object) {
        //根据对象获得类
        Class clazz = object.getClass();

        //获取该类的所有字段
        Field[] fields = null;
        try {
            fields = clazz.getDeclaredFields();
        }catch (Exception e){
//            logger.error("A Exception occurred", e);
        }

        if (fields == null){
            return false;
        }

        for (Field field : fields) {

            try {
                //使其可以访问私有字段
                field.setAccessible(true);
                //根据字段获取其get方法
                PropertyDescriptor propertyDescriptor = new PropertyDescriptor(field.getName(),
                    clazz);
                Method method = propertyDescriptor.getReadMethod();

                Id id = method.getAnnotation(Id.class);
                GeneratedValue generatedValue = method.getAnnotation(GeneratedValue.class);
                if (id != null && generatedValue != null) {
                    return true;
                }
            } catch (Exception e){
//                logger.error("A Exception occurred", e);
            }

        }

        return false;
    }

    /**
     * @Auther chenwei
     * @Date: Created in 2018/11/7 14:45
     * @Description 插入包含自增主键的数据，并返回主键
     * @Param [object]
     * @Return long
     * @Other
     */
    public long insertGeneratedKey(Object object) {
        if (object == null) {
            return -1L;
        }

        //获取类型类
        Class clazz = object.getClass();
        //获取连接
        conn = C3P0Util.getConnection();

        //获取该类的所有字段
        Field[] fields = null;
        try {
            fields = clazz.getDeclaredFields();
        }catch (Exception e){
//            logger.error("A Exception occurred", e);
        }

        if (fields == null){
            return -1L;
        }

        StringBuilder sql = new StringBuilder();
        StringBuilder sqlValue = new StringBuilder();

        //获取该类的数据库名和表名
        String tableName = null;

        try {
            Table table = (Table) clazz.getAnnotation(Table.class);
            tableName = table.name();
        }catch (Exception e){
//            logger.error("A Exception occurred", e);
        }

        if (
            tableName == null){
            return -1L;
        }

        //编辑插入sql
        sql.append("INSERT INTO ").append(getDatabaseTableName(database,tableName)).append(" (");

        //遍历该类的所有字段
        for (int i = 0; i < fields.length; i++) {

            try {
                //使其可以访问私有字段
                fields[i].setAccessible(true);
                //根据字段获取起get方法
                PropertyDescriptor propertyDescriptor = new PropertyDescriptor(fields[i].
                    getName(), clazz);

                Method method = propertyDescriptor.getReadMethod();

                //获取其对应数据库的字段名
                Column column = method.getAnnotation(Column.class);
                //添加字段名
                sql.append(column.name());
                sqlValue.append("?");

                if (i != fields.length - 1) {
                    sql.append(",");
                    sqlValue.append(",");
                }

            } catch (Exception e){
//                logger.error("A Exception occurred", e);
            }
        }

        sql.append(") ").append("VALUES (").append(sqlValue).append(")");

        try {
            ps = conn.prepareStatement(sql.toString(), Statement.RETURN_GENERATED_KEYS);
//            logger.info("prepare execute {}", sql);

            //设置字段值
            Field[] fieldsTemp = clazz.getDeclaredFields();
            for (int i = 0; i < fieldsTemp.length; i++) {
                fieldsTemp[i].setAccessible(true);
                ps.setObject(i + 1, fieldsTemp[i].get(object));
            }

            //执行插入语句
            ps.execute();
            //获取返回结果
            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                //获取主键值
                long generatedKey = rs.getLong(1);
//                logger.info("execute {} result size is {}", sql, generatedKey);
                return generatedKey;
            }

        } catch (Exception e) {
//            logger.error("A IntrospectionException occurred", e);
        } finally {
            C3P0Util.close(conn, ps, rs);
        }
        return -1L;
    }
}
