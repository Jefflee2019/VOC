package com.bfd.parse.client;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import Ice.ObjectPrx;

import com.bfd.crawler.DownloaderPrx;
import com.bfd.crawler.DownloaderPrxHelper;
import com.bfd.crawler.utils.JsonUtils;

public class DownloadClientTest {
	private static final Log LOG = LogFactory.getLog(DownloadClientTest.class);
	private static final String cfgLocator = "bfdcloud/Locator:tcp -h 192.168.61.129 -p 9099";

	public static void main(String[] args) {
		Ice.Communicator __ic = null;
		Ice.Properties properties = Ice.Util.createProperties();
		properties.setProperty("Ice.Default.Locator", cfgLocator);
		properties.setProperty("Ice.Override.Timeout", "2000");

		Ice.InitializationData initData = new Ice.InitializationData();
		initData.properties = properties;
		__ic = Ice.Util.initialize(initData);

		ObjectPrx proxy = __ic.stringToProxy("DownonepageService");
		if (proxy == null){
			return ;
		}
			
		DownloaderPrx download = DownloaderPrxHelper.checkedCast(proxy);
		Map<String, String> map = new HashMap<String, String>();
		map.put("cid", "test");
		map.put("url", "http://blog.csdn.net/moxiaomomo/article/details/6769316");
		String html = download.getOnePage(JsonUtils.toJSONString(map));
	}
}
