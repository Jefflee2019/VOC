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
 * 站点名：Nsinablog
 * 
 * 主要功能：给出列表页下一页
 * 
 * @author bfd_06
 */
public class NsinablogListRe implements ReProcessor {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;

		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		String url = unit.getUrl();
		int pageNum = matchPageNum("&page=(\\d+)", url);
		if (pageNum<100) {
			String nextUrl = url.replace("&page=" + pageNum, "&page="
					+ (pageNum + 1));
			if(pageNum==-1){
				nextUrl = url + "&page=2";
			}
			Map<String, Object> task = new HashMap<String, Object>();
			task.put("link", nextUrl);
			task.put("rawlink", nextUrl);
			task.put("linktype", "newslist");
			resultData.put("nextpage", nextUrl);
			List<Map> tasks = (List<Map>) resultData.get("tasks");
			tasks.add(task);
			ParseUtils.getIid(unit, result);
		}

		return new ReProcessResult(processcode, processdata);
	}

	public int matchPageNum(String regular, String matchedStr) {
		Pattern patten = Pattern.compile(regular);
		Matcher matcher = patten.matcher(matchedStr);
		if (matcher.find()) {
			return Integer.parseInt(matcher.group(1));
		}
		return -1;
	}

}
