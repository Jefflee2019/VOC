package com.bfd.parse.reprocess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

public class NfengContentRe implements ReProcessor{

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		String url = unit.getUrl();
//		http://www.feng.com/iPhone/news/2016-09-08/More-durable-more-independent-of-the-second-generation-Apple-Watch-debut_656478.shtml
		String article_id = this.getCresult(url, "(\\d*).shtml");
		if (resultData != null) {
			Map<String, Object> commentTask = new HashMap<>();
			String commUrl = "http://www.feng.com/publish/comment_v3.php?act=hot&article_id=tempid&page=1&page_num=10";
			commUrl = commUrl.replace("tempid", article_id);
			commentTask.put(Constants.LINK, commUrl);
			commentTask.put(Constants.RAWLINK, commUrl);
			commentTask.put(Constants.LINKTYPE, "newscomment");
			List<Map<String, Object>> tasks = null;
			if(resultData.containsKey("tasks")){
				tasks = (List<Map<String, Object>>) resultData.get("tasks");
			} else {
				tasks = new ArrayList<>();
				resultData.put(Constants.TASKS, tasks);
			}
			tasks.add(commentTask);
			resultData.put(Constants.COMMENT_URL, commUrl);
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
