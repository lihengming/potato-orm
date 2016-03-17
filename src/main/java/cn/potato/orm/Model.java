package cn.potato.orm;

import java.util.List;

import cn.potato.data.ActiveRecord;
import cn.potato.data.Dao;
import cn.potato.data.Query;

/**
 * 模型抽象类，POJO继承该类便成为一个Modle 拥有DAO的能力
 * @author 李恒名
 * @since 2016年3月16日
 */
public abstract class Model<T> extends Dao<T> implements ActiveRecord<T> {

	@Override
	public void save() {
		save(this);
	}
	@Override
	public void update() {
		update(this);
	}
	@Override
	public void delete() {
		delete(this);
	}
	@Override
	public  Query<T> createQuery(){
		return new QueryImpl();
	}
	
	
	
	private final String tableNmae = entityClass.getSimpleName().toLowerCase(); 
	@SuppressWarnings("unchecked")
	private  class QueryImpl implements Query<T>{
		@Override
		public List<T> page(int page, int size) {
			return null;
		}
		@Override
		public T findBy(String column, Object value) {
			String sql = "select * from "+ tableNmae +" where "+column+" = "+"?";
			return (T) query(sql, value).get(0);
		}
		@Override
		public List<T> list() {
			String sql = "select * from "+tableNmae;
			return (List<T>) query(sql, new Object[]{});
		}
	}
	
}

