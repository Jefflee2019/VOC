package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.entity.Constants;

/**
 * 站点名：Nkdslife
 * 
 * 功能：标准化部分字段
 * 
 * @author bfd_06
 */
public class NkdslifeCommentRe implements ReProcessor {

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;

		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();

		if (resultData.containsKey(Constants.COMMENTS)) {
			List<Map<String, Object>> comments = (List<Map<String, Object>>) resultData
					.get(Constants.COMMENTS);
			for (Map<String, Object> comment : comments) {
				// COMMENT_CONTENT
				String commentContent = (String) comment
						.get(Constants.COMMENT_CONTENT);
				commentContent = commentContent.substring(commentContent
						.indexOf(')') + 2);
				comment.put(Constants.COMMENT_CONTENT, commentContent);
				// COMMENT_TIME
				String commentTime = (String) comment
						.get(Constants.COMMENT_TIME);
				int indexA = commentTime.indexOf('(');
				int indexB = commentTime.indexOf(')');
				commentTime = commentTime.substring(indexA + 1, indexB);
				comment.put(Constants.COMMENT_TIME, commentTime);
			}
		}

		return new ReProcessResult(processcode, processdata);
	}
	
}
