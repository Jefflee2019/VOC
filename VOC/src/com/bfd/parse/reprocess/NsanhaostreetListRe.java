package com.bfd.parse.reprocess;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
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
 * @site：网上三好网
 * @function：处理下一页
 * @author huangzecheng 2016-11-28
 *
 */
public class NsanhaostreetListRe implements ReProcessor {
	
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		// 取到解析结果
		Map<String, Object> resultData = result.getParsedata().getData();
		String pageData = unit.getPageData();
		if (resultData != null && !resultData.isEmpty()) {
			String url = unit.getUrl();
			 if (url.contains("search.sanhaostreet.com")) {
				// 处理下一页链接
				Map<String, Object> nextpageTask = new HashMap<String, Object>();
				String oldPageNum = getPage(url);
				int pageNum =Integer.valueOf(oldPageNum);
				String nextpage=null;
				
				if (pageNum>0||pageNum==0) {
					 pageNum = Integer.valueOf(oldPageNum) + 1;
					 nextpage = url.replace("&page=" + oldPageNum, "&page=" + pageNum);
				}
				else{
					new ReProcessResult(processcode, processdata);
				 }
				
				if(pageNum<75){
					nextpageTask.put("link", nextpage);
					nextpageTask.put("rawlink", nextpage);
					nextpageTask.put("linktype", "newslist");
					resultData.put("nextpage", nextpage);
					List<Map> tasks = (List<Map>) resultData.get("tasks");
					tasks.add(nextpageTask);
					List<Map> items = (List<Map>) resultData.get("items");
					items.add(nextpageTask);
				}
			}
		}
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(processcode, processdata);
	}

	private String getPage(String url) {
		if (url.contains("search.sanhaostreet.com")) {
			Pattern iidPatter = Pattern.compile("&page=(\\d+)");
			Matcher match = iidPatter.matcher(url);
			if (match.find()) {
				return match.group(1);
			} else {
				return "0";
			}
		}
		return "0";
	}
}
