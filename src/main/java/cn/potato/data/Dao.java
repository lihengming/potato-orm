package cn.potato.data;

import cn.potato.helper.BeanHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Column;
import javax.sql.DataSource;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.sql.*;
import java.util.*;

/**
 * 数据库访问对象，提供增删改查的操作。
 *
 * @author 李恒名
 * @since 2016年3月16日
 */
public abstract class Dao<T> {
    private static DataSource dataSource = new SimpleDataSource();
    private static final Logger log = LoggerFactory.getLogger(Dao.class);
    protected Class<?> entityClass;

    public Dao() {
        ParameterizedType type = (ParameterizedType) this.getClass().getGenericSuperclass();
        entityClass = (Class<?>) type.getActualTypeArguments()[0];
    }

    //增接口
    protected void save(Object entity) {
        log.debug(">------------ORM Begin--------------<");
        log.debug("Method：Save  |  Object: " + entity);
        String sql = buildSQL(entity, SQLType.Insert);
        Number id = execute(sql);
        //通过反射将生成的主键注入回实体对象
        try {
            BeanHelper.setProperty(entity, "id", id);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("主键注入失败：" + e.getMessage());
        }
        log.debug(">------------ORM End----------------<");
    }

    //删接口
    protected void delete(Object entity) {
        log.debug(">------------ORM Begin--------------<");
        log.debug("Method：Delete  |  Object: " + entity);
        String sql = buildSQL(entity, SQLType.Delete);
        execute(sql);
        log.debug(">------------ORM End----------------<");
    }

    //改接口
    protected void update(Object entity) {
        log.debug(">------------ORM Begin--------------<");
        log.debug("Method：Update  |  Object: " + entity);
        String sql = buildSQL(entity, SQLType.Update);
        execute(sql);
        log.debug(">------------ORM End----------------<");
    }

    //查接口
    protected List<Object> query(String sql, Object... args) {
        log.debug(">------------ORM Begin--------------<");
        log.debug("Method：Query");
        List<Object> list = executeQuery(sql, args);
        log.debug("ORM Result：" + list);
        log.debug(">------------ORM End----------------<");
        return list;
    }


    /**
     * 执行查询的SQL执行器
     *
     * @param entityClass 实体类
     * @param sql         sql语句
     * @param args        查询参数
     * @return
     * @author 李恒名
     * @since 2016年3月16日
     */
    private List<Object> executeQuery(String sql, Object... args) {
        Connection conn = null;
        List<Object> list = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement statement = getStatement(sql, conn, args);
            ResultSet result = statement.executeQuery();
            log.debug("DB：SQL execute finnish!");
            list = parseResultSet(result);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (conn != null)
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
        }
        return list;
    }

    /**
     * 增删改的SQL执行器
     *
     * @param sql
     * @param args
     * @return 主键
     * @author 李恒名
     * @since 2016年3月16日
     */
    private Number execute(String sql, Object... args) {
        Connection conn = null;
        Number primaryKey = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement statement = getStatement(sql, conn, args);
            statement.executeUpdate();
            log.debug("SQL execute finnish!");
            ResultSet result = statement.getGeneratedKeys();
            if (result.next())
                primaryKey = result.getLong(1);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (conn != null)
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
        }
        return primaryKey;
    }

    private Number execute(String sql) {
        return execute(sql, new Object[]{});
    }

    /**
     * 将Entity Bean 转换为数据表行的结构Map
     *
     * @param entity
     * @return
     * @author 李恒名
     * @since 2016年3月16日
     */
    private Map<String, Object> entityChangeToRowMap(Object entity) {
        Map<String, Object> rowMap = new LinkedHashMap<>();
        Class<?> entityClass = entity.getClass();
        Field[] fields = entityClass.getDeclaredFields();
        for (Field field : fields) {
            try {
                PropertyDescriptor propertyDescriptor = new PropertyDescriptor(
                        field.getName(), entityClass);
                Object value = propertyDescriptor.getReadMethod().invoke(
                        entity, new Object[]{});
                if (value != null) {
                    String columnName;
                    Column annotation = field.getAnnotation(Column.class);
                    if (annotation != null && annotation.name() != null) {
                        columnName = annotation.name();
                    } else {
                        columnName = field.getName();
                    }
                    rowMap.put(columnName, value);
                }
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | IntrospectionException e) {
                e.printStackTrace();
            }
        }
        return rowMap;
    }

    /**
     * 构造SQL语句
     *
     * @param entity
     * @param type   SQL语句类型
     * @return
     * @author 李恒名
     * @since 2016年3月16日
     */
    private String buildSQL(Object entity, SQLType type) {
        // 构造SQL
        StringBuilder sql = new StringBuilder();
        String tableName = entity.getClass().getSimpleName().toLowerCase();
        // 表映射
        Map<String, Object> rowMap = entityChangeToRowMap(entity);
        // 表列名
        Set<String> columns = rowMap.keySet();
        switch (type) {
            case Insert:
                sql.append("insert into " + tableName + " (");
                for (String column : columns) {
                    sql.append(column + ",");
                }
                sql.deleteCharAt(sql.lastIndexOf(","));
                sql.append(") ");
                sql.append("values(");
                for (String col : columns) {
                    Object value = rowMap.get(col);
                    if (value instanceof String)
                        value = "'" + value + "'";
                    sql.append(value + ",");
                }
                sql.deleteCharAt(sql.lastIndexOf(","));
                sql.append(")");
                break;
            case Update:
                Object id = rowMap.remove("id");// 先把id提出取来作为一会的where条件
                if (id == null)
                    throw new RuntimeException("更新操作时,实体的主键(Id)不能为空！");

                sql.append("update " + tableName + " set ");
                for (String column : columns) {
                    sql.append(column + " = ");
                    Object value = rowMap.get(column);
                    if (value instanceof String)
                        value = "'" + value + "'";
                    sql.append(value + ",");
                }
                sql.deleteCharAt(sql.lastIndexOf(","));
                sql.append(" where id = " + id);
                break;
            case Delete:
                sql.append("delete from " + tableName + " where id = " + rowMap.get("id"));
                break;
            default:
                break;
        }
        return sql.toString();
    }

    /**
     * 将查询结果转换为Java Entity
     *
     * @param entityClass
     * @param result
     * @return
     * @author 李恒名
     * @since 2016年3月16日
     */
    private List<Object> parseResultSet(ResultSet result) {
        List<Object> list = new ArrayList<>();
        try {
            Field[] fields = entityClass.getDeclaredFields();
            while (result.next()) {
                Object bean = entityClass.newInstance();
                for (Field field : fields) {
                    String name = field.getName();
                    Object value = result.getObject(name);
                    BeanHelper.setProperty(bean, name, value);
                }
                list.add(bean);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    private PreparedStatement getStatement(String sql, Connection conn,
                                           Object... args) throws SQLException {
        log.debug("SQL：" + sql);
        PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        int i = 1;
        for (Object object : args) {
            statement.setObject(i, object);
            ++i;
        }
        return statement;
    }
}


