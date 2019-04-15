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
 * @site:太平洋电脑网游戏-新闻 (Nzolgame)
 * @function 新闻评论页后处理插件 处理楼层数和引用回复
 * 
 * @author bfd_04
 *
 */

public class NzolgameCommentRe implements ReProcessor {
	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = new HashMap<String, Object>();

		// 解析数据
		resultData = result.getParsedata().getData();
		if (resultData == null) {
			return null;
		}

		// 去掉楼层数reply_floor和refer_replyfloor中的"楼"
		if (resultData.containsKey(Constants.COMMENTS)) {
			List<Map<String, Object>> comments = (List<Map<String, Object>>) resultData.get(Constants.COMMENTS);
			for (int i = 0; i < comments.size(); i++) {
				// 各楼层回复
				Map<String,Object> commentMap = comments.get(i);
				// 存放引用回复的map
				Map<String,Object> referComments = new HashMap<String,Object>();
				/**
				 *  "replyfloor": "[34楼]", 
				 */
				if (commentMap.containsKey(Constants.REPLYFLOOR)) {
					String oldreplyfloor = commentMap.get(Constants.REPLYFLOOR).toString();
					int replyfloor = Integer.parseInt(oldreplyfloor.replace("[", "").replace("楼]", "").trim());
					commentMap.put(Constants.REPLYFLOOR, replyfloor);
				}

				if (commentMap.containsKey(Constants.COMMENT_TIME)) {
					String oldCommentTime = commentMap.get(Constants.COMMENT_TIME).toString();
					if (oldCommentTime.contains("分钟前")|oldCommentTime.contains("小时前")||oldCommentTime.contains("内")) {
						String commentTime = ConstantFunc.convertTime(oldCommentTime);
						commentMap.put(Constants.COMMENT_TIME, commentTime);
					}
				}
				//将引用部分提取出来，放置在引用map中
				if ( commentMap.containsKey(Constants.REFER_COMM_USERNAME)
						| commentMap.containsKey(Constants.REFER_COMM_CONTENT)) {
					String referCommUsername = commentMap.get(Constants.REFER_COMM_USERNAME).toString();
					String referCommContent = commentMap.get(Constants.REFER_COMM_CONTENT).toString();
					referComments.put(Constants.REFER_COMM_USERNAME,referCommUsername);
					referComments.put(Constants.REFER_COMM_CONTENT,referCommContent);
					commentMap.remove(Constants.REFER_COMM_USERNAME);
					commentMap.remove(Constants.REFER_COMM_CONTENT);
					//将引用部分整体以字段存放到回复中
					commentMap.put(Constants.REFER_COMMENTS, referComments);
				}
				
			}
		}

		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
}