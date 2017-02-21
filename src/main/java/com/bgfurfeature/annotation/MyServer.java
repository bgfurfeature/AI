package com.bgfurfeature.annotation;

import java.lang.reflect.Method;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

import com.bgfurfeature.annotation.RpcService;
import com.bgfurfeature.annotation.HelloService;

@Component        //ApplicationContextAware会为Component组件调用setApplicationContext方法；
public class MyServer implements ApplicationContextAware {
	@SuppressWarnings("resource")
	public static void main(String[] args) {
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("spring2.xml");
	}


	public void setApplicationContext(ApplicationContext ctx)
			throws BeansException {
		Map<String, Object> serviceBeanMap = ctx
				.getBeansWithAnnotation(RpcService.class);
		for (Object serviceBean : serviceBeanMap.values()) {
			try {
				//获取自定义注解上的value
				String value = serviceBean.getClass().getAnnotation(RpcService.class).value();
				System.out.println("注解上的value: " + value);
				
				//反射被注解类，并调用指定方法
				Method method = serviceBean.getClass().getMethod("hello",
						new Class[] { String.class });
				Object invoke = method.invoke(serviceBean, "bbb");
				System.out.println(invoke);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
