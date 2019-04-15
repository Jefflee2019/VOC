package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.Map;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

/**
 * 站点：N25pp
 * 功能：新闻内容页后处理
 * @author dph 2017年11月17日
 *
 */
public class N25ppContentRe implements ReProcessor{

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace parseFace) {
		Map<String,Object> reusltData = result.getParsedata().getData();
		Map<String,Object> processdata = new HashMap<String,Object>(16);
		//"author": "编辑：zhangcx", 
		if(reusltData.containsKey(Constants.AUTHOR)){
			String author = (String) reusltData.get(Constants.AUTHOR);
			author = author.replace("编辑：", "").trim();
			reusltData.put(Constants.AUTHOR, author);
		}
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}

}
