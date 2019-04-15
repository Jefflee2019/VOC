package com.bfd.parse.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

public class SourceDataLoader implements Runnable {

	private static final Log LOG = LogFactory.getLog(SourceDataLoader.class);

	private List<String> fileNames;
	private String workName;
	private Set<String> cids;
	private boolean filteByCid = true;

	public SourceDataLoader(List<String> fileNames, String workName, Set<String> cids) {
		this.fileNames = fileNames;
		this.workName = workName;
		if (cids == null || cids.isEmpty()) {
			filteByCid = false;
		} else {
			this.cids = cids;
		}
	}

	public void loadData(String fileName) throws IOException {
		RandomAccessFile raf = new RandomAccessFile(fileName, "r");
		int success = 0;
		int total = 0;
		while (true) {
			Map<String, Object> resMap = new HashMap<String, Object>();
			try {
				Map<String, Object> taskData = loadTaskData(raf);
				if (taskData == null || taskData.get("length") == null) {
					break;
				}
				String len = (String) taskData.get("length");
				String jsonLen = (String) taskData.get("jsonlength");
				Map<String, Object> spiderData = loadSpiderData(raf, Integer.valueOf(len),
						StringUtils.isEmpty(jsonLen) ? null : Integer.valueOf(jsonLen));
				resMap.put("taskdata", taskData);
				resMap.put("spiderdata", spiderData);
			} catch (Exception e) {
				LOG.warn("exception, err", e);
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

	private Map<String, Object> loadSpiderData(RandomAccessFile raf, Integer len, Integer jsonlen) throws Exception {
		Map<String, Object> spiderData = new HashMap<String, Object>();
		int datalen = len;
		if (jsonlen != null) {
			datalen = len - jsonlen;
		}
		byte[] bs = new byte[datalen];
		int read = raf.read(bs);
		String data = new String(bs, "utf8");
		spiderData.put("data", data);
		spiderData.put("httpcode", "200");
		spiderData.put("ajaxdata", null);
		spiderData.put("charset", "utf8");
		if (jsonlen != null) {
			byte[] jsonbs = new byte[jsonlen];
			raf.read(jsonbs);
			String ajaxStr = new String(bs, "utf8");
			List<Map<String, Object>> ajaxData = (List<Map<String, Object>>) JsonUtil.parseObject(ajaxStr);
			spiderData.put("ajaxdata", ajaxData);
		}
		return spiderData;
	}

	private Map<String, Object> loadTaskData(RandomAccessFile raf) throws Exception {
		Map<String, Object> taskData = new HashMap<String, Object>();
		boolean flag = true;
		while (flag) {
			String taskInfo = raf.readLine();
			if (taskInfo == null) {
				return null;
			}
			taskInfo = new String(taskInfo.getBytes("8859_1"), "utf8");
			if (taskInfo.startsWith("length:")) {
				taskData.put("length", taskInfo.split(":")[1]);
				break;
			}
			wrapTaskData(taskInfo, taskData);
		}
		return taskData;
	}

	private static void wrapTaskData(String taskInfo, Map<String, Object> taskData) {
		int index = taskInfo.indexOf(':');
		String key = taskInfo.substring(0, index);
		Object value = null;
		String valuestr = taskInfo.substring(index + 1);
		if ("category".equalsIgnoreCase(key)) {
			try {
				if (valuestr.startsWith("[")) {
					value = Arrays.asList(valuestr.substring(1, valuestr.length() - 1).split(","));
				}
			} catch (Exception e) {
				LOG.warn("exception", e);
			}
		} else {
			value = valuestr;
		}
		taskData.put(key, value);
	}

	@Override
	public void run() {
		for (String fileName : fileNames) {
			try {
				loadData(fileName);
			} catch (FileNotFoundException e) {
				LOG.warn("exception, ", e);
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static boolean loadFileData(String path, String suffix, Set<String> cids) {
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
						SourceDataLoader loader = new SourceDataLoader(fileNames, "loadworker-1", cids);
						new Thread(loader).start();
						return true;
					}
					LOG.warn("file size is 0, dir=" + path);
					return false;
				} else if (file.isFile() && file.isAbsolute()) {
					List<String> files = new ArrayList<String>();
					files.add(path);
					SourceDataLoader loader = new SourceDataLoader(files, "loadworker-1", cids);
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
		String path = null;
		String suffix = null;
		String cid = null;
		for (int i = 0; i < args.length; i++) {
			if (args[i].equalsIgnoreCase("-f") && i < args.length - 1) {
				path = args[++i];
			} else if (args[i].equalsIgnoreCase("-s") && i < args.length - 1) {
				suffix = args[++i];
			} else if (args[i].equalsIgnoreCase("-c") && i < args.length - 1) {
				cid = args[++i];
			} else if (args[i].equalsIgnoreCase("-h") || args[i].equalsIgnoreCase("--help")
					|| args[i].equalsIgnoreCase("-?")) {
				printHelpTips();
				return;
			}
		}
		if (args.length < 1 || StringUtils.isEmpty(path)) {
			LOG.error("Error Args: file or dir path is required.");
			printHelpTips();
			return;
		}
		Set<String> cids = new HashSet<String>();
		if (cid.contains(",")) {
			cids.addAll(Arrays.asList(cid.split(",")));
		} else {
			cids.add(cid);
		}
		startSynchConfigTask();
		startFileParseTask(10);
		LOG.info("receive file parse task, path=" + path);
		if (!SourceDataLoader.loadFileData(path, suffix, cids)) {
			LOG.error("load file error, path=" + path);
			System.exit(-1);
		}
	}

	private static void printHelpTips() {
		System.out.println("usage: cmd [-options option-value]");
		System.out.println("    -f : file or direcotry to be parse, required argument");
		System.out.println("    -s : suffix of file to be parse, default is all files");
		System.out.println("    -c : cids to be parse.");
		System.out.println("    -h : print the help tips");
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
