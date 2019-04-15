package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.Map;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;
/**
 * 站点名：移动叔叔
 * 
 * 主要功能：处理发表时间和作者字段
 * 
 * @author bfd_01
 */
public class NydssContentRe implements ReProcessor {
//	private static final Log LOG = LogFactory.getLog(NydssContentRe.class);
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		// 取到解析结果
		Map<String, Object> resultData = result.getParsedata().getData();
		if(resultData != null && !resultData.isEmpty()) {
			// 2017-2-9 09:30| 发布者: muscle| 查看: 3011| 评论: 0|原作者: twin
			if (resultData.containsKey(Constants.POST_TIME)) {
				String posttime = resultData.get(Constants.POST_TIME).toString();
				String author = null;
				String[] item = posttime.split("\\|");
				if (item.length == 5 && item[4].contains("原作者:")) {
					author = item[4].replace("原作者:", "").trim();
				}
				resultData.put(Constants.AUTHOR, author);
				resultData.put(Constants.POST_TIME, item[0]);
			}
				// 后处理插件加上iid
				ParseUtils.getIid(unit, result);
		}
		return new ReProcessResult(processcode, processdata);
	}
}
