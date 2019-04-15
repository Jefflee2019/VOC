package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.Map;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;
/**
 * 站点名：安卓中国
 * 
 * 主要功能：处理发表时间和作者字段
 * 
 * @author bfd_01
 */
public class NanzhuoContentRe implements ReProcessor {
//	private static final Log LOG = LogFactory.getLog(NanzhuoContentRe.class);
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		// 取到解析结果
		Map<String, Object> resultData = result.getParsedata().getData();
		if(resultData != null && !resultData.isEmpty()) {
			// 请叫我啊富 | 2017-05-24 14:36
			if (resultData.containsKey(Constants.POST_TIME)) {
				String posttime = resultData.get(Constants.POST_TIME).toString();
				String author = null;
				String[] item = posttime.split("\\|");
				if (item.length == 2) {
					author = item[0].trim();
				}
				resultData.put(Constants.AUTHOR, author);
				resultData.put(Constants.POST_TIME, item[1].trim());
			}
				// 后处理插件加上iid
				ParseUtils.getIid(unit, result);
		}
		return new ReProcessResult(processcode, processdata);
	}
}
