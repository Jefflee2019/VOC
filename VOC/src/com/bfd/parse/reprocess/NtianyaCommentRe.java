package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.entity.Constants;

/**
 * 站点名：Ntianya
 * 
 * 功能：去掉评论内容前面的空格
 * 
 * @author bfd_06
 */
public class NtianyaCommentRe implements ReProcessor {

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;

		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		
		if(resultData.containsKey(Constants.COMMENTS)){
			List<Map<String, Object>> comments = (List<Map<String, Object>>) resultData.get(Constants.COMMENTS);
			for(Map<String, Object> comment:comments){
				String commentContent = (String) comment.get(Constants.COMMENT_CONTENT);
				commentContent = commentContent.substring(2);
				comment.put(Constants.COMMENT_CONTENT, commentContent);
			}
		}
		
		return new ReProcessResult(processcode, processdata);
	}
	
}
