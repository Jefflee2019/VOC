package com.bfd.parse.preprocess;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;

import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.JsonUtil;

public class BxiciPostPre implements PreProcessor {

	private static final Log LOG = LogFactory.getLog(BxiciPostPre.class);

	@Override
	public boolean process(ParseUnit unit, ParserFace face) {
		String pageData = unit.getPageData();
		pageData = parsePageData(pageData, unit.getUrl());
		unit.setPageData(pageData);
		try {
			unit.setPageBytes(pageData.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			LOG.error("BxiciPostPre preprocess error");
		}
		unit.setPageEncode("utf8");
		return true;
	}

	@SuppressWarnings("unchecked")
	private String parsePageData(String pageData, String url) {
		Pattern p = Pattern.compile("var docData = ([\\s\\S]*?)function");
		Matcher m = p.matcher(pageData);
		Map<Object, Object> jsonMap = null;
		if(m.find()){
			try {
				String json = m.group(1).substring(0, m.group(1).length()-2);
				jsonMap = (Map<Object, Object>) JsonUtil
						.parseObject(json);
			} catch (Exception e) {
				LOG.error("BxiciPostPre jsonStr convert error !");
			}
		}
		if(jsonMap != null && jsonMap.containsKey("result")){
			Object obj = jsonMap.get("result");
			if(obj instanceof Map){
				Map<String, Object> reply = (Map<String, Object>) obj;
				
				List<Object> list = (List<Object>)reply.get("docinfo");
				StringBuilder  divHtml = new StringBuilder();
				for(Object content : list){
					if(content instanceof Map){
						Map<String, Object> contentMap = (Map<String, Object>) content;
						appendHtmlData(contentMap, divHtml);
					}
				}
				appendNextPage(url, divHtml, pageData);
				String title = "";
				if(reply.get("sDocTitle") != null){
					title = reply.get("sDocTitle").toString();
				}
				else {
					Pattern titlePat = Pattern.compile("<title>(.*)</title>");
					Matcher mch = titlePat.matcher(pageData);
					if(mch.find()){
						title = mch.group(1);
					}
				}
				pageData = String.format(getHtml(pageData, title), divHtml);
			}
		}
		/*
		 * 处理<td class=\"doc_sign\">亲爱的，放开手，是我对你最后的疼爱。<br>\r\n<br>\r\n<br>\r\n<\/td>
		 */
		
		HtmlCleaner cleaner = new HtmlCleaner();
		TagNode root = cleaner.clean(pageData);
		try {
			Object[] divs = root.evaluateXPath("//div[@align='right']");
			for (int i = 0; i < divs.length; i++) {
//				System.err.println(((TagNode) divs[i]).getText());
				TagNode node = (TagNode) divs[i];
				node.removeFromTree();
			}
			pageData = cleaner.getInnerHtml(root);
		} catch (XPatherException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return pageData;
	}
	
	private String getHtml(String pageData, String title){
		StringBuilder html = new StringBuilder();
		html.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
	    html.append("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
	    html.append("<head>");
	    html.append("<meta http-equiv=\"content-type\" content=\"text/html; charset=gb2312\" />");
	    html.append("</head>");
		html.append("<body>");
		html.append("<h3>").append(title).append("</h3>");
		html.append("%s");
		html.append("</body>").append("</html>");
		return html.toString();
	}
	
	@SuppressWarnings("unchecked")
	private void appendHtmlData(Map<String, Object> contentMap, StringBuilder  divHtml){
		if(contentMap.containsKey("foolrinfo")){
			Map<String, Object> floorMap = (Map<String, Object>)contentMap.get("foolrinfo");
			String floor = "";
			if(floorMap.containsKey("floorcontent")){
				String floorContent = floorMap.get("floorcontent").toString();
				Pattern floorNum = Pattern.compile("id=\"text_(\\d+)_0\"");
				Matcher m = floorNum.matcher(floorContent);
				if(m.find()){
					floor = m.group(1);
				}
				if(floor.equals("1")){
					divHtml.append("<div class=\"postcontent\">");
				}
				else{
					divHtml.append("<div class=\"reply\">");
				}
				divHtml.append("<div class=\"content\">");
				divHtml.append(floorContent);
				divHtml.append("</div>");
			}
			else {
				//没有楼层信息的是被系统删除了
				return;
			}
			if(floorMap.containsKey("UserName")){
				String useName = floorMap.get("UserName").toString();
				divHtml.append("<div class=\"userName\">");
				divHtml.append("用户名：");
				divHtml.append("<span class=\"name\">").append(useName).append("</span>");
				divHtml.append("</div>");
			}
			if(floorMap.containsKey("really_updated_at")){
				String postTime = floorMap.get("really_updated_at").toString();
				divHtml.append("<div class=\"posttime\">");
				divHtml.append("发表时间：");
				divHtml.append("<span class=\"time\">").append(postTime).append("</span>");
				divHtml.append("</div>");
			}
			if(!floor.equals("")){
				divHtml.append("<div class=\"floor\">");
				divHtml.append("楼层：");
				divHtml.append("<span class=\"floor\">").append(floor).append("</span>");
				divHtml.append("</div>");
			}
			divHtml.append("</div>");
		}
	}
	
	private void appendNextPage(String url, StringBuilder divHtml, String pageData){
		Pattern p = Pattern.compile("(.*)?/d\\d+[.\\d+]*.htm");
		Matcher urlMch = p.matcher(url);
		if(urlMch.find()){
			String nextLinkCompile = "<a name='nextPage' href=\"(/d\\d+[.\\d+]*.htm)\" title=\"下一页\">下一页</a>";
			Pattern nextPat = Pattern.compile(nextLinkCompile);
			Matcher m = nextPat.matcher(pageData);
			if(m.find()){
				divHtml.append("<div class=\"nextPage\">");
				divHtml.append("<a name='nextPage' href=\"");
				divHtml.append(urlMch.group(1)).append(m.group(1));
				divHtml.append("\" title=\"下一页\">下一页</a>");
				divHtml.append("</div>");
			}
		}
	}
}
