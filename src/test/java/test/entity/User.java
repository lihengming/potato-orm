package test.entity;

import cn.potato.orm.Model;

/**
 * 类说明 对应user表
 * @author 李恒名
 * @since 2016年3月16日
 */
public class User  extends Model<User>{//继承Modle成为一个Modle
	private int id;//主键名称约定为id,后期可以使用注解来标识
	private String username;
	private String password;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	@Override
	public String toString() {
		return "User [id=" + id + ", username=" + username + ", password="
				+ password + "]";
	}
	
}
