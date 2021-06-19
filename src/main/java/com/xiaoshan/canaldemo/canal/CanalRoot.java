package com.xiaoshan.canaldemo.canal;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

@Component
public class CanalRoot implements ApplicationContextAware {

	// 不同的项目需要修改包名，指定dbchange所在包完整路径
	private static final String PACKAGE_OF_DBCHANGE = "com.xiaoshan.canaldemo.dbchange.";

	@KafkaListener(autoStartup = "true", id = "canaldemo", topics = "canaldemo")
	public void listen(ConsumerRecord<String, String> record, Acknowledgment ack) {
		ack.acknowledge();

		String changes = Optional.ofNullable(record.value()).orElse(null);
//		System.out.println(changes);

		JSONObject changesJson = JSON.parseObject(changes);
		Boolean isDdl = changesJson.getBoolean("isDdl");
		if (isDdl) {
			return;
		}
		String tableName = changesJson.getString("table");

		if (tableName == null || tableName.trim().length() == 0) {
			return;
		}
		String springEventClassName = String.format("%sEvent", Optional.of(tableName).map(CanalRoot::toCamelCase).map(CanalRoot::upperFirst).get());

		Class<?> springEvent;
		try {
			springEvent = Class.forName(PACKAGE_OF_DBCHANGE + springEventClassName);
		} catch (ClassNotFoundException e) {
			return;
		}

		if (springEvent == null) {
			return;
		}

		Object eventObject  = null;
		try {
			Constructor<?> constructor = springEvent.getConstructor(Object.class);
			eventObject = constructor.newInstance(changesJson);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}

		if (eventObject != null) {
			try {
				applicationContext.publishEvent(eventObject);
			} catch (Throwable e) {
				e.printStackTrace();
			}

		}
	}

	private ApplicationContext applicationContext;
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	/**
	 * 下划线转驼峰
	 * @param underlineStr 带有下划线的字符串
	 * @return 驼峰字符串
	 */
	private static String toCamelCase(String underlineStr) {
		if (underlineStr == null) {
			return null;
		}
		// 分成数组
		char[] charArray = underlineStr.toCharArray();
		// 判断上次循环的字符是否是"_"
		boolean underlineBefore = false;
		StringBuffer buffer = new StringBuffer();
		for (int i = 0, l = charArray.length; i < l; i++) {
			// 判断当前字符是否是"_",如果跳出本次循环
			if (charArray[i] == 95) {
				underlineBefore = true;
			} else if (underlineBefore) {
				// 如果为true，代表上次的字符是"_",当前字符需要转成大写
				buffer.append(charArray[i] -= 32);
				underlineBefore = false;
			} else {
				// 不是"_"后的字符就直接追加
				buffer.append(charArray[i]);
			}
		}
		return buffer.toString();
	}

	/**
	 * 首字母转大写
	 * @param word 字符串
	 * @return 首字母大写的字符串
	 */
	private static String upperFirst(String word) {
		if (word == null) {
			return null;
		}
		if (word.length() == 0) {
			return word;
		}

		return word.substring(0, 1).toUpperCase() + word.substring(1);
	}
}
