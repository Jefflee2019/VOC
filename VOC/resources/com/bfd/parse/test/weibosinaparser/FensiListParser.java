package com.bfd.parse.test.weibosinaparser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;

import com.bfd.parse.client.DownloadClient;
import com.bfd.parse.util.JsonUtil;

public class FensiListParser extends AWeiBoParser{
	private static final Log LOG = LogFactory.getLog(FensiListParser.class);
//	@Override
//	public Map<String, Object> parseHtml(String html, Task task) {
//		Matcher m = getAllHtmlJson.matcher(html);
//		String fensi = "";
//		System.out
//				.println("--------------------------html end!------------------");
//		while (m.find()) {
//			LOG.info(m.group(1));
//			LOG.info("------------------------------");
//			// 内容
//			if (m.group(1).indexOf("Pl_Official_LeftHisRelation") > 0) {
//				fensi = m.group(1);
//			}
//		}
//		parseFensi(fensi);
//		return null;
//	}
	private Map<String, Object> parseFensi(TagNode root,Task task) {
//		LOG.info("fensi:" + fensi);
//		String fensiHtml = Utils.getHtml(fensi);
//		LOG.info(fensiHtml);
//		HtmlCleaner cleaner = new HtmlCleaner();
//		TagNode root = cleaner.clean(fensiHtml);
		Map<String , Object> map = new HashMap<String, Object>();
		List<String> fensiUrls = new ArrayList<String>();
		String furl = "";
		try {
			Object[] as = root.evaluateXPath("//li[@class='clearfix S_line1']");
//			LOG.info(getUrl(task)+" as length : " + as.length);
			for (Object a : as) {

				TagNode at = (TagNode) a;
				TagNode tm = (TagNode) at.evaluateXPath("div[1]/div[1]/a[1]")[0];
				
				furl = tm.getAttributeByName("href");
				furl = formatUserParsedUrl(furl);
//				LOG.info(getUrl(task)+" "+furl);
				fensiUrls.add(furl);
			}
		} catch (XPatherException e) {
			e.printStackTrace();
		}
		List<String> pages = getFensiPage(root,task);
		map.put(Constants.FENSI_URLS, fensiUrls);
		map.put(Constants.PAGE_URLS, pages);
		return map;
	}

	private static List<String> getFensiPage(TagNode root,Task task){
		List<String> rs = new ArrayList<String>();
		
		try {
			int currentPageNum = -1;
			Object[] currentPageObjs = root.evaluateXPath("//a[@class='page S_txt1']");
			if(currentPageObjs.length>0){
				TagNode currentPageTag = (TagNode)currentPageObjs[0];
//				LOG.info(getUrl(task)+" current pagenum is "+currentPageTag.getText().toString());
				currentPageNum = Integer.parseInt(currentPageTag.getText().toString());
				if(currentPageNum!=1){
					return rs;
				}
			}else{
				return rs;
			}
			TagNode currentPageTag = (TagNode)root.evaluateXPath("//a[@class='page S_txt1']")[0];
			Object[] objs = objs = root.evaluateXPath("//a[@class='page S_bg1']");
			if(objs.length>0){
				TagNode tag = (TagNode)objs[objs.length-1];
				int totalNum = Integer.parseInt(tag.getText().toString());
				int rsPageNum = 0;
				
				if(totalNum>10){
					rsPageNum = 10;
				}else{
					rsPageNum = (int)totalNum;
				}
				String exampleUrl = tag.getAttributeByName("href");
				for(int i=2;i<=rsPageNum;i++){
					if(i==currentPageNum){
						continue;
					}
					rs.add("http://weibo.com"+exampleUrl.replaceAll("page=\\d+", "page="+i));
				}
			}
		} catch (XPatherException e) {
			e.printStackTrace();
		}
//		LOG.info(getUrl(task)+" pageurl:"+JsonUtil.toJSONString(rs));
		return rs;
	}
	
	@Override
	public String getFlagStr() {
		return "Pl_Official_LeftHisRelation";
	}

