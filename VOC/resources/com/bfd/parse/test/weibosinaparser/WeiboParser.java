package com.bfd.parse.test.weibosinaparser;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;

import com.bfd.crawler.utils.DataUtil;
import com.bfd.crawler.utils.EncodeUtil;
import com.bfd.crawler.utils.JsonUtils;
//import com.bfd.crawler.download.httclient43.crawler.httpclient.Crawl4HttpClient;
//import com.bfd.crawler.download.httclient43.pojos.HttpRequestPojo;
//import com.bfd.crawler.download.httclient43.pojos.enumeration.HttpRequestMethod;
import com.bfd.crawler.utils.MyStringUtil;
import com.bfd.crawler.utils.ParserException;
import com.bfd.crawler.utils.StringUtil;
import com.bfd.crawler.utils.TextUtil;
//import com.bfd.crawler.download.httclient43.pojos.HttpRequestPojo;
//import com.bfd.crawler.download.httclient43.pojos.enumeration.HttpRequestMethod;
//import com.bfd.crawler.download.httclient43.crawler.httpclient.Crawl4HttpClient;
//import com.bfd.crawler.download.httclient43.pojos.HttpRequestPojo;
//import com.bfd.crawler.download.httclient43.pojos.enumeration.HttpRequestMethod;
//import com.bfd.crawler.download.httclient43.crawler.httpclient.Crawl4HttpClient;
//import com.bfd.crawler.download.httclient43.pojos.HttpRequestPojo;
//import com.bfd.crawler.download.httclient43.pojos.enumeration.HttpRequestMethod;
//import com.bfd.crawler.download.httclient43.crawler.httpclient.Crawl4HttpClient;
//import com.bfd.crawler.download.httclient43.pojos.HttpRequestPojo;
//import com.bfd.crawler.download.httclient43.pojos.enumeration.HttpRequestMethod;
//import com.bfd.crawler.utils.htmlcleaner.GetXpathByContent;
//import com.bfd.crawler.utils.htmlcleaner.SelfNode;
import com.bfd.parse.client.DownloadClient;
import com.bfd.parse.json.JsonData;


//import com.fuwenchao.utils.httpclient.MyCrawler;

public class WeiboParser extends AWeiBoParser {
	private static final Log LOG = LogFactory.getLog(WeiboParser.class);
	public static final Pattern getUrl = Pattern.compile("\\burl=(.*?)&");
	
	@Override
	public void executeParseAjax(String jsonDatas, Task task, Map rs,JsonData jsonData) {
		try {
			Map json = (Map)JsonUtils.parseObject(jsonDatas);
			if(json.get("data")==null){
				return;
			}
			String html = json.get("data").toString();
//			LOG.info("ajaxhtml:"+html);
			HtmlCleaner cleaner = new HtmlCleaner();
			TagNode root = cleaner.clean(html);
//			((Map)rs.get("weibos")).putAll(parseContent(root,task));
			((List)rs.get(Constants.WEIBOS)).addAll((List)parseContent(root, task).get(Constants.WEIBOS));
			getPageUrl(root, task, rs,html);
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("jsonDatas:"+jsonDatas);
		}
	}

	private void getPageUrl(TagNode root, Task task, Map rs,String html){
//		LOG.info("page html:"+html);
		List<String> pageUrls = new ArrayList<String>();
		Object[] objs;
		try {
			objs = root.evaluateXPath("//li[@class='cur']/a[@bpfilter='page']");
			if(objs.length==0){
				return;
			}
			TagNode currTag = (TagNode)objs[0];
			String currString = StringUtil.getRegexGroup(getNumP,currTag.getText().toString(), 1);
			if(!currString.trim().equals("1")){
				return; 
			}
			objs = root.evaluateXPath("//a[@bpfilter='page']");
			if(objs.length>0){
				
				TagNode max = (TagNode)objs[0];
//				LOG.info(getUrl(task)+" max.getText().toString().trim():"+max.getText().toString().trim());
				Pattern p = Pattern.compile("(\\d+)");
				String maxStr = StringUtil.getRegexGroup(p, max.getText().toString(), 1);
//				LOG.info("maxStr : "+maxStr);
				int maxNum = Integer.parseInt(maxStr);
				String url = max.getAttributeByName("href");
				if(maxNum>50){
					maxNum = 50;
				}
				for(int i=2;i<=maxNum;i++){
					pageUrls.add("http://weibo.com"+url.replaceAll("page=\\d+", "page="+i));
				}

//				LOG.info(getUrl(task)+" pageurls:"+JsonUtil.toJSONString(pageUrls));
			}
		} catch (XPatherException e) {
			e.printStackTrace();
		}
		if(pageUrls.size()>0&&rs!=null){
			rs.put(Constants.PAGE_URLS, pageUrls);
		}
	}
	
