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
import com.bfd.parse.util.ParseUtils;

public class BapkbusListRe implements ReProcessor{

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		
		if (resultData != null) {
//			"reply_cnt": "10 个回复 - 10 次查看", 
//            "view_cnt": "10 个回复 - 10 次查看"
			List<Map<String, Object>> items = (List<Map<String, Object>>) resultData.get(Constants.ITEMS);
			for (Map<String, Object> item : items) {
				if(item.containsKey(Constants.VIEW_CNT)) {
					String view_cnt = item.get(Constants.VIEW_CNT).toString();
					if (view_cnt.contains(" 次查看")) {
						String reg = "(\\d*) 次查看";
						view_cnt = this.getCresult(view_cnt, reg);
						item.put(Constants.VIEW_CNT, view_cnt);
					}
				}
				if (!item.containsKey(Constants.REPLY_CNT)) {
					item.put(Constants.REPLY_CNT, -1024);
				} else {
					String reply_cnt = item.get(Constants.REPLY_CNT).toString();
					if (reply_cnt.contains(" 个回复")) {
						String reg = "(\\d*) 个回复";
						reply_cnt = this.getCresult(reply_cnt, reg);
						item.put(Constants.REPLY_CNT, reply_cnt);
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
