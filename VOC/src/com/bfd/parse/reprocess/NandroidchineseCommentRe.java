package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
/**
 * 站点名：安卓中文网
 * <p>
 * 主要功能：处理评论的发表时间
 * @author bfd_01
 *
 */
public class NandroidchineseCommentRe implements ReProcessor {
//	private static final Log LOG = LogFactory
//			.getLog(NandroidchineseCommentRe.class);

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {

		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		// 取到解析结果
		Map<String, Object> resultData = result.getParsedata().getData();
		if (resultData != null && !resultData.isEmpty()) {
			if (resultData.containsKey(Constants.COMMENTS)) {
				List<Map<String, Object>> comments = (List<Map<String, Object>>) resultData
						.get(Constants.COMMENTS);
				for (int i=0;i<comments.size();i++) {
					Map<String, Object> map = comments.get(i);
					Map<String, Object> refer_comments = new HashMap<String, Object>();
					if (map.containsKey(Constants.USERNAME)) {
						String username = map.get(Constants.USERNAME).toString();
						map.put(Constants.USERNAME, username.split("：")[0]);
					}
					if (map.containsKey(Constants.COMMENT_TIME)) {
						String commentTime = map.get(Constants.COMMENT_TIME).toString();
						commentTime = commentTime.replace("发表于", "");
						map.put(Constants.COMMENT_TIME, commentTime);
					}
					
					// 有对评论的回复
					if (map.containsKey(Constants.REFER_COMM_USERNAME)) {
						refer_comments.put(Constants.REFER_COMM_USERNAME, map
								.get(Constants.REFER_COMM_USERNAME).toString()
								.split("：")[0]);
						refer_comments.put(Constants.REFER_COMM_CONTENT,
								map.get(Constants.REFER_COMM_CONTENT));
						refer_comments.put(Constants.REFER_COMM_TIME,
								map.get(Constants.REFER_COMM_TIME).toString()
										.replace("发表于", ""));

						map.remove(Constants.REFER_COMM_USERNAME);
						map.remove(Constants.REFER_COMM_CONTENT);
						map.remove(Constants.REFER_COMM_TIME);
					}
					map.put(Constants.REFER_COMMENTS, refer_comments);
				}
			}
		}
		return new ReProcessResult(processcode, processdata);

	}
}
