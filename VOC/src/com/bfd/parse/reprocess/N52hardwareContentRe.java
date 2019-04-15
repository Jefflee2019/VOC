package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.ConstantFunc;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;
/**
 * 站点名：52硬件
 * <p>
 * 主要功能：发表时间字段处理
 * @author bfd_01
 *
 */
public class N52hardwareContentRe implements ReProcessor {
//	private static final Log LOG = LogFactory.getLog(N52hardwareContentRe.class);
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		// 取到解析结果
		Map<String, Object> resultData = result.getParsedata().getData();
		if (resultData != null && !resultData.isEmpty()) {
			if (resultData.containsKey(Constants.POST_TIME)) {
				String time = resultData.get(Constants.POST_TIME).toString();
				Pattern p = Pattern.compile("(\\d+-\\d+-\\d+)");
				Matcher m = p.matcher(time);
				while (m.find()) {
					time = m.group(1);
				}
				time = ConstantFunc.convertTime(time);
				resultData.put(Constants.POST_TIME, time);
			}
			ParseUtils.getIid(unit, result);
		}
		return new ReProcessResult(processcode, processdata);
	}
}
