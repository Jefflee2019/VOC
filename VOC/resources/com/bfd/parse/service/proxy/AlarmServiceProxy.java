package com.bfd.parse.service.proxy;

import Ice.ObjectPrx;


public class AlarmServiceProxy extends AbstractIceProxy {

	@Override
	public String name() {
		return "alarm_client";
	}

	@Override
	protected ObjectPrx checkedCast() {
		return null;
	}

	@Override
	protected String getProxyConfig() {
		return null;
	}

}
