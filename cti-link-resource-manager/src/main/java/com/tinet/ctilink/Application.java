package com.tinet.ctilink;

import com.alibaba.dubbo.container.Main;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author fengwei //
 * @date 16/4/6 17:09
 */
public class Application {
    private static Logger logger = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        // 记录未捕获的异常
        Thread.currentThread().setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

            public void uncaughtException(Thread t, Throwable e) {
                logger.error(t.toString(), e);
            }
        });

        // 加载Spring容器
        ApplicationContext ctx = new ClassPathXmlApplicationContext("classpath*:spring/*.xml");

        // 使主线程等待以持续提供服务
        synchronized (Main.class) {
            while (true) {
                try {
                    Main.class.wait();
                } catch (Throwable e) {
                }
            }
        }
    }

}

