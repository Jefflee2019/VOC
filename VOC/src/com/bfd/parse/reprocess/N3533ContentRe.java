package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

/**
 * 站点：N3533
 * 功能：内容页后处理
 * @author dph 2017年11月24日
 *
 */
public class N3533ContentRe implements ReProcessor{
	private static final Pattern PATTERN_DATE = Pattern.compile("20\\d{4}");
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace parseFace) {
		Map<String,Object> resultData = result.getParsedata().getData();
		Map<String, Object> processdata = new HashMap<String, Object>();
		String author = null;
		String source = null;
		//"author": "编辑：JQ 来源：手机世界", 
		if(resultData.containsKey(Constants.AUTHOR)){
			author = (String) resultData.get(Constants.AUTHOR);
		}
		if(resultData.containsKey(Constants.SOURCE)){
			source = (String) resultData.get(Constants.SOURCE);
		}
		if(null != author){
			author = author.replace(source, "").trim();
			author = author.replace("编辑：", "").trim();
			resultData.put(Constants.AUTHOR, author);
		}
		//http://www.3533.com/news/16/201711/167843/1.htm
		// "post_time": "11月 23"
		//从URL中获取年份
		String url = unit.getUrl();
		Matcher dateM = PATTERN_DATE.matcher(url);
		String date = null;
		if(dateM.find()){
			date = dateM.group(0);
			date = date.substring(0, 4);
		}
		String postTime = (String) resultData.get(Constants.POST_TIME);
		if(null != date){
			postTime = date + "-" + postTime.replace("月 ", "-").trim();
			resultData.put(Constants.POST_TIME, postTime);
		}
		resultData.remove(Constants.SOURCE);
		ParseUtils.getIid(unit, result);
	
		return new ReProcessResult(SUCCESS, processdata);
	}

}
