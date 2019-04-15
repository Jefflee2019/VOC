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

/**
 * 站点名：Bkdslife
 * 
 * 功能：标准化发表时间 加上第一页的下一页
 * 
 * @author bfd_06
 */
public class BkoolshareListRe implements ReProcessor {

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();

		/**
		 * 标准化发表时间
		 */
		List<Map<String, Object>> ritems = (List<Map<String, Object>>) resultData
				.get(Constants.ITEMS);
		for (Map<String, Object> ritem : ritems) {
			//0 个回复 - 101 次查看
			String reply_cnt = (String) ritem.get(Constants.REPLY_CNT);
			reply_cnt = this.getCresult(reply_cnt, "(\\d+) 个回复");
			String view_cnt = (String) ritem.get(Constants.VIEW_CNT);
			view_cnt = this.getCresult(view_cnt, "(\\d+) 次查看");
			ritem.put(Constants.REPLY_CNT, reply_cnt);
			ritem.put(Constants.VIEW_CNT, view_cnt);
		}
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(processcode, processdata);
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
