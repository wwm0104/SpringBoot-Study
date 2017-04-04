package com.lc.springBoot.druid.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.github.pagehelper.PageHelper;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * cluster节点数据源配置
 *
 * @author lsj <lishuijun1992@gmail.com>
 * @date 17-4-4
 */
@Configuration
@MapperScan(basePackages = {"com.lc.springBoot.druid.mapper.cluster"},
        sqlSessionFactoryRef = "clusterSqlSessionFactory")
public class ClusterDruidDataSourceConfig {

    @Value("${spring.datasource.cluster.clusterMapperLocations}")
    private String clusterMapperLocations;

    @ConfigurationProperties(prefix = "spring.datasource.cluster")
    @Bean(name = "clusterDataSource")
    @Primary
    public DataSource clusterDataSource() {
        return new DruidDataSource();
    }

    /**
     * SqlSessionFactory配置
     *
     * @return
     * @throws Exception
     */
    @Bean(name = "clusterSqlSessionFactory")
    @Primary
    public SqlSessionFactory clusterSqlSessionFactory() throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(clusterDataSource());

        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        //配置mapper文件位置
        sqlSessionFactoryBean.setMapperLocations(resolver.getResources(clusterMapperLocations));

        //配置分页插件
        PageHelper pageHelper = new PageHelper();
        Properties properties = new Properties();
        properties.setProperty("reasonable", "true");
        properties.setProperty("supportMethodsArguments", "true");
        properties.setProperty("returnPageInfo", "check");
        properties.setProperty("params", "count=countSql");
        pageHelper.setProperties(properties);

        //设置插件
        sqlSessionFactoryBean.setPlugins(new Interceptor[]{pageHelper});
        return sqlSessionFactoryBean.getObject();
    }

    /**
     * 配置事物管理器
     *
     * @return
     */
    @Bean(name = "clusterTransactionManager")
    @Primary
    public DataSourceTransactionManager clusterTransactionManager() {
        DataSourceTransactionManager dataSourceTransactionManager = new DataSourceTransactionManager();
        dataSourceTransactionManager.setDataSource(clusterDataSource());
        return dataSourceTransactionManager;
    }
}
