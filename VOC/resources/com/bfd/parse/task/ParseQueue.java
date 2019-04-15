package com.bfd.parse.task;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.crawler.utils.ConfigUtils;

public class ParseQueue {

	private static final Log LOG = LogFactory.getLog(ParseQueue.class);

	private static ParseQueue instance = new ParseQueue();

	protected ParseQueue() {
	}

	public static ParseQueue getInstance() {
		return instance;
	}

	// private static LinkedBlockingDeque<ParseJob> queue;
	public static LinkedBlockingDeque<String> queue;
	public static LinkedBlockingDeque<String> fastQueue;
	static {
		queue = new LinkedBlockingDeque<String>(ConfigUtils.getInstance()
				.getIntProp("Parse.Queue.Size", 100));
		fastQueue = new LinkedBlockingDeque<String>(ConfigUtils.getInstance()
				.getIntProp("Parse.Queue.Size", 100));
	}

	public boolean put(String job) {
		if (job != null) {
			try {
				queue.put(job);
				LOG.info("parse queue size=" + size());
				return true;
			} catch (Exception e) {
				LOG.warn(
						"Exception while puting job into BlockingQueue, queue size="
								+ size(), e);
			}
		}
		return false;
	}

	public boolean putFirst(String job) {
		if (job != null) {
			try {
				queue.putFirst(job);
				LOG.info("parse queue size=" + size());
				return true;
			} catch (Exception e) {
				LOG.warn(
						"Exception while puting job into BlockingQueue, queue size="
								+ size(), e);
			}
		}
		return false;
	}

	public String get() {
		try {
			if(fastQueue.size()>0){
				return fastQueue.take();
			}else{
				return queue.take();
			}
			
		} catch (Exception e) {
			LOG.warn(
					"Exception while taking job from BlockingQueue, queue size="
							+ size(), e);
		}
		return null;
	}

	public boolean put(String request, int jobType) {
		// LOG.info("execute put parsequeue!");
		if (StringUtils.isNotEmpty(request)) {
			if (request.indexOf("tmptasktag\": 1") >= 0) {
				// LOG.info("put first."+request);
				return putFirst(request);
			} else {
				return put(request);
			}
		}
		// if (StringUtils.isNotEmpty(request)) {
		// ParseJob job = null;
		// try {
		// if (jobType == ParseJob.TYPE_DATA) {
		// job = ParseJob.fromDataJsonStr(request);
		// if (job != null && job.size() > 0) {
		// job.setType(jobType);
		// //临时任务优先处理
		// if(request.indexOf("tmptasktag\": 1")>=0){
		// // LOG.info("put first."+request);
		// return putFirst(job);
		// }else{
		// return put(job);
		// }
		//
		// }
		// } else if (jobType == ParseJob.TYPE_FILE) {
		// job = ParseJob.fromFileJsonStr(request);
		// job.setType(jobType);
		// return put(job);
		// }
		// } catch (Exception e) {
		// LOG.info("Create job from json exception, request is:" + request +
		// "\n, err:\n", e);
		// }
		// } else
		LOG.info("WARNING： Request is Empty.");
		LOG.info("execute put parsequeue over!");
		return false;
	}

	public boolean isEmpty() {
		return queue.isEmpty();
	}

	public int size() {
		return queue.size();
	}
}
