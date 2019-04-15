package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.Map;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;

public class NnbdContentRe implements ReProcessor {

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace parseFace) {
		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		
		if(resultData.containsKey(Constants.CONTENT) && resultData.containsKey(Constants.TITLE)
				&&resultData.containsKey(Constants.POST_TIME)){
			String content = (String) resultData.get(Constants.CONTENT);
			String title = (String) resultData.get(Constants.TITLE);
			String post_time = (String) resultData.get(Constants.POST_TIME);
			content = content.replace(title, "").replace(post_time, "").trim();
			resultData.put(Constants.CONTENT, content);
			
		}
		//"author": "责编 何剑岭"
		if(resultData.containsKey(Constants.AUTHOR)){
			String author = (String) resultData.get(Constants.AUTHOR);
			author = author.replace("责编", "").trim();
			resultData.put(Constants.AUTHOR, author);
		}
		
		return new ReProcessResult(processcode, processdata);
	}

}
