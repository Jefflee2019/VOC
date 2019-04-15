package com.bfd.parse.reprocess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.ConstantFunc;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;
/**
 * 站点：亚马逊海外
 * 功能：评论页时间处理
 * @author dph 2018年1月24日
 *
 */
public class EamazonukCommentRe implements ReProcessor{

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace parseFace) {
		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		// "comment_time": "on 11 January 2018"
		if(resultData.containsKey(Constants.COMMENTS)){
			ArrayList<Map<String,String>> commentsMapList = (ArrayList<Map<String, String>>) resultData.get(Constants.COMMENTS);
			for(Map<String,String> commentsMap : commentsMapList){
				if (commentsMap.containsKey(Constants.COMMENT_TIME)) {
					String commentTime = commentsMap.get(Constants.COMMENT_TIME);
					commentTime = commentTime.replace("on", "").trim();
					commentTime = ConstantFunc.convertUKTime(commentTime);
					commentsMap.put(Constants.COMMENT_TIME, commentTime);
				}
			}
		}

		ParseUtils.getIid(unit, result);
		return new ReProcessResult(processcode, processdata);
	}

}
