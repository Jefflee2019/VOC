
package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.Map;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;
/**
 * 站点：N87870
 * 功能：字段规范处理
 * @author dph 2017年12月26日
 *
 */
public class N87870ContentRe implements ReProcessor{

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace parseFace) {
		Map<String,Object> resultData = result.getParsedata().getData();
		Map<String,Object> processdata = new HashMap<>(16);
		//"author": "作者：枫笛", 
		if(resultData.containsKey(Constants.AUTHOR)){
			String author = resultData.get(Constants.AUTHOR).toString();
			author = author.replace("作者：", "").trim();
			resultData.put(Constants.AUTHOR, author);
		}
		
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}

}
