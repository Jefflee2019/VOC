package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.Map;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;
/**
 * 站点：NVR陀螺
 * 功能：规范发表时间字段
 * @author dph 2017年12月25日
 *
 */
public class NvrtuoluoContentRe implements ReProcessor{

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace parseFace) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		// 取到解析结果
		Map<String, Object> resultData = result.getParsedata().getData();
		//"post_time": "发布时间：2017-12-15 | 标签： VR文创 VR游戏 VR行业应用"
		if(resultData.containsKey(Constants.POST_TIME)){
			String posttime = resultData.get(Constants.POST_TIME).toString();
			String[] list = posttime.split(" ");
			if(list.length > 0){
				posttime = list[0];
				posttime = posttime.replace("发布时间：", "").trim();
			}
			resultData.put(Constants.POST_TIME, posttime);
		}
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}

}
