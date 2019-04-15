package com.bfd.parse.task;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.JsonUtil;

/**
 * @author yanhui.ji
 * 
 */
public class ParseJob {

	private static final Log LOG = LogFactory.getLog(ParseJob.class);

	public static final int TYPE_FILE = 1;
	public static final int TYPE_DATA = 0;

	private List<ParseUnit> units;
	private FileParseUnit fileParseInfo;
	private int type = 0;

	public ParseJob(String filename, String resfile, String rsOptoin, String prefix, String bid, String charset,
			String type) {
		this(filename, resfile, rsOptoin, prefix, bid, charset, type, null);
	}

	public ParseJob(String filename, String resfile, String rsOptoin, String prefix, String bid, String charset,
			String type, Map<String, String> config) {
		fileParseInfo = new FileParseUnit(filename, resfile, rsOptoin, prefix, bid, charset, type, config);
		this.type = TYPE_FILE;
	}

	public ParseJob(int type) {
		this();
		this.type = type;
	}

	public ParseJob() {
		units = new ArrayList<ParseUnit>();
	}

	public ParseJob(List<ParseUnit> unitList) {
		if (unitList != null) {
			units = unitList;
		} else {
			units = new ArrayList<ParseUnit>();
		}
	}

	public void AddJobUnit(ParseUnit unit) {
		if (unit == null)
			return;
		if (units == null) {
			units = new ArrayList<ParseUnit>();
		}
		units.add(unit);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static ParseJob fromDataJsonStr(String request) throws Exception {
		List<ParseUnit> unitList = new ArrayList<ParseUnit>();
		List<Object> objs = JsonUtil.parseArray(request);
		long time = System.currentTimeMillis();
		if (objs instanceof List) {
			for (Object o : objs) {
				Map<String, Object> u = (Map<String, Object>) o;
				ParseUnit unit = ParseUnit.fromMap(u, time);
				if (unit != null)
					unitList.add(unit);
				else
					LOG.info("WARNING: Parse from request, parse unit is null.");
			}
		}
		return new ParseJob(unitList);
	}

	@SuppressWarnings("unchecked")
	public static ParseJob fromFileJsonStr(String request) throws Exception {
		Object obj = JsonUtil.parseObject(request);
		Map<String, Object> map = (Map<String, Object>) obj;
		if (!map.containsKey("filename") || !map.containsKey("resfile")) {
			return null;
		}
		String filename = (String) map.get("filename");
		String resfile = (String) map.get("resfile");
		String rsOptoin = null;
		String charset = null;
		String bid = null;
		String type = null;
		String prefix = null;
		if (map.containsKey("rsOptoin")) {
			rsOptoin = (String) map.get("rsOptoin");
		}
		if (map.containsKey("charset")) {
			charset = (String) map.get("charset");
		}
		if (map.containsKey("bid")) {
			bid = String.valueOf(map.get("bid"));
		}
		if (map.containsKey("type")) {
			type = (String) map.get("type");
		}

		if (map.containsKey("prefix")) {
			prefix = (String) map.get("prefix");
		}
		if (map.containsKey("config")) {
			Object o = map.get("config");
			Map<String, String> config = (Map<String, String>) o;
			ParseJob job = new ParseJob(filename, resfile, rsOptoin, prefix, bid, charset, type, config);
			return job;
		}
		ParseJob job = new ParseJob(filename, resfile, rsOptoin, prefix, bid, charset, type);
		return job;
	}

	public static class FileParseUnit {

		private String filename;
		private String resfile;
		private String rsOptoin;
		private String charset;
		private String type;
		private String bid;
		private String prefix;
		private Map<String, String> outputFlds;

		public FileParseUnit(String filename, String resfile, String rsOptoin, String prefix, String bid,
				String charset, String type, Map<String, String> outputFlds) {
			this.filename = filename;
			this.resfile = resfile;
			this.rsOptoin = rsOptoin;
			this.outputFlds = outputFlds;
			this.charset = charset;
			this.type = type;
			this.bid = bid;
			this.prefix = prefix;
		}

		public String getFilename() {
			return filename;
		}

		public void setFilename(String filename) {
			this.filename = filename;
		}

		public String getResfile() {
			return resfile;
		}

		public void setResfile(String resfile) {
			this.resfile = resfile;
		}

		public String getRsOptoin() {
			return rsOptoin;
		}

		public void setRsOptoin(String rsOptoin) {
			this.rsOptoin = rsOptoin;
		}

		public Map<String, String> getOutPutFlds() {
			return outputFlds;
		}

		public void setOutputFlds(Map<String, String> outputFlds) {
			this.outputFlds = outputFlds;
		}

		public void setCharset(String charset) {
			this.charset = charset;
		}

		public String getCharset() {
			return charset;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public String getBid() {
			return bid;
		}

		public void setBid(String bid) {
			this.bid = bid;
		}

		public String getPrefix() {
			return prefix;
		}

		public void setPrefix(String prefix) {
			this.prefix = prefix;
		}
	}

	public List<ParseUnit> getUnits() {
		return units;
	}

	public void setUnits(List<ParseUnit> units) {
		this.units = units;
	}

	public int size() {
		if (units != null)
			return units.size();
		return 0;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public FileParseUnit getFileParseInfo() {
		return fileParseInfo;
	}

	public void setFileParseInfo(FileParseUnit fileParseInfo) {
		this.fileParseInfo = fileParseInfo;
	}
}
