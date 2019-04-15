package com.bfd.parse.client;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.crawler.utils.ConfigUtils;
import com.bfd.parse.Constants;

import Ice.ObjectPrx;


public abstract class AbstractClient implements IceClient {

	private static final Log LOG = LogFactory.getLog(AbstractClient.class);

	private static final String cfgLocator = "bfdcloud/Locator:tcp -h 192.168.61.130 -p 62229";
	protected volatile Integer status = 0;

	protected static boolean testMode;

	static {
		testMode = ConfigUtils.getInstance().getBoolProp("TestMode", false);
	}

	private Ice.Communicator ic = null;
	protected Ice.ObjectPrx service;
	protected Ice.ObjectPrx base;

	/**
	 * 两种初始化，默认不设置最大消息
	 * 
	 * @param setMaxMsgSize
	 */
	protected AbstractClient(boolean setMaxMsgSize) {
		if (init(setMaxMsgSize)) {
			setSatus(STATUS_OK);
		} else {
			setSatus(STATUS_INITFAILED);
		}
	}

	/**
	 * 两种初始化，默认不设置最大消息
	 * 
	 * @param setMaxMsgSize
	 */
	protected AbstractClient(boolean setMaxMsgSize, boolean setMethordTimeout) {
		if (init(setMaxMsgSize, setMethordTimeout)) {
			setSatus(STATUS_OK);
		} else {
			setSatus(STATUS_INITFAILED);
		}
	}

	protected AbstractClient() {
		if (init(false)) {
			setSatus(STATUS_OK);
		} else {
			setSatus(STATUS_INITFAILED);
		}
	}

	protected boolean init() {
		return init(false);
	}

	protected boolean init(boolean setMaxMsgSize) {
		return init(setMaxMsgSize, false);
	}

	protected synchronized boolean init(boolean setMaxMsgSize, boolean setMethordTimeout) {
		String iceTimeout = ConfigUtils.getInstance().getProp(Constants.iceTimeout);
		if(iceTimeout==null||iceTimeout.trim().length()==0){
			ConfigUtils.getInstance().readFile(new File("../etc/crawl-config.properties"));
		}
		try {
			if(ic!=null){
				try {
					ic.destroy();
				} catch (Exception e) {
					LOG.error("ic destroy error!");
					e.printStackTrace();
				}
				ic=null;
				this.service = null;
			}
			if (ic == null) {
				if (ConfigUtils.getInstance().getBoolProp("Distribute.status", true)) {
					Ice.Properties properties = Ice.Util.createProperties();
					properties.setProperty("Ice.Default.Locator",
							ConfigUtils.getInstance().getProp("crawl.public.ice.locator", cfgLocator));
					LOG.info("locator:"+properties.getProperty("Ice.Default.Locator"));
					if (setMaxMsgSize) {
						properties.setProperty("Ice.MessageSizeMax",
								ConfigUtils.getInstance().getProp("Ice.MessageSizeMax", "3076"));
						LOG.info(name() + " service, set MessageSizeMax to "
								+ ConfigUtils.getInstance().getProp("Ice.MessageSizeMax", "3076"));
					}
					//
					properties.setProperty("Ice.Override.Timeout", 
							ConfigUtils.getInstance().getProp(Constants.iceTimeout, "20000"));
					LOG.info("Ice.Override.Timeout:"+ConfigUtils.getInstance().getProp(Constants.iceTimeout, "20000"));
					if (setMethordTimeout == true) {
						properties.setProperty("Ice.Override.ConnectTimeout",
								ConfigUtils.getInstance().getProp("Ice.Override.ConnectTimeout", "10000"));
					}
					Ice.InitializationData initData = new Ice.InitializationData();
					initData.properties = properties;
					ic = Ice.Util.initialize(initData);
				} else {
					ic = Ice.Util.initialize();
				}
			}
			ObjectPrx proxy = ic.stringToProxy(getProxyConfig());
			if (proxy == null)
				return false;
			base = proxy;
			LOG.debug("Seting checked Service to " + name() + " service...");
			setService(checkedCast());
			LOG.info("Connected " + name() + " service. proxy=" + getProxyConfig());
			return true;
		} catch (Ice.LocalException e) {
			e.printStackTrace();
		}
		return false;
	}

	abstract protected ObjectPrx checkedCast();

	abstract protected String getProxyConfig();

	protected Ice.Communicator getIc() {
		return ic;
	}

	protected Ice.ObjectPrx getService() {
		return service;
	}

	public void setService(Ice.ObjectPrx service) {
		this.service = service;
	}

	protected Ice.ObjectPrx getBase() {
		return base;
	}

	@Override
	public synchronized void release() {
		if (ic != null) {
			try {
				ic.destroy();
			} catch (Exception e) {
				LOG.warn(e.getMessage());
			} finally {
				setSatus(STATUS_RELEASE);
				ic = null;
			}
		}
	}

	@Override
	public Integer getStatus() {
		return status;
	}

	protected void setSatus(Integer status) {
		this.status = status;
	}
}
