package com.bfd.parse.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.entity.ConstantFunc;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.client.URLNormalizerClient;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.JsonUtil;
import com.bfd.parse.util.TextUtil;

/**
 * 站点名：Nyidianzixun
 * 
 * 动态解析列表页
 * 
 * @author bfd_06
 * 
 */
public class NyidianzixunCommentJson implements JsonParser {

	private static final Log LOG = LogFactory.getLog(NyidianzixunCommentJson.class);

	@Override
	public JsonParserResult parse(Map<String, Object> taskdata, List<JsonData> dataList,
			URLNormalizerClient urlnormalizerClients, ParseUnit unit) {
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
			// int indexA = json.indexOf("(");
			// int indexB = json.lastIndexOf(")");
			// if (indexA >= 0 && indexB >= 0 && indexA < indexB) {
			// json = json.substring(indexA + 1, indexB);
			// }
			executeParse(parsedata, json, data.getUrl(), unit);
		}
		JsonParserResult result = new JsonParserResult();
		result.setParsecode(parsecode);
		result.setData(parsedata);
		return result;
	}

	@SuppressWarnings("unchecked")
	public void executeParse(Map<String, Object> parsedata, String json, String url, ParseUnit unit) {
		/**
		 * 加上tasks
		 */
		List<Map<String, Object>> tasks = new ArrayList<Map<String, Object>>();
		parsedata.put("tasks", tasks);
		try {
			Map<String, Object> result = (Map<String, Object>) JsonUtil.parseObject(json);
			List<Map<String, Object>> comments = (List<Map<String, Object>>) result.get("comments");
			List<Map<String, Object>> rcomments = new ArrayList<Map<String, Object>>();
			for (Map<String, Object> comment : comments) {
				Map<String, Object> rcomment = new HashMap<String, Object>();
				rcomment.put(Constants.COMMENT_CONTENT, comment.get("comment"));
				rcomment.put(Constants.USERNAME, comment.get("nickname"));
				rcomment.put(Constants.FAVOR_CNT, comment.get("like"));
				rcomment.put(Constants.COMMENT_TIME, ConstantFunc.convertTime(comment.get("createAt").toString()));
				if (comment.containsKey(Constants.COM_REPLY_CNT)) {
					rcomment.put(Constants.COM_REPLY_CNT, comment.get("reply_n"));
				}
				rcomments.add(rcomment);
				if (comment.containsKey("replies")) {
					List<Map<String, Object>> replies = (List<Map<String, Object>>) comment.get("replies");
					for (Map<String, Object> reply : replies) {
						Map<String, Object> urcomment = new HashMap<String, Object>();
						urcomment.put(Constants.REFER_COMM_CONTENT, reply.get("comment"));
						urcomment.put(Constants.REFER_COMM_USERNAME, reply.get("nickname"));
						urcomment.put(Constants.REFER_COMM_TIME,
								ConstantFunc.convertTime(reply.get("createAt").toString()));
						rcomment.put(Constants.REFER_COMMENTS, urcomment);
					}
				}
			}
			parsedata.put(Constants.COMMENTS, rcomments);

			/**
			 * 如果返回结果大于等于30则给出下一页链接
			 */
			int commentsSize = comments.size();
			if (commentsSize >= 30) {
				Map<String, Object> lastComment = comments.get(commentsSize - 1);
				String comment_id = (String) lastComment.get("comment_id");
				String nextUrl = url.replace("&count=30", "&last_comment_id=" + comment_id + "&count=30");
				addNextUrl(parsedata, nextUrl);
			}
		} catch (Exception e) {
			LOG.error("json format conversion error in the executeParse() method", e);
		}

		// System.out.println(parsedata.toString());

	}

	public int matchCstart(String regular, String matchedStr) {
		Pattern patten = Pattern.compile(regular);
		Matcher matcher = patten.matcher(matchedStr);
		if (matcher.find()) {
			return Integer.parseInt(matcher.group(1));
		}
		return 0;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void addNextUrl(Map<String, Object> parsedata, String nextUrl) {
		Map<String, Object> task = new HashMap<String, Object>();
		task.put(Constants.LINK, nextUrl);
		task.put(Constants.RAWLINK, nextUrl);
		task.put(Constants.LINKTYPE, "newscomment");
		parsedata.put(Constants.NEXTPAGE, task);
		List<Map> tasks = (List<Map>) parsedata.get("tasks");
		tasks.add(task);
	}

}
