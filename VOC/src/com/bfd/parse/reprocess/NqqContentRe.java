package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.fastjson.JSONObject;
import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

/**
 * 站点：腾讯网 功能：给出评论页链接 标准化科技新闻时间、摘要字段
 * 
 * @author bfd_06
 */

public class NqqContentRe implements ReProcessor {
	public static final Log LOG = LogFactory.getLog(NqqContentRe.class);
	
	private static final Pattern IIDPATTER = Pattern
			.compile("cmt_id = ['\"]?(\\d+)");

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace arg2) {
		
		String commentUrl = "http://coral.qq.com/article/%s/comment?commentid=0&reqnum=50";
		String commentUrl1 = "http://coral.qq.com/article/%s/comment?commentid=0&reqnum=50";
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		Matcher match = IIDPATTER.matcher(unit.getPageData());
		String url = unit.getUrl();
		/* 为空则返回 */
		if(url==null){
			return new ReProcessResult(SUCCESS, processdata);
		}
		String subUrl = urlFilter(url);
		// 标准化科技新闻时间、摘要字段
		if (subUrl.startsWith("tech")) {
			// POST_TIME
			if (resultData.containsKey(Constants.POST_TIME)) {
				String postTime = (String) resultData.get(Constants.POST_TIME);
				postTime = postTime.replace('年', '-').replace('月', '-')
						.replace('日', ' ');
				resultData.put(Constants.POST_TIME, postTime);
			}
			// BRIEF
			if (resultData.containsKey(Constants.BRIEF)) {
				if (resultData.containsKey(Constants.BRIEF)) {
					formatAttrC(Constants.BRIEF,
							(String) resultData.get(Constants.BRIEF),
							resultData);
				}
			}
		} else if (subUrl.startsWith("finance")) {
			if (subUrl.startsWith("finance.qq.com/original")) {
				// POST_TIME
				if (resultData.containsKey(Constants.POST_TIME)) {
					String postTime = (String) resultData
							.get(Constants.POST_TIME);
					resultData.put(Constants.POST_TIME,
							postTime.substring(postTime.indexOf("|") + 2));
				}
			}
		} else if (subUrl.startsWith("auto")) {
			if (subUrl.startsWith("auto.qq.com/a")) {
				// BRIEF
				if (resultData.containsKey(Constants.BRIEF)) {
					formatAttrC(Constants.BRIEF,
							(String) resultData.get(Constants.BRIEF),
							resultData);
				}
				// CONTENT
				if (resultData.containsKey(Constants.CONTENT)) {
					formatAttrC(Constants.CONTENT,
							(String) resultData.get(Constants.CONTENT),
							resultData);
				}
			}
		} else if (subUrl.startsWith("digi")) {
			if (subUrl.startsWith("digi.tech.qq.com/original/bestchoice")) {
				String author = matchValue("id=\"bjname\">(.+)</h3>",
						unit.getPageData());
				if (author != null) {
					resultData.put(Constants.AUTHOR, author);
				}
			}
		} else if (subUrl.startsWith("hb.qq.com/zt2015/zghx")) {
			formatAttrH(Constants.AUTHOR,
					(String) resultData.get(Constants.AUTHOR),
					resultData);
		} else if (subUrl.startsWith("new.qq.com")) {
			String pageData = unit.getPageData();
			// 这类模板的发表时间和来源放在js中，需要额外处理
			if(pageData.contains("window.DATA = {")) {
				String regex = "window.DATA\\s=\\s*([\\r\\n\\S\\s]*?)\\}";
				Matcher scriptMatch = Pattern.compile(regex).matcher(pageData);
				if(scriptMatch.find()) {
					String script = scriptMatch.group(1)+"}";
					Map<String,Object> scriptMap = JSONObject.parseObject(script);
					// 来源
					if(scriptMap.containsKey("media")) {
						String source = scriptMap.get("media").toString();
						resultData.put(Constants.SOURCE,source);
					}
					// 发表时间
					if(scriptMap.containsKey("pubtime")) {
						String postTime = scriptMap.get("pubtime").toString();
						resultData.put(Constants.POST_TIME, postTime);
					}
					// comment_id
					if(scriptMap.containsKey("comment_id")) {
						String comment_id = scriptMap.get("comment_id").toString();
						commentUrl1 = String.format(commentUrl, comment_id);
					}
				}
			}
		}

		// 判断如果存在评论总数并且不等于0那么则给出评论页
		/*if (resultData.containsKey(Constants.REPLY_CNT)) {
			int replyCnt = Integer.parseInt((String) resultData
					.get(Constants.REPLY_CNT));
			if (replyCnt != 0) {*/
				if (match.find()) {
					Map<String, Object> commentTask = new HashMap<String, Object>();
					// 评论一次最大只能请求50条
					commentUrl = String.format(commentUrl, match.group(1));
					commentTask.put("link", commentUrl);
					commentTask.put("rawlink", commentUrl);
					commentTask.put("linktype", "newscomment");
					if (!resultData.isEmpty()) {
						resultData.put(Constants.COMMENT_URL, commentUrl);
						List<Map> rtasks = (List<Map>) resultData.get("tasks");
						resultData.put(Constants.TASKS, rtasks);
						rtasks.add(commentTask);
					}
				} else {
					if (subUrl.startsWith("new.qq.com")) {
						Map<String, Object> commentTask = new HashMap<String, Object>();
						// 评论一次最大只能请求50条
						commentTask.put("link", commentUrl1);
						commentTask.put("rawlink", commentUrl1);
						commentTask.put("linktype", "newscomment");
						if (!resultData.isEmpty()) {
							resultData.put(Constants.COMMENT_URL, commentUrl1);
							List<Map> rtasks = (List<Map>) resultData.get("tasks");
							resultData.put(Constants.TASKS, rtasks);
							rtasks.add(commentTask);
						}
					}
				}
				ParseUtils.getIid(unit, result);
		/*	}
		}*/

		return new ReProcessResult(SUCCESS, processdata);
	}

	/**
	 * 过滤 http:// https://
	 */
	public String urlFilter(String url) {
		if (url.startsWith("http://")) {
			return url.substring(7);
		} else {
			return url.substring(8);
		}
	}

	public void formatAttrC(String keyName, String value,
			Map<String, Object> result) {
		if (value.startsWith("[摘要]")) {
			result.put(keyName, value.substring(4));
		}
	}
	
	public void formatAttrH(String keyName, String value,
			Map<String, Object> result) {
		if (keyName.equals(Constants.AUTHOR)&&value.contains(" ")) {
			value = value.substring(0,value.indexOf(" "));
			result.put(keyName, value);
		}
	}

	/**
	 * @function 正则匹配
	 * @param regular
	 *            正则表达式
	 * @param matchedStr
	 *            被匹配字符串
	 * @return 匹配到的值
	 */
	public String matchValue(String regular, String matchedStr) {
		Pattern patten = Pattern.compile(regular);
		Matcher matcher = patten.matcher(matchedStr);
		if (matcher.find()) {
			return matcher.group(1);
		}
		return null;
	}
}
