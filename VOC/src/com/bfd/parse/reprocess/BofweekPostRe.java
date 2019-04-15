package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.ConstantFunc;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

/**
 * @site:光电社区(Bofweek)
 * @function 论坛帖子页后处理插件
 * 
 * @author bfd_04
 *
 */

public class BofweekPostRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(BofweekPostRe.class);
//	private static final Pattern PATTERN = Pattern.compile("uid-(\\d+).html");

	@SuppressWarnings({ "unchecked", "rawtypes" })
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

		if (resultData.containsKey(Constants.REPLYS)) {
			List<Map<String, Object>> replys = (List<Map<String, Object>>) resultData.get(Constants.REPLYS);
			if(resultData.containsKey(Constants.VIEWS)) {
				replys.remove(0);
			} else {
				if(resultData.containsKey(Constants.CONTENTS)) {
					resultData.remove(Constants.CONTENTS);
				}
				if(resultData.containsKey(Constants.NEWSTIME)) {
					resultData.remove(Constants.NEWSTIME);
				}
				if(resultData.containsKey(Constants.AUTHOR)) {
					resultData.remove(Constants.AUTHOR);
				}
			}
			if (replys != null && !replys.isEmpty()) {
				for (Map replyData : replys) {

					/**
					 * 发表时间 reply_date:"replydate": "发表于 2015-1-20 10:48:13"
					 */
					if (replyData.containsKey(Constants.REPLYDATE)) {
						String replyDate = (
								String) replyData.get(Constants.REPLYDATE);
						replyDate = replyDate.replace("发表于", "").trim();
						replyDate = ConstantFunc.convertTime(replyDate);
						replyData.put(Constants.REPLYDATE, replyDate);
					}
				}
			}
		}
		/**
		 * 发表时间 标准化 "newstime": "发表于 2015-1-19 15:04:41"
		 */
		if (resultData.containsKey(Constants.NEWSTIME)) {
			String newstime = (String) resultData.get(Constants.NEWSTIME);
			newstime = newstime.replace("发表于", "");
			newstime = ConstantFunc.convertTime(newstime);
			resultData.put(Constants.NEWSTIME, newstime);
		}
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
}
