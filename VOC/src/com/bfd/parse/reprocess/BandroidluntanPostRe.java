package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.entity.ConstantFunc;
import com.bfd.parse.entity.Constants;

/**
 * 站点名：Bandroidluntan
 * 
 * 功能：标准化部分字段 调整第一页与其他页部分字段
 * 
 * @author bfd_06
 */
public class BandroidluntanPostRe implements ReProcessor {

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;

		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();

		// CATE
		if (resultData.containsKey(Constants.CATE)) {
			List<String> cate = (List<String>) resultData.get(Constants.CATE);
			if (cate.get(0).equals("安卓论坛")) {
				cate.remove(0);
			}
		}
		// NEWSTIME
		if (resultData.containsKey(Constants.NEWSTIME)) {
			formatAttr(Constants.NEWSTIME,
					(String) resultData.get(Constants.NEWSTIME), resultData);
		}
		// REPLYS
		List<Map<String, Object>> replys = (List<Map<String, Object>>) resultData
				.get(Constants.REPLYS);
		for (Map<String, Object> reply : replys) {
			// REPLYFLOOR
			formatAttr(Constants.REPLYFLOOR,
					(String) reply.get(Constants.REPLYFLOOR), reply);
			// REPLYDATE
			formatAttr(Constants.REPLYDATE,
					(String) reply.get(Constants.REPLYDATE), reply);
		}
		/**
		 * 调整第一页与其他页部分字段
		 */
		String pageNum = match("(\\d+)-\\d+.html", unit.getUrl());
		if (pageNum == null || pageNum.equals("1")) {
			deleteAttr(replys);
		} else {
			deleteAttr(resultData);
		}

		return new ReProcessResult(processcode, processdata);
	}

	public void formatAttr(String keyName, String value,
			Map<String, Object> result) {
		switch (keyName) {
		case Constants.REPLYFLOOR:
			value = value.replace("楼", "");
			if (value.equals("地板")) {
				value = "4";
			} else if (value.equals("板凳")) {
				value = "3";
			} else if (value.equals("沙发")) {
				value = "2";
			}
			result.put(keyName, value);
			break;
		case Constants.NEWSTIME:
		case Constants.REPLYDATE:
			value = value.substring(value.indexOf("于") + 2);
			value = ConstantFunc.convertTime(value);
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