package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;
import com.bfd.parse.entity.Constants;

/**
 * 站点名：Bkdslife
 * 
 * 功能：标准化部分字段
 * 
 * @author bfd_06
 */
public class BkdslifePostRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(BkdslifePostRe.class);

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
		/**
		 * 保留所有内容 不去掉多余内容
		 */
//		// CONTENTS
//		if (resultData.containsKey(Constants.CONTENTS)) {
//			formatAttr(Constants.CONTENTS,
//					(String) resultData.get(Constants.CONTENTS), resultData);
//		}

		// AUTHOR
		if (resultData.containsKey(Constants.AUTHOR)) {
			List<Map<String, Object>> authors = (List<Map<String, Object>>) resultData
					.get(Constants.AUTHOR);
			Map<String, Object> author = authors.get(0);
			/**
			 * 取样删除不存在字段
			 */
			String sample = (String) author.get(Constants.USER_CITY);
			if (!sample.contains("来自")) {
				author.remove(Constants.USER_CITY);
			}
			if (!sample.contains("注册")) {
				author.remove(Constants.REG_TIME);
			}
			if (!sample.contains("发帖")) {
				author.remove(Constants.POST_CNT);
			}
			// USER_CITY
			if (author.containsKey(Constants.USER_CITY)) {
				formatAttr(Constants.USER_CITY,
						(String) author.get(Constants.USER_CITY), author);
			}
			// REG_TIME
			if (author.containsKey(Constants.REG_TIME)) {
				formatAttr(Constants.REG_TIME,
						(String) author.get(Constants.REG_TIME), author);
			}
			// POST_CNT
			if (author.containsKey(Constants.POST_CNT)) {

				formatAttr(Constants.POST_CNT,
						(String) author.get(Constants.POST_CNT), author);
			}
		}

		// REPLYS
		if (resultData.containsKey(Constants.REPLYS)) {
			List<Map<String, Object>> replys = (List<Map<String, Object>>) resultData
					.get(Constants.REPLYS);
			for (Map<String, Object> reply : replys) {
				/**
				 * 取样删除不存在字段
				 */
				String sample = (String) reply.get(Constants.REPLY_USER_CITY);
				if (!sample.contains("来自")) {
					reply.remove(Constants.REPLY_USER_CITY);
				}
				if (!sample.contains("注册")) {
					reply.remove(Constants.REPLY_REG_TIME);
				}
				if (!sample.contains("发帖")) {
					reply.remove(Constants.REPLY_POST_CNT);
				}
				// REPLY_USER_CITY
				if (reply.containsKey(Constants.REPLY_USER_CITY)) {
					formatAttr(Constants.REPLY_USER_CITY,
							(String) reply.get(Constants.REPLY_USER_CITY),
							reply);
				}
				// REPLY_REG_TIME
				if (reply.containsKey(Constants.REPLY_REG_TIME)) {
					formatAttr(Constants.REPLY_REG_TIME,
							(String) reply.get(Constants.REPLY_REG_TIME), reply);
				}
				// REPLY_POST_CNT
				if (reply.containsKey(Constants.REPLY_POST_CNT)) {
					formatAttr(Constants.REPLY_POST_CNT,
							(String) reply.get(Constants.REPLY_POST_CNT), reply);
				}
				// REPLYDATE
				formatAttr(Constants.REPLYDATE,
						(String) reply.get(Constants.REPLYDATE), reply);
				/**
				 * 保留所有内容 不去掉多余内容
				 */
//				// REPLYCONTENT
//				formatAttr(Constants.REPLYCONTENT,
//						(String) reply.get(Constants.REPLYCONTENT), reply);
				// REPLYFLOOR
				formatAttr(Constants.REPLYFLOOR,
						(String) reply.get(Constants.REPLYFLOOR), reply);
			}
			/**
			 * 删除部分字段并添加下一页
			 */
			List<Map<String, Object>> rtasks = (List<Map<String, Object>>) resultData
					.get(Constants.TASKS);
			String url = unit.getUrl();
			String pageNumStr = match("_\\d+_\\d+_\\d+_(\\d+)__.html", url);
			String replycountStr = (String) resultData
					.get(Constants.REPLYCOUNT);
			int replycount = 0;
			try {
				replycount = Integer.parseInt(replycountStr);
			} catch (Exception e) {
				LOG.warn(" replycount field conversion error , " + e.toString());
			}
			// 如果是第一页
			if (pageNumStr == null) {
				deleteAttr(replys);
				// 添加下一页
				if (Math.ceil(replycount / 50d) >= 2) {
					addNextUrl(url.replace(".html", "_2__.html"), rtasks,
							resultData);
					ParseUtils.getIid(unit, result);
				}
			} else {
				int pageNum = Integer.parseInt(pageNumStr);
				// 如果是第一页
				if (pageNum == 1) {
					deleteAttr(replys);
					// 删除部分属性
				} else {
					deleteAttr(resultData);
				}
				// 添加下一页
				if (pageNum != Math.ceil(replycount / 50d)) {
					addNextUrl(
							url.replace(pageNum + "__.html", (pageNum + 1)
									+ "__.html"), rtasks, resultData);
					ParseUtils.getIid(unit, result);
				}
			}

		}

		return new ReProcessResult(processcode, processdata);
	}

	public void formatAttr(String keyName, String value,
			Map<String, Object> result) {
		switch (keyName) {
		case Constants.VIEWS:
			int indexA = value.indexOf("点击");
			int indexB = value.indexOf("回复");
			value = value.substring(indexA + 3, indexB - 1);
			result.put(keyName, value);
			break;
		case Constants.REPLYCOUNT:
			int index1 = value.indexOf("回复");
			int index2 = value.indexOf("已被");
			value = value.substring(index1 + 3, index2 - 1);
			result.put(keyName, value);
			break;
		case Constants.REPLYFLOOR:
			value = value.replace("...第", "").replace("楼...", "");
			result.put(keyName, value);
			break;
		case Constants.REPLY_USER_CITY:
		case Constants.USER_CITY:
			value = value.substring(3, 5);
			result.put(keyName, value);
			break;
		case Constants.REPLY_REG_TIME:
		case Constants.REG_TIME:
			int index = value.indexOf("注册");
			value = value.substring(index + 3, index + 13);
			result.put(keyName, value);
			break;
		case Constants.REPLYDATE:
		case Constants.NEWSTIME:
			value = value.substring(4);
			result.put(keyName, value);
			break;
		case Constants.REPLY_POST_CNT:
		case Constants.POST_CNT:
			int indexJ = value.indexOf("发帖");
			int indexK = value.indexOf("＋");
			value = value.substring(indexJ + 3, indexK);
			result.put(keyName, value);
			break;
		case Constants.REPLYCONTENT:
		case Constants.CONTENTS:
			/**
			 * 去掉内容中多余字符
			 */
			int indexM = value
					.indexOf("----------------------------------------------");
			if (indexM != -1) {
				value = value.substring(0, indexM - 1);
			}
			int indexN = value.indexOf("-==kds官方");
			if (indexN != -1) {
				value = value.substring(0, indexN);
			}
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

	public void addNextUrl(String nextUrl, List<Map<String, Object>> rtasks,
			Map<String, Object> resultData) {
		Map<String, Object> rtask = new HashMap<String, Object>();
		rtask.put("link", nextUrl);
		rtask.put("rawlink", nextUrl);
		rtask.put("linktype", "bbspost");
		rtasks.add(rtask);
		resultData.put(Constants.NEXTPAGE, nextUrl);
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