	private void parseBlock(List<Map<String, Object>> weibos ,TagNode root,Task task,Object[] divs){
		for (Object div : divs) {
			Map<String, Object> map = new HashMap<String, Object>();
			try {

				TagNode divTag = (TagNode) div;
				map.put("wid", divTag.getAttributeByName("mid"));
				Object[] contentDivs = divTag
						.evaluateXPath("//div[@class='WB_text W_f14']");
				if(contentDivs.length==0){
					LOG.info("wid:"+map.get("wid")+",no content");
					continue;
				}
				TagNode contentTag = (TagNode)contentDivs [0];
				Object[] imgs = contentTag.evaluateXPath("img");
				String content = contentTag.getText().toString().trim();
				content = content.trim();
				for (int i = 0; i < imgs.length; i++) {
					TagNode img = (TagNode) imgs[i];
					String feelingStr = img.getAttributeByName("title");
					if (feelingStr != null && feelingStr.trim().length() != 0) {
						content += feelingStr;
					}

				}

				map.put(Constants.CONTENT, content);
				Object[] timeSources = divTag.evaluateXPath("//div[@class='WB_from S_txt2']/a");
				if(timeSources.length>=2){
					if(timeSources.length==4){//有引用帖子
						TagNode time = (TagNode)timeSources[2];
						String dateStr = time.getAttributeByName("date");
						map.put(Constants.TIME, getSecodeTime(dateStr));
						if(((TagNode) timeSources[3]).getText().toString().equals("举报")){
//							TagNode divTag = (TagNode)attrTagNode.evaluateXPath("div[@class='WB_from']")[0];
							map.put(Constants.SOURCE, "");
						}else{
							map.put(Constants.SOURCE, ((TagNode) timeSources[3]).getText());
						}
					}else{
						TagNode time = (TagNode)timeSources[0];
						String dateStr = time.getAttributeByName("date");
						map.put(Constants.TIME, getSecodeTime(dateStr));
						if(((TagNode) timeSources[1]).getText().toString().equals("举报")){
	//						TagNode divTag = (TagNode)attrTagNode.evaluateXPath("div[@class='WB_from']")[0];
							map.put(Constants.SOURCE, "");
						}else{
							map.put(Constants.SOURCE, ((TagNode) timeSources[1]).getText());
						}
					}
				}
				Object[] urlObj = divTag.evaluateXPath("//ul[@class='WB_row_line WB_row_r4 clearfix S_line2']/li[2]/a[1]");
				if(urlObj.length>0){
					TagNode urlTag = (TagNode)urlObj[0];
					String urlStr = urlTag.getAttributeByName("action-data") ;
					String url = TextUtil.getRegexGroup(getUrl, urlStr, 1);
//					LOG.info("url:"+url);
					map.put(Constants.weiboURL, url);
					map.put(Constants.COMMENT_URL, url+"?type=comment");
					map.put(Constants.REPOST_URL, url+"?type=repost");
				}
				// LOG.info(attrTag.getText().toString());
				getAttr(divTag,map,task);
				Object[] objs = divTag.evaluateXPath("//div[@class='WB_feed_expand']/div[@class='WB_expand S_bg1']/div[5]/div[1]/ul[1]/li[1]/span[1]/a[1]");
				if(objs.length>0){
					TagNode aTag = (TagNode)objs[0];
//					System.out
//					.println(getUrl(task)+" 被转微博url：" + aTag.getAttributeByName("href"));
					map.put(Constants.berepost, ""+aTag.getAttributeByName("href"));
				}
			} catch (XPatherException e) {
				e.printStackTrace();
			}
			// LOG.info("---------------------------------------------");
			weibos.add(map);
		}
	}
	private Map<String, Object> parseContent(TagNode root,Task task) throws XPatherException {
		Map<String, Object> rs = new HashMap<String, Object>();
		// rs.put("name", HtmlCleanerUtil.getValueByXpath(root,
		// "//span[@class='username']", "", "text"));
		List<Map<String, Object>> weibos = new ArrayList<Map<String, Object>>();

		Object[] divs = root.evaluateXPath("//div[@class='WB_cardwrap WB_feed_type S_bg2']");
		System.err.println("divs length:"+divs.length);
		parseBlock(weibos, root, task,divs);
		
		divs = root.evaluateXPath("//div[@class='WB_cardwrap WB_feed_type S_bg2 WB_feed_vipcover']");
		System.err.println("divs length:"+divs.length);
		parseBlock(weibos, root, task,divs);
		rs.put(Constants.WEIBOS, weibos);
		// LOG.info(getUrl(task)+" weibo  is "+JsonUtil.toJSONString(weibos));

		return rs;
	}

