package io.shardingjdbc.spring.boot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

@ConfigurationProperties(prefix = MybatisProperties.MYBATIS_PREFIX)
public class MybatisProperties {

  public static final String MYBATIS_PREFIX = "miracle.mybatis";

  /**
   * Config file path.
   */
  private String configLocation;

  /**
   * Location of mybatis mapper files.
   */
  private String[] mapperLocations;

  /**
   * Package to scan domain objects.
   */
  private String typeAliasesPackage;

  /**
   * Package to scan handlers.
   */
  private String typeHandlersPackage;

  /**
   * Check the config file exists.
   */
  private boolean checkConfigLocation = false;

  /**
   * Execution mode for {@link org.mybatis.spring.SqlSessionTemplate}.
   */
  private ExecutorType executorType;

  /**
   * A Configuration object for customize default settings. If {@link #configLocation}
   * is specified, this property is not used.
   */
  private Configuration configuration;

  /**
   * @since 1.1.0
   */
  public String getConfigLocation() {
    return this.configLocation;
  }

  /**
   * @since 1.1.0
   */
  public void setConfigLocation(String configLocation) {
    this.configLocation = configLocation;
  }

  @Deprecated
  public String getConfig() {
    return this.configLocation;
  }

  @Deprecated
  public void setConfig(String config) {
    this.configLocation = config;
  }

  public String[] getMapperLocations() {
    return this.mapperLocations;
  }

  public void setMapperLocations(String[] mapperLocations) {
    this.mapperLocations = mapperLocations;
  }

  public String getTypeHandlersPackage() {
    return this.typeHandlersPackage;
  }

  public void setTypeHandlersPackage(String typeHandlersPackage) {
    this.typeHandlersPackage = typeHandlersPackage;
  }

  public String getTypeAliasesPackage() {
    return this.typeAliasesPackage;
  }

  public void setTypeAliasesPackage(String typeAliasesPackage) {
    this.typeAliasesPackage = typeAliasesPackage;
  }

  public boolean isCheckConfigLocation() {
    return this.checkConfigLocation;
  }

  public void setCheckConfigLocation(boolean checkConfigLocation) {
    this.checkConfigLocation = checkConfigLocation;
  }

  public ExecutorType getExecutorType() {
    return this.executorType;
  }

  public void setExecutorType(ExecutorType executorType) {
    this.executorType = executorType;
  }

  public Configuration getConfiguration() {
    return configuration;
  }

  public void setConfiguration(Configuration configuration) {
    this.configuration = configuration;
  }

  public Resource[] resolveMapperLocations() {
    List<Resource> resources = new ArrayList<Resource>();
    if (this.mapperLocations != null) {
      for (String mapperLocation : this.mapperLocations) {
        Resource[] mappers;
        try {
          mappers = new PathMatchingResourcePatternResolver().getResources(mapperLocation);
          resources.addAll(Arrays.asList(mappers));
        } catch (IOException e) {

        }
      }
    }

    Resource[] mapperLocations = new Resource[resources.size()];
    mapperLocations = resources.toArray(mapperLocations);
    return mapperLocations;
  }
}
