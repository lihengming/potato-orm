package cn.potato.jdbc;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import cn.potato.helper.ReflectHelper;

/**
 * 封装了一些数据库增删改查操作
 * @author 李恒名
 * @since 2016年3月16日
 */
public class DB {
	private DataSource dataSource;

	public DB() {
		this(new SimpleDataSource());
	}

	public DB(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public void save(Object entity) {
		String sql = buildSQL(entity, SQLType.Insert);
		execute(sql);
	}

	public void delete(Object entity) {
		String sql = buildSQL(entity, SQLType.Delete);
		execute(sql);
	}

	public void update(Object entity) {
		String sql = buildSQL(entity, SQLType.Update);
		execute(sql);
	}

	/**
	 * 查询接口
	 * @author 李恒名
	 * @since 2016年3月16日
	 * @param entityClass 实体类
	 * @param sql sql语句
	 * @param args 查询参数
	 * @return
	 */
	public List<Object> query(Class<?> entityClass, String sql, Object... args) {
		Connection conn = null;
		try {
			conn = dataSource.getConnection();
			PreparedStatement statement = conn.prepareStatement(sql);
			int i = 1;
			for (Object object : args) {
				statement.setObject(i, object);
				++i;
			}
			ResultSet result = statement.executeQuery();
			return parseResultSet(entityClass, result);
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
		return null;
	}

	private int execute(String sql) {
		return execute(sql, new Object[] {});
	}
	
	/**
	 * 增删改的SQL执行器
	 * @author 李恒名
	 * @since 2016年3月16日
	 * @param sql
	 * @param args
	 * @return
	 */
	private int execute(String sql, Object... args) {
		Connection conn = null;
		int count = -1;
		try {
			conn = dataSource.getConnection();
			PreparedStatement statement = conn.prepareStatement(sql);
			int i = 1;
			for (Object object : args) {
				statement.setObject(i, object);
				++i;
			}
			count = statement.executeUpdate();
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
		return count;
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
			} catch (IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | IntrospectionException e) {
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
		StringBuilder sb = new StringBuilder();
		String tableName = entity.getClass().getSimpleName().toLowerCase();
		// 表影射
		Map<String, Object> rowMap = entityChangeToRowMap(entity);
		// 数据库字段栏
		Set<String> columns = rowMap.keySet();
		switch (type) {
		case Insert:
			sb.append("insert into " + tableName + " (");
			for (String column : columns) {
				sb.append(column + ",");
			}
			sb.deleteCharAt(sb.lastIndexOf(","));
			sb.append(") ");
			sb.append("values(");
			for (String col : columns) {
				Object value = rowMap.get(col);
				if (value != null)
					value = "'" + value + "'";
				sb.append(value + ",");
			}
			sb.deleteCharAt(sb.lastIndexOf(","));
			sb.append(")");
			break;
		case Update:
			Object id = rowMap.remove("id");// 先把id提出取来作为一会的where条件
			if (id == null)
				throw new RuntimeException("更新操作时,实体的主键不能为空！");

			sb.append("update " + tableName + " set ");
			for (String column : columns) {
				sb.append(column + " = ");
			}
			for (String col : columns) {
				Object value = rowMap.get(col);
				if (value instanceof String)
					value = "'" + value + "'";
				sb.append(value + ",");
			}
			sb.deleteCharAt(sb.lastIndexOf(","));
			sb.append(" where id = " + id);
			break;
		case Delete:
			sb.append("delete " + tableName + "where id = " + rowMap.get("id"));
			break;
		default:
			break;
		}
		return sb.toString();
	}

	/**
	 * 将查询结果转换为Java Entity
	 * @author 李恒名
	 * @since 2016年3月16日
	 * @param entityClass
	 * @param result
	 * @return
	 */
	private List<Object> parseResultSet(Class<?> entityClass, ResultSet result) {
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
									ReflectHelper.changeStringToObject(
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

}
