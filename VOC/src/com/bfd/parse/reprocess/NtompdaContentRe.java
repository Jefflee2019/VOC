package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.ConstantFunc;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

/**
 * 
 * @author 08
 *
 */
public class NtompdaContentRe implements ReProcessor{

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		String url = unit.getUrl();
		if (resultData != null) {
			//post_time TomPDA.com 作者: TomPDA 2017年03月24日"
			if(resultData.containsKey(Constants.POST_TIME) && resultData.get(Constants.POST_TIME) != ""){
				String post_time = resultData.get(Constants.POST_TIME).toString();
				String reg = "(\\d{4}年\\d{2}月\\d{2}日)";
				post_time = this.getCresult(post_time, reg);
				resultData.put(Constants.POST_TIME, ConstantFunc.getDate(post_time));
			}
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
