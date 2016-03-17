package cn.potato.data;


/**
 * @author 李恒名
 * @since 2016年3月17日
 */
public interface ActiveRecord<T> {
	void save();
	void delete();
	void update();
	Query<T> createQuery();
}
