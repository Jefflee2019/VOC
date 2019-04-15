package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;

/**
 * 站点名：it168(论坛)
 * 
 * 主要功能：处理在线时间，回复数，注册时间，积分，主题数，最后登录时间，好友数
 * 
 * @author bfd_03
 *
 */
public class Bit168InfoRe implements ReProcessor {

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;

		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();

		if (resultData != null && !resultData.isEmpty()) {
			// 在线时间
			if (resultData.containsKey(Constants.ONLINE_HOUR)) {
				stringToMap(resultData, Constants.ONLINE_HOUR);
			}
			// 回复数
			if (resultData.containsKey(Constants.REPLY_CNT)) {
				stringToMap(resultData, Constants.REPLY_CNT);
			}
			// 注册时间
			if (resultData.containsKey(Constants.REG_TIME)) {
				stringToMap(resultData, Constants.REG_TIME);
			}
			// 积分
			if (resultData.containsKey(Constants.FORUM_SCORE)) {
				stringToMap(resultData, Constants.FORUM_SCORE);
			}
			// 主题数
			if (resultData.containsKey(Constants.TOPICCNT)) {
				stringToMap(resultData, Constants.TOPICCNT);
			}
			// 最后登录时间
			if (resultData.containsKey(Constants.LASTLOGIN_TIME)) {
				stringToMap(resultData, Constants.LASTLOGIN_TIME);
			}
			// 好友数
			if (resultData.containsKey(Constants.GOODFRIEND_NUM)) {
				stringToMap(resultData, Constants.GOODFRIEND_NUM);
			}
			// 用户名
			if (resultData.containsKey(Constants.USERNAME_INFO)) {
				stringToMap(resultData, Constants.USERNAME_INFO);
			}
			// 用户标识
			if (resultData.containsKey(Constants.USERID)) {
				stringToMap(resultData, Constants.USERID);
			}
			// 用户唯一标识
			if (resultData.containsKey(Constants.MIID)) {
				stringToMap(resultData, Constants.MIID);
			}
			
			
		}

		return new ReProcessResult(processcode, processdata);
	}

	public void stringToMap(Map<String, Object> resultData, String key) {
		// 用户名
		if (key.equals(Constants.USERNAME_INFO)) {
			String username = (String) resultData.get(key);
			int index = -1;
			if ((index = username.indexOf("(")) > 0) {
				username = username.substring(0, index);
			}
			resultData.put(Constants.USERNAME_INFO, username);
		}
		//在线时间 
		if (key.equals(Constants.ONLINE_HOUR)) {
			resultData.put(
					Constants.ONLINE_HOUR,
					parseByRegex((String) resultData.get(key),
							"(?<=\\D)\\d+(?=\\s*\\D)"));
		}
		//回复数
		if (key.equals(Constants.REPLY_CNT)) {
			resultData.put(
					Constants.REPLY_CNT,
					parseByRegex((String) resultData.get(key),
							"(?<=\\D)\\d+(?=\\b)"));
		}
		//注册时间
		if (key.equals(Constants.REG_TIME)) {
			resultData.put(
					Constants.REG_TIME,
					parseByRegex((String) resultData.get(key),
							"(?<=\\D)[0-9]{4}.[0-9]{1,2}.[0-9]{1,2}.*[0-9]{2}:[0-9]{2}(?=\\b)"));
		}
		//积分
		if (key.equals(Constants.FORUM_SCORE)) {
			resultData.put(
					Constants.FORUM_SCORE,
					parseByRegex((String) resultData.get(key),
							"(?<=\\D)\\d+(?=\\b)"));
		}
		//主题数
		if (key.equals(Constants.TOPICCNT)) {
			resultData.put(
					Constants.TOPICCNT,
					parseByRegex((String) resultData.get(key),
							"(?<=\\D)\\d+(?=\\b)"));
		}
		//最后登录时间
		if (key.equals(Constants.LASTLOGIN_TIME)) {
			resultData.put(
					Constants.LASTLOGIN_TIME,
					parseByRegex((String) resultData.get(key),
							"(?<=\\D)[0-9]{4}.[0-9]{1,2}.[0-9]{1,2}.*[0-9]{2}:[0-9]{2}(?=\\b)"));
		}
		// 好友数
		if (key.equals(Constants.GOODFRIEND_NUM)) {
			resultData.put(
					Constants.GOODFRIEND_NUM,
					parseByRegex((String) resultData.get(key),
							"(?<=\\D)\\d+(?=\\b)"));
		}		
		// 用户标识USERID/用户唯一标识MIID
		if (key.equals(Constants.USERID) || key.equals(Constants.MIID)) {
			resultData.put(
					key,
					parseByRegex((String) resultData.get(key),
							"\\d+"));
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

}
