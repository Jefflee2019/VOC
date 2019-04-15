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
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

/**
 * @author bfd_02
 *
 */

public class Eparis_chileListRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(Eparis_chileListRe.class);

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
		 * @param nextpage
		 * @function 拼接下一页任务
		 */
//		https://www.paris.cl/tecnologia/celulares/smartphones/?sz=40&start=40
		String url = unit.getUrl();
		int total_cnt = Integer.parseInt((String) resultData.get("total_cnt"));
		
		if (total_cnt > 40) {
			String nextpage = "";
			if (url.contains("start")) {
				int start = Integer.parseInt(this.getCresult(url, "start=(\\d+)"));
				if (total_cnt - start > 40  ) {
					nextpage = url.replaceAll("start=" + start,"start=" + (start + 40));
					getNextpage(resultData, nextpage);
				}
			} else {
				//第二页
				nextpage = url.concat("?sz=40&start=40");
				getNextpage(resultData, nextpage);
			}
		}

		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
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

	private void getNextpage(Map<String, Object> resultData, String nextpage) {
		resultData.put(Constants.NEXTPAGE, nextpage);

		Map<String, Object> nextpageTask = new HashMap<String, Object>();
		nextpageTask.put("link", nextpage);
		nextpageTask.put("rawlink", nextpage);
		nextpageTask.put("linktype", "eclist");

		List<Map<String, Object>> tasks = (List<Map<String, Object>>) resultData.get(Constants.TASKS);
		tasks.add(nextpageTask);
	}
	
}