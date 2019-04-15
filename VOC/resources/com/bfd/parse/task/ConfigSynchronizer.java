package com.bfd.parse.task;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.config.Config;

public class ConfigSynchronizer implements Runnable, HookTask {

	private static final Log LOG = LogFactory.getLog(ConfigSynchronizer.class);

	private Config config;

	private volatile boolean runFlag = true;

	private long timesize;

	public ConfigSynchronizer(Config config) {
		this(config, 36000000);
	}

	public ConfigSynchronizer(Config config, long timesize) {
		this.config = config;
		this.timesize = timesize;
	}

	@Override
	public void run() {
		LOG.info(" synchron " + config.name() + " task start..., synchronize time=" + this.timesize);
		while (runFlag) {
			LOG.info(config.name()+" sleep "+timesize);
			try {
				Thread.sleep(timesize);
			} catch (InterruptedException e) {
				LOG.warn(e);
			}
			LOG.info(config.name() + " data will be synchronized...");
			config.requestConfig();
//			LOG.info(config.name() +" after synchronized data is "+JsonUtils.toJSONString(config.getData()));
		}
	}

	@Override
	public void stop() {
		runFlag = false;
	}
}
