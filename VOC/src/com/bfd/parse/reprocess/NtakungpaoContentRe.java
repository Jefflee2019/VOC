package com.bfd.parse.reprocess;

import java.util.HashMap;
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
 * @site:大公网 (Ntakungpao)
 * @function 新闻内容页后处理插件
 * 
 * @author bfd_02
 *
 */

public class NtakungpaoContentRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(NtakungpaoContentRe.class);

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = new HashMap<String, Object>();

		// 解析数据
		resultData = result.getParsedata().getData();
		if (resultData == null) {
			LOG.warn("未找到解析数据");
			return null;
		}

		/**
		 * @param post_time发表时间
		 * @function 格式化post_time "2017-04-11 02:46:39 |"
		 * 
		 */

		if (resultData.containsKey(Constants.POST_TIME)) {
			String postTime = (String) resultData.get(Constants.POST_TIME);
			if (postTime.contains("|")) {
				postTime = postTime.replace("|", "").trim();
			}
			postTime = ConstantFunc.convertTime(postTime);
			resultData.put(Constants.POST_TIME, postTime);
		}
		
		/**
		 * @param author
		 * @function 格式化author:"作者：张子乾|"
		 */
		
		if (resultData.containsKey(Constants.AUTHOR)) {
			String author = resultData.get(Constants.AUTHOR).toString();
			if (author.contains("作者：") || author.contains("|")) {
				author = author.replace("作者：", "").trim();
				author = author.replace("|", "").trim();
			}
			resultData.put(Constants.AUTHOR, author);
		}
		
		/**
		 * @param source
		 * @function 格式化source:"来源：燕赵都市网"
		 */
		
		if(resultData.containsKey(Constants.SOURCE)) {
			String source = resultData.get(Constants.SOURCE).toString();
			if(source.contains("来源：")) {
				source = source.replace("来源：", "").trim();
			}
			resultData.put(Constants.SOURCE, source);
		}
			
		
		 ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
}