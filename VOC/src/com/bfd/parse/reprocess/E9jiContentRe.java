package com.bfd.parse.reprocess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;
import com.bfd.parse.entity.Constants;

/**
 * 站点名：E9ji
 * 
 * @author bfd_06
 */
public class E9jiContentRe implements ReProcessor {

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		// CATE
		if (resultData.containsKey(Constants.CATE))
			formatAttr(Constants.CATE, ((List<String>) resultData.get(Constants.CATE)).get(0),
					resultData);
		// REPLY_CNT
		if (resultData.containsKey(Constants.REPLY_CNT))
			formatAttr(Constants.REPLY_CNT,
					(String) resultData.get(Constants.REPLY_CNT), resultData);
		// 添加评论链接
		int reply_cnt = Integer.parseInt((String) resultData
				.get(Constants.REPLY_CNT));
		if (reply_cnt > 0)
			addCommentUrl(unit, resultData, result);

		return new ReProcessResult(processcode, processdata);
	}

	public String match(String regular, String matchedStr) {
		Pattern patten = Pattern.compile(regular);
		Matcher matcher = patten.matcher(matchedStr);
		if (matcher.find()) {
			return matcher.group(1);
		}

		return null;
	}

	public void formatAttr(String keyName, String value,
			Map<String, Object> resultData) {
		if (keyName.equals(Constants.CATE)) {
			List<String> valueList = new ArrayList<String>();
			for (String value2 : value.split(">")) {
				valueList.add(value2.replace(" ", ""));
			}
			resultData.put(keyName, valueList);
		} else if (keyName.equals(Constants.REPLY_CNT)) {
			value = value.replace("(", "").replace(")", "");
			resultData.put(keyName, value);
		}

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void addCommentUrl(ParseUnit unit, Map<String, Object> resultData,
			ParseResult result) {
		Matcher matcher = Pattern.compile("ID:\\s*(\\d+)").matcher(
				unit.getPageData());
		if (matcher.find()) {
			String id = matcher.group(1);
			String commentUrl = "http://www.9ji.com/ajaxOperate.aspx?act=getAssessV1&pageIndex=1&pageSize=8&pjtype=0&proid="
					+ id;
			Map<String, String> commentTask = new HashMap<String, String>();
			commentTask.put("link", commentUrl);
			commentTask.put("rawlink", commentUrl);
			commentTask.put("linktype", "eccomment");
			resultData.put("comment_url", commentUrl);
			List<Map> tasks = (List<Map>) resultData.get("tasks");
			tasks.add(commentTask);
			ParseUtils.getIid(unit, result);
		}
	}

}
