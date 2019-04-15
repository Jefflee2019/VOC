package com.bfd.parse.service.proxy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import Ice.ObjectPrx;

import com.bfd.crawler.ConfigurerPrx;
import com.bfd.crawler.ConfigurerPrxHelper;
//import com.bfd.crawler.BizConfigurerPrx;
//import com.bfd.crawler.BizConfigurerPrxHelper;
import com.bfd.crawler.utils.ConfigUtils;

public class BizConfigureProxy extends AbstractIceProxy {

	private static final Log LOG = LogFactory.getLog(BizConfigureProxy.class);

	private static BizConfigureProxy instance = new BizConfigureProxy();
	private BizConfigureProxy() {
		super();
	}

	public static BizConfigureProxy getInstance(){
		return instance;
	}
	@Override
	public String name() {
		return "config_client";
	}

	@Override
	protected ObjectPrx checkedCast() {
//		return BizConfigurerPrxHelper.checkedCast(base);
		return ConfigurerPrxHelper.checkedCast(base);
	}

	@Override
	protected String getProxyConfig() {
		return ConfigUtils.getInstance().getProp("BizConfigurer.Proxy", "BizConfigurerService");
	}

	@Override
	public ConfigurerPrx getService() {
		return ((ConfigurerPrx) super.getService());
	}

	public String getConfig(String bizName, String configNames) {
		try {
			return getService().getConfig(bizName, configNames);
		} catch (Exception e) {
			LOG.warn(Thread.currentThread().getName() + " exception while calling getconfig method, Err="
					+ e.getMessage());
			e.printStackTrace();
		}
		long l1 = 1000, l2 = 1000, lmax = 120000, ltemp;
		while (true) {
			try {
				Thread.sleep(l1);
				ltemp = l1;
				l1 = l2;
				l2 = ltemp + l2;
				if (l2 >= lmax) {
					l1 = l2 = 1000;
				}
				if (init())
					return getService().getConfig(bizName, configNames);
			} catch (InterruptedException x) {
			} catch (Exception x) {
				LOG.warn(Thread.currentThread().getName() + " exception while connect to config server: " + l1 / 1000
						+ ", " + l2 / 1000 + "", x);
			}
			LOG.debug(Thread.currentThread().getName() + " try to connect to config server: " + l1 / 1000 + ", " + l2
					/ 1000);
		}
	}
}
