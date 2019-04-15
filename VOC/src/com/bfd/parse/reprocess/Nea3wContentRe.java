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
 * @site:万维家电网 (Nea3w)
 * @function 处理发表时间、作者、来源字段
 * 
 * @author bfd_02
 *
 */

public class Nea3wContentRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(Nea3wContentRe.class);

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
		 * @param post_time
		 * @function 清洗字段
		 *  作者： 
		 *  来源：万维家电网【其他】
		 *  时间：2017-05-31 09:51:35
		 *  tag:赵紫龙丨2016-12-02 11:18:43
		 */

		if (resultData.containsKey(Constants.TAG)) {
			String tag = resultData.get(Constants.TAG).toString();
			if (tag.contains("丨")) {
				String[] tagarr = tag.split("丨");
				resultData.put(Constants.AUTHOR, tagarr[0].trim());
				resultData.put(Constants.POST_TIME, ConstantFunc.convertTime(tagarr[1].trim()));
				resultData.remove(Constants.TAG);
			}
		} else {
			if (resultData.containsKey(Constants.AUTHOR)) {
				String author = resultData.get(Constants.AUTHOR).toString();
				if (author.contains("作者：")) {
					author = author.replace("作者：", "");
				}
				resultData.put(Constants.AUTHOR, author);
			}

			if (resultData.containsKey(Constants.POST_TIME)) {
				String posttime = resultData.get(Constants.POST_TIME).toString();
				if (posttime.contains("时间：")) {
					posttime = posttime.replace("时间：", "").trim();
				}
				resultData.put(Constants.POST_TIME, ConstantFunc.convertTime(posttime));
			}

			if (resultData.containsKey(Constants.SOURCE)) {
				String source = resultData.get(Constants.SOURCE).toString();
				if (source.contains("来源：")) {
					source = source.replace("来源：", "");
				}
				resultData.put(Constants.SOURCE, source);
			}
		}

		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
}