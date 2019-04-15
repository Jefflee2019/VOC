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
 * 站点名：天涯博客
 * 
 * 给出下一页
 * 
 * @author bfd_06
 */
public class NtianyaListRe implements ReProcessor {
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		String url = unit.getUrl();
		if(haveNext("'>下一页</a>",unit.getPageData())){
			int pageNum = matchPageNum("&pn=(\\d+)",url);
			addNextUrl(url, pageNum, resultData, unit, result);
			ParseUtils.getIid(unit, result);
		}
		
		return new ReProcessResult(processcode, processdata);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void addNextUrl(String url, int pageNum,
			Map<String, Object> resultData, ParseUnit unit, ParseResult result) {
			if (pageNum < 75) {
				List<Map> tasks = (List<Map>) resultData.get("tasks");
				Map<String, Object> task = new HashMap<String, Object>();
				String nextUrl = null;
				if(pageNum==1)
					nextUrl = url.replace("&s=6", "&pn=2&s=6");
				else
					nextUrl = url.replace("&pn=" + pageNum, "&pn="
							+ (pageNum + 1));
				task.put("link", nextUrl);
				task.put("rawlink", nextUrl);
				task.put("linktype", "newslist");
				resultData.put("nextpage", nextUrl);
				tasks.add(task);
			}
	}

	public int matchPageNum(String regular, String matchedStr) {
		Pattern patten = Pattern.compile(regular);
		Matcher matcher = patten.matcher(matchedStr);
		if (matcher.find()) {
			return Integer.parseInt(matcher.group(1));
		}
		
		return 1;
	}

	public Boolean haveNext(String regular, String matchedStr) {
		Pattern patten = Pattern.compile(regular);
		Matcher matcher = patten.matcher(matchedStr);
		if (matcher.find()) {
			return true;
		}
		
		return false;
	}
	
}
