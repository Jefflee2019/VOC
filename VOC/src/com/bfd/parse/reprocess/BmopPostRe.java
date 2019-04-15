package com.bfd.parse.reprocess;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;
import com.bfd.parse.entity.ConstantFunc;
import com.bfd.parse.entity.Constants;

/**
 * 站点名：Bmop
 * 
 * 功能：标准化部分字段并给出下一页
 * 
 * @author bfd_06
 */
public class BmopPostRe implements ReProcessor {

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;

		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();

		// COLLECTION_CNT
		if (resultData.containsKey(Constants.COLLECTION_CNT)) {
			formatAttr(Constants.COLLECTION_CNT,
					(String) resultData.get(Constants.COLLECTION_CNT),
					resultData);
		}
		// UP_CNT
		if (resultData.containsKey(Constants.UP_CNT)) {
			formatAttr(Constants.UP_CNT,
					(String) resultData.get(Constants.UP_CNT), resultData);
		}
		// RECOMMEND_CNT
		if (resultData.containsKey(Constants.RECOMMEND_CNT)) {
			formatAttr(Constants.RECOMMEND_CNT,
					(String) resultData.get(Constants.RECOMMEND_CNT),
					resultData);
		}
		// NEWSTIME
		if (resultData.containsKey(Constants.NEWSTIME)) {
			formatAttr(Constants.NEWSTIME,
					(String) resultData.get(Constants.NEWSTIME),
					resultData);
		}
		// REPLYS
		if (resultData.containsKey(Constants.REPLYS)) {
			List<Map<String, Object>> replys = (List<Map<String, Object>>) resultData
					.get(Constants.REPLYS);
			for (Map<String, Object> reply : replys) {
				// REPLY_LEVEL
				if (reply.containsKey(Constants.REPLY_LEVEL)) {
					formatAttr(Constants.REPLY_LEVEL,
							(String) reply.get(Constants.REPLY_LEVEL), reply);
				}
				// REPLY_POST_CNT
				if (reply.containsKey(Constants.REPLY_POST_CNT)) {
					formatAttr(Constants.REPLY_POST_CNT,
							(String) reply.get(Constants.REPLY_POST_CNT), reply);
				}
				// REPLYDATE
				if (reply.containsKey(Constants.REPLYDATE)) {
					formatAttr(Constants.REPLYDATE,
							(String) reply.get(Constants.REPLYDATE), reply);
				}
			}
		}
		// 添加下一页
		if (matchUp(">下一页</a>", unit.getPageData())) {
			String url = unit.getUrl();
			String nextUrl = "";
			int pageNum = matchValue("_(\\d++).html", url);
			Map<String, Object> task = new HashMap<String, Object>();
			if (pageNum == 0 || pageNum == 1)
				nextUrl = url.replace(".html", "_2.html");
			else
				nextUrl = url.replace(pageNum + ".html", pageNum + 1 + ".html");
			task.put("link", nextUrl);
			task.put("rawlink", nextUrl);
			task.put("linktype", "bbspost");
			List<Map<String, Object>> tasks = (List<Map<String, Object>>) resultData
					.get(Constants.TASKS);
			tasks.add(task);
			resultData.put(Constants.NEXTPAGE, nextUrl);
			ParseUtils.getIid(unit, result);
		}

		return new ReProcessResult(processcode, processdata);
	}

	public void formatAttr(String keyName, String value,
			Map<String, Object> result) {
		switch (keyName) {
		case Constants.COLLECTION_CNT:
		case Constants.UP_CNT:
		case Constants.RECOMMEND_CNT:
		case Constants.REPLY_LEVEL:
		case Constants.REPLY_POST_CNT:
			int indexS = value.indexOf("(");
			int indexE = value.indexOf(")");
			if (indexS != -1 && indexE != -1)
				result.put(keyName, value.substring(indexS + 1, indexE));
			else {
				int indexSS = value.indexOf("（");
				int indexEE = value.indexOf("）");
				if (indexSS != -1 && indexEE != -1)
					result.put(keyName, value.substring(indexSS + 1, indexEE));
			}
			
			break;
		case Constants.NEWSTIME:
		case Constants.REPLYDATE:	
			value = ConstantFunc.convertTime(value).replace("今天", new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime()));
			result.put(keyName, value);
		default:
			break;
		}
	}

	public Boolean matchUp(String regular, String matchedStr) {
		Pattern patten = Pattern.compile(regular);
		Matcher matcher = patten.matcher(matchedStr);
		return matcher.find();
	}

	public int matchValue(String regular, String matchedStr) {
		Pattern patten = Pattern.compile(regular);
		Matcher matcher = patten.matcher(matchedStr);
		if (matcher.find()) {
			return Integer.parseInt(matcher.group(1));
		}
		return 0;
	}

}
