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
 * 站点名：it168(新闻)
 * 
 * 主要功能：处理作者，发表时间
 * 
 * @author bfd_03
 *
 */
public class Nit168ContentRe_bak implements ReProcessor {

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;

		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();

		if (resultData != null && !resultData.isEmpty()) {
			// 作者
			if (resultData.containsKey(Constants.AUTHOR)) {
				stringToMap(resultData, Constants.AUTHOR);
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
		// 作者
		if (key.equals(Constants.AUTHOR)) {
			resultData.put(
					Constants.AUTHOR,
					parseByRegex((String) resultData.get(key),
							"(?<=作者:)\\s+(\\S*)(?=\\s)"));
		}
		//发表时间 
		if (key.equals(Constants.POST_TIME)) {
			resultData.put(
					Constants.POST_TIME,
					parseByRegex((String) resultData.get(key),
							"\\d{4}-\\d{1,2}-\\d{1,2}\\s+\\d{1,2}:\\d{1,2}"));
		}

	}

	/**
	 * 集成正则表达式匹配，
	 * 
	 * @param data
	 *            (需要匹配的数据)
	 * @param regex
	 *            (正则表达式)
	 * @return
	 */
	public String parseByRegex(String data, String regex) {
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(data);
		if (matcher.find()) {
			data = matcher.group();
		}
		return data;
	}
	
	
	/**
	 * 拼接评论页的URL的生成任务
	 * @param unit
	 * @param resultData
	 */
	@SuppressWarnings("unchecked")
	private void getCommentUrl(ParseUnit unit, ParseResult result) {
		Map<String, Object> resultData = result.getParsedata().getData();
		String url = (String) unit.getTaskdata().get("url");
		Pattern pattern = Pattern.compile("http://mobile.it168.com/\\w+/\\d+/\\d+/(\\d+).shtml", Pattern.DOTALL);
		Matcher matcher = pattern.matcher(url);
				
		StringBuffer sb = new StringBuffer();
		if (matcher.find()) {
			sb.append("http://it168.duoshuo.com/api/threads/listPosts.json?thread_key=");
			Long threadKey = Long.parseLong(matcher.group(1)); //000001720547转换成1720547
			sb.append(String.valueOf(threadKey));			
			sb.append("&channel_key=6");
		}
		@SuppressWarnings("rawtypes")
		Map commentTask = new HashMap();
		if(sb.length() > 0){
			String commUrl = sb.toString();
			commentTask.put(Constants.LINK, commUrl);
			commentTask.put(Constants.RAWLINK, commUrl);
			commentTask.put(Constants.LINKTYPE, "newscomment");
			if (resultData != null && !resultData.isEmpty()) {
				resultData.put(Constants.COMMENT_URL, commUrl);
				@SuppressWarnings("rawtypes")
				List<Map> tasks = (List<Map>) resultData.get(Constants.TASKS);
				tasks.add(commentTask);	
			}
			ParseUtils.getIid(unit, result);
		}
	}

}
