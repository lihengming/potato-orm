package test;

import java.util.List;

import test.entity.User;
import cn.potato.data.Query;

/**
 * 类说明
 * @author 李恒名
 * @since 2016年3月16日
 */
public class Tester {

	public static void main(String[] args) {
		test2();
		
		
	}
	
	public static void test1(){
		User user = new User();
		user.setUsername("测试用户名1");
		user.setPassword("123456");
		user.save();//插入数据
		
		assert(user.getId()!=null);//插入数据库后自己注入自增主键
		
		user.setUsername("你妹啊");
		user.update();
		Query<User> query = user.createQuery();
		
		List<User> list = query.list();//取所所用用户
		assert(list.size() > 5);
		
		User PUser = query.findBy("username", "测试用户名");//通过Where条件获得用户
		assert(PUser != null);
		
	}
	
	public static void test2(){
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
	public static void test3(){
		
		User user = new User();
		user.setUsername("屌丝男士2");
		user.setPassword("2222");
		user.save();
		Integer id = user.getId();
		
		User user2 = new User();
		user2.setId(id);
		user.setPassword("1111");
		user.update();
		
		assert(user.createQuery().findBy("id", id).getUsername()!=null);//选择性的更新，不会将没有赋值的字段更新为空
	}
}
