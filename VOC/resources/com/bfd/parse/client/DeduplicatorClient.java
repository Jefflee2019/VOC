package com.bfd.parse.client;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import Ice.ObjectPrx;

import com.bfd.crawler.DeduplicatorPrx;
import com.bfd.crawler.DeduplicatorPrxHelper;
import com.bfd.crawler.utils.ConfigUtils;
import com.bfd.crawler.utils.JsonUtils;

public class DeduplicatorClient extends AbstractClient {

	private static final Log LOG = LogFactory.getLog(DeduplicatorClient.class);
	private String thdName;

	public DeduplicatorClient(String workname) {
		this.thdName = workname;
	}

	@Override
	public String name() {
		return "DeduplicatorService";
	}

	@Override
	protected DeduplicatorPrx getService() {
		return (DeduplicatorPrx) super.getService();
	}

	@Override
	protected ObjectPrx checkedCast() {
		return DeduplicatorPrxHelper.checkedCast(base);
	}

	@Override
	protected String getProxyConfig() {
		return ConfigUtils.getInstance().getProp("Deduplicator.Proxy", "DeduplicatorService");
	}

	public boolean duplicate(String request, boolean bAdd) {
		String duplicate = isDuplicate(request, bAdd);
		if (StringUtils.isNotEmpty(duplicate)) {
			try {
				List<Object> res = JsonUtils.parseArray(duplicate);
				Integer resCode = (Integer) res.get(1);
				if (resCode != null && resCode == 1) {
					return true;
				}
			} catch (Exception e) {
				LOG.warn(thdName + " exception while calling duplicate, request -> " + request, e);
			}
		}
		return false;
	}

	public String isDuplicate(String request, boolean bAdd) {
		long callTimesNum=1;
		if (StringUtils.isEmpty(request)) {
			new NullPointerException();
		}
		try {
			return getService().isDuplicate(request, bAdd);
		} catch (Exception e) {
			LOG.warn(thdName + " exception while calling isDuplicate , err:", e);
		}
		long l1 = 1000, l2 = 1000, lmax = 120000, ltemp;
		while (true) {
			LOG.info("call deduplicator call times is "+callTimesNum);
			try {
				Thread.sleep(l1);
				ltemp = l1;
				l1 = l2;
				l2 = ltemp + l2;
				if (l2 >= lmax) {
					l1 = l2 = 1000;
				}
				if (init())
					return getService().isDuplicate(request, bAdd);
				callTimesNum++;
			} catch (InterruptedException x) {
				callTimesNum++;
			} catch (Exception x) {
				callTimesNum++;
				LOG.warn(thdName + " exception while connect to deduplicator server: " + l1 / 1000 + ", " + l2 / 1000
						+ "", x);
			}
			LOG.debug(Thread.currentThread().getName() + " try to connect to deduplicator server: " + l1 / 1000 + ", "
					+ l2 / 1000);
		}
	}

	public static void main(String[] args) {
		String duplicate = new DeduplicatorClient("test").isDuplicate("http://www.cnbeta.com", false);
	}
}
