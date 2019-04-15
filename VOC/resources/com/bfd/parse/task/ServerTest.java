package com.bfd.parse.task;

import java.io.File;

import com.bfd.crawler.kafka7.KfkConsumer;
import com.bfd.crawler.utils.ConfigUtils;
import com.bfd.parse.Constants;
import com.bfd.parse.threadmanager.ParseWorker;

public class ServerTest {

	public static void main(String[] args) {
		ConfigUtils.getInstance().readFile(new File("../etc/crawl-config.properties"));
		String readTopicName = ConfigUtils.getInstance().getProp("crawl.parse.readtopic", "downloadrstopic");
		String readFastTopicName = readTopicName+"_fast";
		KfkConsumer.startReadThread(ParseQueue.queue, readTopicName, Constants.kfk_read_thread_num,Constants.kafka_consumer_group);
		KfkConsumer.startReadThread(ParseQueue.fastQueue, readFastTopicName, Constants.kfk_read_thread_num,Constants.kafka_consumer_group);
		ParseWorker.parseType = 0+"";
		ParseWorker worker = new ParseWorker("worker-" + 0,0);
		new Thread(worker, "worker-" + 0).start();
	}

}
