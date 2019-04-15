package com.bfd.parse.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import com.bfd.parse.client.URLNormalizerClient;
import com.bfd.parse.entity.ConstantFunc;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.JsonUtil;
import com.bfd.parse.util.TextUtil;

/**
 * @site 太平洋电脑网-新闻(Npconline)
 * @function 新闻评论页 评论部分以及下一页问题
 * @author bfd_02
 *
 */

public class NpconlineCommentJson implements JsonParser {
	private static final Log LOG = LogFactory.getLog(NpconlineCommentJson.class);

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
				if (json.indexOf("[") >= 0 && json.indexOf("]") >= 0 && (json.indexOf("[") < json.indexOf("{"))) {
					json = json.substring(json.indexOf("["), json.lastIndexOf("]") + 1);
				} else if (json.indexOf("{") >= 0 && json.indexOf("}") > 0) {
					json = json.substring(json.indexOf("{"), json.lastIndexOf("}") + 1);
				}
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

	@SuppressWarnings("unchecked")
	public void executeParse(Map<String, Object> parsedata, String json, String url, ParseUnit unit) {
		try {
			Object obj = JsonUtil.parseObject(json);
			List<Map<String, Object>> taskList = new ArrayList<Map<String, Object>>();
			parsedata.put(Constants.TASKS, taskList);
			if (obj instanceof Map) {
				Map<String, Object> data = (Map<String, Object>) obj;
				if (data.containsKey("data")) {
					List<Map<String, Object>> tempList = new ArrayList<Map<String, Object>>();
					List<Map<String, Object>> dataList = (List<Map<String, Object>>) data.get("data");
					for (Map<String, Object> dataMap : dataList) {
						// 临时存放数据的map
						HashMap<String, Object> tempmap = new HashMap<String, Object>();
						// 评论人昵称
						if (dataMap.containsKey("nickName")) {
							String username = dataMap.get("nickName").toString();
							tempmap.put(Constants.USER_NAME, username);
						}

						// 评论内容
						if (dataMap.containsKey("content")) {
							String commentContent = dataMap.get("content").toString();
							tempmap.put(Constants.COMMENT_CONTENT, commentContent);
						}

						// 评论时间
						if (dataMap.containsKey("createTime")) {
							String commentTime = dataMap.get("createTime").toString();
							tempmap.put(Constants.COMMENT_TIME, ConstantFunc.convertTime(commentTime));
						}

						// 支持数
						if (dataMap.containsKey("support")) {
							String oldUpCnt = dataMap.get("support").toString();
							tempmap.put(Constants.UP_CNT, Integer.parseInt(oldUpCnt));
						}

						// 反对数
						if (dataMap.containsKey("oppose")) {
							String oldOpposeCnt = dataMap.get("oppose").toString();
							tempmap.put(Constants.AGAIN_CNT, Integer.parseInt(oldOpposeCnt));
						}

						tempList.add(tempmap);
					}
					parsedata.put(Constants.COMMENTS, tempList);
				}

				/**
				 * @param support_cnt 内容点赞数
				 */
				// 内容点赞数
						Matcher match3 = Pattern.compile("(\\d+).html").matcher(url);
						if (match3.find()) {
							String articleId = match3.group(1);
							String dynamicLink = new StringBuffer()
									.append("http://bip.pconline.com.cn/intf/article.jsp?act=getArticleCount&siteId=1&articleId=")
									.append(articleId).toString();
							httpDownloadForSupportCnt(parsedata, url, dynamicLink);
						}else {
							parsedata.put(Constants.SUPPORT_CNT, 0);
						}

				
				// 参与人数
				// http://cmt.pconline.com.cn/action/topic/get_data.jsp?url=http://mobile.pconline.com.cn/1108/11085990.html
				if (url.contains("&pageSize")) {
					Matcher match2 = Pattern.compile("url=(\\S*)&").matcher(url);
					if (match2.find()) {
						String contentUrl = match2.group(1);
						String partakeUrl = new StringBuffer()
								.append("http://cmt.pconline.com.cn/action/topic/get_data.jsp?url=").append(contentUrl)
								.append("&pageSize=15&pageNo=1").toString();
						httpDownloadForPartakeCnt(parsedata, partakeUrl);
					}
				} else {
					parsedata.put(Constants.PARTAKE_CNT, 0);
				}
				// 评论总数
				if (data.containsKey("total")) {
					int replyCnt = Integer.parseInt(data.get("total").toString());
					parsedata.put(Constants.REPLY_CNT, replyCnt);
					// 评论下一页
					Matcher match = Pattern.compile("&pageSize=(\\d+)&pageNo=(\\d+)").matcher(url);
					if (match.find()) {
						int currPageSize = Integer.parseInt(match.group(1));
						int currPage = Integer.parseInt(match.group(2));
						if (currPage < Math.ceil((double) replyCnt / currPageSize)) {
							int page = currPage + 1;
							String nextPage = url.replaceAll("pageNo=" + currPage, "pageNo=" + page);
							Map<String, Object> nextpageTask = new HashMap<String, Object>();
							nextpageTask.put(Constants.LINK, nextPage);
							nextpageTask.put(Constants.RAWLINK, nextPage);
							nextpageTask.put(Constants.LINKTYPE, "newscomment");
							taskList.add(nextpageTask);
							parsedata.put(Constants.NEXTPAGE, nextpageTask);
							parsedata.put(Constants.TASKS, taskList);
						}
					} else {
						LOG.warn("url:" + url + "do not have comment");
					}
				} else {
					parsedata.put(Constants.REPLY_CNT, 0);
				}
			}
		} catch (Exception e) {
			LOG.error("executeParse error " + url);
		}
	}

