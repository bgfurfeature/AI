package com.bgfurfeature.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用户自定义注解
 */
@Target({ ElementType.TYPE }) // 注解用在接口上
@Retention(RetentionPolicy.RUNTIME) // VM将在运行期也保留注释，因此可以通过反射机制读取注解的信息
@interface RpcService {

	String value();
}

// 注解的使用：获取自定义注解上的value，反射被注解类，并调用指定方法
