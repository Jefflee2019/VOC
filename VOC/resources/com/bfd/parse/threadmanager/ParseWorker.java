package com.bfd.parse.threadmanager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.facade.IParse;
import com.bfd.parse.facade.autoparse.AutoParser;
import com.bfd.parse.facade.jspageparse.JsPageParser;
import com.bfd.parse.facade.tmplparse.TmplParser;
import com.bfd.parse.task.HookTask;
import com.bfd.parse.task.ParseQueue;

public class ParseWorker implements Runnable, HookTask {

	private static final Log LOG = LogFactory.getLog(ParseWorker.class);

	public static String parseType = "0";
	private static ParseQueue queue = ParseQueue.getInstance();
	private int threadIndex;

//	private ParserFace parser;
	//每个线程生成一个parser，因为parser里面有个对象属性石parserface，这个对象会生成其他服务的ice连接,
	private IParse parser ;

	private volatile boolean runFlag = true;

	private String name;

	public ParseWorker(String name,int threadIndex) {
		if (name == null)
			name = "";
		this.name = name;
		this.threadIndex = threadIndex;
		init();
	}

	private void init() {
		LOG.info("parseType:"+parseType);
//		parser = new ParserFace(name);
		if("0".equals(parseType)){
			this.parser = new TmplParser();
		}else if("1".equals(parseType)){
			this.parser = new JsPageParser();
		}else{
			this.parser = new AutoParser();
		}
	}

	@Override
	public void run() {
		LOG.info("start parseWorker!threadname:"+this.name);
		runFlag = true;
		while (runFlag) {
			try {
				String taskStr = "";
				if(threadIndex!=1){
					taskStr = queue.get();
				}else{
					taskStr = ParseQueue.fastQueue.take();
				}
				Object results = parser.parse(taskStr);
//				LOG.info(this.name + " is reporting parsing result to dispath service....");
				//
				//写入统计队列和保存返回队列
//				saveResult(unit, res, isTest);
//				dispatchClient.reportResults(JsonUtil.toJSONString(results));
				LOG.info(this.name + " reported to dispath service.");
				
			} catch (Exception e) {
				LOG.warn("Unkown exception while " + this.name + " working, err=", e);
			}
		}
		LOG.info("Parse work " + this.name + " will stop.");
	}

	@Override
	public void stop() {
		runFlag = false;
		LOG.info("Set " + name + "'s runFlag value to false.");
	}

	public static boolean addJob(String request, int jobType) {
		return queue.put(request, jobType);
	}

	public String name() {
		return this.name;
	}
}