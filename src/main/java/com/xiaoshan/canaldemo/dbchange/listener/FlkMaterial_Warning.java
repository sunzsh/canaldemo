package com.xiaoshan.canaldemo.dbchange.listener;

import com.alibaba.fastjson.JSONObject;
import com.xiaoshan.canaldemo.canal.DBChangeBaseListenerSync;
import com.xiaoshan.canaldemo.dbchange.FlkMaterialEvent;
import org.springframework.stereotype.Component;


@Component
public class FlkMaterial_Warning extends DBChangeBaseListenerSync<FlkMaterialEvent> {

	@Override
	public void onInsert(JSONObject data) {
		System.out.println("新增：" + data.toJSONString());
	}

	@Override
	public void onDelete(JSONObject data) {
		System.out.println("删除：" + data.toJSONString());
	}

	@Override
	public void onUpdate(JSONObject data, JSONObject old) {
		if (!old.containsKey("stocks")) {
			return;
		}
		Double stocksNew = data.getDouble("stocks");
		if (stocksNew < 10) {
			System.out.println("库存预警！！！！！：" + data.toJSONString());
		}
	}

}
