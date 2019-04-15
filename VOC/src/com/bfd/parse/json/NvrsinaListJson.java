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
 * 站点：Nvrsina
 * 功能：获取内容链接以及下一页
 * @author dph 2017年12月26日
 *
 */
public class NvrsinaListJson implements JsonParser{
	private static final Log LOG = LogFactory.getLog(NvrsinaListJson.class);

	private static final Pattern PATTERN_HTML = Pattern.compile("<div class=\"listboxwp\">(.|\n)*?</div>");
	private static final Pattern PATTERN_HREF = Pattern.compile("<a href=\"((.|\n)*?)\"");
	private static final Pattern PATTERN_LINK = Pattern.compile("page=(\\d+)");
	@Override
	public JsonParserResult parse(Map<String, Object> taskdata, List<JsonData> dataList,
			URLNormalizerClient normalizerClient, ParseUnit unit) {
		Map<String,Object> parsedata = new HashMap<String,Object>(5);
		int parsecode = 0;
		for(Object obj : dataList){
			JsonData data = (JsonData) obj;
			if(!data.downloadSuccess()){
				continue;
			}
			//解压缩ajax数据
			String json = TextUtil.getUnzipJson(data, unit);
			try{
				if (json.indexOf("[") >= 0 && json.indexOf("]") >= 0
						&& (json.indexOf("[") < json.indexOf("{"))) {
					json = json.substring(json.indexOf("["),
							json.lastIndexOf("]") + 1);
				} else if (json.indexOf("{") >= 0 && json.indexOf("}") > 0) {
					json = json.substring(json.indexOf("{"),
							json.lastIndexOf("}") + 1);
				}
				// 执行从json数据中提取自己感兴趣的数据
				executeParse(parsedata,json,unit);
			}catch(Exception e){
				LOG.warn("json:" + json + ".url:" + taskdata.get("url"));
				LOG.warn("AMJsonParse exception,taskdat url="+ taskdata.get("url") 
								+ ".jsonUrl:"+ data.getUrl(), e);
			}
		}
		// 组装返回结果
		JsonParserResult result = new JsonParserResult();
		try{
			result.setParsecode(parsecode);	
			result.setData(parsedata);
		}catch(Exception e){
			LOG.error("jsonparser reprocess error url:" + taskdata.get("url"));
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	private void executeParse(Map<String, Object> parsedata, String json, ParseUnit unit) {
		System.out.println(json);
		List<Map<String, Object>> taskList =null;
		if(parsedata.get(Constants.TASKS) != null){
			taskList = (List<Map<String,Object>>) parsedata.get(Constants.TASKS);					
		}else{
			taskList = new ArrayList<Map<String,Object>>();
		}
		List<Map<String, Object>> itemList =null;
		if(parsedata.get(Constants.ITEMS) != null){
			itemList = (List<Map<String,Object>>) parsedata.get(Constants.ITEMS);					
		}else{
			itemList = new ArrayList<Map<String,Object>>();
		}
		//将下载到的源代码中的\去掉
		String str = json.replace("\\", "sAs");
		str = str.replace("sAs", "");
		Matcher htmlM = PATTERN_HTML.matcher(str);
		int count = 0;
		while(htmlM.find()){
			count ++;
			Map<String,Object> task = new HashMap<String,Object>(4);
			String html = htmlM.group(0);
			Matcher hrefM = PATTERN_HREF.matcher(html);
			while(hrefM.find()){
				String href = hrefM.group(1);
				task.put(Constants.LINK, href);
				task.put(Constants.RAWLINK, href);
				task.put(Constants.LINKTYPE, "newscontent");
			}
			taskList.add(task);
			Map<String,Object> item = new HashMap<String,Object>(4);
			item.put("title", count);
			item.put("link", task);
			itemList.add(item);
			
		}
		//添加下一页
		Map<String, Object> nextpage= new HashMap<String,Object>(4);
		String link = unit.getUrl();
		if(count == 10){
			Matcher pageM = PATTERN_LINK.matcher(link);
			while(pageM.find()){
				String page = pageM.group(1);
				Integer p = Integer.parseInt(page) + 1;
				link = link.replaceAll("page=\\d+", "page=" + p);
			}
		}else{
			link = link.replaceAll("page=\\d+", "page=" + 1);
		}
		nextpage.put(Constants.LINK, link);
		nextpage.put(Constants.RAWLINK, link);
		nextpage.put(Constants.LINKTYPE, "newslist");
		taskList.add(nextpage);
		parsedata.put(Constants.NEXTPAGE, nextpage);
		parsedata.put(Constants.TASKS, taskList);
		parsedata.put(Constants.ITEMS, itemList);
	}

}
