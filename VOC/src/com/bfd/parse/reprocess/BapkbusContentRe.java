package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.ConstantFunc;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

public class BapkbusContentRe implements ReProcessor{

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		
		if (resultData != null) {
			List<Map<String, Object>> replys = (List<Map<String, Object>>) resultData.get(Constants.REPLYS);
			for (Map<String, Object> item : replys) {
				if(item.containsKey(Constants.REPLYDATE)) {
					String replydate = item.get(Constants.REPLYDATE).toString();
//					"replydate": "发表于 昨天 15:42", 
//					"replydate": "发表于 2016-9-7 21:56:28",
//					"replydate": "发表于 1 小时前", 
					String reg = "发表于 (.*)";
					replydate = this.getCresult(replydate, reg);
					item.put(Constants.REPLYDATE, ConstantFunc.convertTime(replydate));
				}
			}
			//newstime时间
			if(resultData.containsKey(Constants.NEWSTIME) && resultData.get(Constants.NEWSTIME) != ""){
				String newstime = resultData.get(Constants.NEWSTIME).toString();
				String reg = "发表于 (.*)";
				newstime = this.getCresult(newstime, reg);
				resultData.put(Constants.NEWSTIME, ConstantFunc.convertTime(newstime));
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
