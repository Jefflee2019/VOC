package com.bfd.parse.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import Ice.ObjectPrx;



//import com.bfd.crawler.BizConfigurerPrx;
//import com.bfd.crawler.BizConfigurerPrxHelper;
import com.bfd.crawler.ConfigurerPrx;
import com.bfd.crawler.ConfigurerPrxHelper;
import com.bfd.crawler.utils.ConfigUtils;
import com.bfd.crawler.utils.JsonUtils;
import com.bfd.parse.entity.FielddefineEntity;

public class ConfigClient extends AbstractClient {

	private static final Log LOG = LogFactory.getLog(ConfigClient.class);

	public ConfigClient() {
		super();
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
		String configName =  ConfigUtils.getInstance().getProp("crawl.public.ice.service.configure", "BizConfigurerService");
		return configName;
	}

	@Override
	public ConfigurerPrx getService() {
//		ConfigurerPrx
		return ((ConfigurerPrx) super.getService());
	}

	public String getConfig(String bizName, String configNames) {
		try {
			return getService().getConfig(bizName, configNames);
		} catch (Exception e) {
			LOG.warn(Thread.currentThread().getName() + " exception while calling getconfig method, Err=", e);
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
	//TODO:通过pageTypeId得到所有的页面字段
//	public List<Fielddefine> getFieldDefineByPageTypeId(int pageTypeId){
//		List<Fielddefine> field = null;
//		return field;
//	}
	public static void main(String[] args) {
		ConfigClient client = new ConfigClient();
		String bizName = "sitePageConfig";
		Map<String, String> configName = new HashMap<String, String>();
		configName.put("type", "all");
		String configJson = JsonUtils.toJSONString(configName);
		String rs = client.getConfig(bizName, configJson);
	}
}
