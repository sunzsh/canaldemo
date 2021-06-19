package com.xiaoshan.canaldemo.canal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 注解onInsert、onUpdate、onDelete方法使用，仅当监听类继承了DBChangeBaseListenerAsync时有作用
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface DBChangeAsync {

	// 这个默认的线程池要记得配置
	String value() default "defaultCanalExecutor";
}
