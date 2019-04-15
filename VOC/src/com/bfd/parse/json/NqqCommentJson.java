package com.bfd.parse.json;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.crawler.utils.JsonUtils;
import com.bfd.parse.client.URLNormalizerClient;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.TextUtil;

/**
 * 站点：腾讯网 功能：分离评论
 * 
 * @author bfd_06
 */
public class NqqCommentJson implements JsonParser {
	private final static Log LOG = LogFactory.getLog(NqqCommentJson.class);
	private final static Pattern COMMENTIDPATTERN = Pattern
			.compile("commentid=(\\d+)");

	@Override
	public JsonParserResult parse(Map<String, Object> taskdata,
			List<JsonData> dataList, URLNormalizerClient normalizerClient,
			ParseUnit unit) {
		int parseCode = 0;
		Map<String, Object> parsedata = new HashMap<String, Object>();
		for (JsonData data : dataList) {
			if (!data.downloadSuccess()) {
				continue;
			}
			String json = TextUtil.getUnzipJson(data, unit);
			try {
				executeParse(parsedata, json, data.getUrl(), unit);
			} catch (Exception e) {
				LOG.error("executeParse() exception",e);
			}

		}
		JsonParserResult result = new JsonParserResult();
		try {
			result.setData(parsedata);
			result.setParsecode(parseCode);
		} catch (Exception e) {
			LOG.error("result set exception",e);
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public void executeParse(Map<String, Object> parsedata, String json,
			String url, ParseUnit unit) {
		try {
			Map<String, Object> jsonMap = (Map<String, Object>) JsonUtils
					.parseObject(json).get("data");
			List<Map<String, Object>> comments = null;
			boolean hasNext = false;
			if (jsonMap != null) {
				comments = (List<Map<String, Object>>) jsonMap.get("commentid");
				hasNext = (boolean) jsonMap.get("hasnext");
			}
			int total = (int) jsonMap.get("total");
			List<Map<String, Object>> rtasks = new ArrayList<Map<String, Object>>();
			parsedata.put(Constants.TASKS, rtasks);
			/**
			 * 标准化评论
			 */
			if (comments != null && !comments.isEmpty()) {
				List<Map<String, Object>> rcomments = new ArrayList<Map<String, Object>>();
				for (Map<String, Object> comment : comments) {
					// COMMENT_TIME
					long time = (int) comment.get("time");
					Date date = new Date((time * 1000));
					SimpleDateFormat format = new SimpleDateFormat(
							"yyyy-MM-dd HH:mm:ss");
					Map<String, Object> reMap = new HashMap<String, Object>();// 单个回复容器
					// COMMENT_CONTENT
					reMap.put(Constants.COMMENT_CONTENT, comment.get("content"));
					reMap.put(Constants.COMMENT_TIME, format.format(date));
					// COM_REPLY_CNT
					reMap.put(Constants.COM_REPLY_CNT, comment.get("rep"));
					// UP_CNT
					reMap.put(Constants.UP_CNT, comment.get("up"));
					// USERNAME
					Map<String, Object> userinfo = (Map<String, Object>) comment
							.get("userinfo");
					reMap.put(Constants.USERNAME, userinfo.get("nick"));
					rcomments.add(reMap);// 添加单个回复至总的回复
				}
				parsedata.put("comments", rcomments); // parseResult body
				parsedata.put(Constants.REPLY_CNT, total); // parseResult body

				/**
				 * 处理下一页
				 */
				Matcher mCommId = COMMENTIDPATTERN.matcher(url);
				if (comments != null && hasNext && mCommId.find()) {
					// get last comment id
					String lastCommId = (String) comments.get(
							comments.size() - 1).get("id");
					String currCommId = mCommId.group(1);
					String nextUrl = url;
					nextUrl = nextUrl.replace("commentid=" + currCommId,
							"commentid=" + lastCommId);
					Map<String, Object> rtask = new HashMap<String, Object>();
					rtask.put("link", nextUrl);
					rtask.put("rawlink", nextUrl);
					rtask.put("linktype", "newscomment");
					rtasks.add(rtask);
					parsedata.put(Constants.NEXTPAGE, rtask);
				}

				LOG.info("url:" + url + "The result is "
						+ JsonUtils.toJSONString(parsedata));
			}
		} catch (Exception e) {
			LOG.error("executeParse() error " + url);
		}
	}
}
