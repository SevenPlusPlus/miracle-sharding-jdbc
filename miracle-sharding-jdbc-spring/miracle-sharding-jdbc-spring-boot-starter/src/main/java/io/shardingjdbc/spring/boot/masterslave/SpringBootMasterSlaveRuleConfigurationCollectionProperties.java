package io.shardingjdbc.spring.boot.masterslave;

import java.util.List;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "miracle.sjdbc.config.masterslave")
public class SpringBootMasterSlaveRuleConfigurationCollectionProperties{
	
	private List<SpringBootMasterSlaveRuleConfigurationProperties> datasource;
}
