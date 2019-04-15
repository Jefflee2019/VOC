package com.bfd.parse.reprocess;

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
 * 站点名：Bmydigit
 * 
 * 功能：给列表页第一页中的帖子链接加上fpage字段
 * 
 * @author bfd_06
 */
public class BmydigitListRe implements ReProcessor {
	
	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		int pageNum = matchPageNum("page=(\\d+)", unit.getUrl());
		if(pageNum <= 1){
			// items 添加fpage标志
			List<Map<String,Object>> items = (List<Map<String, Object>>) resultData.get("items");
			for(Map<String,Object> item:items){
				Map<String,String> itemlink = (Map<String,String>) item.get("itemlink");
				String link = itemlink.get("link");
				String rawlink = itemlink.get("rawlink");
				link += "&fpage=1";
				rawlink += "&fpage=1";
				itemlink.put("link", link);
				itemlink.put("rawlink", rawlink);
			}
			// tasks 添加fpage标志
			List<Map<String,String>> tasks = (List<Map<String, String>>) resultData.get("tasks");
			for(Map<String,String> task:tasks){
				String linktype = task.get("linktype");
				if(linktype.equals("bbspostlist"))
					continue;
				String link = task.get("link");
				String rawlink = task.get("rawlink");
				link += "&fpage=1";
				rawlink += "&fpage=1";
				task.put("link", link);
				task.put("rawlink", rawlink);
			}
		}
		
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(processcode, processdata);
	}
	
	public int matchPageNum(String regular, String matchedStr) {
		Pattern patten = Pattern.compile(regular);
		Matcher matcher = patten.matcher(matchedStr);
		if (matcher.find()) {
			return Integer.parseInt(matcher.group(1));
		}

		return 0;
	}
	
}
