## 简介
Potato ORM 是用Java实现的一个简单的ORM框架，SQL方言目前(未来也)仅支持MySQL，仅供娱乐参考~
## 如何使用？
1.引入Maven 依赖
```xml
<!--Potato ORM-->
<dependency>
    <groupId>com.github.lihengming</groupId>
    <artifactId>potato-orm</artifactId>
    <version>1.1</version>
</dependency>
<!--JDBC-MySQL-->
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>5.1.38</version>
</dependency>
<!--如果你需要打印日志的话，引入任何一种SLF4J的实现，例如logback。-->
<dependency>
    <groupId>ch.qos.logback</groupId>
    <artifactId>logback-classic</artifactId>
    <version>1.1.7</version>
</dependency>
```
2.在classpath下添加配置文件application.properties
```xml
# JDBC配置，请以你的实际参数进行更改
jdbc.url=jdbc:mysql://localhost:3306/example?useUnicode=true&amp;characterEncoding=utf-8
jdbc.user=root
jdbc.password=123456

# JDBC连接池配置但并不是必须的
# pool.minsize=5
# pool.maxsize=15
```
3.创建Model 并继承Model<T>
```java
public class User extends Model<User> {
    private Long id;
    private String username;
    private Integer age;
    @Column(name = "nick_name")//如果你的表字段如Model的属性名称一致的话需要使用@Column注解
    private String nickName;
    
    //省略getter、setter
}
```
4.使用例子
```java
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
```

## 使用建议
本框架仅限于娱乐，因为它可能只是一个简简单单的玩具轮子，并且随时可能会爆胎，所以并不建议你使用它上路~。
