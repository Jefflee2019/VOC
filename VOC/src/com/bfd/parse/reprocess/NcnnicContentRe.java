package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
/**
 * 站点名：CNNIC
 * <p>
 * 主要功能：处理下一页链接
 * @author bfd_01
 *
 */
public class NcnnicContentRe implements ReProcessor {
//	private static final Log LOG = LogFactory.getLog(NcnnicContentRe.class);
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		// 取到解析结果
		Map<String, Object> resultData = result.getParsedata().getData();
		if (resultData != null && !resultData.isEmpty()) {
			// 2013年11月18日 11:51
			if (resultData.containsKey(Constants.POST_TIME)) {
				String time = resultData.get(Constants.POST_TIME).toString();
				time = time.replace("年", "-").replace("月", "-").replace("日", "");
				resultData.put(Constants.POST_TIME, time);
			}
		}
		return new ReProcessResult(processcode, processdata);
	}
	
}
