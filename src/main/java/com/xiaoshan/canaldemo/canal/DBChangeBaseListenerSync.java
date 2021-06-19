package com.xiaoshan.canaldemo.canal;

import com.alibaba.fastjson.JSONObject;
import org.springframework.context.ApplicationEvent;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 同步版本的监听消费父类：
 * 如果消费时，对数据变动的顺序敏感（如果同一行数据多次变动，业务要求java接收到的顺序不能乱），则需要继承此类
 * @param <T>
 */
public class DBChangeBaseListenerSync<T extends ApplicationEvent> extends DBChangeAbstractListener<T> {

	ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());

	public void onApplicationEvent(T t) {
		JSONObject source = preEvent(t);
		if (source == null) {
			return;
		}
		process(source, executor);
	}

	public ThreadPoolExecutor getExecutor() {
		return executor;
	}
}
