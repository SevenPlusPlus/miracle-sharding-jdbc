package io.shardingjdbc.spring.boot.sharding;

import lombok.Getter;
import lombok.Setter;
import io.shardingjdbc.core.yaml.sharding.YamlShardingRuleConfiguration;

/**
 * Sharding rule configuration properties.
 *
 * @author caohao
 */
@Getter
@Setter
public class SpringBootShardingRuleConfigurationProperties extends YamlShardingRuleConfiguration {
	private String name;
}
