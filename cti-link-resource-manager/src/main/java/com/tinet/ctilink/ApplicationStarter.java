package com.tinet.ctilink;

import com.tinet.ctilink.cache.RedisService;
import com.tinet.ctilink.resourcemanager.cache.ReloadCacheJob;
import com.tinet.ctilink.resourcemanager.cache.ResourceManagerCacheInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.List;

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

	@Autowired
	List<ResourceManagerCacheInterface> resourceManagerCacheInterfaceList;

	@Override
	public void onApplicationEvent(final ContextRefreshedEvent event) {
		// 设置JVM的DNS缓存时间
		// http://docs.amazonaws.cn/AWSSdkDocsJava/latest/DeveloperGuide/java-dg-jvm-ttl.html
		java.security.Security.setProperty("networkaddress.cache.ttl", "60");

		//配置了加载所有缓存  -DloadCache=true
		String loadCache = System.getProperty("loadCache");
		//if (loadCache != null && "true".equals(loadCache)) {
		ReloadCacheJob reloadCacheJob = new ReloadCacheJob();
		reloadCacheJob.setResourceManagerCacheInterfaceList(resourceManagerCacheInterfaceList);
		Thread reloadThread = new Thread(reloadCacheJob);
		reloadThread.start();
		//}


		logger.info("cti-link-resource-manager启动成功");
		System.out.println("cti-clink-resource-manager启动成功");
	}
}