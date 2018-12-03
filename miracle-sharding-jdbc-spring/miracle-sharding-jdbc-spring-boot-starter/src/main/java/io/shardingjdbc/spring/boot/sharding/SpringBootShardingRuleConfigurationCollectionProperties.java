package io.shardingjdbc.spring.boot.sharding;

import java.util.List;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "miracle.sjdbc.config.sharding")
public class SpringBootShardingRuleConfigurationCollectionProperties {
	
	private List<SpringBootShardingRuleConfigurationProperties> datasource;
}

