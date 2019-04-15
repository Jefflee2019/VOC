package com.bfd.parse.reprocess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.entity.Constants;

/**
 * 站点名：Ngywb
 * 
 * 标准化部分字段
 * 
 * @author bfd_06
 */
public class NgywbContentRe implements ReProcessor {

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();

		// POST_TIME
		if (resultData.containsKey(Constants.POST_TIME)) {
			String postTime = (String) resultData.get(Constants.POST_TIME);
			if (resultData.containsKey(Constants.CATE)
					&& ((!postTime.contains("时间") && !postTime.contains("日期")))) {
				resultData.remove(Constants.POST_TIME);
			} else {
				formatAttr(Constants.POST_TIME, postTime, resultData);
			}
		}
		// SOURCE
		if (resultData.containsKey(Constants.SOURCE)) {
			String source = (String) resultData.get(Constants.SOURCE);
			if (resultData.containsKey(Constants.CATE)
					&& ((!source.contains("来源") && !source.contains("稿源")))) {
				resultData.remove(Constants.SOURCE);
			} else {
				formatAttr(Constants.SOURCE, source, resultData);
			}
		}
		// BRIEF
		if (resultData.containsKey(Constants.BRIEF)) {
			formatAttr(Constants.BRIEF,
					(String) resultData.get(Constants.BRIEF), resultData);
		}
		// CATE
		if (resultData.containsKey(Constants.CATE)) {
			Object obj = resultData.get(Constants.CATE);
			if (obj instanceof String)
				formatAttr(Constants.CATE, (String) obj, resultData);
		}

		return new ReProcessResult(processcode, processdata);
	}

	public void formatAttr(String keyName, String value,
			Map<String, Object> result) {
		if (keyName.equals(Constants.POST_TIME)) {
			if (value.contains("时间")) {
				int index1 = value.indexOf("时间");
				if (value.contains("来源")) {
					int index2 = value.indexOf("来源");
					value = value.substring(index1 + 3, index2 - 1);
				} else {
					value = value.substring(index1 + 3);
				}
			} else if (value.contains("日期")) {
				int index = value.indexOf("日期");
				value = value.substring(index + 3).replace("]", "");
			}
			value = value.trim();
			result.put(keyName, value);
		} else if (keyName.equals(Constants.SOURCE)) {
			if (value.contains("来源")) {
				int index1 = value.indexOf("来源");
				value = value.substring(index1 + 3).trim();
				int index2 = value.indexOf("点击");
				if (index2 != -1)
					value = value.substring(0, index2 - 1);
			} else if (value.contains("稿源")) {
				int index2 = value.indexOf("稿源");
				value = value.substring(index2 + 3).replace("[", "")
						.replace("]", "");
			} else {
				value = value.substring(value.indexOf(" ") + 1);
			}
			result.put(keyName, value);
		} else if (keyName.equals(Constants.BRIEF)) {
			if (value.startsWith("摘要"))
				value = value.substring(3);
			result.put(keyName, value);
		} else if (keyName.equals(Constants.CATE)) {
			String[] cateArray = value.split(">");
			List<String> resultList = new ArrayList<String>();
			for (int i = 0; i < cateArray.length; i++) {
				resultList.add(cateArray[i].trim());
			}
			result.put(keyName, resultList);
		}
	}
}
