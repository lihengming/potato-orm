package cn.potato.data;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import cn.potato.config.Configuration;
import cn.potato.config.DefaultConfigurationImpl;
import cn.potato.helper.Converter;

/**
 * 数据库访问对象，提供增删改查的操作。
 * @author 李恒名
 * @since 2016年3月16日
 */
public  abstract class Dao<T> {
	protected Class<?> entityClass;
	private DataSource dataSource;
	private Configuration config = new DefaultConfigurationImpl();
	public Dao() {
		this.dataSource = config.getDataSource();
		ParameterizedType type = (ParameterizedType)this.getClass().getGenericSuperclass();
		entityClass= (Class<?>) type.getActualTypeArguments()[0];
	}

	//增接口
	protected void save(Object entity) {
		String sql = buildSQL(entity, SQLType.Insert);
		Number id = execute(sql);
			try {
				//通过反射将生成的主键注入回实体对象
				PropertyDescriptor propertyDescriptor = new PropertyDescriptor(
						"id", entity.getClass());
				Object arg = Converter.convertNumberType(id, propertyDescriptor.getPropertyType());
				propertyDescriptor.getWriteMethod().invoke(entity, arg);
			} catch (Exception e) {
				throw new RuntimeException("主键注入失败："+e.getMessage());
			}
	}
	//删接口
	protected void delete(Object entity) {
		String sql = buildSQL(entity, SQLType.Delete);
		execute(sql);
	}
	//改接口
	protected void update(Object entity) {
		String sql = buildSQL(entity, SQLType.Update);
		execute(sql);
	}
	//查接口
	protected List<Object> query(String sql, Object... args) {
		return executeQuery(sql,args);
	}

	
	
	
	
	
	
	
	
	
	
	
	
	/**
	 * 执行查询的SQL执行器
	 * @author 李恒名
	 * @since 2016年3月16日
	 * @param entityClass 实体类
	 * @param sql sql语句
	 * @param args 查询参数
	 * @return
	 */
	private  List<Object> executeQuery(String sql, Object... args) {
		System.out.println("---------------------------");
		System.out.println("SQL："+sql);
		Connection conn = null;
		List<Object> list = null;
		try {
			conn = dataSource.getConnection();
			PreparedStatement statement = getStatement(sql, conn, args);
			ResultSet result = statement.executeQuery();
			System.out.println("SQL execute finnish!");
			list =  parseResultSet(result);
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
	 * @author 李恒名
	 * @since 2016年3月16日
	 * @param sql
	 * @param args
	 * @return 主键
	 */
	private Number execute(String sql, Object... args) {
		System.out.println("---------------------------");
		System.out.println("SQL："+sql);
		Connection conn = null;
		Number primaryKey = null;
		try {
			conn = dataSource.getConnection();
			PreparedStatement statement = getStatement(sql, conn, args);
			statement.executeUpdate();
			System.out.println("SQL execute finnish!");
			ResultSet result = statement.getGeneratedKeys();
			if(result.next())
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
		return execute(sql, new Object[] {});
	}
	/**
	 * 将Entity Bean 转换为数据表行的结构Map
	 * @author 李恒名
	 * @since 2016年3月16日
	 * @param entity
	 * @return
	 */
	private Map<String, Object> entityChangeToRowMap(Object entity) {
		Map<String, Object> rowMap = new HashMap<>();
		Class<?> entityClass = entity.getClass();
		Field[] fields = entityClass.getDeclaredFields();
		for (Field field : fields) {
			try {
				PropertyDescriptor propertyDescriptor = new PropertyDescriptor(
						field.getName(), entityClass);
				Object value = propertyDescriptor.getReadMethod().invoke(
						entity, new Object[] {});
				rowMap.put(field.getName(), value);
			} catch (IllegalAccessException | IllegalArgumentException| InvocationTargetException | IntrospectionException e) {
				e.printStackTrace();
			}
		}
		return rowMap;
	}

	/**
	 * 构造SQL语句
	 * @author 李恒名
	 * @since 2016年3月16日
	 * @param entity
	 * @param type SQL语句类型
	 * @return
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
			sql.append("delete " + tableName + "where id = " + rowMap.get("id"));
			break;
		default:
			break;
		}
		return sql.toString();
	}

	/**
	 * 将查询结果转换为Java Entity
	 * @author 李恒名
	 * @since 2016年3月16日
	 * @param entityClass
	 * @param result
	 * @return
	 */
	private List<Object> parseResultSet(ResultSet result) {
		List<Object> list = new ArrayList<>();
		try {
			Object obj = entityClass.newInstance();
			while (result.next()) {
				Field[] fields = entityClass.getDeclaredFields();
				for (Field field : fields) {
					String name = field.getName();
					String value = result.getString(name);
					PropertyDescriptor propertyDescriptor = new PropertyDescriptor(
							name, entityClass);
					propertyDescriptor.getWriteMethod()
							.invoke(obj,
									Converter.convertStringToObject(
											propertyDescriptor
													.getPropertyType(), value));
					list.add(obj);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}
	private PreparedStatement getStatement(String sql, Connection conn,
			Object... args) throws SQLException {
		PreparedStatement statement = conn.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
		int i = 1;
		for (Object object : args) {
			statement.setObject(i, object);
			++i;
		}
		return statement;
	}
}


