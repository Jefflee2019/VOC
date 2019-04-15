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
import com.bfd.parse.entity.Constants;

/**
 * 站点名：Ncnwnews
 * 
 * 标准化部分字段以及给出评论页链接
 * 
 * @author bfd_06
 */
public class NcnwnewsContentRe implements ReProcessor {

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		Map<String, Object> parseData = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		if(resultData.containsKey(Constants.POST_TIME)){
			String post_time = resultData.get(Constants.POST_TIME).toString();
			String reg = "(\\d{4}年\\d{2}月\\d{2}日 \\S+:\\d{2})";
			post_time = this.getCresult(post_time, reg);
			resultData.put(Constants.POST_TIME, post_time.trim().replace("年", "-").replace("月", "-").replace("日", ""));
		}
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, parseData);
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
