package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.ConstantFunc;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

/**
 * @site: Nhenanfzb 河南法制报
 * @function: 处理评论的时间
 * @author bfd_04
 */
public class NhenanfzbCommentRe implements ReProcessor {

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		if(resultData != null && !resultData.isEmpty() ){
			if(resultData.containsKey(Constants.COMMENTS)) {
				List commList = (List) resultData.get(Constants.COMMENTS);
				if(commList != null && !commList.isEmpty()) {
					for(Object obj : commList) {
						Map tempMap = (Map)obj;
						if (tempMap.containsKey(Constants.COMMENT_TIME)) {
							String commentTime = tempMap.get(Constants.COMMENT_TIME).toString();
							commentTime = ConstantFunc.convertTime(commentTime);
							tempMap.put(Constants.COMMENT_TIME, commentTime);
						}
						if (tempMap.containsKey(Constants.UP_CNT)) {
							String upCnt = tempMap.get(Constants.UP_CNT).toString();
							upCnt = upCnt.replace("[", "").replace("]", "").trim();
							tempMap.put(Constants.UP_CNT, upCnt);
						}
						if (tempMap.containsKey(Constants.COMMENTER_IP)) {
							String commenterIP = tempMap.get(Constants.COMMENTER_IP).toString();
							commenterIP = commenterIP.replace("[", "").replace("]", "").trim();
							tempMap.put(Constants.COMMENTER_IP, commenterIP);
						}
					}
				}
			}
		}
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
}
