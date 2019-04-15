package com.bfd.parse.reprocess;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.ConstantFunc;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;

public class NaskciContentRe implements ReProcessor {
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace parseFace) {
		Map<String, Object> resultData = new HashMap<String, Object>();
		Map<String, Object> processdata = new HashMap<String, Object>();
		resultData = result.getParsedata().getData();
//		String pageData = unit.getPageData();
		if ((resultData != null) && (!resultData.isEmpty())) {
			if (resultData.containsKey(Constants.POST_TIME)) {
				resultData.put(Constants.POST_TIME, ConstantFunc.getDate((String) resultData.get(Constants.POST_TIME)).trim());
			}
			if (resultData.containsKey(Constants.SOURCE)) {
				resultData.put(Constants.SOURCE, ConstantFunc.getSource((String) resultData.get(Constants.SOURCE)).trim());
			}
			if (resultData.containsKey(Constants.CATE)) {
				@SuppressWarnings("unchecked")
				List<String> cate = (List<String>) resultData.get(Constants.CATE);
				String[] catetemp = cate.toString().replaceAll("\\s", "").replaceAll("\\+", " ").replace("[", "")
						.replace("]", "").split(" ");
				resultData.put(Constants.CATE, Arrays.asList(catetemp));
			}
			if (resultData.containsKey(Constants.EDITOR)) {
				String editor = (String)resultData.get(Constants.EDITOR);
				int indx = editor.indexOf("编辑：");
				if(indx >= 0) {
					String[] s = editor.substring(indx+3).trim().split(" ");
					resultData.put(Constants.EDITOR, s[0]);
				}
			}
			if (resultData.containsKey(Constants.KEYWORD)) {
				String keyword = (String) resultData.get(Constants.KEYWORD);
				String[] keywordtemp = keyword.toString().replaceAll("\\t", " ").replace("[", "").replace("]", "")
						.split(" ");
				resultData.put(Constants.KEYWORD, Arrays.asList(keywordtemp));
			}
		}
		return new ReProcessResult(0, processdata);
	}
}
