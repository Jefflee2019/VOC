package com.bfd.parse.util;

import org.apache.log4j.Logger;

import com.bfd.crawler.kafka7.KfkProducer;

public class KfkUtils {
	private static Logger LOG = Logger.getLogger(KfkProducer.class);
	public static void sendKfk(String topic,String message){
		boolean flag = KfkProducer.getInstance().send(topic, message);
		int sendCount = 0;
		while(!flag){
			LOG.debug("send kfkf count is "+sendCount);
			flag = KfkProducer.getInstance().send(topic, message);
		}
	}
}
