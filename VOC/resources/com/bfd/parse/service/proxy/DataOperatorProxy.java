package com.bfd.parse.service.proxy;

import java.io.UnsupportedEncodingException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import Ice.ObjectPrx;

import com.bfd.crawler.DataOperatorPrx;
import com.bfd.crawler.DataOperatorPrxHelper;
import com.bfd.crawler.utils.ConfigUtils;

public class DataOperatorProxy extends AbstractIceProxy {

	private static final Log LOG = LogFactory.getLog(DataOperatorProxy.class);

	public DataOperatorProxy() {
		// 设置最大参数
		super(true);
	}

	@Override
	protected String getProxyConfig() {
		return ConfigUtils.getInstance().getProp("DataOperator.Proxy", "DataOperatorService");
	}

	@Override
	protected ObjectPrx checkedCast() {
		return DataOperatorPrxHelper.checkedCast(base);
	}

	@Override
	protected DataOperatorPrx getService() {
		return ((DataOperatorPrx) super.getService());
	}

	public int saveData(String attr, String data) {
		if (StringUtils.isEmpty(attr) || StringUtils.isEmpty(data)) {
			LOG.warn("Invalid data, attr or data is empty.");
			return -1;
		}
		try {
			return getService().saveData(attr, data);
		} catch (Ice.MarshalException e) {
			LOG.warn(Thread.currentThread().getName() + " ice.MarshalException while calling save data method, Err:", e);
			try {
				data = new String(data.getBytes("gbk"), "gbk");
				return getService().saveData(attr, data);
			} catch (UnsupportedEncodingException e1) {
				LOG.warn(Thread.currentThread().getName() + " exception while convert encode, Err:", e1);
				return -1;
			}
		} catch (Exception e) {
			LOG.warn(Thread.currentThread().getName() + " exception while calling save data method, Err:", e);
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
				if (init(true))
					return getService().saveData(attr, data);
			} catch (InterruptedException x) {
			} catch (Exception x) {
				LOG.warn(Thread.currentThread().getName() + " exception while connect to dataoperator server: " + l1
						/ 1000 + ", " + l2 / 1000, x);
				LOG.debug(Thread.currentThread().getName() + " save data exception, attr=" + attr);
			}
			LOG.debug(Thread.currentThread().getName() + " try to connect to dataoperator server: " + l1 / 1000 + ", "
					+ l2 / 1000);
		}
	}

	@Override
	public String name() {
		return "dataoperator";
	}
}
