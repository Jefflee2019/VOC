package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.entity.Constants;

/**
 * Bxcar
 * 
 * @function：标准化部分字段
 * @author BFD_06
 */
public class BxcarPostRe implements ReProcessor {

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		// VIEWS
		if (resultData.containsKey(Constants.VIEWS)) {
			formatAttr(Constants.VIEWS,
					(String) resultData.get(Constants.VIEWS), resultData);
		}
		// REPLYCOUNT
		if (resultData.containsKey(Constants.REPLYCOUNT)) {
			formatAttr(Constants.REPLYCOUNT,
					(String) resultData.get(Constants.REPLYCOUNT), resultData);
		}
		// NEWSTIME
		if (resultData.containsKey(Constants.NEWSTIME)) {
			formatAttr(Constants.NEWSTIME,
					(String) resultData.get(Constants.NEWSTIME), resultData);
		}
		// AUTHOR
		if (resultData.containsKey(Constants.AUTHOR)) {
			List<Map<String, Object>> authors = (List<Map<String, Object>>) resultData
					.get(Constants.AUTHOR);
			Map<String, Object> author = authors.get(0);
			// USER_CITY
			formatAttr(Constants.USER_CITY,
					(String) author.get(Constants.USER_CITY), author);
			// REG_TIME
			formatAttr(Constants.REG_TIME,
					(String) author.get(Constants.REG_TIME), author);
			// FORUM_MONEY
			formatAttr(Constants.FORUM_MONEY,
					(String) author.get(Constants.FORUM_MONEY), author);
			// POST_CNT
			formatAttr(Constants.POST_CNT,
					(String) author.get(Constants.POST_CNT), author);
		}
		// REPLYS
		if (resultData.containsKey(Constants.REPLYS)) {
			List<Map<String, Object>> replys = (List<Map<String, Object>>) resultData
					.get(Constants.REPLYS);
			for (Map<String, Object> reply : replys) {
				// REPLY_USER_CITY
				formatAttr(Constants.REPLY_USER_CITY,
						(String) reply.get(Constants.REPLY_USER_CITY), reply);
				// REPLY_REG_TIME
				formatAttr(Constants.REPLY_REG_TIME,
						(String) reply.get(Constants.REPLY_REG_TIME), reply);
				// REPLY_FORUM_MONEY
				formatAttr(Constants.REPLY_FORUM_MONEY,
						(String) reply.get(Constants.REPLY_FORUM_MONEY), reply);
				// REPLYDATE
				formatAttr(Constants.REPLYDATE,
						(String) reply.get(Constants.REPLYDATE), reply);
				// REPLY_POST_CNT
				formatAttr(Constants.REPLY_POST_CNT,
						(String) reply.get(Constants.REPLY_POST_CNT), reply);
				// REPLYFLOOR
				formatAttr(Constants.REPLYFLOOR,
						(String) reply.get(Constants.REPLYFLOOR), reply);
			}
			/**
			 * 删除第一页与其他页部分字段
			 */
			String pageNum = match("page=(\\d+)", unit.getUrl());
			if (pageNum == null || pageNum.equals("1")) {
				deleteAttr(replys);
			} else {
				deleteAttr(resultData);
			}
		}

		return new ReProcessResult(processcode, processdata);
	}

	public void formatAttr(String keyName, String value,
			Map<String, Object> result) {
		switch (keyName) {
		case Constants.VIEWS:
			value = value.substring(value.indexOf("查看") + 3);
			result.put(keyName, value);
			break;
		case Constants.REPLYCOUNT:
			int indexA = value.indexOf("回复");
			int indexB = value.indexOf("查看");
			value = value.substring(indexA + 3, indexB - 1);
			result.put(keyName, value);
			break;
		case Constants.REPLYFLOOR:
			value = value.replace("楼", "");
			result.put(keyName, value);
			break;
		case Constants.REPLY_USER_CITY:
		case Constants.USER_CITY:
			value = value.substring(value.indexOf("来自") + 4);
			result.put(keyName, value);
			break;
		case Constants.REPLY_REG_TIME:
		case Constants.REG_TIME:
			int index = value.indexOf("注册");
			value = value.substring(index + 4, index + 14);
			result.put(keyName, value);
			break;
		case Constants.REPLY_FORUM_MONEY:
		case Constants.FORUM_MONEY:
			int index1 = value.indexOf("财产");
			int index2 = value.indexOf("爱卡币");
			value = value.substring(index1 + 4, index2 - 1);
			result.put(keyName, value);
			break;
		case Constants.REPLYDATE:
		case Constants.NEWSTIME:
			Pattern patten = Pattern.compile("\\d{4}-\\d{1,2}-\\d{1,2}\\s+\\d{1,2}:\\d{1,2}");
			Matcher matcher = patten.matcher(value);
			if(matcher.find())
				result.put(keyName, matcher.group());
			break;
		case Constants.REPLY_POST_CNT:
		case Constants.POST_CNT:
			value = value.substring(0, value.indexOf("帖"));
			result.put(keyName, value);
			break;
		default:
			break;
		}
	}

	public String match(String regular, String matchedStr) {
		Pattern patten = Pattern.compile(regular);
		Matcher matcher = patten.matcher(matchedStr);
		if (matcher.find()) {
			return matcher.group(1);
		}

		return null;
	}

	public void deleteAttr(Map<String, Object> resultData) {
		resultData.remove(Constants.AUTHOR);
		resultData.remove(Constants.CONTENTS);
		resultData.remove(Constants.NEWSTIME);
	}

	public void deleteAttr(List<Map<String, Object>> replys) {
		if (replys != null) {
			replys.remove(0);
		}
	}

}
