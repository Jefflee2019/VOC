package com.bfd.parse.test.weibosinaparser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.htmlcleaner.ContentNode;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.HtmlNode;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;

import com.bfd.parse.client.DownloadClient;
import com.bfd.parse.json.JsonData;
import com.bfd.parse.util.JsonUtil;
//import com.fuwenchao.utils.httpclient.MyCrawler;


public class RepostParser extends AWeiBoParser {
	private static final Log LOG = LogFactory.getLog(RepostParser.class);

	
	@Override
	public void executeParseAjax(String jsonDatas, Task task, Map rs,JsonData jsonData) {
		try {
			Map json = (Map)JsonUtil.parseObject(jsonDatas);
			Map data = (Map)json.get("data");
			String html = data.get("html").toString();
			HtmlCleaner cleaner = new HtmlCleaner();
			TagNode root = cleaner.clean(html);
			((List)rs.get(Constants.REPOSTS)).addAll((List)parseRepost(root,true,task).get(Constants.REPOSTS));
		} catch (Exception e) {
			LOG.error(" Repost json parser error!jsonDatas is "+jsonDatas);
			e.printStackTrace();
		}
	}

	public Map<String, Object> parseRepost(TagNode root,boolean isAjax,Task task){
//		String html = Utils.getHtml(repost);
//		LOG.info("html:"+html);
//		HtmlCleaner cleaner = new HtmlCleaner();
//		TagNode root = cleaner.clean(html);
		Map<String, Object> rs = new HashMap<String, Object>();
		Object[] divs;
		
		List<Map<String, Object>> reposts = new ArrayList<Map<String, Object>>();
		try {
			if(isAjax){
				divs = root.evaluateXPath("//div[@class='list_li S_line1 clearfix']");
			}else{
				divs = root.evaluateXPath("//dl[@class='list_li S_line1 clearfix']");
			}
//			String userUrl = "";
			for(int i=0;i<divs.length;i++){
				Map<String, Object> map = new HashMap<String, Object>();
				TagNode div = (TagNode)divs[i];
//				TagNode a = (TagNode)div.evaluateXPath("dt[1]/a[1]")[0];
//				
//				userUrl = formatUserParsedUrl(a.getAttributeByName("href"));
//				map.put(Constants.USER_URL, userUrl);
//				LOG.info(getUrl(task)+" user url :"+userUrl);
				map.put(Constants.mid, div.getAttributeByName(Constants.mid));
				String content = getContent(div);
//				LOG.info("content:"+dd.getText().toString());
				
				map.put(Constants.CONTENT, content);
				TagNode time = (TagNode)div.evaluateXPath("div[2]/div[2]/div[2]/a[1]")[0];
//				LOG.info("时间:"+time.getAttributeByName("date"));
				String dateStr = time.getAttributeByName("date");
				map.put(Constants.TIME, getSecodeTime(dateStr));
				Object[] as = div.evaluateXPath("div[2]/div[1]/a");
				getUserInfoFromCommRepost(as, map);
				reposts.add(map);
			}
			rs.put(Constants.REPOSTS, reposts);
		}catch (XPatherException e) {
			e.printStackTrace();
		}
//		LOG.info("rs:"+JsonUtil.toJSONString(rs));
		return rs;
	}
	
	private String getContent(TagNode div){
		String temp = "";
		String content = "";
		try {
			TagNode dd = (TagNode)div.evaluateXPath("div[2]/div[1]")[0];
			List<HtmlNode> children = (List<HtmlNode>)dd.getAllChildren();
//		LOG.info("-------content----------------");
			for(int j=0;j<children.size();j++){
				if(children.get(j) instanceof ContentNode){
					temp = ((ContentNode)children.get(j)).getContent();
					if(temp.trim().length()>0){
						content += temp;
//					LOG.info(temp);
					}
					
				}
			}
			TagNode span = (TagNode)div.evaluateXPath("div[2]/div[1]/span[1]")[0];
			content+=span.getText().toString();
			Object[] imgs = dd.evaluateXPath("//img");
			for(Object img:imgs){
				TagNode imgNode = (TagNode)img;
				content+=imgNode.getAttributeByName("title");
			}
		} catch (XPatherException e) {
			e.printStackTrace();
		}
		return content;
	}
	
