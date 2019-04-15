package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.Map;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;
/**
 * 站点：N3310
 * 功能：处理字段规范
 * @author dph 2017年12月20日
 *
 */
public class N3310ContentRe implements ReProcessor{

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace parseFace) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		// 取到解析结果
		Map<String, Object> resultData = result.getParsedata().getData();
		if(resultData != null && !resultData.isEmpty()) {
			// "author": "小编：qqy",  
			if (resultData.containsKey(Constants.AUTHOR)) {
				String author = resultData.get(Constants.AUTHOR).toString();
				author = author.replace("小编：", "").trim();
				resultData.put(Constants.AUTHOR, author);
			}
			// "source": "来源：好卓网", 
			if (resultData.containsKey(Constants.SOURCE)) {
				String source = resultData.get(Constants.SOURCE).toString();
				source = source.replace("来源：", "").trim();
				resultData.put(Constants.SOURCE, source);
			}
			//  "post_time": "时间：2015-06-12 10:58"  
			if (resultData.containsKey(Constants.POST_TIME)) {
				String posttime = resultData.get(Constants.POST_TIME).toString();
				posttime = posttime.replace("时间：", "").trim();
				resultData.put(Constants.POST_TIME, posttime);
			}
		}
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}

}
