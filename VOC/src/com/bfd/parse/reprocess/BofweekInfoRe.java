package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

/**
 * @site:光电社区(Bofweek)
 * @function 论坛用户信息页后处理插件
 * 
 * @author bfd_04
 *
 */

public class BofweekInfoRe implements ReProcessor {
	@SuppressWarnings("unused")
	private static final Log LOG = LogFactory.getLog(BofweekInfoRe.class);

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
		 *"reply_cnt": "回帖数 71", 
		 */
		if (resultData.containsKey(Constants.REPLY_CNT)) {
			String replyCnt = (String) resultData.get(Constants.REPLY_CNT);
			replyCnt = replyCnt.replace("回帖数 ", "").trim();
			resultData.put(Constants.REPLY_CNT, replyCnt);
		}

		/**
		 *   "reg_time": "注册时间2014-8-20 10:10", 
		 */
		if (resultData.containsKey(Constants.REG_TIME)) {
			String regTime = (String) resultData.get(Constants.REG_TIME);
			regTime = regTime.replace("注册时间", "").trim();
			resultData.put(Constants.REG_TIME, regTime);
		}

		/**
		 * "forum_score": "积分575", 
		 */
		if (resultData.containsKey(Constants.FORUM_SCORE)) {
			String froumScore = (String) resultData.get(Constants.FORUM_SCORE);
			froumScore = froumScore.replace("积分", "").trim();
			resultData.put(Constants.FORUM_SCORE, froumScore);
		}

		/**
		 * "miid": "(UID: 229940)", 
		 */
		if (resultData.containsKey(Constants.MIID)) {
			String miid = (String) resultData.get(Constants.MIID);
			miid = miid.replace("(UID:", "");
			miid = miid.replace(")", "").trim();
			resultData.put(Constants.MIID, miid);
		}

		/**
		 *  "topiccnt": "主题数 53",  
		 */
		if (resultData.containsKey(Constants.TOPICCNT)) {
			String topicCnt = (String) resultData.get(Constants.TOPICCNT);
			topicCnt = topicCnt.replace("主题数", "").trim();
			resultData.put(Constants.TOPICCNT, topicCnt);
		}
		/**
		 *  "experience_cnt": "金币427"
		 */
		if (resultData.containsKey(Constants.EXPERIENCE_CNT)) {
			String expCnt = (String) resultData.get(Constants.EXPERIENCE_CNT);
			expCnt = expCnt.replace("金币", "").trim();
			resultData.put(Constants.EXPERIENCE_CNT, expCnt);
		}
		/**
		 *  "contribute_cnt": "贡献值148"
		 */
		if (resultData.containsKey(Constants.CONTRIBUTE_CNT)) {
			String contrCnt = (String) resultData.get(Constants.CONTRIBUTE_CNT);
			contrCnt = contrCnt.replace("贡献值", "").trim();
			resultData.put(Constants.CONTRIBUTE_CNT, contrCnt);
		}
		/**
		 * "lastLogin_time": "最后访问2015-5-18 15:53"
		 */
		if (resultData.containsKey(Constants.LASTLOGIN_TIME)) {
			String lLoginTime = (String) resultData.get(Constants.LASTLOGIN_TIME);
			lLoginTime = lLoginTime.replace("最后访问", "").trim();
			resultData.put(Constants.LASTLOGIN_TIME, lLoginTime);
		}
		/**
		 *   "goodFriend_num": "好友数 1"
		 */
		if (resultData.containsKey(Constants.GOODFRIEND_NUM)) {
			String gFriendNum = (String) resultData.get(Constants.GOODFRIEND_NUM);
			gFriendNum = gFriendNum.replace("好友数", "").trim();
			resultData.put(Constants.GOODFRIEND_NUM, gFriendNum);
		}
		//ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
}
