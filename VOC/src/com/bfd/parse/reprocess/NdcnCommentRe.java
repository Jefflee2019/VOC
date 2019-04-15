package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;

/**
 * 站点：当乐网
 * 功能：评论后处理
 * @author dph 2017年11月7日
 *
 */
public class NdcnCommentRe implements ReProcessor{

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace parseFace) {
		Map<String,Object> resultData = result.getParsedata().getData();
		Map<String,Object> processdata = new HashMap<String, Object>(16);
		List<Map<String, Object>> comments = (List<Map<String, Object>>) resultData
				.get("comments");
		if(null != comments ){
			for(Map<String, Object> comment : comments){
				// "city": "2017-09-11 15:06:49 海南省" 
				if(comment.containsKey(Constants.CITY)){
					String city = (String) comment.get(Constants.CITY);
					String[] str = city.split(" ");
					city = str[str.length - 1];
					comment.put(Constants.CITY, city);
				}
				// "city": "2017-09-11 15:06:49 海南省" 
				if(comment.containsKey(Constants.COMMENT_TIME)){
					String time = (String) comment.get(Constants.COMMENT_TIME);
					String[] str = time.split(" ");
					time = str[0] + " " + str[1];
					comment.put(Constants.COMMENT_TIME, time);
				}
			}
		}
		return new ReProcessResult(SUCCESS, processdata);
	}

}
