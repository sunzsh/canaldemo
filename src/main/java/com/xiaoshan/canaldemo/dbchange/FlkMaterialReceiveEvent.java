package com.xiaoshan.canaldemo.dbchange;

import org.springframework.context.ApplicationEvent;
/**
 * 材料领用记录事件
 */
public class FlkMaterialReceiveEvent extends ApplicationEvent {
	public FlkMaterialReceiveEvent(Object source) {
		super(source);
	}
}
