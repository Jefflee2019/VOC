package com.bfd.parse.task;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;

import Ice.Application;

import com.bfd.config.util.CrawlMonitor;
import com.bfd.config.util.CrawlMonitor.CrawlService;
import com.bfd.crawler.kafka7.KfkConsumer;
//import com.bfd.config.util.CrawlMonitor;
//import com.bfd.config.util.CrawlMonitor.CrawlService;
import com.bfd.crawler.utils.ConfigUtils;
import com.bfd.parse.Constants;
import com.bfd.parse.threadmanager.ParseWorker;
import com.bfd.parse.zkmonitor.ParseMonitor;

public class Server extends Application {

	
	public static final Log log = LogFactory.getLog(Server.class);

	private class RunTimeShutdownHook extends Thread {
		private List<HookTask> taskList;

		public RunTimeShutdownHook(List<HookTask> taskList) {
			this.taskList = taskList;
		}

		public void run() {
			log.info("run runTimeShutdownHook!");
			KfkConsumer.stopKfkConsumerThreads();
			while(ParseQueue.queue.size()!=0){
				log.info("workqueue size is "+ParseQueue.queue.size());
				try {
					Thread.currentThread().sleep(1000*20);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
//			try {
//				//这为什么报空指针？
//				communicator().destroy();
//			} catch (Ice.LocalException e) {
//				log.error("shutdown hook, destory communicator exception.", e);
//			}catch (Exception e) {
//				e.printStackTrace();
//			}
			if (taskList != null) {
				for (HookTask task : taskList) {
					task.stop();
				}
			}
		}
	}

	private String getParseType(String[] args){
		String parseType = "";
		for (int i = 0; i < args.length; i++) {
			if (args[i].equalsIgnoreCase("-parsetype") && i < args.length - 1) {
				parseType = args[++i];
			}else if(args[i].equalsIgnoreCase("-host") && i < args.length - 1){
				
				Constants.host = args[++i]+"";
				log.info("host:"+Constants.host);
			}
			else if (args[i].equalsIgnoreCase("-h") || args[i].equalsIgnoreCase("--help") || args[i].equalsIgnoreCase("-?")) {
				System.out.println("usage: cmd [-options option-value]");
				System.out.println("    -parsetype : parsetype 0 is templateParse; 1 is weibo parser;2 is auto parser default is template parser");
				
			}
		}
		return parseType;
	}
	@Override
	public int run(String[] args) {
		ParseWorker.parseType = getParseType(args);
		log.info("parsetype:"+ParseWorker.parseType);
		ConfigUtils.getInstance().readFile(new File("../etc/crawl-config.properties"));
		String endPoints = ConfigUtils.getInstance().getProp("PageParser.Endpoints", "default");
		Ice.ObjectAdapter adapter = null;
		String readTopicName = "";
		String readFastTopicName = "";
		if(ParseWorker.parseType.equals("0")){
			adapter = communicator().createObjectAdapterWithEndpoints("PageParserServiceAdapter",
					endPoints);
			adapter.add(
					new PageParserI(),
					communicator().stringToIdentity(
							ConfigUtils.getInstance().getProp("PageParser.Identity", "PageParserService")));
			readTopicName = ConfigUtils.getInstance().getProp("crawl.parse.readtopic", "downloadrstopic");
			readFastTopicName = readTopicName+"_fast";
		}else if(ParseWorker.parseType.equals("1")){
			adapter = communicator().createObjectAdapterWithEndpoints("WeiboParserServiceAdapter",
					endPoints);
			adapter.add(
					new PageParserI(),
					communicator().stringToIdentity(
							ConfigUtils.getInstance().getProp("PageParser.Identity", "WeiboParserService")));
			readTopicName = ConfigUtils.getInstance().getProp("crawl.weiboparse.readtopic", "weibodownloadrstopic");
			readFastTopicName = readTopicName+"_fast";
		}
		
		
		log.info("pageparse service listen:" + endPoints);
		
		
		log.info("readTopicName:"+readTopicName+",readfasttopicname:"+readFastTopicName+",parseType:"+ParseWorker.parseType);
		//TODO:通过zk监控，不需要这些更新线程了
		if (!this.prepare()) {
			log.error("client connection refused, server will stop.");
			return 2;
		}
		//TODO:zk监控
		startMonitor();
		String parseGroup = ConfigUtils.getInstance().getProp("parse_group", Constants.kafka_consumer_group);
		//
		int kfkReadThreadNum = ConfigUtils.getInstance().getIntProp("crawl.parse.kafka.readthread", 3);
		log.info("kfkReadThreadNum:"+kfkReadThreadNum);
//		KfkConsumer.startReadThread(ParseQueue.queue, readTopicName, kfkReadThreadNum,parseGroup);
//		KfkConsumer.startReadThread(ParseQueue.fastQueue, readFastTopicName, kfkReadThreadNum,parseGroup);
		startKfkReadThread(readTopicName, kfkReadThreadNum, parseGroup);
		adapter.activate();
		communicator().waitForShutdown();
		if (interrupted())
			log.info(appName() + ": terminating");
		return 0;
	}
	
	private void startKfkReadThread(String readTopics,int kfkReadThreadNum,String parseGroup){
		String[] topics = readTopics.split(",");
		for(String topic : topics){
			KfkConsumer.startReadThread(ParseQueue.queue, topic, kfkReadThreadNum,parseGroup);
			KfkConsumer.startReadThread(ParseQueue.fastQueue, topic+"_fast", kfkReadThreadNum,parseGroup);
		}
	}

	private void startMonitor(){
		log.info("startMonitor! ");
//		ConfigUtils.getInstance().readFile(new File("../etc/zk.conf"));
		ConfigUtils.getInstance().readFile(new File("../etc/crawl-config.properties"));
		String zkAddr = ConfigUtils.getInstance().getProp("crawl.public.zookeeper.addr");
		String zkPath = ConfigUtils.getInstance().getProp("crawl.public.zookeeper.root", "/crawler_dev");
		log.info("zkAddr:"+zkAddr+",zkpath:"+zkPath);
		Map<String, String> params = new HashMap<String, String>();
		params.put("zkAddress", zkAddr);
		params.put("zkRootName", zkPath);
		CrawlMonitor.getInstance(params, ParseMonitor.monitor, CrawlService.PARSE).startMonitor();
	}
	
	private boolean prepare() {
		List<HookTask> taskList = new ArrayList<HookTask>();
		int threadNum = ConfigUtils.getInstance().getIntProp("Parse.ThreadNum", 10);
		if (!ConfigUtils.getInstance().getBoolProp("TestMode", false)) {
			taskList.addAll(startParseTask(threadNum));
		}
		//注释掉下面的一行，不在通过这种方式同步数据库，通过zk同步
//		taskList.addAll(startSynchConfigTask());
//		setInterruptHook(new ShutdownHook(taskList));
		Runtime.getRuntime().addShutdownHook(new RunTimeShutdownHook(taskList));
		
		return true;
	}

	private List<HookTask> startParseTask(int threadNum) {
		List<HookTask> workers = new ArrayList<HookTask>(threadNum);
		for (int i = 0; i < threadNum; i++) {
			ParseWorker worker = new ParseWorker("worker-" + i,i);
			new Thread(worker, "worker-" + i).start();
			log.info("Parse worker-" + i + " start...");
			workers.add(worker);
		}
		return workers;
	}

//	private List<HookTask> startSynchConfigTask() {
//		ConfigUtils.getInstance().readFile(new File("../etc/parse-config.properties"));
//		log.info("iid load interval "+ConfigUtils.getInstance()
//				.getLongProp("Parse.RuleSynchTime", 3600000L));
//		ConfigSynchronizer task = new ConfigSynchronizer(JudgeRuleConfig.getInstance(), ConfigUtils.getInstance()
//				.getLongProp("Parse.RuleSynchTime", 3600000L));
//
//		ConfigSynchronizer task2 = new ConfigSynchronizer(DomConfig.getInstance(), ConfigUtils.getInstance()
//				.getLongProp("Parse.TmplSynchTime", 3600000L));
//
//		ConfigSynchronizer task3 = new ConfigSynchronizer(PluginConfig.getInstance(), ConfigUtils.getInstance()
//				.getLongProp("Parse.PluginSynchTime", 120000L));
//
//		ConfigSynchronizer task4 = new ConfigSynchronizer(ItemFldMapConfig.getInstance(), ConfigUtils.getInstance()
//				.getLongProp("Parse.ItemFldMapSynchTime", 120000L));
//		ConfigSynchronizer task5 = new ConfigSynchronizer(ParseConfigure.getInstance(), ConfigUtils.getInstance()
//				.getLongProp("Parse.ItemFldMapSynchTime", 120000L));
//		ConfigSynchronizer task6 = new ConfigSynchronizer(WebsiteConfig.getInstance(), ConfigUtils.getInstance()
//				.getLongProp("Parse.WebsiteMapSynchTime", 120000L));
//		new Thread(task).start();
//		new Thread(task2).start();
//		new Thread(task3).start();
//		new Thread(task4).start();
//		new Thread(task5).start();
//		new Thread(task6).start();
//		ArrayList<HookTask> tasks = new ArrayList<HookTask>();
//		tasks.add(task);
//		tasks.add(task2);
//		tasks.add(task3);
//		tasks.add(task4);
//		tasks.add(task5);
//		return tasks;
//	}

	public static void main(String[] args) {
		PropertyConfigurator.configure("../etc/log4j.properties");
		Server app = new Server();
		int status = app.main("ParseServer", args);
		System.exit(status);
	}
}
