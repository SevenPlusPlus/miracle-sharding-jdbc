package io.shardingjdbc.spring.boot;

import io.shardingjdbc.core.api.MasterSlaveDataSourceFactory;
import io.shardingjdbc.core.api.ShardingDataSourceFactory;
import io.shardingjdbc.core.constant.ShardingPropertiesConstant;
import io.shardingjdbc.core.exception.ShardingJdbcException;
import io.shardingjdbc.core.jdbc.core.datasource.MasterSlaveDataSource;
import io.shardingjdbc.core.jdbc.core.datasource.ShardingDataSource;
import io.shardingjdbc.core.util.DataSourceUtil;
import io.shardingjdbc.spring.boot.masterslave.SpringBootMasterSlaveRuleConfigurationCollectionProperties;
import io.shardingjdbc.spring.boot.masterslave.SpringBootMasterSlaveRuleConfigurationProperties;

import io.shardingjdbc.spring.boot.sharding.SpringBootShardingRuleConfigurationCollectionProperties;
import io.shardingjdbc.spring.boot.sharding.SpringBootShardingRuleConfigurationProperties;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import lombok.extern.slf4j.Slf4j;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.mapper.ClassPathMapperScanner;
import org.mybatis.spring.mapper.MapperFactoryBean;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.alibaba.druid.pool.DruidDataSource;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;

/**
 * Spring boot sharding and master-slave configuration.
 *
 * @author caohao
 */
@Slf4j
@ConditionalOnClass({ SqlSessionFactory.class, SqlSessionFactoryBean.class, 
	MasterSlaveDataSource.class,  ShardingDataSource.class })
@ConditionalOnMissingBean(name={"getDataSourceMap"})
@Configuration
@EnableConfigurationProperties({SpringBootShardingRuleConfigurationCollectionProperties.class, 
	SpringBootMasterSlaveRuleConfigurationCollectionProperties.class,
	MybatisProperties.class})
public class SpringBootConfiguration implements EnvironmentAware, ApplicationContextAware {
    
    @Autowired(required = false)
    private SpringBootShardingRuleConfigurationCollectionProperties shardingRuleConfigs;
    
    @Autowired(required = false)
    private SpringBootMasterSlaveRuleConfigurationCollectionProperties masterSlaveRuleConfigs;

    
    @Autowired
	private MybatisProperties properties;
    
    @Autowired
	private ResourceLoader resourceLoader = new DefaultResourceLoader();
    
    @Autowired(required = false)
	private DatabaseIdProvider databaseIdProvider;
    
	@Autowired(required = false)
	private Interceptor[] interceptors;
    
    private final Map<String, DataSource> simpleDataSourceMap = new HashMap<>();
    
    private List<String> mybatisSourceNames;
    
    private final Properties props = new Properties();
    
    
    public class SJdbcDataSourceMap extends HashMap<String, DataSource>
	{
		private static final long serialVersionUID = -4248342929373325592L;

		public void initDruidDataSources() throws SQLException
		{
			if(this.size() > 0)
			{
				for(DataSource ds : this.values())
				{
					if(ds instanceof DruidDataSource)
					{
						((DruidDataSource) ds).init();
					}
				}
			}
		}
		
		public void destroyDruidDataSources()
		{
			if(this.size() > 0)
			{
				for(DataSource ds : this.values())
				{
					if(ds instanceof DruidDataSource)
					{
						((DruidDataSource) ds).close();
					}
				}
			}
		}
	}
	
	@Bean(initMethod = "initDruidDataSources", destroyMethod = "destroyDruidDataSources") 
	public SJdbcDataSourceMap dataSourceMap() throws SQLException
	{
		SJdbcDataSourceMap dsMap = new SJdbcDataSourceMap();
		
		Map<String, DataSource> allDataSource = new HashMap<>();
		if(simpleDataSourceMap != null && simpleDataSourceMap.size() > 0)
		{
			allDataSource.putAll(simpleDataSourceMap);
		}
		
		List<SpringBootMasterSlaveRuleConfigurationProperties> masterSlaves = 
				masterSlaveRuleConfigs.getDatasource();
		if(masterSlaves != null && masterSlaves.size() > 0)
		{
			for(SpringBootMasterSlaveRuleConfigurationProperties masterSlaveProps : masterSlaves)
			{
				DataSource msDs = MasterSlaveDataSourceFactory.createDataSource(allDataSource,
						masterSlaveProps.getMasterSlaveRuleConfiguration(), null);
				allDataSource.put(masterSlaveProps.getName(), msDs);
			}
		}
		
		List<SpringBootShardingRuleConfigurationProperties> shardings = 
				shardingRuleConfigs.getDatasource();
		if(shardings != null && shardings.size() > 0)
		{
			for(SpringBootShardingRuleConfigurationProperties shardingProps : shardings)
			{
				DataSource shardingDs = ShardingDataSourceFactory.createDataSource(allDataSource, 
						shardingProps.getShardingRuleConfiguration(), null, props);
				allDataSource.put(shardingProps.getName(), shardingDs);
			}
		}
		
		dsMap.putAll(allDataSource);
		return dsMap;
	}
	
    
    @Override
    public void setEnvironment(final Environment environment) {
    	setMybatisSourceNames(environment);
        setDataSourceMap(environment);
        setShardingProperties(environment);
    }
    
