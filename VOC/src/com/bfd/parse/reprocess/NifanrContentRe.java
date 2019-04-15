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

/**
 * 站点名：Nifanr(爱范儿)
 * 
 * 主要功能： 处理发表时间和路径 生成评论页链接
 * 
 * @author bfd_03
 *
 */
public class NifanrContentRe implements ReProcessor {

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace parseFace) {
		int processcode = 0;

		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();

		if (resultData != null && !resultData.isEmpty()) {
			// 发表时间
			if (resultData.containsKey(Constants.POST_TIME)) {
				String sPostTime = (String) resultData.get(Constants.POST_TIME);
				sPostTime = ConstantFunc.convertTime(sPostTime);
				resultData.put(Constants.POST_TIME, sPostTime);
			}

			/**
			 * 拼接评论页的URL的生成任务
			 * 
			 * @param unit
			 * @param resultData
			 */
			getComment(unit, resultData);
		}
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(processcode, processdata);
	}

	/**
	 * @param unit
	 * @param resultData
	 */
	private void getComment(ParseUnit unit, Map<String, Object> resultData) {
		String url = unit.getUrl();
		StringBuffer sb = new StringBuffer();
		Matcher match = Pattern.compile("/(\\d+)").matcher(url);
		if (match.find()) {
			sb.append("https://sso.ifanr.com/api/wp/comment/?post_id=");
			sb.append(match.group(1));
			sb.append("&order_by__in=created_at");

			if (sb.length() > 0) {
				Map<String, Object> commentTask = new HashMap<String, Object>();
				String sCommUrl = sb.toString();
				commentTask.put(Constants.LINK, sCommUrl);
				commentTask.put(Constants.RAWLINK, sCommUrl);
				commentTask.put(Constants.LINKTYPE, "newscomment");
				if (resultData != null && !resultData.isEmpty()) {
					resultData.put(Constants.COMMENT_URL, sCommUrl);
					@SuppressWarnings("unchecked")
					List<Map<String, Object>> tasks = (List<Map<String, Object>>) resultData.get(Constants.TASKS);
					tasks.add(commentTask);
				}
			}
		}
	}
}