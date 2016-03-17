package test;

import java.util.List;

import test.entity.User;
import cn.potato.orm.Query;

/**
 * 类说明
 * @author 李恒名
 * @since 2016年3月16日
 */
public class Tester {

	public static void main(String[] args) {
		User user = new User();
		user.setUsername("测试用户名1");
		user.setPassword("123456");
		user.save();//插入数据
		
		assert(user.getId()!=null);
		
		user.setUsername("你妹啊");
		user.update();
		Query<User> query = user.createQuery();
		
		List<User> list = query.list();//取所所用用户
		assert(list.size() > 5);
		
		User PUser = query.findBy("username", "测试用户名");//通过Where条件获得用户
		assert(PUser != null);
		
	/*	System.out.println(Integer.class ==Integer.class);
		System.out.println(Integer.class.equals(Integer.TYPE));
		System.out.println(Integer.class.equals(int.class));
		System.out.println(int.class.equals(int.class));
		System.out.println(int.class == int.class);
		System.out.println(int.class == Integer.TYPE);*/
		
	}
}
