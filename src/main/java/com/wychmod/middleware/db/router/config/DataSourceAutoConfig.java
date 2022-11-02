package com.wychmod.middleware.db.router.config;

import com.wychmod.middleware.db.router.DBRouterConfig;
import com.wychmod.middleware.db.router.DBRouterJoinPoint;
import com.wychmod.middleware.db.router.dynamic.DynamicDataSource;
import com.wychmod.middleware.db.router.dynamic.DynamicMybatisPlugin;
import com.wychmod.middleware.db.router.strategy.IDBRouterStrategy;
import com.wychmod.middleware.db.router.strategy.impl.DBRouterStrategyHashCode;
import com.wychmod.middleware.db.router.util.PropertyUtil;
import org.apache.ibatis.plugin.Interceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * @description: 数据源配置解析
 * @author: wychmod
 * @date: 2022/11/2
 */
@Configuration
public class DataSourceAutoConfig implements EnvironmentAware {

    /**
     * 数据源配置组
     */
    private Map<String, Map<String, Object>> dataSourceMap = new HashMap<>();

    /**
     * 默认数据源配置
     */
    private Map<String, Object> defaultDataSourceConfig;

    /**
     * 分库数量
     */
    private int dbCount;

    /**
     * 分表数量
     */
    private int tbCount;

    /**
     * 路由字段
     */
    private String routerKey;

    /**
     * 将切面注册为bean
     * @param dbRouterConfig
     * @param dbRouterStrategy
     */
    @Bean(name = "db-router-point")
    @ConditionalOnMissingBean
    public DBRouterJoinPoint point(DBRouterConfig dbRouterConfig, IDBRouterStrategy dbRouterStrategy) {
        return new DBRouterJoinPoint(dbRouterConfig, dbRouterStrategy);
    }

    /**
     * dbRouterConfig()，主要为了把分库分表配置生成一个 Bean 对象，方便在切面类中进行注入使用。
     * @return 分库分表配置
     */
    @Bean
    public DBRouterConfig dbRouterConfig() {
        return new DBRouterConfig(dbCount, tbCount, routerKey);
    }

    @Bean
    public Interceptor plugin() {
        return new DynamicMybatisPlugin();
    }

    @Bean
    public IDBRouterStrategy dbRouterStrategy(DBRouterConfig dbRouterConfig) {
        return new DBRouterStrategyHashCode(dbRouterConfig);
    }

    /**
     * 动态配置数据源
     * @return 数据源
     */
    @Bean
    public DataSource dataSource() {
        // 创建数据源
        Map<Object, Object> targetDataSources = new HashMap<>();
        for (String dbInfo : dataSourceMap.keySet()) {
            Map<String, Object> objMap = dataSourceMap.get(dbInfo);
            targetDataSources.put(dbInfo, new DriverManagerDataSource(objMap.get("url").toString(), objMap.get("username").toString(), objMap.get("password").toString()));
        }

        // 设置数据源
        DynamicDataSource dynamicDataSource = new DynamicDataSource();
        dynamicDataSource.setTargetDataSources(targetDataSources);
        dynamicDataSource.setDefaultTargetDataSource(new DriverManagerDataSource(defaultDataSourceConfig.get("url").toString(), defaultDataSourceConfig.get("username").toString(), defaultDataSourceConfig.get("password").toString()));

        return dynamicDataSource;
    }

    @Bean
    public TransactionTemplate transactionTemplate(DataSource dataSource) {
        DataSourceTransactionManager dataSourceTransactionManager = new DataSourceTransactionManager();
        dataSourceTransactionManager.setDataSource(dataSource);

        TransactionTemplate transactionTemplate = new TransactionTemplate();
        transactionTemplate.setTransactionManager(dataSourceTransactionManager);
        transactionTemplate.setPropagationBehaviorName("PROPAGATION_REQUIRED");
        return transactionTemplate;
    }

    /**
     * 读取自定义配置，因为我们要设置的是一个在 yml 配置多组数据源
     * @param environment 环境配置
     */
    @Override
    public void setEnvironment(Environment environment) {
        String prefix = "mini-db-router.jdbc.datasource.";

        dbCount = Integer.parseInt(environment.getProperty(prefix + "dbCount"));
        tbCount = Integer.parseInt(environment.getProperty(prefix + "tbCount"));

        // 分库分表的数据源
        String dataSources = environment.getProperty(prefix + "list");
        assert dataSources != null;
        for (String dbInfo : dataSources.split(",")) {
            Map<String, Object> dataSourceProps = PropertyUtil.handle(environment, prefix + dbInfo, Map.class);
            dataSourceMap.put(dbInfo, dataSourceProps);
        }

        // 默认数据源
        String defaultData = environment.getProperty(prefix + "default");
        defaultDataSourceConfig = PropertyUtil.handle(environment, prefix + defaultData, Map.class);
    }
}
