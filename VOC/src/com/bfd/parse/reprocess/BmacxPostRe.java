package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.ConstantFunc;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;

/**
 * @site:麦克叉(Bmacx)
 * @function 帖子列表页
 * 
 * @author bfd_02
 *
 */

public class BmacxPostRe implements ReProcessor {
	@SuppressWarnings("unused")
	private static final Log LOG = LogFactory.getLog(BmacxPostRe.class);

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace arg2) {
		Map<String, Object> resultData = result.getParsedata().getData();
		Map<String, Object> processdata = new HashMap<String, Object>();
		if (resultData.containsKey("replys")) {
			List<Map<String, Object>> itemList = (List<Map<String, Object>>) resultData.get("replys");
			if (itemList != null && !itemList.isEmpty()) {
				for (Map tempMap : itemList) {
					// 回复时间
					if (tempMap.containsKey("replydate")) {
						String replydate = tempMap.get("replydate").toString().replace("分享", "").trim();
						// 18-7-28 12:12:12
						Matcher match = Pattern.compile("\\d{2}-\\d{1,2}-\\d{1,2}").matcher(replydate);
						if (match.find()) {
							replydate = "20" + replydate;
						} else {
							// 分享 昨天 12:42
							replydate = ConstantFunc.convertTime(replydate);
						}
						tempMap.put(Constants.REPLYDATE, replydate);
					}

					// 引用的回复
					if (tempMap.containsKey("refer_comment")) {
						String referComment = tempMap.get("refer_comment").toString();
						Map<String, Object> referComments = new HashMap<String, Object>();
						// Wally 发表于 18-8-7 12:42 可以可以。
						if (referComment.contains("发表于")) {
							String[] referArr = referComment.split("\\s");
							if (referArr.length >= 5) {
								referComments.put(Constants.REFER_COMM_USERNAME, referArr[0]);
								referComments.put(Constants.REFER_COMM_TIME, 20 + referArr[2] + " " + referArr[3]);
								referComments.put(Constants.REFER_COMM_CONTENT, referArr[4]);
							}
						}
						String replyContent = tempMap.get("replycontent").toString();
						replyContent = replyContent.replace(referComment, "").trim();
						tempMap.remove("refer_comment");
						tempMap.put(Constants.REFER_COMMENTS, referComments);
						tempMap.put(Constants.REPLYCONTENT, replyContent);
					}
				}
			}
		}

		// 发表时间 newstime
		if (resultData.containsKey("newstime")) {
			String newstime = resultData.get("newstime").toString();
			Matcher match = Pattern.compile("\\d+-\\d+-\\d+\\s*\\d+:\\d+").matcher(newstime);
			if (match.find()) {
				newstime = match.group();
				resultData.put(Constants.NEWSTIME, newstime);
			}
		}

		return new ReProcessResult(SUCCESS, processdata);
	}
}
