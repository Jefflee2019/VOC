package com.bfd.parse.reprocess;

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
 * 站点名：Eamazon
 * 
 * 功能：标准化商品评论部分字段 解决资讯页面边界问题
 * 
 * @author bfd_06
 */
public class EamazonCommentRe implements ReProcessor {

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();

		// 如果是评论页面 标准化部分字段
		if (resultData.containsKey(Constants.COMMENTS)) {
			List<Map<String, Object>> comments = (List<Map<String, Object>>) resultData
					.get(Constants.COMMENTS);
			for (Map<String, Object> comment : comments) {
				// COMMENT_TIME
				if (comment.containsKey(Constants.COMMENT_TIME)) {
					comment.put(Constants.COMMENT_TIME, ConstantFunc.getDate(((String) comment.get(Constants.COMMENT_TIME)).replace("于", "")));
				}
			}
		}
		// GOOD_RATE
		if (resultData.containsKey(Constants.GOOD_RATE)) {
			String goodrate = (String) resultData.get(Constants.GOOD_RATE);
			goodrate = "0." + goodrate.split("%")[0];
			double goodrated = Double.parseDouble(goodrate);
			resultData.put(Constants.GOOD_RATE,goodrate);
//					formatAttr(Constants.GOOD_RATE,
//							(String) resultData.get(Constants.GOOD_RATE), resultData);
		}
//		ParseUtils.getIid(unit, result);
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
			Map<String, Object> result) {
		switch (keyName) {
		case Constants.COLOR:
			String markStr = value;
			if (markStr.contains("颜色") && markStr.contains("产品款式")) {
				int indexY = markStr.indexOf("颜色");
				int indexK = markStr.indexOf("款式");
				if (indexY < indexK) {
					String color = markStr.substring(4, indexK - 2);
					String buyType = markStr.substring(indexK + 4);
					result.put(keyName, color);
					result.put(Constants.BUY_TYPE, buyType);
				} else {
					String color = markStr.substring(indexY + 4);
					String buyType = markStr.substring(6, indexY - 2);
					result.put(keyName, color);
					result.put(Constants.BUY_TYPE, buyType);
				}
			} else if (markStr.contains("颜色") && !markStr.contains("产品款式")) {
				String color = markStr.substring(4);
				result.put(keyName, color);
				result.remove(Constants.BUY_TYPE);
			} else if (!markStr.contains("颜色") && markStr.contains("产品款式")) {
				int index = markStr.indexOf("款式");
				String buyType = markStr.substring(index + 4);
				result.put(Constants.BUY_TYPE, buyType);
				result.remove(keyName);
			}
			break;
		case Constants.COMMENT_TIME:
			value = value.substring(value.indexOf(' ') + 1);
			result.put(keyName, value);
			break;
		case Constants.COMMENT_REPLY_CNT:
			if (value.equals("回应")) {
				value = "0";
			} else {
				value = value.substring(0, value.indexOf(' '));
			}
			result.put(keyName, value);
			break;
		case Constants.SCORE:
		case Constants.GOOD_RATE:
			value = value.substring(0, value.indexOf("颗星") - 1);
			result.put(keyName, Float.parseFloat(value));
			break;
		case Constants.REPLY_CNT:
			value = value.replace(",", "");
			result.put(keyName, value);
			break;
		default:
			break;
		}
	}

}
