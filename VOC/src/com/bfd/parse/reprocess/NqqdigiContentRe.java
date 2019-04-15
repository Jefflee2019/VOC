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
 * 站点名：腾讯手机/数码(Nqqdigi)
 * 
 * 主要功能：处理简介，发表时间
 * 
 * @author bfd_03
 *
 */
public class NqqdigiContentRe implements ReProcessor {

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;

		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();

		if (resultData != null && !resultData.isEmpty()) {
			// 简介
			if (resultData.containsKey(Constants.BRIEF)) {
				stringToMap(resultData, Constants.BRIEF);
			}
			// 发表时间
			if (resultData.containsKey(Constants.POST_TIME)) {
				stringToMap(resultData, Constants.POST_TIME);
			}

		}
		
		getCommentUrl(unit, result);

		return new ReProcessResult(processcode, processdata);
	}

	public void stringToMap(Map<String, Object> resultData, String key) {
		// 简介
		if (key.equals(Constants.BRIEF)) {
			String brief = (String) resultData.get(key);
			if (brief.contains("[摘要]")) {
				brief = brief.replace("[摘要]", "");
			}
			resultData.put(Constants.BRIEF, brief);
		}
		// 发表时间
		if (key.equals(Constants.POST_TIME)) {
			String sPostTime = (String) resultData.get(key);
			if (sPostTime.contains("年") || sPostTime.contains("月")
					|| sPostTime.contains("日")) {
				sPostTime = sPostTime.replace("年", "-").replace("月", "-")
						.replace("日", " ");
			}
			resultData.put(Constants.POST_TIME, sPostTime);
		}

	}
	
	/**
	 * 拼接评论页的URL的生成任务
	 * 从页面源码中获取cmt_id = 1164663232
	 * @param unit
	 * @param resultData
	 */
	@SuppressWarnings("unchecked")
	private void getCommentUrl(ParseUnit unit, ParseResult result) {
		Map<String, Object> resultData = result.getParsedata().getData();
		String pageData = (String) unit.getPageData();
		Pattern pattern = Pattern.compile("cmt_id = (\\d+)", Pattern.DOTALL);
		Matcher matcher = pattern.matcher(pageData);
				
		StringBuffer sb = new StringBuffer();
		if (matcher.find()) {
			//http://coral.qq.com/article/1164663232/comment?commentid=0&reqnum=10
			sb.append("http://coral.qq.com/article/"); 
			sb.append(matcher.group(1));			
			sb.append("/comment?commentid=0&reqnum=10");
		}
		@SuppressWarnings("rawtypes")
		Map commentTask = new HashMap();
		if(sb.length() > 0){
			String sCommUrl = sb.toString();
			commentTask.put(Constants.LINK, sCommUrl);
			commentTask.put(Constants.RAWLINK, sCommUrl);
			commentTask.put(Constants.LINKTYPE, "newscomment");
			if (resultData != null && !resultData.isEmpty()) {
				resultData.put(Constants.COMMENT_URL, sCommUrl);
				@SuppressWarnings("rawtypes")
				List<Map> tasks = (List<Map>) resultData.get(Constants.TASKS);
				tasks.add(commentTask);	
			}
			ParseUtils.getIid(unit, result);
		}
	}

}
