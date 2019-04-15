package com.bfd.parse.reprocess;

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

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class qqeduInfoReprocess implements ReProcessor{
	private static final Log LOG = LogFactory.getLog(qqeduInfoReprocess.class);

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace parseFace) {
		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		try {
				String cmtid = "";
				String pageData = unit.getPageData();
				Pattern pattern = Pattern.compile("cmt_id = (\\d+)");
				Pattern patternaid = Pattern.compile("aid: \"(\\d+)\"");
				if(pageData.contains("cmt_id")){
					Matcher matcher = pattern.matcher(pageData);
					if (matcher.find()) {
						cmtid = matcher.group(1);
					}
				}else{
					Matcher matcher = patternaid.matcher(pageData);
					if (matcher.find()) {
						String aid = matcher.group(1);
						String html1 = getHtml("http://coral.qq.com/" + aid );
						if(html1.contains("cmt_id")){
							Matcher matchermid = pattern.matcher(html1);
							if (matchermid.find()) {
								cmtid = matchermid.group(1);
							}
						}
					}
				}
					System.out.println(cmtid);
					String commenturl = "http://coral.qq.com/article/" + cmtid  + "/comment?commentid=0&reqnum=20&tag=&callback=mainComment";
					List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
					resultData.put("comment_url", commenturl);
					
					Map<String,Object> map =new HashMap<String,Object>();
					map.put("link", commenturl);
					map.put("rawlink", commenturl);
					map.put("linktype", "tecentedunewscomment");
					list.add(map);
					resultData.put("tasks", list);
		} catch (Exception e) {
			e.printStackTrace();
			LOG.info( this.getClass().getName() + "reprocess exception...");
			processcode = 1;
		}
		// 解析结果返回值 0代表成功
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(processcode, processdata);
	}
	
	// 下载页面源码
	public static String getHtml (String url) {
		String html = "";
		try {
			HttpClientBuilder builder = HttpClientBuilder.create();
			builder.setUserAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:52.0) Gecko/20100101 Firefox/52.0");
			HttpClient client = builder.build();
			
			
			HttpGet request = new HttpGet(url);
			HttpResponse response = client.execute(request);
			
			html = EntityUtils.toString(response.getEntity(),"utf-8");
			
			
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
		return html;
	}
	
	public static String getNextCommentPageUrl (String url,String cmt_id) {
		String nextCommentPage = "";
		String html = getHtml(url);
		String commentJson = html.replace("mainComment(", "").substring(0, html.replace("mainComment(", "").length()-1);
		
		
		 ObjectMapper mapper = new ObjectMapper();
		 Map<String, Object> map = new HashMap<>();
		    try {
		    	map = mapper.readValue(commentJson, new TypeReference<Map<String, Object>>() {});
		    	Map<String, Object> commentInfoMap = mapper.readValue(commentJson, new TypeReference<Map<String, Object>>() {});
				Map<String, Object> dataMap =  (Map<String, Object>) commentInfoMap.get("data");
				boolean hasnext = (boolean) dataMap.get("hasnext");
				String last = (String) dataMap.get("last");
				
				// 判断评论有没有下一页
				if (String.valueOf(hasnext).equals("true")) {
					nextCommentPage = "http://coral.qq.com/article/"+ cmt_id +"/comment?commentid=" + last + "&reqnum=20&tag=&callback=mainComment&_=" + System.currentTimeMillis();
				}
		    } catch (Exception e) {
				e.printStackTrace();
			} 	
	
		
		return nextCommentPage;
	}
}