	@Override
	public Map<String, Object> executeParse(TagNode root,Task task) {
		return parseFensi(root,task);
	}
	
	
	public static void main(String[] args) {
		DownloadClient client = new DownloadClient();
		// 个人用户信息url
//		 String url = "http://weibo.com/p/1006061957258370/weibo";
//		String url = "http://weibo.com/tclmobile";
		//微博
//		 String url = "http://weibo.com/p/1005051172294045/weibo";
		// 粉丝url
		 String url =
		 "http://weibo.com/p/1006061812511057/follow?relate=fans";
//		 "http://weibo.com/p/1006061812511057/follow?pids=Pl_Official_LeftHisRelation__35&relate=fans&page=2#place";
		String refer = "http://weibo.com";
		// String cookie =
		// "UOR=t.58.com,widget.weibo.com,uniweibo.com; SUBP=0033WrSXqPxfM725Ws9jqgMF55529P9D9WhO7LVEKZ.CkNhfjsx_vaoK5JpX5KMt; SINAGLOBAL=494012703352.6509.1400581736816; ULV=1409716104656:15:2:2:5013886898212.255.1409716104653:1409651338546; ALF=1441248547; wvr=5; YF-V5-G0=5161d669e1ac749e79d0f9c1904bc7bf; YF-Ugrow-G0=98bd50ad2e44607f8f0afd849e67c645; SUS=SID-1661429500-1409712550-GZ-ypznf-339ca1bee4ed9b219bec536267c28657; SUE=es%3Dbfebe83c58d2caa9f74d479a586b2501%26ev%3Dv1%26es2%3D7b4bccfab3be45a61fd1f2a697b4db15%26rs0%3DUCWny%252FVr8SleJiZqQo8C7wp1r11Iet6oJnNKHvtvPqET1BN%252B%252BTIDrKJEitumf94TZJNje2g%252FgUIBPHtT7BPcCSymrHpmG8UrKSszfdnwocJ2TH%252FzB%252FJytsnxciZUVpGvpTzV%252FxSs3XXMrnWiZr5radfJ5xU8Iv%252B18APfONo7OdA%253D%26rv%3D0; SUP=cv%3D1%26bt%3D1409712550%26et%3D1409798950%26d%3Dc909%26i%3D8657%26us%3D1%26vf%3D0%26vt%3D0%26ac%3D0%26st%3D0%26uid%3D1661429500%26name%3Dbakerham1982%2540sina.cn%26nick%3D%25E8%2593%259D%25E5%25A4%25A9%26fmp%3D%26lcp%3D2012-06-29%252010%253A12%253A45; SUB=_2AkMjWg6Ra8NlrAJXnvkRzmLnbI1H-jyQgMJnAn7uJhIyGxgv7mgDqSU-472N8lMfFiWbTbDahZKdBz31AQ..; SSOLoginState=1409712550; YF-Page-G0=cf25a00b541269674d0feadd72dce35f; _s_tentry=uniweibo.com; Apache=5013886898212.255.1409716104653";
//		String cookie = "	UOR=t.58.com,widget.weibo.com,login.sina.com.cn; SUBP=0033WrSXqPxfM725Ws9jqgMF55529P9D9WhO7LVEKZ.CkNhfjsx_vaoK5JpX5K2t; SINAGLOBAL=494012703352.6509.1400581736816; ULV=1410502817499:22:9:6:9119966039234.223.1410502817495:1410502470669; SWB=usrmd1081; WBStore=b9261755eac1ef77|undefined; _s_tentry=s.weibo.com; Apache=9119966039234.223.1410502817495; WBtopGlobal_register_version=b40d2924ae85ed0f; SUS=SID-1661429500-1410503257-GZ-iwyv4-827bdccebdde60e78aae66140f088657; SUE=es%3D6f89208ba35f7d965d86f50fe40a0142%26ev%3Dv1%26es2%3D57cb8a761c44ce9b27fdf26399fed6ce%26rs0%3DMJTtIoUui49AoC0SXimOjxVGPjdKVA%252BJVW4nbjg64pey4Yy1GghgKxMxXBmHxoScGsI7bEmaL1pj9%252BfB%252BrwUiaAcfA5UVHxj2I%252BI6Fy9%252BiBbwRJES1hMajpCjZBClZgeuW6MqIheJPyN6JzU5tYWeFlpcrwvfEST%252Fv99HHpSOhY%253D%26rv%3D0; SUP=cv%3D1%26bt%3D1410503257%26et%3D1410589657%26d%3Dc909%26i%3D8657%26us%3D1%26vf%3D0%26vt%3D0%26ac%3D0%26st%3D0%26uid%3D1661429500%26name%3Dbakerham1982%2540sina.cn%26nick%3D%25E8%2593%259D%25E5%25A4%25A9%26fmp%3D%26lcp%3D2012-06-29%252010%253A12%253A45; SUB=_2AkMjTh1ua8NlrAJXnvkRzmLnbI1H-jyQnNOYAn7uJhIyHRgv7nI2qSUShDIOiUDfElSixwIf2T8BzwUL3g..; ALF=1442039257; SSOLoginState=1410503257; un=15110295667";
		String html = client.getPageData(url, "0","Csina","item", refer, cookie,"");
		Task task = getTestTask(url);
		try {
			new FensiListParser().parseHtml(html, task);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
}
