package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.Map;


import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
/**
 * 站点名：我爱卡
 * <p>
 * 主要功能：处理来源字段
 * @author bfd_01
 *
 */
public class N51creditContentRe implements ReProcessor {
//	private static final Log LOG = LogFactory.getLog(N51creditContentRe.class);
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		// 取到解析结果
		Map<String, Object> resultData = result.getParsedata().getData();
		if (resultData != null && !resultData.isEmpty()) {
			if(resultData.containsKey(Constants.SOURCE)){
				String source = resultData.get(Constants.SOURCE).toString();
				source = source.replace("来源：", "");
				resultData.put(Constants.SOURCE, source);
			}
			
		}
		return new ReProcessResult(processcode, processdata);
	}
}
