package com.bfd.parse.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;

import com.bfd.crawler.utils.DataUtil;
import com.bfd.parse.client.URLNormalizerClient;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.JsonUtil;
import com.bfd.parse.util.TextUtil;

/**
 * 站点名：当当网
 * 主要功能： 
 * 		获取价格
 *   
 * @author bfd_03
 *
 */
public class EdangdangCommentJson implements JsonParser {

	private static final Log LOG = LogFactory.getLog(EdangdangCommentJson.class);

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
		/**
		 * 加上tasks
		 */
		List<Map<String, Object>> tasks = new ArrayList<Map<String, Object>>();
		parsedata.put("tasks", tasks);
		// 获取参数
		if (obj instanceof Map) {
			Map<String,Object> map = (Map<String,Object>) obj;
			if (map.containsKey("data")) {
				Map data = (Map) map.get("data"); 
				Map list = (Map) data.get("list"); 
				Map summary = (Map) list.get("summary");
				String html = (String) list.get("html");
				//全部评论数
				int total_comment_num = Integer.parseInt((String) summary.get("total_comment_num"));
				//评论不为0
				if (total_comment_num != 0) {
					int total_crazy_count = Integer.parseInt((String) summary.get("total_crazy_count"));//好评
					int total_indifferent_count = Integer.parseInt((String) summary.get("total_indifferent_count"));//中评
					int total_detest_count = Integer.parseInt((String) summary.get("total_detest_count"));//差评
					float average_score = Float.parseFloat((String) summary.get("average_score"));//平均分
					int total_image_count = Integer.parseInt((String) summary.get("total_image_count"));//晒图
					float goodRate = Float.parseFloat((String) summary.get("goodRate"));//好评率
					int pageCount = Integer.parseInt((String) summary.get("pageCount"));//页面数
					int pageIndex = Integer.parseInt((String) summary.get("pageIndex"));//当前页数
					parsedata.put("comment_cnt", total_comment_num);
					parsedata.put(Constants.GOOD_RATE, goodRate);
					parsedata.put(Constants.GOOD_CNT, total_crazy_count);
					parsedata.put(Constants.GENERAL_CNT, total_indifferent_count);
					parsedata.put(Constants.POOR_CNT, total_detest_count);
					parsedata.put(Constants.WITHPIC_CNT, total_image_count);
					//html解析
					this.parseCommentHtml(html, parsedata);
					//下一页链接处理
					this.createNextUrl(pageIndex, pageCount, url, parsedata);
				}
			}
		} 

	}
	/**
	 * 正则匹配字符串
	 * @param str
	 * @param pattern
	 * @return
	 */
	private String getCresult(String str,String reg){
		Pattern pattern = Pattern.compile(reg);
		Matcher mch = pattern.matcher(str);
		if(mch.find()){
			return mch.group(1);
		}
		return str;
	}
	
	/**
	 * 评论html解析
	 * @param html
	 * @param parsedata
	 */
	private void parseCommentHtml(String html, Map<String, Object> parsedata){
		List<Map<String, Object>> comments = new ArrayList<Map<String, Object>>();
		HtmlCleaner cleaner = new HtmlCleaner(); // 封装成HtmlCleaner
		TagNode root = cleaner.clean(html);
		try {
			Object[] divs = root.evaluateXPath("//div[@class='comment_items clearfix']");
			for (Object object : divs) {
				Map map = new HashMap();
				TagNode obj = (TagNode) object;
				TagNode items_right = (TagNode) obj.evaluateXPath("//div[@class='items_right']")[0];
				TagNode describe_detail = (TagNode) items_right.evaluateXPath("//div[@class='describe_detail']/span")[0];
				String comment_content = describe_detail.getText().toString();//内容
				TagNode starline = (TagNode) items_right.evaluateXPath("//div[@class='starline clearfix']/span")[0];
				String comment_time = starline.getText().toString();//发表时间
				TagNode items_left_pic1 = (TagNode) obj.evaluateXPath("//div[@class='items_left_pic']/span")[0];
				TagNode items_left_pic2 = (TagNode) obj.evaluateXPath("//div[@class='items_left_pic']/span")[1];
				String commenter_name = items_left_pic1.getText().toString();//名称
				String commenter_level = items_left_pic2.getText().toString();//等级  普通用户等级为空
				map.put(Constants.COMMENT_CONTENT, comment_content);
				map.put(Constants.COMMENT_TIME, comment_time);
				map.put(Constants.COMMENTER_NAME, commenter_name);
				map.put(Constants.COMMENTER_LEVEL, commenter_level);
				comments.add(map);
			}
			parsedata.put(Constants.COMMENTS, comments);
		} catch (XPatherException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * 下一页链接处理
	 * @param pageIndex
	 * @param pageConut
	 * @param url
	 * @param parsedata
	 */
	private void createNextUrl(int pageIndex,int pageCount,String url,Map<String, Object> parsedata){
		List<Map<String, Object>> tasks = (List<Map<String, Object>>) parsedata.get("tasks");
		if (pageCount != pageIndex) {
			pageIndex += 1;
			String id = this.getCresult(url, "(pageIndex=\\d+)");
			String sCommUrl = url.replaceAll(id, "pageIndex="+pageIndex);
			Map commentTask = new HashMap();
			commentTask.put(Constants.LINK, sCommUrl);
			commentTask.put(Constants.RAWLINK, sCommUrl);
			commentTask.put(Constants.LINKTYPE, "eccomment");
			commentTask.put("iid", DataUtil.calcMD5(sCommUrl + ""));
			parsedata.put(Constants.COMMENT_URL, sCommUrl);
			tasks.add(commentTask);	
			parsedata.put("tasks", tasks);
		}
	}
}
