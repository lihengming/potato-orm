package cn.potato.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
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
import java.util.Properties;

/**
 * 简单数据源实现
 * @author 李恒名
 * @since 2016年3月16日
 */
public class SimpleDataSource implements DataSource {
	private static final Logger log =  LoggerFactory.getLogger(SimpleDataSource.class);
	
	private List<Connection> pool = Collections.synchronizedList(new LinkedList<Connection>());
	
	private static String DEFAULT_PROPERTIES_FILE_NAME = "application.properties";//默认的框架配置文件
	
	private static int MIN_POOL_SIZE;//最小连接数
	private static int MAX_POOL_SIZE;//最大连接数
	
	private static String USER;//用户名
	private static String PASSWORD;//密码
	private static String URL;//数据库连接地址
	private static String DRIVER_CLASS;//数据库驱动类名称
	
	static{
		try {
			Properties properties = new Properties();
			properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(DEFAULT_PROPERTIES_FILE_NAME));
			DRIVER_CLASS = properties.getProperty("jdbc.driver", "com.mysql.jdbc.Driver");
			URL = properties.getProperty("jdbc.url", "");
			USER = properties.getProperty("jdbc.user", "");
			PASSWORD = properties.getProperty("jdbc.password", "");
			MIN_POOL_SIZE =Integer.valueOf(properties.getProperty("pool.minsize", "5"));
			MAX_POOL_SIZE = Integer.valueOf(properties.getProperty("pool.maxsize", "15"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}	
	
	public  SimpleDataSource(){
		initPool();
		log.debug("初始化DataSource："+this);
	}
	private void initPool() {
		try {
			Class.forName(DRIVER_CLASS);
			addConnection(MIN_POOL_SIZE);
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
				Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
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
	public  Connection getConnection() throws SQLException {
		
		checkCapacity();
		
		final Connection connection = pool.remove(0);//取走连接池中第一个连接
		
		//使用JDK动态代理创建代理对象，修改Connection.close()方式使其调用时放入连接池而不是关闭。
		Connection proxy = (Connection) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), connection.getClass().getInterfaces(), new InvocationHandler() {
			@Override
			public Object invoke(Object proxy, Method method, Object[] args)
					throws Throwable {
				if("close".equals(method.getName())){//找到close()方法
					if(pool.size() < MAX_POOL_SIZE){//如果连接池连接数未达到上限放回连接池，否则调用close()关闭。
						pool.add(connection);
						log.debug("DataSource："+connection+"返回连接池!");
					}else{
						connection.close();
						log.debug("DataSource："+connection+"已被关闭!");
					}
					return null;
				}
				return method.invoke(connection, args);
			}
		});
		log.debug("DataSource："+"获得Connection:"+proxy);
		return proxy;
	}

	@Override
	public Connection getConnection(String username, String password)
			throws SQLException {
		return DriverManager.getConnection(URL, username, password);
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
	public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
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

}
