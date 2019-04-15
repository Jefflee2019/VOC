package com.bfd.parse.reprocess;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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

/**
 * 
 * @author 08
 *
 */
public class BgomeContentRe implements ReProcessor{

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
//		http://china.caixin.com/2017-04-07/101075491.html
		String url = unit.getUrl();
		if (resultData != null) {
			//newstime
			String reg = "(\\d{4}-\\d{2}-\\d{2} \\S+:\\d{2})";
			String newstime = resultData.get(Constants.NEWSTIME).toString();
			newstime = this.getCresult(newstime, reg);
			resultData.put(Constants.NEWSTIME, newstime.trim());
			
			List replys = (List) resultData.get("replys");
			if (replys.size() != 0) {
				for (Object object : replys) {
					Map obj = (Map) object;
					//replydate 
					if(obj.containsKey(Constants.REPLYDATE) && obj.get(Constants.REPLYDATE) != ""){
						String replydate = obj.get(Constants.REPLYDATE).toString();
						replydate = this.getCresult(replydate, reg);
						obj.put(Constants.REPLYDATE, replydate.trim());
					}
				}
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
