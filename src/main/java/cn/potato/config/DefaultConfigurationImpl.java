package cn.potato.config;

import java.io.IOException;
import java.util.Properties;

import javax.sql.DataSource;

import cn.potato.data.SimpleDataSource;

/**
 * 默认的基于properties文件的数据源配置实现
 * @author 李恒名
 * @since 2016年3月16日
 */
public class DefaultConfigurationImpl implements Configuration {
	
	private static String DEFALT_PROPERTIES_FILE_NAME = "application.properties";
	private static String DEFALT_DRIVER_CLASS = "com.mysql.jdbc.Driver";
	private static int DEFALT_MIN_POOL_SIZE = 5;
	private static int DEFALT_MAX_POOL_SIZE = 15;
	private static Properties properties;
	
	static{
		try {
			properties = new Properties();
			properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(DEFALT_PROPERTIES_FILE_NAME));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getDriverClass() {
		String driverClass = getProperty("jdbc.driver");
		return driverClass != null ? driverClass : DEFALT_DRIVER_CLASS;
	}

	@Override
	public String getUrl() {
		return  getProperty("jdbc.url");
	}

	@Override
	public String getUser() {
		return  getProperty("jdbc.user");
	}

	@Override
	public String getPassword() {
		return  getProperty("jdbc.password");
	}

	@Override
	public Integer getMinPoolSize() {
		Integer value = getProperty("pool.minsize");
		return value != null?value:DEFALT_MIN_POOL_SIZE;
	}

	@Override
	public Integer getMaxPoolSize() {
		Integer value = getProperty("pool.maxsize");
		return value != null?value:DEFALT_MAX_POOL_SIZE;
	}
	
	@SuppressWarnings("unchecked")
	private <T> T getProperty(String key){
		if(properties!=null)
			return (T)properties.getProperty(key);
		return null;
	}

	@Override
	public DataSource getDataSource() {
		DataSource dataSource = null;
		String className = getProperty("dataSource");
		if(className!=null){
			try {
				@SuppressWarnings("unchecked")
				Class<DataSource> clazz = (Class<DataSource>) Class.forName(className);
				dataSource = clazz.newInstance();
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
				dataSource = new SimpleDataSource();
			}
		}else{
			dataSource = new SimpleDataSource();
		}
		return dataSource;
	}
}
