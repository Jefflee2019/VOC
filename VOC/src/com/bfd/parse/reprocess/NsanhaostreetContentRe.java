package com.bfd.parse.reprocess;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bfd.parse.entity.ConstantFunc;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;

/**
 * @site：深圳新闻网
 * @function：新闻内容页后处理
 * @author huangzecheng 2016-11-28
 *
 */
public class NsanhaostreetContentRe implements ReProcessor {
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		Map<String, Object> resultData = new HashMap<String, Object>();
		Map<String, Object> processdata = new HashMap<String, Object>();
		resultData = result.getParsedata().getData();
//		String pageData = unit.getPageData();
		if (resultData != null && !resultData.isEmpty()) {
			if (resultData.containsKey(Constants.POST_TIME)) {
				resultData.put(Constants.POST_TIME,
						ConstantFunc.getDate((String)resultData.get(Constants.POST_TIME)).trim());
			}
			if (resultData.containsKey(Constants.SOURCE)) {
				resultData.put(Constants.SOURCE,
						ConstantFunc.getSource((String)resultData.get(Constants.SOURCE)).trim());
			}
			if (resultData.containsKey(Constants.EDITOR)) {
				resultData.put(Constants.EDITOR,
						ConstantFunc.getEditor((String)resultData.get(Constants.EDITOR)).trim());
			}
			if (resultData.containsKey(Constants.CATE)) {
				@SuppressWarnings("unchecked")
				List<String> cate = (List<String>) resultData
						.get(Constants.CATE);
				String[] catetemp = cate.toString().replaceAll(" ","").replace("[", "")
						.replace("]", "").split(">");
				resultData.put(Constants.CATE,Arrays.asList(catetemp));
			}
			
		}
		return new ReProcessResult(SUCCESS, processdata);
	}
}
