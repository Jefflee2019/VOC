package com.bfd.parse.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;

import com.bfd.parse.config.dom.DomConfig;
import com.bfd.parse.config.parseplugin.PluginConfig;
import com.bfd.parse.config.shelf.JudgeRuleConfig;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.task.HookTask;
import com.bfd.parse.task.ConfigSynchronizer;
import com.bfd.crawler.utils.ConfigUtils;
import com.bfd.crawler.utils.FileUtil;
import com.bfd.parse.util.JsonUtil;

/**
 * taskdata:{}
 * length:
 * {"data":"sss"}
 * @author ian
 *
 */
public class DataLoader implements Runnable {

	private static final Log LOG = LogFactory.getLog(DataLoader.class);

	private List<String> fileNames;
	private String workName;

	public DataLoader(List<String> fileNames, String workName) {
		this.fileNames = fileNames;
		this.workName = workName;
	}

	public void loadData(String fileName) throws IOException {
		RandomAccessFile raf = new RandomAccessFile(fileName, "r");
		int success = 0;
		int total = 0;
		while (true) {
			Map<String, Object> resMap = new HashMap<String, Object>();
			try {
				Map<String, Object> taskData = loadTaskData(raf);
				if (taskData == null) {
					break;
				}
				String len = loadLength(raf);
				Map<String, Object> spiderData = loadSpiderData(raf, Integer.valueOf(len) + 1);
				spiderData.put("httpcode", "200");
				taskData.put("projname", "Idea4Media");
				resMap.put("taskdata", taskData);
				resMap.put("spiderdata", spiderData);
			} catch (Exception e) {
				e.printStackTrace();
				LOG.warn(e);
			}
			ParseUnit unit = ParseUnit.fromMap(resMap, System.currentTimeMillis());
			if (unit != null) {
				FileParseWorker.put(unit);
				success++;
			} else {
				LOG.warn("load unit failed,");
			}
			total++;
		}
		LOG.info(workName + " load data finished, parseunit size=" + success + ", total=" + total + ", fileName="
				+ fileName);
		raf.close();
	}

	private Map<String, Object> loadSpiderData(RandomAccessFile raf, Integer len) throws Exception {
		byte[] bs = new byte[len];
		int read = raf.read(bs);
		return (Map<String, Object>) JsonUtil.parseObject(new String(bs, "utf8"));
	}

	private String loadLength(RandomAccessFile raf) throws IOException {
		String string = raf.readLine();
		if (StringUtils.isEmpty(string)) {
			return null;
		}
		String[] lengthSeg = string.split(":");
		return lengthSeg[1];
	}

	private Map<String, Object> loadTaskData(RandomAccessFile raf) throws Exception {
		while (true) {
			String taskData = raf.readLine();
			if (taskData == null) {
				return null;
			}
			taskData = new String(taskData.getBytes("8859_1"), "utf8");
			if (!taskData.startsWith("taskdata:")) {
				LOG.info("dirty data, will skip.");
				continue;
			}
			taskData = taskData.substring(9);
			return (Map<String, Object>) JsonUtil.parseObject(taskData);
		}
	}

	@Override
	public void run() {
		for (String fileName : fileNames) {
			try {
				loadData(fileName);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static boolean loadFileData(String path, String suffix) {
		try {
			if (StringUtils.isNotEmpty(path)) {
				File file = new File(path);
				if (!file.exists()) {
					LOG.warn("file is not exist. dir=" + path);
					return false;
				}
				if (file.isDirectory()) {
					List files = FileUtil.getListFiles(path, suffix, true);
					if (files.size() > 0) {
						List<String> fileNames = new ArrayList<String>();
						fileNames.addAll(files);
						LOG.info(" found " + fileNames.size() + " files in dir -> " + path);
						DataLoader loader = new DataLoader(fileNames, "loadworker-1");
						new Thread(loader).start();
						return true;
					}
					LOG.warn("file size is 0, dir=" + path);
					return false;
				} else if (file.isFile() && file.isAbsolute()) {
					List<String> files = new ArrayList<String>();
					files.add(path);
					DataLoader loader = new DataLoader(files, "loadworker-1");
					new Thread(loader).start();
					return true;
				}
				return false;

			}
			return false;
		} catch (Exception e) {
			LOG.warn(e);
		}
		return false;
	}

	public static void main(String[] args) {
		PropertyConfigurator.configure("log4j.properties");
		if (args.length < 1) {
			LOG.error("error args, for example: java -cp  com.bfd.parse.file.DataLoader filepath filesuffix");
			System.exit(-1);
		}
		String path = args[0];
		String suffix = null;
		if (args.length > 1) {
			suffix = args[1];
		}
		startSynchConfigTask();
		startFileParseTask(10);
		LOG.info("receive file parse task, path=" + path);
		if (!DataLoader.loadFileData(path, suffix)) {
			LOG.error("load file error, path=" + path);
			System.exit(-1);
		}
	}

	private static List<HookTask> startSynchConfigTask() {
		ConfigSynchronizer task = new ConfigSynchronizer(JudgeRuleConfig.getInstance(), ConfigUtils.getInstance().getLongProp(
				"Parse.RuleSynchTime", 3600000L));

		ConfigSynchronizer task2 = new ConfigSynchronizer(DomConfig.getInstance(), ConfigUtils.getInstance().getLongProp(
				"Parse.TmplSynchTime", 3600000L));

		ConfigSynchronizer task3 = new ConfigSynchronizer(PluginConfig.getInstance(), ConfigUtils.getInstance().getLongProp(
				"Parse.PluginSynchTime", 120000L));
		new Thread(task).start();
		new Thread(task2).start();
		new Thread(task3).start();
		ArrayList<HookTask> tasks = new ArrayList<HookTask>();
		tasks.add(task);
		tasks.add(task2);
		tasks.add(task3);
		return tasks;
	}

	private static List<HookTask> startFileParseTask(int threadNum) {
		List<HookTask> workers = new ArrayList<HookTask>(threadNum);
		for (int i = 0; i < threadNum; i++) {
			FileParseWorker worker = new FileParseWorker("fileworker-" + i);
			new Thread(worker, "fileworker-" + i).start();
			LOG.info("FileParseWorker worker-" + i + " start...");
			workers.add(worker);
		}
		return workers;
	}
}
