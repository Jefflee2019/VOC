package com.bfd.parse.reprocess;

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

public class NcaijingContentRe implements ReProcessor{

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		
		String pageData = unit.getPageData();
		if(resultData.containsKey(Constants.SOURCE)){
			String source = resultData.get(Constants.SOURCE).toString();
			source = source.replace("来源：", "");
			resultData.put(Constants.SOURCE, source);
		}
		Pattern p = Pattern.compile("var topicid = '(\\d+)'");
		Matcher mch = p.matcher(pageData);
		if(mch.find()){
			String topicsID = mch.group(1);
			Map<String, Object> commentTask = new HashMap<>();
			String commUrl = "http://app.caijing.com.cn/?app=comment&controller=review&action=page&all=1&page=1&pagesize=10&topicid=%s";
			commUrl = String.format(commUrl, topicsID);
			commentTask.put(Constants.LINK, commUrl);
			commentTask.put(Constants.RAWLINK, commUrl);
			commentTask.put(Constants.LINKTYPE, "newscomment");
			List<Map<String,Object>> tasks = (List<Map<String, Object>>) resultData.get(Constants.TASKS);
			tasks.add(commentTask);
			resultData.put(Constants.COMMENT_URL, commUrl);
		}
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}

}
