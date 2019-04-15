package com.bfd.parse.test.weibosinaparser;

//import java.util.HashMap;
//import java.util.Map;
//import java.util.Properties;
//
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
//
//import com.bfd.crawler.LoginServicePrx;
//import com.bfd.crawler.LoginServicePrxHelper;
//import com.bfd.dp.kfk.util.LoadConfig;
//import com.bfd.dp.kfk.util.Util;
//import com.bfd.parse.util.JsonUtil;

public class LoginServiceClient {

//	public static final Log LOG = LogFactory.getLog(LoginServiceClient.class);
//
//	private static LoginServicePrx login = null;
//	private static Ice.Communicator __ic = null;
//
//	private static int sleep = 10;
//
//	private Properties pro = null;
//	private String cfgLocator = null;
//	private String proxy = null;
//
//	public LoginServiceClient() {
//		super();
//		pro = LoadConfig.getInstance().getPro("../etc/config");
//		cfgLocator = "bfdcloud/Locator:tcp -h 192.168.61.129 -p 9099";
//		// if("false".equals(pro.getProperty("deduplicator.TestMode",
//		// "false"))){
//		// proxy = pro.getProperty("deduplicator.server.proxy", "LoginService")
//		// ;
//		// }else{
//		// proxy = pro.getProperty("deduplicator.local.proxy",
//		// "LoginService -h 127.0.0.1 -p 10000") ;
//		// }
//		proxy = "LoginService";
//		init();
//	}
//
//	class ShutdownHook extends Thread {
//		public void run() {
//			try {
//				__ic.destroy();
//			} catch (Ice.LocalException ex) {
//				ex.printStackTrace();
//			}
//		}
//	}
//
//	private void init() {
//		try {
//			if (__ic == null) {
//				Ice.Properties properties = Ice.Util.createProperties();
//				properties.setProperty("Ice.Default.Locator", cfgLocator);
//				Ice.InitializationData initData = new Ice.InitializationData();
//				initData.properties = properties;
//				__ic = Ice.Util.initialize(initData);
//			}
//			Ice.ObjectPrx base = __ic.stringToProxy(proxy); // locate the
//															// configurer
//															// service
//			login = LoginServicePrxHelper.checkedCast(base);
//			if (login == null) {
//				LOG.error("invalid proxy");
//			}
//		} catch (Ice.LocalException e) {
//			LOG.error(Thread.currentThread()
//					+ " init DeduplicatorService error,try again after "
//					+ sleep + " seconds", e);
//			Util.sleep(sleep);
//			sleep = sleep + 10;
//			if (sleep > 60)
//				return;
//			init();
//		}
//	}
//
//	// 第二个参数为true，表示如果没有就添加。
//	public String login(String ip, String username, String passwd) {
//		String status = null;
//		Map<String, Object> param = new HashMap<String, Object>();
//		param.put("ip", ip);
//		param.put("username", username);
//		param.put("passwd", passwd);
//		try {
//			status = login.login(JsonUtil.toJSONString(param));
//		} catch (Exception e) {
//			destroy();
//			Util.sleep(sleep);
//			sleep = sleep + 10;
//			if (sleep > 60)
//				return null;
//			init();
//		}
//		return status;
//	}
//
//	private void destroy() {
//		__ic.destroy();
//		__ic = null;
//		login = null;
//	}
//
//	public static void main(String[] args) {
//		LoginServiceClient loginServiceClient = new LoginServiceClient();
//		String res = loginServiceClient.login("117.121.9.166", "jvblsq0asmxuz@163.com",
//				"rl4f27ocne");
//		System.out.println(res);
//	}
}