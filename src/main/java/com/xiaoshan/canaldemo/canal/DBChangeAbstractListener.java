package com.xiaoshan.canaldemo.canal;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executor;


public abstract class DBChangeAbstractListener<T extends ApplicationEvent> implements ApplicationListener<T> {

	@Autowired
	private Environment env;

	protected void process(JSONObject source, Executor executor) {

		String type = source.getString("type");
		if (type == null) {
			return;
		}

		JSONArray data = source.getJSONArray("data");

		if ("INSERT".equals(type)) {
			for (int i = 0; i < data.size(); i++) {
				JSONObject obj = (JSONObject)data.get(i);
				execIfExecutor(executor, ()-> onInsert(obj));
			}
		} else if ("DELETE".equals(type)) {
			for (int i = 0; i < data.size(); i++) {
				JSONObject obj = (JSONObject)data.get(i);
				execIfExecutor(executor, ()-> onDelete(obj));
			}

		} else if ("UPDATE".equals(type)) {
			JSONArray old = source.getJSONArray("old");
			for (int i = 0; i < data.size(); i++) {
				JSONObject obj = (JSONObject) data.get(i);
				JSONObject oldObj = (JSONObject) old.get(i);
				execIfExecutor(executor, ()-> onUpdate(obj, oldObj));
			}
		}

	}

	protected JSONObject preEvent(T t) {
		String listenerName = this.getClass().getName().replaceAll(".*\\.", "");
		String disableKey = "dbchange.disable." + listenerName;
		String property = env.getProperty(disableKey);
		if ("true".equals(property)) {
			System.err.println(listenerName + " 监听已禁用！");
			return null;
		}

		JSONObject source = Optional.ofNullable((JSONObject) t.getSource())
				.map(JSONObject::clone)
				.map(s -> (JSONObject)s)
				.orElse(null);

		if (source == null) {
			return null;
		}

		return source;

	}

	public void onInsert(JSONObject data) { }
	public void onDelete(JSONObject data) { }
	public void onUpdate(JSONObject data, JSONObject old) { }


	public boolean processIfDelFlagDefault(JSONObject data, JSONObject old) {
		return processIfDelFlag(data, old, "del_flag", "0");
	}

	/**
	 * 根据修改的内容，判断是否修改了del_flag
	 * @param data 新值
	 * @param old 旧值
	 * @param delFlagColumnName 是否删除标记字段名
	 * @param delFlagNormalValue 正常情况（未删除） 是否删除标记值
	 */
	public boolean processIfDelFlag(JSONObject data, JSONObject old, String delFlagColumnName, String delFlagNormalValue) {
		if (data == null || old == null || delFlagColumnName == null) {
			return false;
		}
		if (!old.containsKey(delFlagColumnName)) {
			return false;
		}
		if (delFlagNormalValue == null || delFlagNormalValue.trim().length() == 0) {
			delFlagNormalValue = "0";
		}

		if (Objects.equals(data.get(delFlagColumnName), delFlagNormalValue)) {
			onInsert(data);
		} else {
			onDelete(data);
		}
		return true;
	}


	private void execIfExecutor(Executor executor, Runnable runnable) {
		if (executor == null) {
			runnable.run();
		} else {
			executor.execute(runnable);
		}
	}
}
