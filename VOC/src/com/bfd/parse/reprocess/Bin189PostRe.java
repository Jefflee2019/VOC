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
 * @site:添翼圈 (Bin189)
 * @function 论坛帖子页后处理插件
 * @author bfd_04
 */

public class Bin189PostRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(Bin189PostRe.class);

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
					 *   "replydate": "发表于 2014-4-13 20:26:45",
					 */
					if (replyData.containsKey(Constants.REPLYDATE)) {
						String replydate = (String) replyData.get(Constants.REPLYDATE);
						replydate = replydate.replace("发表于", "").trim();
						replydate = ConstantFunc.convertTime(replydate);
						replyData.put(Constants.REPLYDATE, replydate);
					}
				}
			}
		}
		
		/**
		 * "newstime": "发表于 2014-4-13 20:26:45"
		 */
		if (resultData.containsKey(Constants.NEWSTIME)) {
			String newstime = resultData.get(Constants.NEWSTIME).toString();
			newstime = newstime.replace("发表于", "").trim();
			newstime = ConstantFunc.convertTime(newstime);
			resultData.put(Constants.NEWSTIME, newstime);
		}
		
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
}
