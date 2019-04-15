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
 * 
 * @author 08
 *
 */
public class NcsdnContentRe implements ReProcessor{

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		String url = unit.getUrl();
		if (resultData != null) {
			//"post_time": "2016 九月 13"
			if(resultData.containsKey(Constants.POST_TIME) && resultData.get(Constants.POST_TIME) != ""){
				String post_time = resultData.get(Constants.POST_TIME).toString();
				String reg = "([\u4E00-\u9FA5]+)";
				String month = this.getCresult(post_time, reg);
				if (!post_time.equals(month)) {
					post_time = post_time.replace(month, this.getMonth(month)).replace(" ", "-");
				}
				resultData.put(Constants.POST_TIME, post_time.trim());
			}
		}
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
	/**
	 * 获取日期
	 * @param month
	 * @return
	 */
	private String getMonth(String month) {
		switch (month) {
		case "一月":
			month = "01";
			break;
		case "二月":
			month = "02";
			break;
		case "三月":
			month = "03";
			break;
		case "四月":
			month = "04";
			break;
		case "五月":
			month = "05";
			break;
		case "六月":
			month = "06";
			break;
		case "七月":
			month = "07";
			break;
		case "八月":
			month = "08";
			break;
		case "九月":
			month = "09";
			break;
		case "十月":
			month = "10";
			break;
		case "十一月":
			month = "11";
			break;
		case "十二月":
			month = "12";
			break;
		default:
			break;
		}
		return month;
	}

	/**
	 * 正则匹配字符串
	 * @param str
	 * @param pattern
	 * @return
	 */
	private String getCresult(String str,String reg1){
		Pattern pattern = Pattern.compile(reg1);
		Matcher mch = pattern.matcher(str);
		if(mch.find()){
			return mch.group(1);
		}
		return str;
	}

}
