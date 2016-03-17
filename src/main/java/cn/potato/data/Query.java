package cn.potato.data;

import java.util.List;

/**
 * 类说明
 * @author 李恒名
 * @since 2016年3月16日
 */
public interface Query<T> {
	List<T> list();
	List<T> page(int page,int size);
	T findBy(String column,Object value);
}
