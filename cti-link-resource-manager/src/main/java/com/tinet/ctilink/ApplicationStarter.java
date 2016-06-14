package com.tinet.ctilink;

import com.tinet.ctilink.cache.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * 应用程序启动器
 * 
 * @author Jiangsl
 *
 */
@Component
public class ApplicationStarter implements ApplicationListener<ContextRefreshedEvent> {
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	RedisService redisService;

	@Override
	public void onApplicationEvent(final ContextRefreshedEvent event) {
		// 设置JVM的DNS缓存时间
		// http://docs.amazonaws.cn/AWSSdkDocsJava/latest/DeveloperGuide/java-dg-jvm-ttl.html
		java.security.Security.setProperty("networkaddress.cache.ttl", "60");


		logger.info("cti-link-resource-manager启动成功");
		System.out.println("cti-link-resource-manager启动成功");
	}
}