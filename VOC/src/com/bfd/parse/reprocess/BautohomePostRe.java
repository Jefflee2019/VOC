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
import com.bfd.parse.util.ParseUtils;

/**
 * @site:汽车之家 (Bautohome)
 * @function 论坛帖子页后处理插件
 * @author bfd_04
 */

public class BautohomePostRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(BautohomePostRe.class);

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = new HashMap<String, Object>();
		// 解析数据
		resultData = result.getParsedata().getData();
		if (resultData == null) {
			LOG.info("未获取到解析数据");
			return null;
		}

		if (resultData.containsKey(Constants.REPLYS)) {
			List<Map<String, Object>> replys = (List<Map<String, Object>>) resultData
					.get(Constants.REPLYS);
			if (!resultData.containsKey(Constants.VIEWS)) {
				if (resultData.containsKey(Constants.CONTENTS)) {
					resultData.remove(Constants.CONTENTS);
				}
				if (resultData.containsKey(Constants.NEWSTIME)) {
					resultData.remove(Constants.NEWSTIME);
				}
				if (resultData.containsKey(Constants.AUTHOR)) {
					resultData.remove(Constants.AUTHOR);
				}
			}
			if (!replys.isEmpty()) {
				for (Map replyData : replys) {

					/**
					 * "reg_time": "注册：2014年12月17日"
					 */
					if (replyData.containsKey(Constants.REPLY_REG_TIME)) {
						String replyRegTime = (String) replyData
								.get(Constants.REPLY_REG_TIME);
						replyRegTime = replyRegTime.replace("注册：", "").trim();
						replyData.put(Constants.REPLY_REG_TIME, replyRegTime);
					}
					/**
					 * "essence_cnt": "精华：0帖"
					 */
					if (replyData.containsKey(Constants.REPLY_ESSENCE_CNT)) {
						String replyEssenceCnt = (String) replyData
								.get(Constants.REPLY_ESSENCE_CNT);
						replyEssenceCnt = replyEssenceCnt.replace("精华：", "")
								.replace("帖", "").trim();
						replyData.put(Constants.REPLY_ESSENCE_CNT,
								replyEssenceCnt);
					}
					/**
					 * "reply_post_cnt": "1011回"
					 */
					if (replyData.containsKey("reply_reply_cnt")) {
						String replyReplyCnt = (String) replyData
								.get("reply_reply_cnt");
						replyReplyCnt = replyReplyCnt.replace("回", "").trim();
						replyData.put("reply_reply_cnt", replyReplyCnt);
					}
					/**
					 * "post_cnt": "9帖"
					 */
					if (replyData.containsKey(Constants.REPLY_POST_CNT)) {
						String replyPostCnt = (String) replyData
								.get(Constants.REPLY_POST_CNT);
						replyPostCnt = replyPostCnt.replace("帖", "").trim();
						replyData.put(Constants.REPLY_POST_CNT, replyPostCnt);
					}
					/**
					 * 楼层数 replyfloor": "2#
					 * 
					 * @function 去掉 "#"
					 */
					if (replyData.containsKey(Constants.REPLYFLOOR)) {
						String oldReplyfloor = (String) replyData
								.get(Constants.REPLYFLOOR);

						if (!oldReplyfloor.equals("")) {
							if (oldReplyfloor.contains("楼主")) {
								String newReplyfloor = oldReplyfloor.replace(
										oldReplyfloor, "1");
								replyData.put(Constants.REPLYFLOOR,
										newReplyfloor);
							} else if (oldReplyfloor.contains("沙发")) {
								String newReplyfloor = oldReplyfloor.replace(
										oldReplyfloor, "2");
								replyData.put(Constants.REPLYFLOOR,
										newReplyfloor);
							} else if (oldReplyfloor.contains("板凳")) {
								String newReplyfloor = oldReplyfloor.replace(
										oldReplyfloor, "3");
								replyData.put(Constants.REPLYFLOOR,
										newReplyfloor);
							} else if (oldReplyfloor.contains("地板")) {
								String newReplyfloor = oldReplyfloor.replace(
										oldReplyfloor, "4");
								replyData.put(Constants.REPLYFLOOR,
										newReplyfloor);
							} else if (oldReplyfloor.contains("楼")) {
								String newReplyfloor = oldReplyfloor.replace(
										"楼", "").trim();
								replyData.put(Constants.REPLYFLOOR,
										newReplyfloor);
							}
						}

					}
				}
			}
		}
		/**
		 * deal with author
		 */
		if (resultData.containsKey(Constants.AUTHOR)) {
			List authorList = (List) resultData.get(Constants.AUTHOR);
			if (!authorList.isEmpty()) {
				Map author = (Map) authorList.get(0);
				/**
				 * "reg_time": "注册：2014年12月17日"
				 */
				if (author.containsKey(Constants.REG_TIME)) {
					String regTime = (String) author.get(Constants.REG_TIME);
					regTime = regTime.replace("注册：", "").trim();
					author.put(Constants.REG_TIME, regTime);
				}
				/**
				 * "essence_cnt": "精华：0帖"
				 */
				if (author.containsKey(Constants.ESSENCE_CNT)) {
					String essenceCnt = (String) author
							.get(Constants.ESSENCE_CNT);
					essenceCnt = essenceCnt.replace("精华：", "").replace("帖", "")
							.trim();
					author.put(Constants.ESSENCE_CNT, essenceCnt);
				}
				/**
				 * "reply_cnt": "1011回"
				 */
				if (author.containsKey(Constants.REPLY_CNT)) {
					String replyPostCnt = (String) author
							.get(Constants.REPLY_CNT);
					replyPostCnt = replyPostCnt.replace("回", "").trim();
					author.put(Constants.REPLY_CNT, replyPostCnt);
				}
				/**
				 * "post_cnt": "9帖"
				 */
				if (author.containsKey(Constants.POST_CNT)) {
					String postCnt = (String) author.get(Constants.POST_CNT);
					postCnt = postCnt.replace("帖", "").trim();
					author.put(Constants.POST_CNT, postCnt);
				}
			}
		}
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
}
