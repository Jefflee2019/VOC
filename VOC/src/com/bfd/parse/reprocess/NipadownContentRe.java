package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.Map;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

/**
 * 站点：Nipadown
 * 功能：内容页后处理
 * @author dph 2017年11月16日
 *
 */
public class NipadownContentRe implements ReProcessor{

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace parseFace) {
		Map<String, Object> processdata = new HashMap<String, Object>(16);
		Map<String,Object> resultData = result.getParsedata().getData();
		//"author": "2010-04-26 | susan", 
		if(resultData.containsKey(Constants.AUTHOR)){
			String author = (String) resultData.get(Constants.AUTHOR);
			author = author.replaceAll("\\S+\\s\\|\\s", "").trim();
			resultData.put(Constants.AUTHOR, author);
		}
		//"author": "2010-04-26 | susan", 
		if(resultData.containsKey(Constants.POST_TIME)){
			String posttome = (String) resultData.get(Constants.POST_TIME);
			posttome = posttome.replaceAll("\\s\\|\\s\\S+", "").trim();
			resultData.put(Constants.POST_TIME, posttome);
		}
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}

}
