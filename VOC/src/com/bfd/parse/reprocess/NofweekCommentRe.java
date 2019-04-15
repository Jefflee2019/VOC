package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;

/**
 * @site 光电新闻网
 * @function：新闻评论页后处理
 * @author bfd_04
 */

public class NofweekCommentRe implements ReProcessor {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		if(resultData != null && resultData.containsKey("comments1")) {
			List comment1 = (List)resultData.get("comments1");
			resultData.put(Constants.COMMENTS, comment1);
			resultData.remove("comments1");
			if(resultData != null && resultData.containsKey("comments2")) {
				List comment2 = (List)resultData.get("comments2");
				comment2.addAll(comment1);
				resultData.put(Constants.COMMENTS, comment2);
				resultData.remove("comments2");
			}
		}
		if (resultData != null && resultData.containsKey("comments")) {
			List<Map<String, Object>> comments = (List<Map<String, Object>>) resultData.get("comments");
			for (Map<String, Object> comment : comments) {
				/**
				 * "username": "jackhe90[会员] 说 ：", 
				 */
				if (comment.containsKey(Constants.USERNAME)) {
					String username = comment.get(Constants.USERNAME).toString();
					username = username.replace("[会员] 说 ：", "").trim();
					comment.put(Constants.USERNAME, username);
				}
				/**
				 * "up_cnt": "[0]", 
				 */
				if (comment.containsKey(Constants.UP_CNT)) {
					String upCnt = comment.get(Constants.UP_CNT).toString();
					upCnt = upCnt.replace("[", "").replace("]", "").trim();
					comment.put(Constants.UP_CNT, upCnt);
				}
				/**
				 * "down_cnt": "[0]"
				 */
				if (comment.containsKey(Constants.DOWN_CNT)) {
					String downCnt = comment.get(Constants.DOWN_CNT).toString();
					downCnt = downCnt.replace("[", "").replace("]", "").trim();
					comment.put(Constants.DOWN_CNT, downCnt);
				}
			}
		}
		return new ReProcessResult(SUCCESS, processdata);
	}
}