	public static void main(String[] args) {
//		DownloadClient client = new DownloadClient();
		// 个人用户信息url
//		 String url = "http://weibo.com/p/1006061957258370/weibo";
//		String url = "http://weibo.com/tclmobile";
		//微博
//		 String url = "http://weibo.com/p/1005051172294045/weibo";
		// 粉丝url
		 String url =
//		 "http://weibo.com/p/1006061812511057/follow?relate=fans";
//		 "http://weibo.com/2608693591/Bxgz7wf1x?type=repost";
		 "http://weibo.com/aj/v6/mblog/info/big?ajwvr=6&id=3778620541216800&__rnd=1416967570776";
		String refer = "http://weibo.com";
		// String cookie =
		// "UOR=t.58.com,widget.weibo.com,uniweibo.com; SUBP=0033WrSXqPxfM725Ws9jqgMF55529P9D9WhO7LVEKZ.CkNhfjsx_vaoK5JpX5KMt; SINAGLOBAL=494012703352.6509.1400581736816; ULV=1409716104656:15:2:2:5013886898212.255.1409716104653:1409651338546; ALF=1441248547; wvr=5; YF-V5-G0=5161d669e1ac749e79d0f9c1904bc7bf; YF-Ugrow-G0=98bd50ad2e44607f8f0afd849e67c645; SUS=SID-1661429500-1409712550-GZ-ypznf-339ca1bee4ed9b219bec536267c28657; SUE=es%3Dbfebe83c58d2caa9f74d479a586b2501%26ev%3Dv1%26es2%3D7b4bccfab3be45a61fd1f2a697b4db15%26rs0%3DUCWny%252FVr8SleJiZqQo8C7wp1r11Iet6oJnNKHvtvPqET1BN%252B%252BTIDrKJEitumf94TZJNje2g%252FgUIBPHtT7BPcCSymrHpmG8UrKSszfdnwocJ2TH%252FzB%252FJytsnxciZUVpGvpTzV%252FxSs3XXMrnWiZr5radfJ5xU8Iv%252B18APfONo7OdA%253D%26rv%3D0; SUP=cv%3D1%26bt%3D1409712550%26et%3D1409798950%26d%3Dc909%26i%3D8657%26us%3D1%26vf%3D0%26vt%3D0%26ac%3D0%26st%3D0%26uid%3D1661429500%26name%3Dbakerham1982%2540sina.cn%26nick%3D%25E8%2593%259D%25E5%25A4%25A9%26fmp%3D%26lcp%3D2012-06-29%252010%253A12%253A45; SUB=_2AkMjWg6Ra8NlrAJXnvkRzmLnbI1H-jyQgMJnAn7uJhIyGxgv7mgDqSU-472N8lMfFiWbTbDahZKdBz31AQ..; SSOLoginState=1409712550; YF-Page-G0=cf25a00b541269674d0feadd72dce35f; _s_tentry=uniweibo.com; Apache=5013886898212.255.1409716104653";
//		String cookie = "	UOR=t.58.com,widget.weibo.com,login.sina.com.cn; SUBP=0033WrSXqPxfM725Ws9jqgMF55529P9D9WhO7LVEKZ.CkNhfjsx_vaoK5JpX5K2t; SINAGLOBAL=494012703352.6509.1400581736816; ULV=1410502817499:22:9:6:9119966039234.223.1410502817495:1410502470669; SWB=usrmd1081; WBStore=b9261755eac1ef77|undefined; _s_tentry=s.weibo.com; Apache=9119966039234.223.1410502817495; WBtopGlobal_register_version=b40d2924ae85ed0f; SUS=SID-1661429500-1410503257-GZ-iwyv4-827bdccebdde60e78aae66140f088657; SUE=es%3D6f89208ba35f7d965d86f50fe40a0142%26ev%3Dv1%26es2%3D57cb8a761c44ce9b27fdf26399fed6ce%26rs0%3DMJTtIoUui49AoC0SXimOjxVGPjdKVA%252BJVW4nbjg64pey4Yy1GghgKxMxXBmHxoScGsI7bEmaL1pj9%252BfB%252BrwUiaAcfA5UVHxj2I%252BI6Fy9%252BiBbwRJES1hMajpCjZBClZgeuW6MqIheJPyN6JzU5tYWeFlpcrwvfEST%252Fv99HHpSOhY%253D%26rv%3D0; SUP=cv%3D1%26bt%3D1410503257%26et%3D1410589657%26d%3Dc909%26i%3D8657%26us%3D1%26vf%3D0%26vt%3D0%26ac%3D0%26st%3D0%26uid%3D1661429500%26name%3Dbakerham1982%2540sina.cn%26nick%3D%25E8%2593%259D%25E5%25A4%25A9%26fmp%3D%26lcp%3D2012-06-29%252010%253A12%253A45; SUB=_2AkMjTh1ua8NlrAJXnvkRzmLnbI1H-jyQnNOYAn7uJhIyHRgv7nI2qSUShDIOiUDfElSixwIf2T8BzwUL3g..; ALF=1442039257; SSOLoginState=1410503257; un=15110295667";
//		String html = client.getPageData(url, "", refer, cookie);
//		String html = getByMyCrawler(url);
//		Task task = new Task();
//		Map map2 = new HashMap();
//		task.setTaskData(map2);
//		map2.put("url", url);
//		new RepostParser().parseHtml(html, task);
	}
//	private static String getByMyCrawler(String url){
//		MyCrawler crawler = new MyCrawler();
//		String html = "";
//		String htmls[] = crawler.get(url,
//				"http://d.weibo.com/?topnav=1&mod=logo&wvr=6", cookie);
//		Task task = getTestTask(url);
//		try {
//			Map<String, Object> map = (Map<String, Object>) JsonUtil.parseObject(htmls[1]);
//			Map<String, Object> htmlMap = (Map<String, Object>) map.get("data");
//			html = htmlMap.get("html").toString();
//			TagNode root = new HtmlCleaner().clean(html);
//			new RepostParser().parseRepost(root, true, task);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		
//		return htmls[1];
//	}

	public static void main3(String[] args) {
		DownloadClient client = new DownloadClient();
		String url = "http://weibo.com/aj/mblog/info/big?_wv=5&id=3722827083521137&max_id=3754353020281274&filter=0&page=2&__rnd=1410602892791";
//		String url = "http://weibo.com/1649145663/BeLzbq8VI?type=repost";
		String json = client.getPageData(url, "0","Csina","item", refer, cookie,"");
		LOG.info("json:"+json);
		Map map = null;
		try {
			map = (Map)JsonUtil.parseObject(json);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Map data = (Map)map.get("data");
		String html = data.get("html").toString(); 
		HtmlCleaner cleaner = new HtmlCleaner();
		TagNode root = cleaner.clean(html);
		Task task = new Task();
		Map map2 = new HashMap();
		task.setTaskdata(map2);
		map2.put("url", url);
		new RepostParser().parseRepost(root,true,task);
	}
	
	@Override
	public String getFlagStr() {
//		return "Pl_Official_LeftWeiboDetail__37";
		return "pl.content.weiboDetail.index";
	}

	@Override
	public Map<String, Object> executeParse(TagNode root,Task task) {
		return parseRepost(root,false,task);
	}
}
