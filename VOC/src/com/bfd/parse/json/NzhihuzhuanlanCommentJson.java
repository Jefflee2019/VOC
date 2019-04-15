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
 * @site：知乎专栏(Nzhihuzhuanlan)
 * @function：获取评论内容及相关参数
 * 
 * @author bfd_02
 */
public class NzhihuzhuanlanCommentJson implements JsonParser {
	private final static Log LOG = LogFactory.getLog(NzhihuzhuanlanCommentJson.class);

	@Override
	public JsonParserResult parse(Map<String, Object> taskdata, List<JsonData> dataList,
			URLNormalizerClient normalizerClient, ParseUnit unit) {
		int parsecode = 0;
		Map<String, Object> parsedata = new HashMap<String, Object>();
		for (JsonData data : dataList) {
			// 判断该ajax数据是否下载成功
			if (!data.downloadSuccess()) {
				continue;
			}
			// 解压缩ajax数据
			String json = TextUtil.getUnzipJson(data, unit);
			try {
				// 将ajax数据转化为json数据格式
				if (json.indexOf("[") >= 0 && json.indexOf("]") >= 0 && (json.indexOf("[") < json.indexOf("{"))) {
					json = json.substring(json.indexOf("["), json.lastIndexOf("]") + 1);
				} else if (json.indexOf("{") >= 0 && json.indexOf("}") > 0) {
					json = json.substring(json.indexOf("{"), json.lastIndexOf("}") + 1);
				}
				// 执行从json数据中提取自己感兴趣的数据
				executeParse(parsedata, json, data.getUrl(), unit);
			} catch (Exception e) {
				LOG.warn("AMJsonParser exception, taskdata url=" + taskdata.get("url") + ".jsonUrl :" + data.getUrl(),
						e);
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
		Object obj = null;
		try {
			obj = JsonUtil.parseObject(json);
		} catch (Exception e) {
			LOG.error("excuteParse error");
		}
		if (obj instanceof Map) {
			Map<String, Object> jsonMap = (Map<String, Object>) obj;
			List<Map<String, Object>> taskList = null;
			if (parsedata.get(Constants.TASKS) != null) {
				taskList = (List<Map<String, Object>>) parsedata.get(Constants.TASKS);
			} else {
				taskList = new ArrayList<Map<String, Object>>();
			}
			/**
			 * comments 评论部分
			 */
			if (jsonMap.containsKey("data") && jsonMap.get("data") instanceof List) {
				List<Map<String, Object>> dataList = (List<Map<String, Object>>) jsonMap.get("data");
				if (dataList != null && !dataList.isEmpty()) {
					// 存放组装好的数据
					ArrayList<Map<String, Object>> tempList = new ArrayList<Map<String, Object>>();
					for (Map<String, Object> dataMap : dataList) {
						// 临时存放数据的map
						HashMap<String, Object> tempmap = new HashMap<String, Object>();
						// 评论人昵称
						if (dataMap.containsKey("author")) {
							Map<String, Object> authorMap = (Map<String, Object>) dataMap.get("author");
							if (authorMap.containsKey("member")) {
								Map<String, Object> memberMap = (Map<String, Object>) authorMap.get("member");
								if (memberMap.containsKey("name")) {
									String username = memberMap.get("name").toString();
									tempmap.put(Constants.USER_NAME, username);
								}
							}
						}
						// 评论内容
						if (dataMap.containsKey("content")) {
							String content = dataMap.get("content").toString();
							content = content.replaceAll("\\<.*?>", "");
							tempmap.put(Constants.COMMENT_CONTENT, content);
						}

						// 评论时间
						if (dataMap.containsKey("created_time")) {
							String commentTime = dataMap.get("created_time").toString();
							tempmap.put(Constants.COMMENT_TIME, ConstantFunc.normalTime(commentTime));
						}

						// 评论点赞数
						if (dataMap.containsKey("vote_count")) {
							int upCnt = Integer.parseInt(dataMap.get("vote_count").toString());
							tempmap.put(Constants.UP_CNT, upCnt);
						}

						tempList.add(tempmap);
					}
					parsedata.put(Constants.COMMENTS, tempList);
				}
			}

			/**
			 * 总回复数
			 */
			if (jsonMap.containsKey("common_counts")) {
				int replyCnt = Integer.parseInt(jsonMap.get("common_counts").toString());
				parsedata.put(Constants.REPLY_CNT, replyCnt);

				/**
				 * 下一页链接
				 */
				String pagenoRex = "offset=(\\d+)&";
				String pagesizeRex = "limit=(\\d+)&";
				int pageno = getRex(pagenoRex, url);
				int pagesize = getRex(pagesizeRex, url);
				if (pageno < replyCnt) {
					String nextpageUrl = url.replace("offset=" + pageno, "offset=" + (pageno + pagesize));
					Map<String, Object> nextpageTask = new HashMap<String, Object>();
					nextpageTask.put(Constants.LINK, nextpageUrl);
					nextpageTask.put(Constants.RAWLINK, nextpageUrl);
					nextpageTask.put(Constants.LINKTYPE, "newscomment");
					taskList.add(nextpageTask);
					parsedata.put(Constants.NEXTPAGE, nextpageTask);
					parsedata.put(Constants.TASKS, taskList);
				}
			}
		}

		/**
		 * 赞同数 赞同 <!-- -->87<
		 */
		if (url.contains("articles")) {
			Matcher match = Pattern.compile("articles/(\\d+)").matcher(url);
			if (match.find()) {
				String itemId = match.group(1);
				String contentUrl = new StringBuffer().append("https://zhuanlan.zhihu.com/p/").append(itemId)
						.toString();
				httpDownloadForSupportCnt(parsedata, contentUrl);
			}
		}
	}

	/**
	 * 获取pagesize、pageno
	 */
	private int getRex(String rex, String url) {
		Matcher match = Pattern.compile(rex).matcher(url);
		int pagesize = 0;
		if (match.find()) {
			pagesize = Integer.parseInt(match.group(1).toString());
		}
		return pagesize;
	}

	private static void httpDownloadForSupportCnt(Map<String, Object> parsedata, String contentUrl) {
		HttpClientBuilder builder = HttpClientBuilder.create();
		builder.setUserAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36");
		HttpClient client = builder.build();
		HttpGet request = new HttpGet(contentUrl);
		HttpResponse response;
		try {
			response = client.execute(request);
			String data = EntityUtils.toString(response.getEntity(), "gbk");
			// 内容评论数
			// 赞同 <!-- -->344<
			if (data.contains("赞同")) {
				Matcher match = Pattern.compile("赞同 <!-- -->(\\d+)").matcher(data);
				if (match.find()) {
					int supportCnt = Integer.parseInt(match.group(1));
					parsedata.put("support_cnt", supportCnt);
				}
			} else {
				parsedata.put("support_cnt", 0);
			}
		} catch (Exception e) {
			System.out.println("httprequest download failed" + contentUrl);
		}
	}

}
