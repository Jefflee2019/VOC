package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.Map;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.ConstantFunc;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

/**
 * 站点：N0574online
 * 功能：字段规范处理
 * @author dph 2017年12月26日
 *
 */
public class N0574onlineContentRe implements ReProcessor{

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace parseFace) {
		Map<String,Object> resultData = result.getParsedata().getData();
		Map<String,Object> processdata = new HashMap<>(16);
		//"post_time": "12月25日"
		if(resultData.containsKey(Constants.POST_TIME)){
			String posttime = resultData.get(Constants.POST_TIME).toString();
			posttime = "2017年" + posttime;
			posttime = ConstantFunc.getDate(posttime);
			resultData.put(Constants.POST_TIME, posttime);
		}
		
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}

}
