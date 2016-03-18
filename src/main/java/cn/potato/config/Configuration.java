package cn.potato.config;

import javax.sql.DataSource;

/**
 * 框架配置接口
 * @author 李恒名
 * @since 2016年3月16日
 */
public interface Configuration {
	public DataSource getDataSource();
}
