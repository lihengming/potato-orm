package test.entity;

import cn.potato.orm.Model;

import javax.persistence.Column;

/**
 * 类说明 对应user表
 * @author 李恒名
 * @since 2016年3月16日
 */
public class User  extends Model<User>{//继承Model成为一个Model
	private Long id;//主键名称约定为id,后期可以使用注解来标识
	@Column(name = "username")
	private String username;
	private Integer age;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Integer getAge() {
		return age;
	}

	public void setAge(Integer age) {
		this.age = age;
	}
	
}
