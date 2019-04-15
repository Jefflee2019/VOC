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
 * 站点名：Nnjdaily
 * 
 * 标准化部分字段
 * 
 * @author bfd_06
 */
public class NnjdailyContentRe implements ReProcessor {

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
		// CATE
		if (resultData.containsKey(Constants.CATE)) {
			Object obj = resultData.get(Constants.CATE);
			if(obj instanceof String){
				formatAttr(Constants.CATE,
						(String) resultData.get(Constants.CATE), resultData);
			}
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
		case Constants.CATE:
			value = value.replace(" > ", " ");
			String[] valueArray = value.split(" ");
			List<String> valueList = new ArrayList<String>();
			for(String content:valueArray){
				valueList.add(content);
			}
			result.put(keyName, valueList);
			break;
		default:
			break;
		}
	}

}