    private void setMybatisSourceNames(final Environment environment)
    {
    	RelaxedPropertyResolver propertyResolver = new RelaxedPropertyResolver(environment, "melot.sjdbc.mybatis.source.");
        String sourceNames = propertyResolver.getProperty("names");
        mybatisSourceNames = Splitter.on(",").trimResults().splitToList(sourceNames);
    }
    
    private void setDataSourceMap(final Environment environment) {
        RelaxedPropertyResolver propertyResolver = new RelaxedPropertyResolver(environment, "melot.sjdbc.datasource.");
        String dataSources = propertyResolver.getProperty("names");
        for (String each : dataSources.split(",")) {
            try {
                Map<String, Object> dataSourceProps = propertyResolver.getSubProperties(each + ".");
                Preconditions.checkState(!dataSourceProps.isEmpty(), "Wrong datasource properties!");
                DataSource dataSource = DataSourceUtil.getDataSource(dataSourceProps.get("type").toString(), dataSourceProps);
                simpleDataSourceMap.put(each, dataSource);
            } catch (final ReflectiveOperationException ex) {
                throw new ShardingJdbcException("Can't find datasource type!", ex);
            }
        }
    }
    
    private void setShardingProperties(final Environment environment) {
        RelaxedPropertyResolver propertyResolver = new RelaxedPropertyResolver(environment, "melot.sjdbc.config.sharding.props.");
        String showSQL = propertyResolver.getProperty(ShardingPropertiesConstant.SQL_SHOW.getKey());
        if (!Strings.isNullOrEmpty(showSQL)) {
            props.setProperty(ShardingPropertiesConstant.SQL_SHOW.getKey(), showSQL);
        }
        String executorSize = propertyResolver.getProperty(ShardingPropertiesConstant.EXECUTOR_SIZE.getKey());
        if (!Strings.isNullOrEmpty(executorSize)) {
            props.setProperty(ShardingPropertiesConstant.EXECUTOR_SIZE.getKey(), executorSize);
        }
    }
    
    @Bean
	public BeanPostProcessor mybatisBeanPostProcessor(SJdbcDataSourceMap dataSourceMap) {
		checkConfigFileExists();
		registerMybatis(dataSourceMap);
		return new BeanPostProcessor() {
			@Override
			public Object postProcessAfterInitialization(Object arg0,
					String arg1) throws BeansException {
				return arg0;
			}

			@Override
			public Object postProcessBeforeInitialization(Object arg0,
					String arg1) throws BeansException {
				return arg0;
			}
			
		};
	}

	private void registerMybatis(SJdbcDataSourceMap dataSourceMap) {
		
		List<String> mybatisTargetSources = new ArrayList<>();
		if(mybatisSourceNames != null)
		{
			mybatisTargetSources.addAll(mybatisSourceNames);
		}
		else
		{
			mybatisTargetSources.addAll(dataSourceMap.keySet());
		}
		
		for (String sourceName : mybatisTargetSources) {
			DataSource ds = dataSourceMap.get(sourceName);
			if(ds != null)
			{
				try {
					SqlSessionFactory sqlFactory = getSqlSessionFactory(ds);
					SqlSessionTemplate sqlTemplate = getSqlSessionTemplate(sqlFactory);
					registerBean(sqlFactory, "sqlSessionFactory_" + sourceName);
					registerBean(sqlTemplate, "sqlSessionTemplate_" + sourceName);
				} catch (Exception e) {
					log.warn(
							"error datasource config when create bean sqlSessionTemplate or sqlSessionFactory : "
									+ ds.toString(), e);
				}
			}
		}
	}

	private void registerBean(Object bean, String sourceName) {
		defaultListableBeanFactory.registerSingleton(sourceName, bean);
	}

	private void checkConfigFileExists() {
		if (this.properties.isCheckConfigLocation()
				&& StringUtils.hasText(this.properties.getConfigLocation())) 
		{
			Resource resource = this.resourceLoader.getResource(this.properties.getConfigLocation());
			Assert.state(resource.exists(), "Cannot find config location: "
					+ resource
					+ " (please add config file or check your Mybatis "
					+ "configuration)");
		}
	}

