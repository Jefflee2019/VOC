package com.bfd.parse.file;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.task.HookTask;

public class FileParseWorker implements Runnable, HookTask {
	private static final Log LOG = LogFactory.getLog(FileParseWorker.class);
	private static BlockingQueue<ParseUnit> queue;
	static {
		queue = new ArrayBlockingQueue<ParseUnit>(50);
	}

	private volatile boolean runFlag = true;
	private String workName;
	private ParserFace parser;

	public FileParseWorker(String workName) {
		this.workName = workName;
		parser = new ParserFace(workName);
	}

	public static boolean put(ParseUnit unit) {
		try {
			queue.put(unit);
			LOG.info(" file queue size=" + queue.size());
			return true;
		} catch (InterruptedException e) {
			LOG.warn(e);
		}
		return false;
	}

	@Override
	public void run() {
		while (runFlag) {
			try {
				ParseUnit unit = queue.take();

				if (StringUtils.isEmpty(unit.getPageType()) || unit.getPageType().toLowerCase().endsWith("img")) {
					LOG.info("Parse type=" + unit.getPageType() + ", will skip");
					continue;
				}

				Integer rid = (Integer) unit.getTaskdata().get("rid");
				LOG.info(workName + " parsing parseunit, rid=" + rid);
				parser.parse(unit);
				LOG.info(workName + " parsed parseunit, rid=" + rid);
			} catch (Exception e) {
				LOG.warn(e);
			}
		}
	}

	@Override
	public void stop() {
		runFlag = false;
	}

}
