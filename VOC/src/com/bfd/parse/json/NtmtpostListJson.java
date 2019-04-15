package com.bfd.parse.json;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
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
 * @site:钛媒体(Ntmtpost)
 * @function:列表页第二页开始是动态请求，列表页不走模板，以json插件获取数据
 * @author bfd_02
 *
 */

public class NtmtpostListJson implements JsonParser {
	private static final Log LOG = LogFactory.getLog(NtmtpostListJson.class);

	@Override
	public JsonParserResult parse(Map<String, Object> taskdata, List<JsonData> dataList,
			URLNormalizerClient normalizerClient, ParseUnit unit) {
		int parsecode = 0;
		Map<String, Object> parseData = new HashMap<String, Object>();
		// 遍历dataList
		for (JsonData jsonData : dataList) {
			if (!jsonData.downloadSuccess()) {
				continue;
			}
			String json = TextUtil.getUnzipJson(jsonData, unit);
			try {
				if (json.indexOf("[") >= 0 && json.indexOf("]") >= 0 && (json.indexOf("[") < json.indexOf("{"))) {
					json = json.substring(json.indexOf("["), json.lastIndexOf("]") + 1);
				} else if (json.indexOf("{") >= 0 && json.indexOf("}") > 0) {
					json = json.substring(json.indexOf("{"), json.lastIndexOf("}") + 1);
				}
				executeParse(parseData, json, jsonData.getUrl());
			} catch (Exception e) {
				parsecode = 500012;
				LOG.warn(
						"JsonParser exception, taskdata url=" + taskdata.get("url") + ".jsonUrl :" + jsonData.getUrl(),
						e);
			}
		}
		// 组装返回结果
		JsonParserResult result = new JsonParserResult();
		try {
			result.setParsecode(parsecode);
			result.setData(parseData);
		} catch (Exception e) {
			LOG.error("jsonparser  error url:" + taskdata.get("url"));
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	private void executeParse(Map<String, Object> parseData, String json, String url) {
		List<Map<String, Object>> tasks = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
		parseData.put(Constants.TASKS, tasks);
		parseData.put(Constants.ITEMS, items);
		try {
			Object jsonObj = JsonUtil.parseObject(json);
			if (jsonObj instanceof Map) {
				Map<String, Object> map = (Map<String, Object>) jsonObj;
				if (map.containsKey("data")) {
					Map<String, Object> dataMap = (Map<String, Object>) map.get("data");
					/**
					 * 组装列表页参数：items和tasks 本页页码
					 * http://www.tmtpost.com/ajax/common
					 * /get?url=%2Fv1%2Fposts%2F
					 * multiple_group_search&data=subtype
					 * %3Dpost%26limit%3D0%26offset
					 * %3D0%26keyword%3D%E5%8D%8E%E4%B8%BA 部分转码
					 * http://www.tmtpost .com/ajax/common/get?url=/v1/posts/
					 * multiple_group_search
					 * &data=subtype=post&limit=10&offset=10&keyword=华为
					 */
					if (dataMap.containsKey("post")) {
						List<Map<String, Object>> itemsList = (List<Map<String, Object>>) dataMap.get("post");
						if (!itemsList.isEmpty()) {
							for (Map<String, Object> itemsMap : itemsList) {
								Map<String, Object> itemMap = new HashMap<String, Object>();
								Map<String, Object> link = new HashMap<String, Object>();
								if (itemsMap.containsKey("title")) {
									String title = itemsMap.get("title").toString();
									itemMap.put(Constants.TITLE, title);
								}
								if (itemsMap.containsKey("guid")) {
									String guid = itemsMap.get("guid").toString();
									// 拼接内容页url
									String newsUrl = new StringBuffer("http://www.tmtpost.com/").append(guid)
											.append(".html").toString();
									link.put("link", newsUrl);
									link.put("rawlink", newsUrl);
									link.put("linktype", "newscontent");
									itemMap.put(Constants.LINK, link);
								}
								items.add(itemMap);
								tasks.add(link);
							}
						}
					}

					/**
					 * 生成下一页任务
					 */
					if (map.containsKey("cursor")) {
						Map<String, Object> cursorMap = (Map<String, Object>) map.get("cursor");
						if (cursorMap.containsKey("post")) {
							Map<String, Object> postMap = (Map<String, Object>) cursorMap.get("post");
							// 获取总页码数
							double totalSize = Double.parseDouble(postMap.get("total").toString());
							// 默认的单页页码为limit=10
							if (totalSize / 10 > 1 && url.contains("url=")) {
								// 截取url中unicode转码的部分，用于拼接下一页任务
								getNextpage(parseData, url, tasks);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			LOG.warn("json parse error,and url is" + url);
		}

	}

	private void getNextpage(Map<String, Object> parseData, String url, List<Map<String, Object>> tasks)
			throws UnsupportedEncodingException {
		int index = url.indexOf("&data=subtype");
		String urlUnicode = url.substring(index + 13);
		urlUnicode = URLDecoder.decode(urlUnicode, "utf8");
		// 拼接下一页链接
		String regex = "offset=(\\d+)&";
		Matcher match = Pattern.compile(regex).matcher(urlUnicode);
		if (match.find()) {
			int pageid = Integer.parseInt(match.group(1));
			// 截取部分下一页拼接
			String nextpUnicode = urlUnicode.replace("offset=" + pageid, "offset=" + (pageid + 10));
			// 下一页链接截取部分encode，保持和首页链接规则一致
			nextpUnicode = URLEncoder.encode(nextpUnicode, "utf8");
			// 拼接下一页链接
			String nextPage = new StringBuffer(
					"http://www.tmtpost.com/ajax/common/get?url=%2Fv1%2Fposts%2Fmultiple_group_search&data=subtype")
					.append(nextpUnicode).toString();
			Map<String, Object> nextMap = new HashMap<String, Object>();
			nextMap.put(Constants.LINK, nextPage);
			nextMap.put(Constants.RAWLINK, nextPage);
			nextMap.put(Constants.LINKTYPE, "newslist");
			tasks.add(nextMap);
			parseData.put(Constants.NEXTPAGE, nextMap);
		}
	}
}
