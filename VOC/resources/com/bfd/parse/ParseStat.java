package com.bfd.parse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

//import sun.util.logging.resources.logging;

import com.bfd.crawler.kafka7.KfkProducer;
import com.bfd.crawler.utils.ConfigUtils;
import com.bfd.crawler.utils.JsonUtils;
import com.bfd.parse.util.JsonUtil;
import com.bfd.parse.util.KfkUtils;

public class ParseStat {

	private static final Log LOG = LogFactory.getLog(ParseStat.class);
	private Map<StatItem, Long> statMap;
	private String workName;
	private static long reportStatTime;
	private static String statTopicName ;

	static {
		reportStatTime = ConfigUtils.getInstance().getLongProp("StatReport.time", 120000);
		statTopicName = ConfigUtils.getInstance().getProp("crawl.public.statistics.kafka.topic", "statistics");
	}
	private long time;

	public ParseStat(String name) {
		statMap = new HashMap<StatItem, Long>();
		time = System.currentTimeMillis();
		workName = name;
	}

	private boolean timeIsUp() {
		return System.currentTimeMillis() - time >= reportStatTime && statMap.size() != 0;
	}

	/**
	 * 上报统计信息到统计服务
	 */
	public void report() {
//		List<StatItem> list = new ArrayList<StatItem>();
//		boolean success = true;
		for (StatItem stat : statMap.keySet()) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("cid", stat.getCid());
			map.put("pagetype", stat.getType());
			map.put("code", String.valueOf(stat.getParseCode()));
			map.put("projectname", stat.getProjectName());
			map.put("count", statMap.get(stat));
			map.put("stattime", ""+(System.currentTimeMillis()/1000));
			map.put("host", Constants.host);
			
			Map<String, Object> statData = new HashMap<String, Object>();
			statData.put("data", map);
			statData.put("procname", "parse");
			statData.put("stattype", "parseres");
//			LOG.debug(workName + " report stat info to StatService :" + JsonUtil.toJSONString(map));
			//TODO:提交到保存kakfa里面,如果提交kafka失败,怎么处理?
//			if (success == false || !statServiceClient.reportData("parse", "parseres", JsonUtil.toJSONString(map))) {
//				success = false;
//				list.add(stat);
//			}
			LOG.info("statTopicName:"+statTopicName);
			KfkUtils.sendKfk(statTopicName, JsonUtils.toJSONString(statData));
		}
//		if (success) {
			LOG.info(workName + " reported stat success, stat size=" + statMap.size());
			LOG.debug("Reported statMap=" + JsonUtil.toJSONString(statMap));
			statMap.clear();
//		} else {
//			LOG.warn(workName + " reported stat failed, statMap will remain failed stat key="
//					+ JsonUtil.toJSONString(list));
//			Map<StatItem, Long> map = new HashMap<StatItem, Long>();
//			for (StatItem stat : list) {
//				map.put(stat, statMap.get(stat));
//			}
//			statMap = null;
//			statMap = map;
//		}
		time = System.currentTimeMillis();
	}

	/**
	 * 统计计数+1
	 * 
	 * @param projectname
	 * @param cid
	 * @param type
	 * @param parsecode
	 */
	public void increment(String projectname, String cid, String type
										, int parsecode) {
		try {
			StatItem statUnit = new StatItem(projectname, cid, type, parsecode);
			if (statMap.get(statUnit) == null) {
				statMap.put(statUnit, 1L);
				return;
			}
			statMap.put(statUnit, statMap.get(statUnit) + 1);
		} catch (Exception e) {
			LOG.warn(workName + " increment exception, ", e);
		}
		if (timeIsUp()) {
			report();
		}
	}


	class StatItem {
		private String projectName;
		private String cid;
		private String type;
		private int parseCode; // 0->success, 1->no template, 2->parse failed

		public StatItem(String projectName, String cid, String type, int parseCode) {
			this.projectName = projectName;
			this.cid = cid;
			this.type = type;
			this.parseCode = parseCode;
		}

		public String getCid() {
			return cid;
		}

		public void setCid(String cid) {
			this.cid = cid;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public int getParseCode() {
			return parseCode;
		}

		public void setParseCode(int parsecode) {
			this.parseCode = parsecode;
		}

		public String getProjectName() {
			return projectName;
		}

		public void setProjectName(String projectName) {
			this.projectName = projectName;
		}

		@Override
		public int hashCode() {
			int result = (getCid() == null) ? 17 : getCid().hashCode();
			result = (getProjectName() == null) ? result * 11 : result * 11 + getProjectName().hashCode();
			result = (getType() == null) ? result * 7 : result * 7 + getType().hashCode();
			result = result * 13 + getParseCode();
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj instanceof StatItem) {
				StatItem su = (StatItem) obj;
				return getCid().equals(su.getCid()) && getType().equals(su.getType())
						&& getParseCode() == su.getParseCode() && getProjectName().equals(su.getProjectName());
			}
			return false;
		}

	}

}
