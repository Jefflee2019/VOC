package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.Map;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.entity.Constants;

/**
 * 站点名：Nhowbuy
 * 
 * 标准化部分字段
 * 
 * @author bfd_06
 */
public class NhowbuyContentRe implements ReProcessor {

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		// SOURCE
		if (resultData.containsKey(Constants.SOURCE)) {
			formatAttr(Constants.SOURCE,
					(String) resultData.get(Constants.SOURCE), resultData);
		}
		// POST_TIME
		if (resultData.containsKey(Constants.POST_TIME)) {
			formatAttr(Constants.POST_TIME,
					(String) resultData.get(Constants.POST_TIME), resultData);
		}

		return new ReProcessResult(processcode, processdata);
	}

	public void formatAttr(String keyName, String value,
			Map<String, Object> result) {
		switch (keyName) {
		case Constants.SOURCE:
			int index = value.indexOf("来源：");
			if (index != -1) {
				value = value.substring(index + 3);
			}
			result.put(keyName, value);
			break;
		case Constants.POST_TIME:
			int indexA = value.indexOf("howbuy.com");
			int indexB = value.indexOf("来源：");
			if (indexA != -1 && indexB != -1) {
				value = value.substring(indexA + 11, indexB - 1);
			}
			result.put(keyName, value);
			break;
		default:
			break;
		}
	}

}
