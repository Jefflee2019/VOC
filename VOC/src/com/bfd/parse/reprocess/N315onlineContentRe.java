package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.JsonUtil;
/**
 * 站点名：N315online 质量万里行
 * 
 * 主要功能：处理投诉主题，投诉时间，投诉人
 * 
 * @author bfd_03
 *
 */
public class N315onlineContentRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(N315onlineContentRe.class);
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		Map<String, Object> resultData = new HashMap<String,Object>();
		Map<String, Object> processdata = new HashMap<String, Object>();
		resultData = result.getParsedata().getData();
		if (resultData != null) {
			// deal with title
			if (resultData.containsKey(Constants.TITLE)) {
				String title = resultData.get(Constants.TITLE).toString()
						.replace("投诉主题:", "").trim();
				resultData.put(Constants.TITLE, title);
			}
			// deal with post_time
			if (resultData.containsKey(Constants.POST_TIME)) {
				String sPostTime = resultData.get(Constants.POST_TIME)
						.toString();
				sPostTime = sPostTime.replace("投诉日期:", "").replace("投诉时间：", "").trim();
				resultData.put(Constants.POST_TIME, sPostTime);
			}
			// deal with author
			if (resultData.containsKey(Constants.AUTHOR)) {
				String author = resultData.get(Constants.AUTHOR).toString();
				author = author.replace("投诉人:", "").replace("投 诉 人：", "").replace(": 投 诉 人：", "")
						.trim();
				resultData.put(Constants.AUTHOR, author);
			}
			// deal with city
			if (resultData.containsKey(Constants.CITY)) {
				String author = resultData.get(Constants.CITY).toString();
				author = author.replace("投诉地区:", "").replace("投诉地区：", "").replace(": 投诉地区：", "")
						.trim();
				resultData.put(Constants.CITY, author);
			}
			// deal with source
			if (resultData.containsKey(Constants.SOURCE)) {
				String value = resultData.get(Constants.SOURCE).toString();
				value = value.replace("作者:", "").trim();
				int index = value.indexOf("时间:");
				if (index >= 0) {
					value = value.replace("时间:", "").trim();
					index = value.indexOf("来源:");
					if (index >= 0) {
						String sPostTime = value.substring(0, index-1);
						String source = value.substring(index+3);
						resultData.put(Constants.SOURCE, source);
						resultData.put(Constants.POST_TIME, sPostTime);
					}
				}
			}

			LOG.info("url:" + unit.getUrl() + ".after reprocess rs is "
					+ JsonUtil.toJSONString(resultData));
		} else {
			LOG.info("url:" + unit.getUrl() + "result.getParsedata().getData() is null");
		}
		return new ReProcessResult(SUCCESS, processdata);
	}
}