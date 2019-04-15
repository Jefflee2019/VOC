package com.bfd.parse;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.crawler.utils.JsonUtils;
import com.bfd.parse.facade.parseunit.ParseUnit;

public class ParseResult implements Serializable{

	private static final Log LOG = LogFactory.getLog(ParseResult.class);

	
	public static final int SUCCESS = 0;
	public static final int NOFOUND_TEMPLATE = 500001;
	public static final int NOFOUND_PREPROCESSOR = 500002;
	public static final int nofound_reprocessor = 500003;
	public static final int nofound_jsonprocessor = 500004;
	public static final int needLogin = 500005;
	public static final int nohtmldata = 500006;
	public static final int notFoundField = 500007;
	public static final int GETIID_FAILED = 500008;
	public static final int FAILED = 500009;
	public static final int preprocessor_fail = 500010;
	public static final int REPROCESS_FAILED = 500011;
	public static final int jsonprocess_FAILED = 500012;
	public static final int OFF_SHELF = 500016;
	public static final int PARSECODE_DWONLOADFAILED = 500018;
	public static final int cookie_nouse = 500019;
	public static final int uncompress_fail = 50020;
	public static final int nofound_sitepageconfig = 500021;
	public static final int jsonparseerror = 500022;
	public static final int weiboparse_error = 500023;
	
	public static final int REPROCESS_NONESAVE = -3;// 不保存，直接返回，以后不再调用
	public static final int NOT_ITEM_TASK = -4;
	public static final int AUTO_PARSE_FAILED = -5;
	
//	public static final int FAILED_NO = "002";
	
	
	
	
	
	
	
//	public static final int NOFIND_REPROCEE_PLUGIN = 7;

	private Map<String, Object> taskdata;
	private Map<String, Object> spiderdata;
	private ParseData parsedata;

	public static class ParseData implements Serializable{

		private int parsecode;
		private long begintime;
		private long parsebegintime;
		private long endtime;
		private String errMsg;
		
		

		public String getErrMsg() {
			return errMsg;
		}

		public void setErrMsg(String errMsg) {
			this.errMsg = errMsg;
		}

		private Map<String, Object> data;

		public ParseData() {
			data = new HashMap<String, Object>();
		}

		public int getParsecode() {
			return parsecode;
		}

//		public void mergeParsecode(String parsecode) {
//			this.parsecode = parsecode | this.parsecode;
//		}

		public void setParsecode(int parsecode) {
			this.parsecode = parsecode;
		}

		public long getBegintime() {
			return begintime;
		}

		public void setBegintime(long begintime) {
			this.begintime = begintime;
		}

		public long getEndtime() {
			return endtime;
		}

		public void setEndtime(long endtime) {
			this.endtime = endtime;
		}

		public Map<String, Object> getData() {
			return data;
		}

		public void setData(Map<String, Object> data) {
			this.data = data;
		}

		public long getParsebegintime() {
			return parsebegintime;
		}

		public void setParsebegintime(long parsebegintime) {
			this.parsebegintime = parsebegintime;
		}

		public void addData(Map<String, Object> data) {
			for (String key : data.keySet()) {
				if (this.data.containsKey(key)) {
					Object o1 = this.data.get(key);
					Object o2 = data.get(key);
					if (o1 instanceof List && o2 instanceof List) {
						List list = new ArrayList();
						list.addAll((Collection) o1);
						list.addAll((Collection) o2);
						LOG.debug("parseResut addData method merged list1.size=" + ((List) o1).size() + ", list2.size="
								+ ((List) o2).size() + ", key=" + key + ",list.size=" + list.size());
						this.data.put(key, list);
					} else {
						this.data.put(key, data.get(key));
					}
				} else {
					this.data.put(key, data.get(key));
				}
			}
		}

		public static void main(String[] args) {
			ParseData pd = new ParseData();
			Map<String, Object> data2 = pd.getData();
			ArrayList<String> list = new ArrayList<String>();
			list.add("x");
			list.add("y");
			list.add("z");
			data2.put("a", "a");
			data2.put("item", list);

			System.out.println(pd.getData());

			HashMap<String, Object> map = new HashMap<String, Object>();
			ArrayList<String> list2 = new ArrayList<String>();
			list2.add("o");
			list2.add("p");
			list2.add("q");
			map.put("item", list2);
			map.put("a", "b");

			pd.addData(map);
			System.out.println(pd.getData());
		}

		public void putData(String key, Object value) {
			this.data.put(key, value);
		}
	}

	public static ParseResult prepareObj(ParseUnit unit) {
		ParseResult result = new ParseResult();
		try {
			result.setTaskdata(new HashMap(unit.getTaskdata()));
			Map<String, Object> spiderdata = new HashMap<String, Object>();
			spiderdata.putAll(unit.getSpiderdata());
			if (spiderdata.containsKey("ajaxdata")) {
				spiderdata.remove("ajaxdata");
			}
			if (spiderdata.containsKey("data")) {
				spiderdata.remove("data");
			}
			result.setSpiderdata(spiderdata);
			result.setParsedata(new ParseData());
			if (unit.getStartTime() != null) {
				result.getParsedata().getData().put("start_time", unit.getStartTime());
			}
			result.getParsedata().setBegintime(unit.getBegintime());
		} catch (Exception e) {
			LOG.warn(e);
		}
		return result;
	}

	public void setSpiderdata(Map<String, Object> spiderdata) {
		this.spiderdata = spiderdata;
	}

	public Map<String, Object> getSpiderdata() {
		return spiderdata;
	}

	public void setTaskdata(Map<String, Object> taskdata) {
		this.taskdata = taskdata;
	}

	public Map<String, Object> getTaskdata() {
		return taskdata;
	}

	public void setParsedata(ParseData parsedata) {
		this.parsedata = parsedata;
	}

	public ParseData getParsedata() {
		return parsedata;
	}

	public static void main(String[] args) {
		ParseResult pr = new ParseResult();
		ParseData data = new ParseData();
		pr.setParsedata(data);
		data.putData("type", "");
		data.putData("nextpage", "");
		data.putData("multipage", "");
		System.out.println(JsonUtils.toJSONString(pr));
	}
}
