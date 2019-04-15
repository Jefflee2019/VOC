package com.bfd.parse.reprocess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

/**
 * 
 * @author 08
 *
 */
public class NsohuContentRe implements ReProcessor{

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		List<Map<String, Object>> _tasks = new ArrayList<Map<String, Object>>();
		resultData.put("tasks", _tasks);
		String getPageData = unit.getPageData();
		String url = unit.getUrl();
		String tempurl = "http://apiv2.sohu.com/api/topic/load?page_size=10&page_no=1&hot_size=5&source_id=mp_news_id";
		if (getPageData.contains("news_id")) {
			//拼接评论页链接
			String news_id = this.getCresult(getPageData, "news_id: \"(.*)\"");
			String commenturl = tempurl.replaceAll("news_id", news_id);
			List tasks = (List) resultData.get("tasks");
			Map<String, Object> task = new HashMap<String, Object>();
			task.put("link", commenturl);
			task.put("rawlink", commenturl);
			task.put("linktype", "newscomment");
			resultData.put("comment_url", commenturl);
			tasks.add(task);
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

}
