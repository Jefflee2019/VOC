package com.bfd.parse.reprocess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;

import com.alibaba.fastjson.JSONObject;
import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
/**
 * 一号店华为搜索列表页
 * @author dph 2018年4月10日
 *
 */
public class Eyhd_hw_sListRe implements ReProcessor{

	
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace parseFace) {
		String htmlcontent = gethtml(unit.getUrl());
		Map<String,Object> parsedata = new HashMap<String, Object>(16);
		Map<String, Object> resultData = new HashMap<String, Object>();
		resultData = result.getParsedata().getData();
		JSONObject json = JSONObject.parseObject(htmlcontent);  
		String value = null;
		if(json instanceof Map){
			value = (String) json.get("value");
			HtmlCleaner cleaner = new HtmlCleaner();
			TagNode tn = cleaner.clean(value);
			try {
				//评论items
				getCommentItems(tn, resultData,unit.getUrl());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else{
			getNextpage(resultData,unit.getUrl());
		}
		return new ReProcessResult(SUCCESS, parsedata);
	}
	
	/**
	 * 获取商品列表
	 * @param tn
	 * @param resultData
	 * @param url
	 * @throws Exception
	 */
	private void getCommentItems(TagNode tn, Map<String, Object> resultData, String url)	throws Exception {
		List<Map<String, Object>> itemList = new ArrayList<Map<String,Object>>();
		List<Map<String, Object>> taskList = new ArrayList<Map<String,Object>>();
	
		String xpath = "//div[@class='mod_search_pro']";
		Object[] objli = null;
		objli = tn.evaluateXPath(xpath);

		if (objli != null && objli.length > 0) {
			for (Object obj : objli) {
				TagNode tnInformp = (TagNode) obj;

				//获取商品ID
				String stnComproid =  (String) tnInformp.evaluateXPath("//div[@pageType='simple_table_nonFashion']/@comproid")[0];
				// 商品名  
				String stnTitle = (String) (tnInformp.evaluateXPath("//a[@id='pdlink2_" + stnComproid + "']/@title")[0]);
				//商品链接
				String stnHref = (String) (tnInformp.evaluateXPath("//a[@id='pdlink2_" + stnComproid + "']/@href")[0]);
				stnHref = "http:" + stnHref;
				
				Map<String,Object> item =new HashMap<String,Object>(4);
				Map<String,Object> task = new HashMap<String,Object>(4);
				Map<String,String> itemLink = new HashMap<String,String>(4);
				itemLink.put(Constants.LINK, stnHref);
				itemLink.put(Constants.RAWLINK, stnHref);
				itemLink.put(Constants.LINKTYPE, "eccontent");
				item.put(Constants.ITEMLINK, itemLink);
				item.put(Constants.ITEMNAME, stnTitle);
				itemList.add(item);
				task.put(Constants.LINK, stnHref);
				task.put(Constants.RAWLINK, stnHref);
				task.put(Constants.LINKTYPE, "eccontent");
				taskList.add(task);
			}
		}
		resultData.put(Constants.TASKS, taskList);
		resultData.put(Constants.ITEMS, itemList);
		getNextpage(resultData,url);
	}
	
	/**
	 * 下一页
	 * @param resultData
	 * @param url
	 */
	@SuppressWarnings("unchecked")
	private static void getNextpage(Map<String, Object> resultData,String url){
		String regex = "isGetMoreProducts=(\\d+)";
		Matcher m = Pattern.compile(regex).matcher(url);
		int nextpageNum = 1;
		String nextpage;
		if(m.find()){
			nextpageNum = Integer.parseInt(m.group(1)) + 1;
		}
		nextpage = url.replaceAll("isGetMoreProducts=\\d+", "isGetMoreProducts=" + nextpageNum);
		resultData.put(Constants.NEXTPAGE, nextpage);
		List<Map<String, Object>> tasks  = null;
		if(resultData.get(Constants.TASKS) == null || resultData.get(Constants.TASKS).equals("")){
			tasks  = new ArrayList<Map<String, Object>>();
		}else{
			tasks = (List<Map<String, Object>>) resultData.get(Constants.TASKS);
		}
		Map<String,Object> nextpageMap = new HashMap<String,Object>();
		nextpageMap.put(Constants.LINK, nextpage);
		nextpageMap.put(Constants.RAWLINK, nextpage);
		nextpageMap.put(Constants.LINKTYPE, "eclist");
		tasks.add(nextpageMap);
		resultData.put(Constants.TASKS, tasks);
	}
	
	/**
	 * 下载页面数据
	 * @param url
	 * @return
	 */
	public static String gethtml (String url) {
		HttpClientBuilder builder = HttpClientBuilder.create();
        HttpClient client = builder.build();
		HttpGet request = new HttpGet(url);
		Matcher keywordM = Pattern.compile("(k\\S+)/").matcher(url);
		String keyword = null;
		if(keywordM.find()){
			keyword = keywordM.group(1);
		}
		request.setHeader("Referer", "http://search.yhd.com/c0-0/" + keyword);
		try {
			HttpResponse response = client.execute(request);
			return EntityUtils.toString(response.getEntity());
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return null;
	}
}