	public SqlSessionFactory getSqlSessionFactory(DataSource dataSource)
			throws Exception {
		SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
		factory.setDataSource(dataSource);
		factory.setVfs(SpringBootVFS.class);
		if (StringUtils.hasText(this.properties.getConfigLocation())) {
			factory.setConfigLocation(this.resourceLoader
					.getResource(this.properties.getConfigLocation()));
		}
		factory.setConfiguration(properties.getConfiguration());
		if (!ObjectUtils.isEmpty(this.interceptors)) {
			factory.setPlugins(this.interceptors);
		}
		if (this.databaseIdProvider != null) {
			factory.setDatabaseIdProvider(this.databaseIdProvider);
		}
		if (StringUtils.hasLength(this.properties.getTypeAliasesPackage())) {
			factory.setTypeAliasesPackage(this.properties
					.getTypeAliasesPackage());
		}
		if (StringUtils.hasLength(this.properties.getTypeHandlersPackage())) {
			factory.setTypeHandlersPackage(this.properties
					.getTypeHandlersPackage());
		}
		if (!ObjectUtils.isEmpty(this.properties.resolveMapperLocations())) {
			factory.setMapperLocations(this.properties.resolveMapperLocations());
		}

		return factory.getObject();
	}

	public SqlSessionTemplate getSqlSessionTemplate(
			SqlSessionFactory sqlSessionFactory) {
		ExecutorType executorType = this.properties.getExecutorType();
		if (executorType != null) {
			return new SqlSessionTemplate(sqlSessionFactory, executorType);
		} else {
			return new SqlSessionTemplate(sqlSessionFactory);
		}
	}

	/**
	 * This will just scan the same base package as Spring Boot does. If you
	 * want more power, you can explicitly use
	 * {@link org.mybatis.spring.annotation.MapperScan} but this will get typed
	 * mappers working correctly, out-of-the-box, similar to using Spring Data
	 * JPA repositories.
	 */
	public static class AutoConfiguredMapperScannerRegistrar implements
			BeanFactoryAware, ImportBeanDefinitionRegistrar,
			ResourceLoaderAware {

		private BeanFactory beanFactory;

		private ResourceLoader resourceLoader;

		@Override
		public void registerBeanDefinitions(
				AnnotationMetadata importingClassMetadata,
				BeanDefinitionRegistry registry) {

			log.debug("Searching for mappers annotated with @Mapper'");

			ClassPathMapperScanner scanner = new ClassPathMapperScanner(
					registry);

			try {
				if (this.resourceLoader != null) {
					scanner.setResourceLoader(this.resourceLoader);
				}

				List<String> pkgs = AutoConfigurationPackages
						.get(this.beanFactory);
				for (String pkg : pkgs) {
					log.debug("Using auto-configuration base package '" + pkg
							+ "'");
				}

				scanner.setAnnotationClass(Mapper.class);
				scanner.registerFilters();
				scanner.doScan(StringUtils.toStringArray(pkgs));
			} catch (IllegalStateException ex) {
				log.debug("Could not determine auto-configuration "
						+ "package, automatic mapper scanning disabled.");
			}
		}

		@Override
		public void setBeanFactory(BeanFactory beanFactory)
				throws BeansException {
			this.beanFactory = beanFactory;
		}

		@Override
		public void setResourceLoader(ResourceLoader resourceLoader) {
			this.resourceLoader = resourceLoader;
		}
	}

	/**
	 * {@link org.mybatis.spring.annotation.MapperScan} ultimately ends up
	 * creating instances of {@link MapperFactoryBean}. If
	 * {@link org.mybatis.spring.annotation.MapperScan} is used then this
	 * auto-configuration is not needed. If it is _not_ used, however, then this
	 * will bring in a bean registrar and automatically register components
	 * based on the same component-scanning path as Spring Boot itself.
	 */
	@Configuration
	@Import({ AutoConfiguredMapperScannerRegistrar.class })
	@ConditionalOnMissingBean(MapperFactoryBean.class)
	public static class MapperScannerRegistrarNotFoundConfiguration {

		@PostConstruct
		public void afterPropertiesSet() {
			log.debug(String.format("No %s found.",
					MapperFactoryBean.class.getName()));
		}
	}

	private ApplicationContext applicationContext;
	private DefaultListableBeanFactory defaultListableBeanFactory;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
		this.defaultListableBeanFactory = (DefaultListableBeanFactory) this.applicationContext
				.getAutowireCapableBeanFactory();
	}

}
