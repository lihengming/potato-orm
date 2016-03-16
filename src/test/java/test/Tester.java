package test;

import java.lang.reflect.Method;
import java.util.List;

import cn.potato.orm.Query;
import test.entity.User;

/**
 * 类说明
 * @author 李恒名
 * @since 2016年3月16日
 */
public class Tester {

	public static void main(String[] args) {
		User user = new User();
		user.setUsername("测试用户名");
		user.setPassword("123456");
		//user.save();//插入数据
		
		Query<User> query = user.createQuery();
		
		List<User> list = query.list();//取所所用用户
		assert(list.size() > 5);
		User PUser = query.findBy("username", "测试用户名");//通过Where条件获得用户
		assert(PUser != null);
		
	}
}
