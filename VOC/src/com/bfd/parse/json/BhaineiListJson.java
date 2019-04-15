package com.bfd.parse.json;

import java.io.UnsupportedEncodingException;
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
 * 站点：海内
 * 功能：获取列表页
 * @author dph 2017年11月27日
 *
 */
public class BhaineiListJson implements JsonParser{
	private static final Log LOG = LogFactory.getLog(EthmallListJson.class);
	private static final Pattern PATTERN_TBODY = Pattern.compile("<tbody id=\"normalthread_\\d+\">(.|\n)*?</tbody>");
	private static final Pattern PATTERN_LINKNAME = Pattern.compile(
			"<a href=\"https://www.hainei.org/thread-\\d+-\\d+-\\d+.html\""
			+ " onclick=\"atarget\\(this\\)\" class=\"xst\">(\\S+\\s*){1,5}?</a>");
	private static final Pattern PATTERN_LINK = Pattern.compile("https://www.hainei.org/thread-\\d+-\\d+-\\d+.html");
	private static final Pattern PATTERN_NAME = Pattern.compile("xst\">(\\S+\\s*){1,5}?</a>");
	private static final Pattern PATTERN_REPLY_CNT = Pattern.compile(">\\d+</em>");
	private static final Pattern PATTERN_POSTTIME = Pattern.compile(">\\d+.*</span>");
	private static final Pattern PATTERN_NEXTPAGE = Pattern.compile(
			"<a href=\"https://www.hainei.org/forum-\\d+-\\d+.html\" class=\"nxt\">");
	private static final Pattern PATTERN_PAGE = Pattern.compile("https://www.hainei.org/forum-\\d+-\\d+.html");
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
				executeParse(parsedata,json);
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
	/**
	 * 从json中提取信息
	 * @param parsedata
	 * @param json
	 */
	@SuppressWarnings({ "unchecked" })
	public void executeParse(Map<String, Object> parsedata, String json) {
		String htmlData = null;
		try {
			htmlData = new String(json.getBytes("gbk"),"utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
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
		Matcher tbodtMatcher = PATTERN_TBODY.matcher(htmlData);
		String tbody = null;
		while(tbodtMatcher.find()){
			tbody = tbodtMatcher.group(0);
			Matcher linknameMatcher = PATTERN_LINKNAME.matcher(tbody);
			String linkname = null;
			String link = null;
			String rawlink = null;
			String name = null;
			while(linknameMatcher.find()){
				linkname = linknameMatcher.group(0);
				Matcher linkMatcher = PATTERN_LINK.matcher(linkname);
				while(linkMatcher.find()){
					link = linkMatcher.group(0);
					rawlink = link;
				}
				Matcher nameMatcher = PATTERN_NAME.matcher(linkname);
				while(nameMatcher.find()){
					name = nameMatcher.group(0);
					name = name.replace("xst\">", "").replace("</a>", "").trim();
				}
			}
			Matcher replyCntMatcher = PATTERN_REPLY_CNT.matcher(tbody);
			String repleCnt = null;
			while(replyCntMatcher.find()){
				repleCnt = replyCntMatcher.group(0);
				repleCnt = repleCnt.replace(">", "").replace("</em>", "").trim();
			}
			Matcher posttimeMatcher = PATTERN_POSTTIME.matcher(tbody);
			String posttime = null;
			while(posttimeMatcher.find()){
				posttime = posttimeMatcher.group(0);
				posttime = posttime.replace(">", "").replace("</span>", "").trim();
			}
			Map<String,Object> task = new HashMap<String,Object>(4);
			Map<String,Object> item =new HashMap<String,Object>(4);
			task.put(Constants.LINK, link);
			task.put(Constants.RAWLINK, rawlink);
			task.put(Constants.LINKTYPE, "bbspost");
			taskList.add(task);
			parsedata.put(Constants.TASKS, taskList);
			
			Map<String,String> itemLink = new HashMap<String,String>(4);
			itemLink.put(Constants.LINK, link);
			itemLink.put(Constants.RAWLINK, rawlink);
			itemLink.put(Constants.LINKTYPE, "bbspost");		
			item.put(Constants.ITEMLINK, itemLink);
			item.put(Constants.ITEMNAME, name);
			item.put(Constants.REPLY_CNT, repleCnt);
			item.put(Constants.POSTTIME, posttime);
			itemList.add(item);
			parsedata.put(Constants.ITEMS, itemList);
		}
		//拼接下一页链接
		Map<String, Object> nextpageMap= new HashMap<String,Object>(4);
		Matcher nextpageMatcher = PATTERN_NEXTPAGE.matcher(htmlData);
		String nextpage = null;
		while(nextpageMatcher.find()){
			nextpage = nextpageMatcher.group(0);
			Matcher pageMatche = PATTERN_PAGE.matcher(nextpage);
			String page = null;
			while(pageMatche.find()){
				page = pageMatche.group(0);
			}
			nextpageMap.put(Constants.LINK, page);
			nextpageMap.put(Constants.RAWLINK, page);
			nextpageMap.put(Constants.LINKTYPE, "bbspostlist");
			taskList.add(nextpageMap);
			parsedata.put(Constants.NEXTPAGE, nextpage);
			parsedata.put(Constants.TASKS, taskList);
		}
		
	}
}
