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

public class NsohutvListRe implements ReProcessor {
	
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		String url = unit.getUrl(); // 列表页搜索链接
		if (resultData != null && resultData.containsKey("next_page")) {
			// 处理下一页链接
			String nextpage = (String)resultData.get("next_page"); // 临时存放下一页信息
			if(nextpage != null && nextpage.contains("下一页")) { // 包含‘下一页’说明还没有到达最后一页
				Map<String, String> nextpageTask = new HashMap<String, String>();
				String oldPageNum = getPage(url);
				if (oldPageNum.equals("0")) { // 可能列表页第一页没有给出页数
					nextpage = url + "&p=2";
				} else {
					int pageNum = Integer.valueOf(oldPageNum) + 1;
					nextpage = url.replace("&p=" + oldPageNum, "&p=" + pageNum);
				}
				@SuppressWarnings("unchecked")
				List<Object> tasks = (List<Object>) resultData.get(Constants.TASKS); // 获取抓取任务集合
				nextpageTask.put("link", nextpage);
				nextpageTask.put("rawlink", nextpage);
				nextpageTask.put("linktype", "newslist");
				resultData.put("nextpage", nextpage);
				tasks.add(nextpageTask);	// 添加下一页任务
			}
		}
		return new ReProcessResult(processcode, processdata);
	}

	//获取页数
	private String getPage(String url) {
		Pattern iidPatter = Pattern.compile("&p=(\\d+)");
		Matcher match = iidPatter.matcher(url);
		if (match.find()) {
			return match.group(1);
		} else {
			return "0";
		}
	}
}