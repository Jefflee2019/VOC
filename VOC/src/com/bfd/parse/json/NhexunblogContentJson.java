package com.bfd.parse.json;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.client.URLNormalizerClient;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.TextUtil;

/**
 * @site：和讯博客 (Nhexunblog)
 * @function：内容页 Json插件 获取作者、浏览数、评论数
 * 
 * @author bfd_02
 */
public class NhexunblogContentJson implements JsonParser {

	private static final Log LOG = LogFactory.getLog(NhexunblogContentJson.class);

	@Override
	public JsonParserResult parse(Map<String, Object> taskdata, List<JsonData> dataList,
			URLNormalizerClient normalizerClient, ParseUnit unit) {
		int parsecode = 0;
		Map<String, Object> parsedata = new HashMap<String, Object>();
		for (JsonData data : dataList) {
			if (!data.downloadSuccess()) {
				continue;
			}
			String json = TextUtil.getUnzipJson(data, unit);
			try {
				executeParse(parsedata, json, data.getUrl(), unit);
			} catch (Exception e) {
				LOG.warn("JsonParser exception, taskdata url=" + taskdata.get("url") + ".jsonUrl :" + data.getUrl(), e);
			}
		}
		JsonParserResult result = new JsonParserResult();
		try {
			result.setData(parsedata);
			result.setParsecode(parsecode);
		} catch (Exception e) {
			LOG.error("jsonparser reprocess error url:" + taskdata.get("url"));
		}
		return result;
	}

	public void executeParse(Map<String, Object> parsedata, String json, String url, ParseUnit unit) {
		try {
			// 获取作者字段
			if (url.contains("userid") && json.contains("主人：")) {
				String useridReg = "主人：\\s*(\\S*)";
				String author = regMatch(useridReg, json);
				if (!author.equals("")) {
					parsedata.put(Constants.AUTHOR, author);
				}
			} else if (url.contains("blogid")) {
				String viewReg = "getElementById(\"articleClickCount\").innerHTML\\s*=\\s*(\\d+)";
				String viewCnt = regMatch(viewReg, json);
				if (!viewCnt.equals("")) {
					// 浏览数
					parsedata.put(Constants.VIEW_CNT, Integer.parseInt(viewCnt));
				}
				String commReg = "getElementById(\"articleCommentCount\").innerHTML\\s*=\\s*(\\d+)";
				String replyCnt = regMatch(commReg, json);
				if (!replyCnt.equals("")) {
					// 评论数
					parsedata.put(Constants.REPLY_CNT, Integer.parseInt(replyCnt));
				}
			}
		} catch (Exception e) {
			LOG.error("excuteParser error url is :" + url);
		}
	}

	private String regMatch(String useridReg, String json) {
		Matcher match = Pattern.compile(useridReg).matcher(json);
		if (match.find()) {
			String result = match.group(1);
			return result;
		}
		return "";
	}
}