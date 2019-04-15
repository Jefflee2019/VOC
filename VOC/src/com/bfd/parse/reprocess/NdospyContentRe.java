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
 * @sie:DOSPY (Ndospy)
 * @function 新闻内容页后处理插件
 * 
 * @author bfd_02
 *
 */

public class NdospyContentRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(NdospyContentRe.class);

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
		 * @function 格式化post_time 
		 * eg:"发表于：2017-03-31 17:15:26"
		 */

		if (resultData.containsKey(Constants.POST_TIME)) {
			String postTime = (String) resultData.get(Constants.POST_TIME);
			if (postTime.contains("发表于：")) {
				postTime = postTime.replace("发表于：", "");
			}
			postTime = ConstantFunc.convertTime(postTime);
			resultData.put(Constants.POST_TIME, postTime);
		}

		/**
		 * @param author 作者
		 * @function 格式化 author 
		 * eg:"作者:Bee"
		 */

		if (resultData.containsKey(Constants.AUTHOR)) {
			String author = (String) resultData.get(Constants.AUTHOR);
			if (author.contains("作者:")) {
				author = author.replace("作者:", "");
			}
			resultData.put(Constants.AUTHOR, author);
		}
		
		/**
		 * @param content 内容
		 * @function 清洗内容字段，其包含标题、发表时间等
		 * @keyword 无用字段，包含发表时间、来源、作者字段内容
		 */
		if (resultData.containsKey(Constants.CONTENT)) {
			String content = resultData.get(Constants.CONTENT).toString();
			if (resultData.containsKey(Constants.KEYWORD)) {
				// keyword 无用字段，用于去掉参杂在内容中的无关属性
				String keyword = resultData.get(Constants.KEYWORD).toString();
				String title = resultData.get(Constants.TITLE).toString();
				content = content.replace(keyword, "").replace(title, "").trim();
				resultData.remove(Constants.KEYWORD);
			}
			resultData.put(Constants.CONTENT, content);
		}

		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
}