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
import com.bfd.parse.util.JsonUtil;
import com.bfd.parse.util.TextUtil;

/**
 * 站点名：京东 
 * 主要功能： 
 * 		获取价格
 * 		获取评价标签
 * 		生成评论任务
 *   
 * @author bfd_03
 *
 */
public class Ejdxiaomi2ContentJson implements JsonParser {

	private static final Log LOG = LogFactory.getLog(Ejdxiaomi2ContentJson.class);

	@Override
	public JsonParserResult parse(Map<String, Object> taskdata,
			List<JsonData> dataList, URLNormalizerClient urlnormalizerClients,
			ParseUnit unit) {
		int parsecode = 0;
		Map<String, Object> parsedata = new HashMap<String, Object>();

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
				json = new String(data.getData(), "GBK");
				
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
				//e.printStackTrace();
				//LOG.warn("json:" + json + ".url:" + taskdata.get("url"));
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
			//e.printStackTrace();
			LOG.error("jsonparser reprocess error url:" + taskdata.get("url"));
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public void executeParse(Map<String, Object> parsedata, String json,
			String url, ParseUnit unit) {
		Object obj = null;
		try {
			obj = JsonUtil.parseObject(json);
		} catch (Exception e) {
			LOG.error("json parse error or json is null");
		}
		
		// 获取价格
		if (url.indexOf("prices") > 0) {
			if (obj instanceof Map) {
				Map<String,Object> map = (Map<String,Object>) obj;
				if (map.containsKey("p")) {
					parsedata.put(Constants.PRICE, map.get("p"));
				}
			} else if (obj instanceof List) {
				Map<String,Object> map =  ((List<Map<String,Object>>) obj).get(0);
				if (map.containsKey("p")) {
					parsedata.put(Constants.PRICE, map.get("p"));
				}
			}
		}
		
		//获取评价标签
		if(url.indexOf("productpage")>0){
			if(obj instanceof Map){
				Map<String, Object> map = (Map<String, Object>)obj;
				//买家印象
				if(map.containsKey("hotCommentTagStatistics")){
					List<Map<String,Object>> hotCommentTag = (List<Map<String,Object>>) map.get("hotCommentTagStatistics");
					StringBuffer sb = new StringBuffer();
					for(Map<String, Object> temp:hotCommentTag){
						sb.append(temp.get("name")+":");
						sb.append(temp.get("count")+",");
					}
					if(sb.length() > 0){
						parsedata.put(Constants.BUYER_IMPRESSION, sb.substring(0, sb.length()-1));//买家印象
					}
					
				}
				
			}
		}
		
//		String count = null;
//		if (url.contains("http://club.jd.com/review/")) {
//			Pattern p = Pattern.compile("全部评价<em>\\((.*)\\)</em>");
//			Matcher m = p.matcher(json);
//			if (m.find()) {
//				count = m.group(1);
//			}
//		}
		//create comment task
//		if ((count != null) && (!"0".equals(count))) {
			getCommentUrl(url,parsedata);
//		}
	}
	private void getCommentUrl(String url, Map<String, Object> parsedata) {
		
		Pattern pattern = Pattern.compile("http://club.jd.com/review/(\\d+)", Pattern.DOTALL);
		Matcher matcher = pattern.matcher(url);
		StringBuffer sb = new StringBuffer();
		String sCommUrl = "";
		if (matcher.find()) {
			sb.append("http://club.jd.com/review/");
			sb.append(matcher.group(1));			
			sb.append("-3-1-0.html");
			sCommUrl = sb.toString();
		}
		
		//临时处理单个商品的评论链接
//		if(sCommUrl.contains("3918172")) {
//			sCommUrl = sCommUrl.replace("?d=d", "?b=b");
//		}
		Map<String, Object> commentTask = new HashMap<String, Object>();
		List<Map<String,Object>> tasks = new ArrayList<Map<String,Object>>();
		if (sb.length() > 0) {
			commentTask.put(Constants.LINK, sCommUrl);
			commentTask.put(Constants.RAWLINK, sCommUrl);
			commentTask.put(Constants.LINKTYPE, "eccomment");
			parsedata.put(Constants.COMMENT_URL, sCommUrl);
			tasks.add(commentTask);
			parsedata.put(Constants.TASKS, tasks);
			
		}
	}
}
