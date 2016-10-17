package test;

import cn.potato.data.Query;
import org.junit.Assert;
import org.junit.Test;
import test.entity.User;

import java.util.List;

/**
 * 类说明
 * @author 李恒名
 * @since 2016年3月16日
 */
public class Tester {

	@Test
	public void test() {
		User user = new User();
		user.setUsername("potato");
		user.setAge(24);

		user.save();//持久化
		Assert.assertNotNull(user.getId());//持久化之后自动注入自增主键

		Query<User> query = user.createQuery();//获得Query对象

		List<User> userList = query.list();//查询所有用户
		Assert.assertTrue(userList.size() > 0);
		Assert.assertTrue("potato".equals(userList.get(0).getUsername()));

		user.setUsername("potato2");//重新设置username
		user.update();//更新
		User result  = query.findBy("username", "potato2");//根据条件查询
		Assert.assertTrue("potato2".equals(result.getUsername()));

		result.delete();//删除
		Assert.assertTrue(query.list().isEmpty());
	}
	
	public  void test2(){
		Query<User> query = new User().createQuery();
		long begin = System.currentTimeMillis();
		int i = 1 ;
		while(i <=1000){
			List<User> list = query.list();
			System.out.println("第"+i+"次："+list);
			i++;
		}
		long end = System.currentTimeMillis();
		System.out.println("执行1000次 select * from user 并映射为List<User> 操作耗时："+(end-begin)+"ms");
	}
}
