package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

/**
 * 站点名：Nqqdigi
 * <p>
 * 主要功能：处理生成任务的链接
 * 
 * @author bfd_03
 *
 */
public class NqqvideoContentRe implements ReProcessor {
//	private static final Log LOG = LogFactory.getLog(NqqvideoContentRe.class);

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace parseFace) {

		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		// 取到解析结果
		Map<String, Object> resultData = result.getParsedata().getData();
		if (resultData != null && !resultData.isEmpty()) {
			if (resultData.containsKey("post_time")) {
				String posttime = resultData.get("post_time").toString();
				// 2017年09月17日发布
				Pattern p = Pattern.compile("(\\d+)\\S(\\d+)\\S(\\d+)");
				Matcher m = p.matcher(posttime);
				while (m.find()) {
					posttime = m.group(1) + "-" + m.group(2) + "-" + m.group(3);
				}
				resultData.put(Constants.POST_TIME, posttime);
			}
		}
		 ParseUtils.getIid(unit, result);
		return new ReProcessResult(processcode, processdata);
	}
}