	private static void getAttr(TagNode divTagNode,Map map,Task task) {
		Object[] as;
		try {
			as = divTagNode.evaluateXPath("//span[@class='pos']");
			String subPath = "span/span/em[2]";
			int supportNum = getNums(((TagNode)as[3]).getText().toString(), task);
			map.put(Constants.SUPPORT, supportNum);
			Object[] repostObj = ((TagNode)as[1]).evaluateXPath(subPath);
			int repostNum = getNums(((TagNode)repostObj[0]).getText().toString(), task);
			map.put(Constants.REPOST, repostNum);
			
			Object[] commentObj = ((TagNode)as[2]).evaluateXPath(subPath);
			int commentNum = getNums(((TagNode)commentObj[0]).getText().toString(), task);
			map.put(Constants.COMMENT, commentNum);
		} catch (XPatherException e) {
			e.printStackTrace();
		}

	}

	public static String getHtml(Map<String, Object> spider) {
		int httpcode = (Integer) spider.get("code");
		try {
			if (httpcode == 0) {
				String pagedata = (String) spider.get("data");
				String charset = (String) spider.get("charset");
				
//				charset = "gbk";
				LOG.info("charset:"+charset);
				System.out.println(pagedata);
				byte[] bytes = DataUtil.unzipAndDecode(pagedata);
				String encode = "utf-8";
				LOG.info("encode:"+encode);
				LOG.info(Thread.currentThread().getName()
						+ " download client  got page data");
				return new String(bytes, encode);
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	public void parseOtherScript(String html, Map<String, String> rs) {
		html = MyStringUtil.loadConvert(html);
		String regex = "<h1 class=\"username\">(.*?)</h1>";
		try {
			String username = MyStringUtil.getRegexGroup(regex, html, 1);
			rs.put("username", username);
		} catch (ParserException e) {
			e.printStackTrace();
		}

	}
	
	public static boolean checkCookieNouse(String html){
		String regex = "[\\s\\S]*?\\$CONFIG\\['islogin'\\].*?=.*?'0'[\\s\\S]*?";
		if(!html.matches(regex)&&html.contains("$CONFIG['uid']")){
			return true;
		}else{
			return false;
		}
	}

	public static void main(String[] args) {
		DownloadClient client = new DownloadClient();
//		String url = "UOR=qiusuoge.com,widget.weibo.com,os.51cto.com; SINAGLOBAL=2940834093009.452.1442541021780; ULV=1454938668047:34:4:1:906033196600.9661.1454938668031:1454493669912; SUHB=0ftobqKQSQuobe; wb_feed_unfolded_1661429500=1; myuid=1661429500; un=15110295667; SUBP=0033WrSXqPxfM725Ws9jqgMF55529P9D9WhO7LVEKZ.CkNhfjsx_vaoK5JpX5K2t; SUB=_2A257vOogDeRxGedI7VMV8ifJyzyIHXVYyFzorDV8PUNbuNAMLVCjkW9LHesfyLnVAt8AfImMDs8Eiahu8K3g3A..; _s_tentry=www.wooyun.org; login_sid_t=deaefdbef3f84297ebed616235aa27bd; YF-Ugrow-G0=b02489d329584fca03ad6347fc915997; Apache=906033196600.9661.1454938668031; SUS=SID-1661429500-1454938736-JA-xv4bj-9ac3e8e0ad57d6aec19fdfa265761a1e; SUE=es%3Df5edaa7aa6fe5bec80ae61e34874d910%26ev%3Dv1%26es2%3Dc05d0a27f10cb1cce8156b014bb964ee%26rs0%3DcDVHDgmLxN7Rj1IGtsHrq9c113j8cjesOaraE1tzhLK1NKjxwbehU11KhtPP36FJyzeev0dsGFDBCjgTpbypTqopyykOccPGNc3wHlRWeZwq2xJqt%252BOEEBAeb0ak9ayRU1UhpPF9qeNQf6%252BbeTQMEpgEO1mM6uVPOJstFkuuM7w%253D%26rv%3D0; SUP=cv%3D1%26bt%3D1454938736%26et%3D1455025136%26d%3Dc909%26i%3D1a1e%26us%3D1%26vf%3D0%26vt%3D0%26ac%3D2%26st%3D0%26uid%3D1661429500%26name%3Dbakerham1982%2540sina.cn%26nick%3D%25E8%2593%259D%25E5%25A4%25A9%26fmp%3D%26lcp%3D2015-12-26%252022%253A12%253A10; ALF=1486474736; SSOLoginState=1454938736; wvr=6; YF-V5-G0=8d795ebe002ad1309b7c59a48532ef7d; YF-Page-G0=b98b45d9bba85e843a07e69c0880151a";
		String url = "http://s.weibo.com/weibo/%E4%B9%90%E8%A7%86TV&xsort=time&nodup=1";
//		String url = "http://weibo.com/p/1005051649020634/home?profile_ftype=1&is_all=1#_0";
//		String url = "http://s.weibo.com/weibo/ABC&xsort=time&nodup=1";
//		String url = "http://s.weibo.com/weibo/TCL&xsort=time&nodup=1&page=20";
//		String url = "http://weixin.sogou.com/websearch/art.jsp?sg=CBf80b2xkgZdlrlP83m3xgEMeacTVcpEF8KWIvA4_n51cyNNyejYkZHXbnWRKDp6gGRCdYYSc_4rujf9h8Jhg4Y7sr8Rlk4YwfqCEUR7aTQAcHp4KQJgrM0bHQX1RR6d&url=p0OVDH8R4SHyUySb8E88hkJm8GF_McJfBfynRTbN8whQJg_0WPiWa9jGpcKi4811RD7iN2JSGUvIJyoW3Cfu5mQ3JxMQ3374wPGHHcB_Q_aS2cSBxZvpt-257UkqmhpJPRQzELLiAppYy-5x5In7jJFmExjqCxhpkyjFvwP6PuGcQ64lGQ2ZDMuqxplQrsbk";
		String cookie = "UOR=shouji.jd.com,widget.weibo.com,book.51cto.com; SINAGLOBAL=6318989197987.216.1458006293837; ULV=1459402764179:4:4:2:3484122892346.8516.1459402764170:1459242178856; SUHB=0YhQNBfUgg2RrZ; wb_publish_vip_1661429500=3; _s_tentry=weibo.com; Apache=3484122892346.8516.1459402764170; TC-V5-G0=ffc89a27ffa5c92ffdaf08972449df02; TC-Ugrow-G0=e66b2e50a7e7f417f6cc12eec600f517; wvr=6; TC-Page-G0=0cd4658437f38175b9211f1336161d7d; YF-Ugrow-G0=5b31332af1361e117ff29bb32e4d8439; YF-V5-G0=3d0866500b190395de868745b0875841; YF-Page-G0=00acf392ca0910c1098d285f7eb74a11; SUS=SID-1661429500-1460255931-JA-ay7ux-5c719d7fa486e6d341ca3395b1eda83c; SUE=es%3Def24ce572984a4b9413f4569a4e405a3%26ev%3Dv1%26es2%3D09d2353c8d3043a9e8d968c4cff1342d%26rs0%3DSttiyQNcDgE%252BrTkMvFwM5BpOjZe5%252FS0PBGvmRK5ICnGGPqNmKV%252FolWrbw5CF9SQFqKuKzTtmwAxmaxNVaQPNrt4opf5IOWIwYtFeJCQV3ESLXtCTj1Qje7ZWtci1pgaaYM4IeBOT459CqyA2l2PwWbysHv4RcPt2QNpYZBUDAco%253D%26rv%3D0; SUP=cv%3D1%26bt%3D1460255931%26et%3D1460342331%26d%3Dc909%26i%3Da83c%26us%3D1%26vf%3D0%26vt%3D0%26ac%3D0%26st%3D0%26uid%3D1661429500%26name%3Dbakerham1982%2540sina.cn%26nick%3D%25E8%2593%259D%25E5%25A4%25A9%26fmp%3D%26lcp%3D2015-12-26%252022%253A12%253A10; SUB=_2A256DczrDeRxGedI7VMV8ifJyzyIHXVZerkjrDV8PUNbvtAMLWLNkW9LHesEBrQsks4CuUet0gfn8UkpM3GL2g..; SUBP=0033WrSXqPxfM725Ws9jqgMF55529P9D9WhO7LVEKZ.CkNhfjsx_vaoK5JpX5KMt; ALF=1491791931; SSOLoginState=1460255931";
		String stringZip = client.getPage(url, "1", "sina", "test", refer,
				cookie, "keyword", "35");
		System.out.println("stringZip:" + stringZip);
		Map<String, Object> resMap = null;
		try {
			resMap = (Map<String, Object>) JsonUtils.parseObject(stringZip);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		Map<String, Object> spider = (Map<String, Object>) resMap
				.get("spiderdata");
		
		Task task = new Task();
		task.setUrl(url);
		task.setSpiderdata(spider);
		// task.setSpiderData(null);
		Map<String, Object> taskdata = new HashMap<String, Object>();
		taskdata.put("url", url);
		task.setTaskdata(taskdata);
		String html = getHtml(spider);
//		html = getHtmlFromFile("D:\\html\\weibo.html", "utf-8");
		// String html = getHtmlByHttpclient43(url, cookie);
		System.out.println(html);
		if(html.indexOf("<html>")<0){
			System.out.println("not html");
		}
		if(!checkCookieNouse(html)){
			System.out.println("cookie no use");
			return;
		}
		
		try {
			 new SearchParser().parseHtml(html, task);
//			new WeiboParser().parseHtml(html, task);
//			new CommentParser().parseHtml(html, task);
//			new UserInfoParser().parseHtml(html, task);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void testregex(String html) {
		String regex = "[\\s\\S]*?\\$CONFIG\\['islogin'\\].*?=.*?'1'[\\s\\S]*?";
		boolean flag = html.matches(regex);
		if (!flag) {
			System.out.println("need login");
		}
	}

	// public static void main3(String[] args) {
	// DownloadClient client = new DownloadClient();
	// String url =
	// "http://weibo.com/p/aj/mblog/mbloglist?domain=100606&pre_page=1&page=1&count=15&pagebar=1&id=1006061812511057&script_uri=/p/1006061812511057/weibo";
	// String json = client.getPageData(url, "0","Csina","item", refer,
	// cookie,"");
	// LOG.info("json:"+json);
	// Map map = null;
	// try {
	// map = (Map)JsonUtil.parseObject(json);
	// } catch (Exception e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// String html = map.get("data").toString();
	// HtmlCleaner cleaner = new HtmlCleaner();
	// TagNode root = cleaner.clean(html);
	// new WeiboParser().parseContent(root,null);
	// new WeiboParser().getPageUrl(root, null, null,html);
	// }
	@Override
	public String getFlagStr() {
		return "pl.content.homeFeed.index";
	}

	@Override
	public Map<String, Object> executeParse(TagNode root, Task task)
			throws XPatherException {
		return parseContent(root, task);
	}
}
