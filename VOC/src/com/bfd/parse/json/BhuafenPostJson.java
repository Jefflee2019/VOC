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
 * 站点名：花粉俱乐部
 * <p>
 * 主要功能：处理发帖人信息
 * @author bfd_01
 *
 */
public class BhuafenPostJson implements JsonParser {
	private static final Log LOG = LogFactory.getLog(BhuafenPostJson.class);

	@Override
	public JsonParserResult parse(Map<String, Object> taskdata,
			List<JsonData> dataList, URLNormalizerClient normalizerClient,
			ParseUnit unit) {
		unit.setPageEncode("utf-8");
		int parsecode = 0;
		Map<String, Object> parsedata = new HashMap<String, Object>();
		List<Map<String, Object>> taskList = new ArrayList<Map<String, Object>>();
		parsedata.put("tasks", taskList);
		// 遍历dataList
		for (Object obj : dataList) {
			JsonData data = (JsonData) obj;
			// 判断该ajax数据是否下载成功
			if (!data.downloadSuccess()) {
				continue;
			}
			// 解压缩ajax数据
			String json = TextUtil.getUnzipJson(data, unit);
			try {
				// 将ajax数据转化为json数据格式
				if (json.indexOf("[") >= 0 && json.indexOf("]") >= 0
						&& (json.indexOf("[") < json.indexOf("{"))) {
					json = json.substring(json.indexOf("["),
							json.lastIndexOf("]") + 1);
				} else if (json.indexOf("{") >= 0 && json.indexOf("}") > 0) {
					json = json.substring(json.indexOf("{"),
							json.lastIndexOf("}") + 1);
				}
				// 执行从json数据中提取自己感兴趣的数据
				executeParse(parsedata, json, data.getUrl(), unit);
			} catch (Exception e) {
				// e.printStackTrace();
				// LOG.warn("json:" + json + ".url:" + taskdata.get("url"));
				LOG.warn(
						"AMJsonParse exception,taskdat url="
								+ taskdata.get("url") + ".jsonUrl:"
								+ data.getUrl(), e);
			}
		}

		// 组装返回结果
		JsonParserResult result = new JsonParserResult();
		try {
			result.setParsecode(parsecode);
			result.setData(parsedata);
		} catch (Exception e) {
			// e.printStackTrace();
			LOG.error("jsonparser reprocess error url:" + taskdata.get("url"));
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public void executeParse(Map<String, Object> parsedata, String json,
			String url, ParseUnit unit) {
		
		Map<String, Object> author = null;
		if (parsedata.containsKey(Constants.AUTHOR)) {
			author = (Map<String, Object>) parsedata.get(Constants.AUTHOR);
		} else {
			author = new HashMap<String, Object>();
		}
		// 积分
		Pattern pscore = Pattern.compile("积分: (\\d+)", Pattern.DOTALL);
		Matcher mscore = pscore.matcher(json);
		while (mscore.find()) {
			String forum_score = mscore.group(1);
			author.put(Constants.FORUM_SCORE, forum_score);
		}
		// 签到天数
		Pattern pcheckin = Pattern.compile("签到天数： (\\d+)", Pattern.DOTALL);
		Matcher mcheckin = pcheckin.matcher(json);
		while (mcheckin.find()) {
			String checkin_days = mcheckin.group(1);
			author.put(Constants.CHECKIN_DAYS, checkin_days);
		}
		// UID
		Pattern puid = Pattern.compile("UID: (\\d+)", Pattern.DOTALL);
		Matcher muid = puid.matcher(json);
		while (muid.find()) {
			String uid = muid.group(1);
			author.put(Constants.FORUM_SCORE, uid);
		}
		// 帖子数
		Pattern ppost = Pattern.compile("target=\"_blank\">(\\d+)",
				Pattern.DOTALL);
		Matcher mpost = ppost.matcher(json);
		while (mpost.find()) {
			String post_cnt = mpost.group(1);
			author.put(Constants.POST_CNT, post_cnt);
		}
		// 在线时间
		Pattern ponlinetime = Pattern.compile("在线时间: (\\d+)", Pattern.DOTALL);
		Matcher monlinetime = ponlinetime.matcher(json);
		while (monlinetime.find()) {
			String onlinetime = monlinetime.group(1);
			author.put("onlinetime", onlinetime);
		}
		// 人气
		Pattern ppopularity = Pattern.compile("人气: (\\d+)", Pattern.DOTALL);
		Matcher mpopularity = ppopularity.matcher(json);
		while (mpopularity.find()) {
			String popularity = mpopularity.group(1);
			author.put("popularity", popularity);
		}
		// 威望
		Pattern pprestige = Pattern.compile("威望: (\\d+)", Pattern.DOTALL);
		Matcher mprestige = pprestige.matcher(json);
		while (mprestige.find()) {
			String prestige = mprestige.group(1);
			author.put("popularity", prestige);
		}
		// 花瓣数
		Pattern pflowers = Pattern.compile("花瓣: (\\d+)", Pattern.DOTALL);
		Matcher mflowers = pflowers.matcher(json);
		while (mflowers.find()) {
			String flowers = mflowers.group(1);
			author.put("flowers", flowers);
		}

		// 勋章
		Pattern pmedal = Pattern.compile(
				"<div class=\"card-medal\">(.*?)</div>", Pattern.DOTALL);
		Matcher mmedal = pmedal.matcher(json);
		String cardmedal = "";
		String medal = "";
		while (mmedal.find()) {
			cardmedal = mmedal.group(1);
		}

		Matcher m = Pattern.compile("gif\" />(.*?)</a>", Pattern.DOTALL)
				.matcher(cardmedal);
		while (m.find()) {
			String temp = m.group(1);
			medal = medal + temp + ",";
		}
		if (!"".equals(medal)) {
			author.put("medal", medal.substring(0, medal.length() - 1));
		}
		parsedata.put(Constants.AUTHOR, author);
	}
}
