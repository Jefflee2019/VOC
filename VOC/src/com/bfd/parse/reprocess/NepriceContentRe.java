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
 * 站点名：Neprice
 * 
 * 主要功能： 生成评论页链接
 */
public class NepriceContentRe implements ReProcessor {

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace parseFace) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		String url = unit.getUrl();
		if (resultData != null) {
			// 发布时间
			if (resultData.containsKey(Constants.POST_TIME) && resultData.get(Constants.POST_TIME) != "") {
				String post_time = resultData.get(Constants.POST_TIME).toString();
				post_time = this.getDate(post_time);
				resultData.put(Constants.POST_TIME, post_time);
			}

			if (resultData.containsKey("reply_floor")) { // 如果有回复楼层，就生成评论任务
				HashMap<String, String> commentTask = new HashMap<>();
				commentTask.put(Constants.LINK, url);
				commentTask.put(Constants.RAWLINK, url);
				commentTask.put(Constants.LINKTYPE, "newscomment");
				@SuppressWarnings("unchecked")
				List<Map<String, String>> tasks = (List<Map<String, String>>) resultData.get(Constants.TASKS);
				resultData.put(Constants.COMMENT_URL, url);
				tasks.add(commentTask);
			}
		}
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}

	/**
	 * 正则匹配日期
	 */
	private String getDate(String str) {
		Pattern pattern = Pattern.compile("(\\d{4}-\\d{2}-\\d{2}\\s+\\d{2}:\\d{2})");
		Matcher mch = pattern.matcher(str);
		if (mch.find()) {
			return mch.group(1);
		}
		return str;
	}

}
