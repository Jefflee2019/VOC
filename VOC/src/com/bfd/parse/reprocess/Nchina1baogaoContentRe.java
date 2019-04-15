package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;

public class Nchina1baogaoContentRe implements ReProcessor {

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {

		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		// 取到解析结果
		Map<String, Object> resultData = result.getParsedata().getData();
		if(resultData != null && !resultData.isEmpty()) {
			if (resultData.containsKey(Constants.CATE) && resultData.get(Constants.CATE) != null) {
				@SuppressWarnings("unchecked")
				List<String> list = (List<String>)resultData.get(Constants.CATE);
					list.remove(list.size()-1);
				resultData.put(Constants.CATE, list);
			}
			if (resultData.containsKey(Constants.SOURCE)) {
				String source = resultData.get(Constants.SOURCE).toString();
				source = source.replace("（http://www.china1baogao.com）", "");
				resultData.put(Constants.SOURCE, source);
			}
			
			if (resultData.containsKey(Constants.POST_TIME)) {
				String posttime = resultData.get(Constants.POST_TIME).toString();
				if (posttime.contains("日期：")) {
					posttime = posttime.split("日期：")[1];
					resultData.put(Constants.POST_TIME, posttime.split(" ")[0]);
				}
			}
		}
		return new ReProcessResult(processcode, processdata);
	}

}
