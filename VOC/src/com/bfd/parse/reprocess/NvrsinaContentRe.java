package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.Map;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

/**
 * 站点:Nvrsina
 * 功能：字段规范处理
 * @author dph 2017年12月26日
 *
 */
public class NvrsinaContentRe implements ReProcessor{

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace parseFace) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		// 取到解析结果
		Map<String, Object> resultData = result.getParsedata().getData();
		//"post_time": "2017-12-17 21:23:51 来源： 新浪游戏"
		if(resultData.containsKey(Constants.POST_TIME)){
			String posttime = resultData.get(Constants.POST_TIME).toString();
			String[] list = posttime.split("来源：");
			if(list.length > 0){
				posttime = list[0];
			}
			resultData.put(Constants.POST_TIME, posttime);
		}
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}

}
