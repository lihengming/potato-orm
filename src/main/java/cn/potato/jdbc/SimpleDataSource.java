package cn.potato.jdbc;

import java.io.PrintWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import javax.sql.DataSource;

/**
 * 简单数据源实现
 * @author 李恒名
 * @since 2016年3月16日
 */
public class SimpleDataSource implements DataSource {
	
	private List<Connection> pool = Collections.synchronizedList(new LinkedList<Connection>());
	
	private Configuration config;
	
	public SimpleDataSource(){
		this(new DefaultConfigurationImpl());
	}
	public SimpleDataSource(Configuration config){
		this.config = config;
		initPool();
	}
	private void initPool() {
		try {
			Class.forName(config.getDriverClass());
			addConnection(config.getMinPoolSize());
		}catch (ClassNotFoundException | SQLException e) {
			throw new RuntimeException(e.getMessage());
		}
	}
	
	
	/**
	 * 向连接池添加链接
	 * @author 李恒名
	 * @since 2016年3月16日
	 * @param quantity 数量
	 */
	private void addConnection (int quantity) throws SQLException{
			while(quantity > 0){
				Connection connection = DriverManager.getConnection(config.getUrl(), config.getUser(), config.getPassword());
				pool.add(connection);
				--quantity;
			}
	}
	
	/**
	 * 检查连接池容量
	 * @author 李恒名
	 * @since 2016年3月16日
	 */
	private synchronized void  checkCapacity(){
		if(pool.size() < 1){//连接池没有连接时扩容
			try {
				addConnection(5);
			} catch (SQLException e) {
				throw new RuntimeException("扩容失败!",e);
			}
		}
	}
	@Override
	public PrintWriter getLogWriter() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setLogWriter(PrintWriter out) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setLoginTimeout(int seconds) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getLoginTimeout() throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public  Connection getConnection() throws SQLException {
		
		checkCapacity();
		
		final Connection connection = pool.remove(0);//取走连接池中第一个连接
		
		//使用JDK动态代理创建代理对象，修改Connection.close()方式使其调用时放入连接池而不是关闭。
		Connection proxy = (Connection) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), connection.getClass().getInterfaces(), new InvocationHandler() {
			@Override
			public Object invoke(Object proxy, Method method, Object[] args)
					throws Throwable {
				if("close".equals(method.getName())){//找到close()方法
					if(pool.size() < config.getMaxPoolSize()){//如果连接池连接数未达到上限放回连接池，否则调用close()关闭。
						pool.add(connection);
						System.out.println(connection+"返回连接池!");
					}else{
						connection.close();
						System.out.println(connection+"已被关闭!");
					}
					System.out.println("当前连接池连接数："+pool.size());
					return null;
				}
				return method.invoke(connection, args);
			}
		});
		
		return proxy;
	}

	@Override
	public Connection getConnection(String username, String password)
			throws SQLException {
		return DriverManager.getConnection(config.getUrl(), username, password);
	}
}
