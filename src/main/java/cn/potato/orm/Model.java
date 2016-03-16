package cn.potato.orm;

import java.lang.reflect.ParameterizedType;
import java.util.List;

import cn.potato.jdbc.DB;

/**
 * 模型抽象类，POJO继承该类便成为一个Modle 拥有增删改查的能力
 * @author 李恒名
 * @since 2016年3月16日
 */
public abstract class Model<Entity> {
	
	private Class<?> entityClass;
	private DB db = new DB();
	
	public Model(){
		ParameterizedType type = (ParameterizedType)this.getClass().getGenericSuperclass();
		entityClass= (Class<?>) type.getActualTypeArguments()[0];
	}
	
	public void save() {
		db.save(this);
	}
	public void update() {
		db.update(this);
	}
	public void delete() {
		db.delete(this);
	}
	public  Query<Entity> createQuery(){
		return new QueryImpl();
	}
	@SuppressWarnings("unchecked")
	private  class QueryImpl implements Query<Entity>{

		@Override
		public List<Entity> page(int page, int size) {
			return null;
		}

		@Override
		public Entity findBy(String column, Object value) {
			String sql = "select * from user where "+column+" = "+"?";
			return (Entity) db.query(entityClass, sql, value).get(0);
		}
	
		@Override
		public List<Entity> list() {
			String sql = "select * from user";
			return (List<Entity>) db.query(entityClass, sql, new Object[]{});
		}
	}
	
}

