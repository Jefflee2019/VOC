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
 * 站点：eepw
 * 功能：新闻评论页后处理时间
 * @author bfd_05
 */

public class NeepwCommentRe implements ReProcessor {
	
	private static final Pattern PATTIME = Pattern.compile("[0-9]{4}-[0-9]{1,2}-[0-9]{1,2}\\s([0-9]{2}:[0-9]{2})*");
	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		String url = unit.getUrl();
		if (resultData != null) {
			//reply_cnt
			if(resultData.containsKey(Constants.REPLY_CNT) && resultData.get(Constants.REPLY_CNT) != ""){
				String reply_cnt = resultData.get(Constants.REPLY_CNT).toString();
				String reg = "共(\\d*)条";
				reply_cnt = this.getCresult(reply_cnt, reg);
				resultData.put(Constants.REPLY_CNT, reply_cnt.trim());
			}
			
			List comments = (List) resultData.get("comments");
			if (comments.size() != 0) {
				for (Object object : comments) {
					Map obj = (Map) object;
					//comment_time时间
					if(obj.containsKey(Constants.COMMENT_TIME) && obj.get(Constants.COMMENT_TIME) != ""){
						String comment_time = obj.get(Constants.COMMENT_TIME).toString();
						String reg = "(\\d{4}-\\d{2}-\\d{2} \\S+:\\d{2})";
						comment_time = this.getCresult(comment_time, reg);
						obj.put(Constants.COMMENT_TIME, comment_time.trim());
					}
					//usernameEEPW匿名网友 · 2016-02-26 10:56:43 回复
					if(obj.containsKey(Constants.USERNAME) && obj.get(Constants.USERNAME) != ""){
						String username = obj.get(Constants.USERNAME).toString();
						String reg = "([^·]*)";
						username = this.getCresult(username, reg);
						obj.put(Constants.USERNAME, username.trim());
					}
				}
			}
		}
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	};
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
