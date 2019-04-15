package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;

/**
 * @site:驱动之家 (Bmydrivers)
 * @function 论坛用户信息页后处理插件
 * 
 * @author bfd_04
 *
 */

public class BmydriversInfoRe implements ReProcessor {
	@SuppressWarnings("unused")
	private static final Log LOG = LogFactory.getLog(BmydriversInfoRe.class);

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = new HashMap<String, Object>();
		// 解析数据
		resultData = result.getParsedata().getData();
		if (resultData == null) {
			LOG.info("未获取到解析数据");
			return null;
		}

		/**
		 *  "reg_time": "注册时间2013-10-26 09:51", 
		 */
		if (resultData.containsKey(Constants.REG_TIME)) {
			String regTime = (String) resultData.get(Constants.REG_TIME);
			regTime = regTime.replace("注册时间", "").trim();
			resultData.put(Constants.REG_TIME, regTime);
		}

		/**
		 *    "forum_score": "积分: 14", 
		 */
		if (resultData.containsKey(Constants.FORUM_SCORE)) {
			String froumScore = (String) resultData.get(Constants.FORUM_SCORE);
			froumScore = froumScore.replace("积分:", "").trim();
			resultData.put(Constants.FORUM_SCORE, froumScore);
		}

		/**
		 *  "topiccnt": "主题数: 0"
		 */
		if (resultData.containsKey(Constants.TOPICCNT)) {
			String topicCnt = (String) resultData.get(Constants.TOPICCNT);
			topicCnt = topicCnt.replace("主题数:", "").trim();
			resultData.put(Constants.TOPICCNT, topicCnt);
		}
		/**
		 *     "experience_cnt": "积分: 14", 
		 */
		if (resultData.containsKey(Constants.EXPERIENCE_CNT)) {
			String expCnt = (String) resultData.get(Constants.EXPERIENCE_CNT);
			expCnt = expCnt.replace("积分:", "").trim();
			resultData.put(Constants.EXPERIENCE_CNT, expCnt);
		}
		/**
		 *   "contribute_cnt": "声望: 0",
		 */
		if (resultData.containsKey(Constants.CONTRIBUTE_CNT)) {
			String contrCnt = (String) resultData.get(Constants.CONTRIBUTE_CNT);
			contrCnt = contrCnt.replace("声望:", "").trim();
			resultData.put(Constants.CONTRIBUTE_CNT, contrCnt);
		}
		/**
		 *    "lastLogin_time": "上次发表时间: 2013-11-12 10:08"
		 */
		if (resultData.containsKey(Constants.LASTLOGIN_TIME)) {
			String lLoginTime = (String) resultData.get(Constants.LASTLOGIN_TIME);
			lLoginTime = lLoginTime.replace("上次发表时间:", "").trim();
			resultData.put(Constants.LASTLOGIN_TIME, lLoginTime);
		}
		/**
		 *  "reg_time": "注册时间: 2013-11-12 08:39",
		 */
		 if (resultData.containsKey(Constants.REG_TIME)) {
				String lLoginTime = (String) resultData.get(Constants.REG_TIME);
				lLoginTime = lLoginTime.replace("注册时间:", "").trim();
				resultData.put(Constants.REG_TIME, lLoginTime);
			}
		/**
		 * "goodFriend_num": "好友数: 0"
		 */
		if (resultData.containsKey(Constants.GOODFRIEND_NUM)) {
			String gFriendNum = (String) resultData.get(Constants.GOODFRIEND_NUM);
			gFriendNum = gFriendNum.replace("好友数:", "").trim();
			resultData.put(Constants.GOODFRIEND_NUM, gFriendNum);
		}
		/**
		 * "post_cnt": "帖子数: 2536", 
		 */
		if (resultData.containsKey(Constants.POST_CNT)) {
			String gFriendNum = (String) resultData.get(Constants.POST_CNT);
			gFriendNum = gFriendNum.replace("帖子数:", "").trim();
			resultData.put(Constants.POST_CNT, gFriendNum);
		}
		//ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
}
