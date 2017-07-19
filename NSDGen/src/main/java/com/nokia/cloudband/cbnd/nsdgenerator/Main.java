package com.nokia.cloudband.cbnd.nsdgenerator;

import java.util.Properties;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;

@SpringBootApplication
public class Main extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(Main.class).properties(getProperties());
	}

	public static void main(String[] args) {
		new SpringApplicationBuilder(Main.class).sources(Main.class).properties(getProperties()).run(args);
	}

	public static Properties getProperties() {
		Properties props = new Properties();
		props.put("spring.config.location", "classpath:/application.properties,file:/etc/huawei-sbi-config.properties,file:/tmp/huawei-sbi-config.properties");
		return props;
	}

}
