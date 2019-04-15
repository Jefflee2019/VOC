package com.bfd.parse.reprocess;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.alibaba.fastjson.JSONObject;
import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

/**
 * @ClassName: NelecfansContentRe
 * @author: taihua.li
 * @date: 2019年3月25日 上午11:03:51
 * @Description:TODO(通过评论数来判定是否扩散评论及如扩散，拼接评论链接)
 */
public class NelecfansContentRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(NelecfansContentRe.class);

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = new HashMap<String, Object>();

		// 解析数据
		resultData = result.getParsedata().getData();
		if (resultData == null) {
			return null;
		}

		String url = unit.getUrl();
		// 获取评论数
		String commentCountUrl = getCommentCountUrl(url, "http://www.elecfans.com/webapi/arcinfo/arcinfo?aid=");
		if (commentCountUrl != null) {
			// 下载评论数所在页面，获取评论数
			String commentCountData = getCommentCountData(commentCountUrl);
			if (commentCountData != null) {
				Map<String, Object> commentCountMap = JSONObject.parseObject(commentCountData);
				if (commentCountMap.containsKey("replynum") || commentCountMap.containsKey("clicknum")) {
					int clicknum = Integer.parseInt(commentCountMap.get("clicknum").toString());
					resultData.put(Constants.VIEW_CNT, clicknum);
					int replynum = Integer.parseInt(commentCountMap.get("replynum").toString());
					// 评论数大于0，表示有评论，扩散评论
					if (replynum > 0) {
						getCommentTask(resultData, url);
					}
				}
			}
		}

		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}

	/**
	 * @Title: getCommentTask
	 * @Description: TODO(这里用一句话描述这个方法的作用)
	 * @param: @param resultData
	 * @param: @param url
	 * @return: void
	 * @throws
	 */
	private void getCommentTask(Map<String, Object> resultData, String url) {
		List<Map<String, Object>> tasks = new ArrayList<Map<String, Object>>();
		Map<String, Object> commentTaskMap = new HashMap<String, Object>();
		String commentUrl = getCommentCountUrl(url,
				"http://www.elecfans.com/webapi/arcinfo/apiGetCommentJson?page=1&order=new&aid=");
		commentTaskMap.put("link", commentUrl);
		commentTaskMap.put("rawlink", commentUrl);
		commentTaskMap.put("linktype", "newscomment");
		resultData.put("comment_url", commentUrl);
		tasks.add(commentTaskMap);
		resultData.put(Constants.TASKS,tasks);
	}

	/**
	 * @Title: getCommentCountData
	 * @Description: TODO(这里用一句话描述这个方法的作用)
	 * @param: @param commentCountUrl
	 * @return: void
	 * @throws
	 */
	private String getCommentCountData(String commentCountUrl) {
		String commentCountData = null;
		CloseableHttpClient client = HttpClients.createDefault();
		HttpGet request = new HttpGet(commentCountUrl);
		request.setHeader("User-Agent",
				"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36");
		CloseableHttpResponse response = null;
		try {
			response = client.execute(request);
			commentCountData = EntityUtils.toString(response.getEntity(), "gbk");

		} catch (IOException e) {
			e.printStackTrace();
			LOG.info("commentCountUrl failed to download,url :" + commentCountUrl);
		}
		return commentCountData;
	}

	/**
	 * @Title: getCommentCountUrl
	 * @Description: TODO(这里用一句话描述这个方法的作用)
	 * @param: @param unit
	 * @return: void
	 * @throws
	 */
	private String getCommentCountUrl(String url, String attach) {
		String commentcountUrl = null;
		Matcher match = Pattern.compile("/(\\d+).html").matcher(url);
		if (match.find()) {
			commentcountUrl = attach + match.group(1);
		}
		return commentcountUrl;
	}
}