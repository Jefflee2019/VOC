package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.Map;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
/**
 * 站点名：百信手机网
 * <p>
 * 主要功能：处理字段中的多余信息
 * @author bfd_01
 *
 */
public class N958shopContentRe implements ReProcessor {
	
	
	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {

		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		// 取到解析结果
		Map<String, Object> resultData = result.getParsedata().getData();
		if (resultData.containsKey(Constants.AUTHOR)) {
			String author = resultData.get(Constants.AUTHOR).toString();
			author = author.replace("作者：", "");
			resultData.put(Constants.AUTHOR, author);
		}
		if (resultData.containsKey(Constants.SOURCE)) {
			String source = resultData.get(Constants.SOURCE).toString();
			source = source.replace("来源：", "");
			resultData.put(Constants.SOURCE, source);
		}
		
		if (resultData.containsKey(Constants.POST_TIME)) {
			String posttime = resultData.get(Constants.POST_TIME).toString();
			// 发布时间：2015/3/13 10:30:33
			posttime = posttime.replace("发布时间：", "");
			resultData.put(Constants.POST_TIME, posttime);
		}
		return new ReProcessResult(processcode, processdata);
	}

}
