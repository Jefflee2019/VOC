package com.bfd.parse.json;

import java.net.URLDecoder;
import java.util.ArrayList;
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
import com.bfd.parse.util.JsonUtil;
import com.bfd.parse.util.TextUtil;

/**
 * 
 * 
 * @author bfd_06
 * 
 */
public class NsinablogCommentJson implements JsonParser {

	private static final Log LOG = LogFactory
			.getLog(NsinablogCommentJson.class);

	@Override
	public JsonParserResult parse(Map<String, Object> taskdata,
			List<JsonData> dataList, URLNormalizerClient urlnormalizerClients,
			ParseUnit unit) {
		int parsecode = 0;
		Map<String, Object> parsedata = new HashMap<String, Object>();
		/**
		 * JsonData为List的原因为jsEngine有时会请求好几个链接
		 */
		for (Object obj : dataList) {
			JsonData data = (JsonData) obj;
			if (!data.downloadSuccess()) {
				continue;
			}
			String json = TextUtil.getUnzipJson(data, unit);
			executeParse(parsedata, json, data.getUrl(), unit);
		}
		JsonParserResult result = new JsonParserResult();
		result.setParsecode(parsecode);
		result.setData(parsedata);
		return result;
	}

	@SuppressWarnings("unchecked")
	public void executeParse(Map<String, Object> parsedata, String json,
			String url, ParseUnit unit) {
		List<Map<String, Object>> tasks = new ArrayList<Map<String, Object>>();
		parsedata.put(Constants.TASKS, tasks);
		try {
			Map<String, Object> result = (Map<String, Object>) JsonUtil
					.parseObject(json);
			Map<String, Object> data = ((Map<String, Object>) result
					.get("data"));
			List<Map<String, Object>> comment_data = (List<Map<String, Object>>) data
					.get("comment_data");
			List<Map<String, Object>> comments = new ArrayList<Map<String, Object>>();
			for (Map<String, Object> ucomment : comment_data) {
				Map<String, Object> comment = new HashMap<String, Object>();
				comment.put(Constants.USERNAME, ucomment.get("uname"));
				String commentContent = (String) ucomment.get("cms_body");
				commentContent = URLDecoder.decode(commentContent, "UTF-8");
				comment.put(Constants.COMMENT_CONTENT, commentContent);
				comment.put(Constants.COMMENT_TIME, ucomment.get("cms_pubdate"));
				comment.put(Constants.COM_REPLY_CNT,
						ucomment.get("cms_reply_num"));
				comments.add(comment);
			}
			parsedata.put("comments", comments);
			int comment_total_num = Integer.parseInt((String) data
					.get("comment_total_num"));
			int pageNum = matchInt("_(\\d+).html", url);
			if (50 * pageNum < comment_total_num) {
				String nextUrl = url.replace(pageNum + ".html", pageNum + 1
						+ ".html");
				addNextUrl(nextUrl, parsedata);
			}

			// System.out.println(parsedata.toString());

		} catch (Exception e) {
			LOG.error(
					"json format conversion error in the executeParse() method",
					e);
		}
	}

	public int matchInt(String regular, String matchedStr) {
		Pattern patten = Pattern.compile(regular);
		Matcher matcher = patten.matcher(matchedStr);
		if (matcher.find())
			return Integer.parseInt(matcher.group(1));

		return 1;
	}

	public Matcher matchTopicId(String regular, String matchedStr) {
		Pattern patten = Pattern.compile(regular);
		Matcher matcher = patten.matcher(matchedStr);

		return matcher;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void addNextUrl(String nextUrl, Map<String, Object> parsedata) {
		Map<String, String> task = new HashMap<String, String>();
		task.put("link", nextUrl);
		task.put("rawlink", nextUrl);
		task.put("linktype", "newscomment");
		parsedata.put(Constants.NEXTPAGE, task);
		List<Map> tasks = (List<Map>) parsedata.get("tasks");
		tasks.add(task);
	}

}
