package com.bfd.parse.json;

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
import com.bfd.parse.util.TextUtil;

/**
 * @site 中国日报网(Nchinadaily)
 * @function 新闻内容页json处理插件
 */
public class NchinadailyContentJson implements JsonParser {
	private static final Log LOG = LogFactory.getLog(NchinadailyContentJson.class);
	private static final Pattern cmt_sum = Pattern.compile("\"cmt_sum\":\\s*(\\d+)");
	private static final Pattern partn_sum = Pattern.compile("\"participation_sum\":\\s*(\\d+)");
	private static final Pattern topic_id = Pattern.compile("\"topic_id\":\\s*(\\d+)");
	
	@Override
	public JsonParserResult parse(Map<String, Object> taskdata, List<JsonData> dataList,
			URLNormalizerClient normalizerClient, ParseUnit unit) {
		int parsecode = 0;
		Map<String, Object> parsedata = new HashMap<String, Object>();
		for (JsonData data : dataList) { // 遍历dataList
			// 判断该ajax数据是否下载成功
			if (!data.downloadSuccess()) {
				continue;
			}
			// 解压缩ajax数据
			String json = TextUtil.getUnzipJson(data, unit);
			try {
				json = new String(data.getData(), "UTF-8");
				// 执行从json数据中提取自己感兴趣的数据
				executeParse(parsedata, json, data.getUrl(), unit);
			} catch (Exception e) {
				LOG.warn("taskdat url=" + taskdata.get("url") + ".jsonUrl:"
								+ data.getUrl(), e);
			}
		}
		// 组装返回结果
		JsonParserResult result = new JsonParserResult();
		result.setParsecode(parsecode);
		result.setData(parsedata);
		return result;
	}
	
	public void executeParse(Map<String, Object> parsedata, String json,
			String url, ParseUnit unit) throws Exception {
		Matcher matcher = cmt_sum.matcher(json);
		if(matcher.find()) {
			String cmtSum = matcher.group(1);
			parsedata.put(Constants.MSG_CNT, cmtSum); // 留言数
			if(!"0".equals(cmtSum)) {
				matcher = partn_sum.matcher(json);
				if(matcher.find()) {
					parsedata.put(Constants.JREPLY_CNT, matcher.group(1)); // 参与评论的总人数
				}
				matcher = topic_id.matcher(json);
				if(matcher.find()) {
					String commUrl = "http://changyan.sohu.com/api/2/topic/comments?client_id=cyrePABlP"
							+ "&page_size=10&page_no=1"
							+ "&total_page_no=" + (Integer.parseInt(cmtSum)+9)/10
							+ "&topic_id=" + matcher.group(1);
					parsedata.put(Constants.COMMENT_URL, commUrl);
					List<Map<String, Object>> taskList = new ArrayList<Map<String, Object>>();
					parsedata.put(Constants.TASKS, taskList); // tasks 字段是必须的
					Map<String, Object> task = new HashMap<>();
					task.put(Constants.LINK, commUrl);
					task.put(Constants.RAWLINK, commUrl);
					task.put(Constants.LINKTYPE, "newscomment");
					taskList.add(task); // 添加评论任务
				}
			}
		}
	}
}