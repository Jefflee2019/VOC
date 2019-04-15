package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

/**
 * @site: Nxinmin 新民网
 * @function: 处理评论的时间
 * @author bfd_04
 */
public class NxinminCommentRe implements ReProcessor {

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		if(resultData != null && !resultData.isEmpty() ){
			if(resultData.containsKey(Constants.COMMENTS)) {
				List commList = (List) resultData.get(Constants.COMMENTS);
				/**
				 *  "commentTime": "发表日期：2015-11-09 16:07:48"
				 */
				if(commList != null && !commList.isEmpty()) {
					for(Object obj : commList) {
						Map tempMap = (Map)obj;
						if (tempMap.containsKey(Constants.COMMENT_TIME)) {
							String commentTime = tempMap.get(Constants.COMMENT_TIME).toString();
//							commentTime = ConstantFunc.convertTime(commentTime);
							commentTime = commentTime.replace("发表日期：", "").trim();
							tempMap.put(Constants.COMMENT_TIME, commentTime);
						}
					}
				}
			}
		}
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
}
