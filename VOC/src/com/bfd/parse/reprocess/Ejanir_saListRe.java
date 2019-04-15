package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

/**
 * @author bfd_02
 *
 */

public class Ejanir_saListRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(Ejanir_saListRe.class);

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = new HashMap<String, Object>();

		// 解析数据
		resultData = result.getParsedata().getData();
		if (resultData == null) {
			LOG.warn("未找到解析数据");
			return null;
		}
		
		/**
		 * 
		 */
		List<Map<String, Object>> items = (List<Map<String, Object>>) resultData.get("items");
		if (items.size() == 20) {
			//下一页https://www.jarir.com/sa-en/smartphones-accessories/honor.html?is_ajax=1&p=2&is_scroll=0
			String url = unit.getUrl();
			String nextPage = url.replaceAll(getCresult1(url, "&p=(\\d+)"), "&p=" + (Integer.parseInt(getCresult(url, "&p=(\\d+)")) + 1));
			initTask(resultData, nextPage);
		}

		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
	
	public static void main(String[] args) {
		Ejanir_saListRe ejanir_saListRe = new Ejanir_saListRe();
		String url = "https://www.jarir.com/sa-en/shopby?brand=239875&cat=12&is_ajax=1&p=1&is_scroll=0";
		String nextPage = url.replaceAll(ejanir_saListRe.getCresult1(url, "&p=(\\d+)"), "&p=" + (Integer.parseInt(ejanir_saListRe.getCresult(url, "&p=(\\d+)")) + 1));
		System.err.println(nextPage);
	}
	
	/**
	 * 正则匹配字符串
	 * @param str
	 * @param pattern
	 * @return
	 */
	private String getCresult(String str,String reg){
		Pattern pattern = Pattern.compile(reg);
		Matcher mch = pattern.matcher(str);
		if(mch.find()){
			return mch.group(1);
		}
		return str;
	}
	/**
	 * 正则匹配字符串
	 * @param str
	 * @param pattern
	 * @return
	 */
	private String getCresult1(String str,String reg){
		Pattern pattern = Pattern.compile(reg);
		Matcher mch = pattern.matcher(str);
		if(mch.find()){
			return mch.group(0);
		}
		return str;
	}
	
	/**
	 * 组装task
	 * @param parsedata
	 * @param nextPage
	 */
	@SuppressWarnings("unchecked")
	private void initTask(Map<String, Object> parsedata, String nextPage) {
		List<Map<String, Object>> taskList = (List<Map<String, Object>>) parsedata.get("tasks");
		Map<String, Object> nextpageTask = new HashMap<String, Object>();
		nextpageTask.put("link", nextPage);
		nextpageTask.put("rawlink", nextPage);
		nextpageTask.put("linktype", "eclist");
		
		taskList.add(nextpageTask);
		parsedata.put("nextpage", nextPage);
		parsedata.put("tasks", taskList);
	}

}