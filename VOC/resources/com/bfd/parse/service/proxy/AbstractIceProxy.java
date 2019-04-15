package com.bfd.parse.service.proxy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import Ice.ObjectPrx;

import com.bfd.crawler.utils.ConfigUtils;

public abstract class AbstractIceProxy implements IceProxy {

	private static final Log LOG = LogFactory.getLog(AbstractIceProxy.class);

	private static final String cfgLocator = "crawler/Locator:tcp -h 192.168.3.112 -p 9030:tcp -h 192.168.3.113 -p 9030";
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
	 * @param setMaxMsgSize
	 */
	protected AbstractIceProxy(boolean setMaxMsgSize) {
		if (init(setMaxMsgSize)) {
			setSatus(STATUS_OK);
		} else {
			setSatus(STATUS_INITFAILED);
		}
	}

	protected AbstractIceProxy() {
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
		try {
			if (ic == null) {
				if (ConfigUtils.getInstance().getBoolProp("Distribute.status", true)) {
					Ice.Properties properties = Ice.Util.createProperties();
					properties.setProperty("Ice.Default.Locator",
							ConfigUtils.getInstance().getProp("Distribute.configLocator", cfgLocator));
					if (setMaxMsgSize) {
						properties.setProperty("Ice.MessageSizeMax",
								ConfigUtils.getInstance().getProp("Ice.MessageSizeMax", "3076"));
						LOG.info(name() + " service, set MessageSizeMax to "
								+ ConfigUtils.getInstance().getProp("Ice.MessageSizeMax", "3076"));
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
	public void release() {
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
