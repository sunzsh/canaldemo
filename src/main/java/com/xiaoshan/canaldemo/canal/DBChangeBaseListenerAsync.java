package com.xiaoshan.canaldemo.canal;

import com.alibaba.fastjson.JSONObject;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.scheduling.annotation.Async;

import java.util.concurrent.Executor;

/**
 * 异步版本的监听消费父类：
 * 如果消费时，对数据变动的顺序不敏感（如果同一行数据多次变动，业务场景不关心java接收到的顺序是否正确），则可以继承此类
 * @param <T>
 */
public class DBChangeBaseListenerAsync<T extends ApplicationEvent> extends DBChangeAbstractListener<T> implements ApplicationContextAware  {

	@Async
	public void onApplicationEvent(T t) {
		JSONObject source = preEvent(t);
		if (source == null) {
			return;
		}
		Executor executor = null;

		DBChangeAsync annotation = this.getClass().getAnnotation(DBChangeAsync.class);
		if (annotation != null) {
			String executorName = annotation.value();
			executor = (Executor)applicationContext.getBean(executorName);
		}

		process(source, executor);
	}

	ApplicationContext applicationContext;
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
}