	/**
	 * @param url
	 * @param dynamicLink
	 * @function http带cookie下载新闻页点赞数/点踩数页面
	 */
	@SuppressWarnings("unchecked")
	private void httpDownloadForSupportCnt(Map<String, Object> parsedata, String url, String dynamicLink) {
		HttpClientBuilder builder = HttpClientBuilder.create();
		builder.setUserAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36");
		HttpClient client = builder.build();
		HttpGet request = new HttpGet(dynamicLink);
		HttpResponse response;
		try {
			response = client.execute(request);
			String data = EntityUtils.toString(response.getEntity(), "gbk");
			// 格式化动态数据
			if (data.indexOf("[") >= 0 && data.indexOf("]") >= 0 && (data.indexOf("[") < data.indexOf("{"))) {
				data = data.substring(data.indexOf("["), data.lastIndexOf("]") + 1);
			} else if (data.indexOf("{") >= 0 && data.indexOf("}") > 0) {
				data = data.substring(data.indexOf("{"), data.lastIndexOf("}") + 1);
			}
			Object dataObject = JsonUtil.parseObject(data);
			if (dataObject instanceof Map) {
				Map<String, Object> dataMap = (Map<String, Object>) dataObject;
				// 内容点赞数 support_cnt
				if (dataMap.containsKey("agreeCount")) {
					String supportCnt = dataMap.get("agreeCount").toString();
					parsedata.put(Constants.SUPPORT_CNT, Integer.parseInt(supportCnt));
				}
			}
		} catch (Exception e) {
			LOG.error("httprequest download failed" + url);
		}
	}

	
	/**
	 * @param url
	 * @param dynamicLink
	 * @function http带cookie下载新闻页点赞数/点踩数页面
	 */
	@SuppressWarnings("unchecked")
	private void httpDownloadForPartakeCnt(Map<String, Object> parsedata, String dynamicLink) {
		HttpClientBuilder builder = HttpClientBuilder.create();
		builder.setUserAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36");
		HttpClient client = builder.build();
		HttpGet request = new HttpGet(dynamicLink);
		request.setHeader("User-Agent",
				"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36");
		HttpResponse response;
		try {
			response = client.execute(request);
			String data = EntityUtils.toString(response.getEntity(), "gbk");
			// 格式化动态数据
			if (data.indexOf("[") >= 0 && data.indexOf("]") >= 0 && (data.indexOf("[") < data.indexOf("{"))) {
				data = data.substring(data.indexOf("["), data.lastIndexOf("]") + 1);
			} else if (data.indexOf("{") >= 0 && data.indexOf("}") > 0) {
				data = data.substring(data.indexOf("{"), data.lastIndexOf("}") + 1);
			}
			Object dataObject = JsonUtil.parseObject(data);
			if (dataObject instanceof Map) {
				Map<String, Object> dataMap = (Map<String, Object>) dataObject;
				// 内容点赞数 support_cnt
				if (dataMap.containsKey("commentRelNum")) {
					String partakeCnt = dataMap.get("commentRelNum").toString();
					parsedata.put(Constants.PARTAKE_CNT, Integer.parseInt(partakeCnt));
				}
			}
		} catch (Exception e) {
			LOG.error("httprequest download failed" + dynamicLink);
		}
	}

}
