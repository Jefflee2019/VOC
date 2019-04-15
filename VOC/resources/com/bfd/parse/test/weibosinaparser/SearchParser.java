package com.bfd.parse.test.weibosinaparser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;

//import sun.util.logging.resources.logging;

import com.bfd.crawler.utils.JsonUtils;
import com.bfd.crawler.utils.StringUtil;
import com.bfd.crawler.utils.htmlcleaner.HtmlCleanerUtil;
import com.bfd.crawler.utils.htmlcleaner.SelfNode;
import com.bfd.parse.client.DownloadClient;
//import com.fuwenchao.utils.MyStringUtil;
//import com.fuwenchao.utils.MyStringUtil;
//import com.fuwenchao.utils.httpclient.MyCrawler;

public class SearchParser extends AWeiBoParser {
	private static final Log LOG = LogFactory.getLog(SearchParser.class);

	public String getFragmentHtml(String html, Task task) {
		Matcher m = getAllHtmlJson_search.matcher(html);
		String searchRs = "";
		while (m.find()) {
			// 内容
			if (m.group(1).indexOf(getFlagStr()) > 0) {
				searchRs = m.group(1);
			}
		}
		return searchRs;
	}

	private List<String> getPageNum(TagNode root, Task task) {
		List<String> rs = new ArrayList<String>();

		try {
			Object[] pageObjs = root
					.evaluateXPath("//div[@class='W_pages']/span[1]/div[1]/ul[1]/li[@class='cur']/a[1]");
			if (pageObjs.length == 0) {
				return rs;
			}
			TagNode currPage = (TagNode) pageObjs[0];
			int currPageNum = getNums(currPage.getText().toString(), null);
			if (currPageNum != 1) {
				return rs;
			}
			Object[] objs = root
					.evaluateXPath("//div[@class='W_pages']/span[1]/div[1]/ul[1]/li/a[1]");
			if (objs.length > 0 && objs.length > 1) {
				String url = ((TagNode) objs[objs.length - 1])
						.getAttributeByName("href");
				int maxNum = getNums(((TagNode) objs[objs.length - 1])
						.getText().toString(), null);
				if (maxNum > 50) {
					maxNum = 50;
				}
				for (int i = 2; i <= maxNum; i++) {
					rs.add("http://s.weibo.com"
							+ url.replaceAll("page=\\d+", "page=" + i));
				}
			}
		} catch (XPatherException e) {
			e.printStackTrace();
		}
		return rs;
	}

	private void getSupportAndComment(TagNode div, Map<String, Object> map,
			Task task) throws XPatherException {
		String content = map.get(Constants.CONTENT).toString();
//		System.out.println("continue: content:" + map.get(Constants.CONTENT));
		List<TagNode> spans = HtmlCleanerUtil.getNodesByXpath(div,
				"div[@class='feed_action clearfix']/ul[1]/li/a[1]/span[1]");
		// List<TagNode> spans1 = HtmlCleanerUtil.getNodesByXpath(div,
		// "div[@class='feed_action clearfix']/ul[1]/li/a/span");
		if (content.indexOf("深成指") >= 0) {
			System.out.println();
		}
//		HtmlCleanerUtil.getAllNodePathByTagNode(div, 0);

		int num = 0;
		int repostNum = 0;
		int commentNum = 0;
		int supportNum = 0;
		int beginIndex = 0;
		if (spans.size() == 3) {
			beginIndex = 0;
		} else {
			beginIndex = 1;
		}
		for (int spanindex = beginIndex; spanindex < spans.size(); spanindex++) {
			if (spans.get(spanindex).evaluateXPath("em").length == 0) {
				num = 0;
			} else {
				num = getNums(HtmlCleanerUtil.getValueByXpath(
						spans.get(spanindex), "em", "0", "text"), task);
			}
			if (spanindex == beginIndex) {
				repostNum = num;
			} else if (spanindex == beginIndex + 1) {
				commentNum = num;
			} else {
				supportNum = num;
			}
		}
		map.put(Constants.REPOST, repostNum);
		map.put(Constants.COMMENT, commentNum);
		map.put(Constants.SUPPORT, supportNum);

		if (repostNum > 0) {
			String repostUrl = map.get(Constants.weiboURL) + "?type=repost";
			map.put(Constants.REPOST_URL, repostUrl);
		}
		// LOG.info(getUrl(task)+" 转发:"+repostNum);
		
// TODO 有没有评论都生成评论url
//		if (commentNum > 0) {
			String commentUrl = map.get(Constants.weiboURL) + "?type=comment";
			// LOG.info(getUrl(task)+" commentUrl:"+commentUrl);
			map.put(Constants.COMMENT_URL, commentUrl);
//		}
			
		// LOG.info(getUrl(task)+" 评论："+comment.getText().toString());
		String berepostUrl = HtmlCleanerUtil.getValueByXpath(div,
				"//div[@class='comment_info']/div[3]/div[1]/ul[1]/li[2]/a", "",
				"href");
		// 被转发微博url
		map.put(Constants.berepost, berepostUrl);
	}

	private void getWeiboImgs(TagNode dl, Map<String, Object> map) {
		/**
		 * @author tsg
		 * @description to get imgs of weibo content; the field is called
		 *              weiboImgs
		 */
		// >>>>>>>>>>>>>>>>>>>>>>--------------weiboImgs----------------------------------------
		TagNode contentImgsTag = HtmlCleanerUtil.getNodeByXpath(dl,
				"//div[@class='media_box']/ul");
		Object[] contentImgs = null;
		try {
			contentImgs = contentImgsTag.evaluateXPath("/li/img");
		} catch (NullPointerException e) {
			LOG.info(SearchParser.class.getName() + "---contentImgs is null");
		} catch (Exception e) {
			e.printStackTrace();
		}
		StringBuffer contentImgsSb = new StringBuffer();
		String contentImgsStr = "";
		if (null != contentImgs) {
			for (int j = 0; j < contentImgs.length; j++) {
				TagNode contentImg = (TagNode) contentImgs[j];
				String contentImgUrl = contentImg.getAttributeByName("src");
				contentImgsSb.append(contentImgUrl + "|");
			}
			contentImgsStr = contentImgsSb.toString();
			if (contentImgsStr.length() > 1) {
				contentImgsStr = contentImgsStr.substring(0,
						contentImgsStr.length() - 1);
			}
		}
		map.put(Constants.weiboImgs, contentImgsStr);
	}

	private Map<String, Object> parseSearchRs(TagNode root, Task task)
			throws XPatherException {

		Map<String, Object> rs = new HashMap<String, Object>();
		List<Map<String, Object>> searchWeibos = new ArrayList<Map<String, Object>>();
		// try {
		Object[] divs = root
				.evaluateXPath("//div[@action-type='feed_list_item']");
		for (int i = 0; i < divs.length; i++) {
			Map<String, Object> map = new HashMap<String, Object>();
			try {
				TagNode div = (TagNode) divs[i];
				//---new star
				TagNode divTag = (TagNode) div;
				map.put("wid", divTag.getAttributeByName("mid"));
				//----- new end
				TagNode dl = HtmlCleanerUtil.getNodeByXpath(div, "div[1]/dl[1]");
				if(dl==null){
					continue;
				}
				TagNode a = HtmlCleanerUtil.getNodeByXpath(dl, "//div[@class='face']/a[1]");
				if(a==null){
					continue;
				}
				String surl = HtmlCleanerUtil.getValueByXpath(dl, "//div[@class='face']/a[1]", "", "href");
				surl = formatUserParsedUrl(surl);
				map.put(Constants.USER_URL, surl);
				String imgUrl = HtmlCleanerUtil.getValueByXpath(a, "img[1]", "", "src");
				map.put(Constants.iconIMG, imgUrl);

				TagNode contentTag = HtmlCleanerUtil.getNodeByXpath(dl, "//div[@class='feed_content wbcon']/p[@class='comment_txt']");
				if(contentTag==null){
					continue;
				}
				String content = contentTag.getText().toString();
				Object[] imgs = contentTag.evaluateXPath("img");
				for (int j = 0; j < imgs.length; j++) {
					TagNode img = (TagNode) imgs[j];
					String feelingStr = img.getAttributeByName("title");
					if (feelingStr != null && feelingStr.trim().length() != 0) {
						content += feelingStr;

					}

				}
				map.put(Constants.CONTENT, content);
				getWeiboImgs(dl,map);
				// <<<<<<<<<<<<<<<<<<<<-----------------weiboImgs--------------------------------------
				String timeXpath = "//div[@class='content clearfix']/div[@class='feed_from W_textb']/a[@class='W_textb']";
//				HtmlCleanerUtil.getAllNodePathByTagNode(dl, 0);
				String dateStr = HtmlCleanerUtil.getValueByXpath(dl, timeXpath, "0", "date");
				String weiboUrl = HtmlCleanerUtil.getValueByXpath(dl, timeXpath, "0", "href");
				map.put(Constants.TIME, getSecodeTime(dateStr));
				map.put(Constants.weiboURL, weiboUrl);
				String source = HtmlCleanerUtil.getValueByXpath(dl, "//div[@class='content clearfix']/div[@class='feed_from W_textb']/a[@rel='nofollow']", "", "text");
				if(source.length()==0){
					source = HtmlCleanerUtil.getValueByXpath(dl, "dd[1]/p[@class='info W_linkb W_textb']", "", "text");
				}
				map.put(Constants.SOURCE, source);
				/**
				 * Author tsg this is to get APP source and it's 'href' attr;
				 */
				// >>>>>>>>>>>>>>>>>>>>>>-----------source With
				// href----------------------------------------
				TagNode sourceNode = HtmlCleanerUtil.getNodeByXpath(dl,
						"//div[@class='feed_from W_textb']/a[@rel='nofollow']");
				String hrefAndSource = "";
				if (sourceNode != null) {
					hrefAndSource = "<a href=\""
							+ sourceNode.getAttributeByName("href")
							+ " rel=\"nofollow\">" + sourceNode.getText()
							+ "</a>";
				}
				map.put(Constants.hrefAndSource, hrefAndSource);
				// <<<<<<<<<<<<<<<<<<<<<<---------------source With
				// href-----------------------------------------			

//				Object[] attrs = div
//						.evaluateXPath("//span[@class='line S_line1']");
//				List<TagNode> spans = HtmlCleanerUtil.getNodesByXpath(div, "//span[@class='line S_line1']");
				
//					continue;
//				}
				
				getSupportAndComment(div, map,task);
				String userInfoXpath = "//div[@class='face']/a[1]";
				String username = HtmlCleanerUtil.getValueByXpath(dl, userInfoXpath, "", "title");
				String userUrl = HtmlCleanerUtil.getValueByXpath(dl, userInfoXpath, "", "href");
				map.put(Constants.username, username);
				map.put(Constants.USER_URL, formatUserParsedUrl(userUrl));
				
				
				
//				Object[] aObjs = HtmlCleanerUtil.getNodeByXpath(dl, "//div[@class='feed_content wbcon']/a[1]/a");
//						dl.evaluateXPath("div[1]/div[2]/div[1]/a");
				List<TagNode> nodes = HtmlCleanerUtil.getNodesByXpath(div, "//div[@class='feed_content wbcon']/a");
				
				List<String> checks = new ArrayList<String>();
				String check = "";
				for (int j = 0; j < nodes.size(); j++) {
					TagNode aTag = nodes.get(j);
					if (j == 0) {
						// map.put(Constants.username, aTag.getText().toString()
						// .trim());
						// map.put(Constants.USER_URL, formatUserParsedUrl(aTag
						// .getAttributeByName("href")));
						// LOG.info(rs.get("username")+":"+rs.get("userUrl"));
						continue;
					}
					if (aTag.getAttributeByName("title") != null
							&& aTag.getAttributeByName("title").trim().length() > 0) {
						// LOG.info("check1:"+aTag.getAttributeByName("title"));
						check = aTag.getAttributeByName("title");
					} else if (aTag.evaluateXPath("img").length > 0) {
						TagNode img = (TagNode) aTag.evaluateXPath("img")[0];
						// LOG.info("check2:"+img.getAttributeByName("title"));
						check = img.getAttributeByName("title");
					}
					if (check.equals("微博个人认证")) {
						map.put(Constants.isChecked, 1);
						map.put(Constants.userType, 1);
						continue;
					} else if (check.equals("微博机构认证")) {
						map.put(Constants.isChecked, 1);
						map.put(Constants.userType, 2);
						continue;
					} else if (check.equals("微博会员")) {
						map.put(Constants.isVIP, 1);
						continue;
					}

					checks.add(check);
				}
				if (!map.containsKey(Constants.userType)) {
					map.put(Constants.userType, 1);
				}
				map.put(Constants.checks, checks);
				// LOG.info("---------------------------------------");
			} catch (XPatherException e) {
				e.printStackTrace();
			}
			searchWeibos.add(map);
		}
		// } catch (XPatherException e) {
		// e.printStackTrace();
		// }
		List<String> pageUrls = getPageNum(root, task);
		rs.put(Constants.WEIBOS, searchWeibos);
		rs.put(Constants.PAGE_URLS, pageUrls);
		return rs;
	}

	@Override
	public String getFlagStr() {
		// return "pl_wb_feedlist";
		return "pl_weibo_direct";
	}

	@Override
	public Map<String, Object> executeParse(TagNode root, Task task)
			throws XPatherException {
		return parseSearchRs(root, task);
	}

	public static void main(String[] args) {
		DownloadClient client = new DownloadClient();
		// 个人用户信息url
		// String url = "http://weibo.com/p/1006061957258370/weibo";
		// String url = "http://weibo.com/tclmobile";
		// 微博
		// String url = "http://weibo.com/p/1005051172294045/weibo";
		// 粉丝url
		String url =
		// "http://weibo.com/p/1006061812511057/follow?relate=fans";
		"http://s.weibo.com/wb/%25E5%258D%258E%25E4%25B8%25BA%25E8%258D%25A3%25E8%2580%2580&xsort=time";
		// String realUrl = "";
		String html = "";
		String ip = "117.121.9.166";
		/**
		 * Annotation tsg the 'html' string is the html with weibo list content
		 * ;
		 */
		html = client.getPageData(url, "0", "sina", "keyword", refer, cookie,
				ip);
		// LOG.info("html:"+html);
		// // html = MyStringUtil.loadConvert(html);
		// if(html.indexOf("未通过审核应用")>0){
		// break;
		// }
		// }
		// if(i==13){
		// return;
		// }
		// html = client.getPageData(realUrl, "", refer, cookie);
		// String cookies =
		// "SUS=SID-3735289334-1415585936-GZ-nl07w-7e18bc43201cf4ec90c7497729518657; path=/; domain=.weibo.com;SUS=SID-3735289334-1415585936-GZ-nl07w-7e18bc43201cf4ec90c7497729518657; path=/; domain=.weibo.com; httponly;SUE=es%3D4c869506727c1b8d5050809d374de52b%26ev%3Dv1%26es2%3D090f0cd1325c4477c6b98aa81190f540%26rs0%3DP6TQUaN5iByw2X5HfePWcuEdPVuCAb5Im1SyGHGgqPfcDIpjI58vHn2TjVYmjCLXTclGIzWP1sYVpFNL0y8qCI30jPXfjjyZizdeCIKeUcKAwwPCL5QUrVA%252FoR4HO02L3EgEOqm7J%252FONnrjUZ5fl87bPDXQTcpcJL6eXHDHwArc%253D%26rv%3D0;path=/;domain=.weibo.com;Httponly;SUP=cv%3D1%26bt%3D1415585936%26et%3D1415672336%26d%3Dc909%26i%3D8657%26us%3D1%26vf%3D0%26vt%3D0%26ac%3D2%26st%3D0%26uid%3D3735289334%26name%3D13640703072%26nick%3D%25E8%2589%25BA%25E8%25BD%25AE%25E8%258A%259C%25E6%2599%25B4%25E5%2590%259FtE7O66%26fmp%3D%26lcp%3D;path=/;domain=.weibo.com;SUB=_2AkMjPK-na8NlrABWm_0XxGLhb4lH-jyQ6yVRAn7uJhIyHRgv7lI_qSVaDSuToRtwo2KnogChFcfqYvpKfg..; path=/; domain=.weibo.com; httponly;SUBP=0033WrSXqPxfM725Ws9jqgMF55529P9D9WWodbidkSIl85xRrI67LflU5JpX5K2t; expires=Tuesday, 10-Nov-15 02:18:56 GMT; path=/; domain=.weibo.com;SRT=E.vAfqiqJsJZXuJqJrvcoRAvmBvXvCvXM4RCCAvnEABvzvvv4maJyHfXRmvvvDwnVmPFID!vXFvRvvvXLhCXzvBvrBJAtBvA0BvMmCvvzNvAz7*B.vAflW-P9Rc0lR-ykADvnJqiQVbiRVPBtS!r3JZPQVqbgVdWiMZ4siOzu4DbmKPWQU-M8TdMh4rbjKDPt4eHHiZWJScoPi49ndDPIJeA7; expires=Thursday, 07-Nov-24 02:18:56 GMT; path=/; domain=.passport.weibo.com; httponly;SRF=1415585936; expires=Thursday, 07-Nov-24 02:18:56 GMT; path=/; domain=.passport.weibo.com;ALF=1447121936; expires=Tue, 10-Nov-2015 02:18:56 GMT; path=/; domain=.weibo.com;SSOLoginState=1415585936; path=/; domain=.weibo.com";
		// MyCrawler crawler = new MyCrawler();
		// String htmls[] = crawler.get(url,
		// "http://d.weibo.com/?topnav=1&mod=logo&wvr=6", cookie);
		// // html = MyStringUtil.loadConvert(htmls[1]);
		System.out.println(html);
		Task task = getTestTask(url);
		try {
			new SearchParser().parseHtml(html, task);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main2(String[] args) {
		DownloadClient client = new DownloadClient();
		// 个人用户信息url
		// String url = "http://weibo.com/p/1006061957258370/weibo";
		// String url = "http://weibo.com/tclmobile";
		// 微博
		// String url = "http://weibo.com/p/1005051172294045/weibo";
		// 粉丝url
		String url =
		// "http://weibo.com/p/1006061812511057/follow?relate=fans";
		"http://s.weibo.com/wb/%25E5%258D%258E%25E4%25B8%25BA%25E8%258D%25A3%25E8%2580%2580&xsort=time";
		// String refer = "http://weibo.com";
		// String cookie =
		// "UOR=t.58.com,widget.weibo.com,uniweibo.com; SUBP=0033WrSXqPxfM725Ws9jqgMF55529P9D9WhO7LVEKZ.CkNhfjsx_vaoK5JpX5KMt; SINAGLOBAL=494012703352.6509.1400581736816; ULV=1409716104656:15:2:2:5013886898212.255.1409716104653:1409651338546; ALF=1441248547; wvr=5; YF-V5-G0=5161d669e1ac749e79d0f9c1904bc7bf; YF-Ugrow-G0=98bd50ad2e44607f8f0afd849e67c645; SUS=SID-1661429500-1409712550-GZ-ypznf-339ca1bee4ed9b219bec536267c28657; SUE=es%3Dbfebe83c58d2caa9f74d479a586b2501%26ev%3Dv1%26es2%3D7b4bccfab3be45a61fd1f2a697b4db15%26rs0%3DUCWny%252FVr8SleJiZqQo8C7wp1r11Iet6oJnNKHvtvPqET1BN%252B%252BTIDrKJEitumf94TZJNje2g%252FgUIBPHtT7BPcCSymrHpmG8UrKSszfdnwocJ2TH%252FzB%252FJytsnxciZUVpGvpTzV%252FxSs3XXMrnWiZr5radfJ5xU8Iv%252B18APfONo7OdA%253D%26rv%3D0; SUP=cv%3D1%26bt%3D1409712550%26et%3D1409798950%26d%3Dc909%26i%3D8657%26us%3D1%26vf%3D0%26vt%3D0%26ac%3D0%26st%3D0%26uid%3D1661429500%26name%3Dbakerham1982%2540sina.cn%26nick%3D%25E8%2593%259D%25E5%25A4%25A9%26fmp%3D%26lcp%3D2012-06-29%252010%253A12%253A45; SUB=_2AkMjWg6Ra8NlrAJXnvkRzmLnbI1H-jyQgMJnAn7uJhIyGxgv7mgDqSU-472N8lMfFiWbTbDahZKdBz31AQ..; SSOLoginState=1409712550; YF-Page-G0=cf25a00b541269674d0feadd72dce35f; _s_tentry=uniweibo.com; Apache=5013886898212.255.1409716104653";
		// String cookie =
		// "UOR=t.58.com,widget.weibo.com,login.sina.com.cn; SUBP=0033WrSXqPxfM725Ws9jqgMF55529P9D9WhO7LVEKZ.CkNhfjsx_vaoK5JpX5K2t; SINAGLOBAL=494012703352.6509.1400581736816; ULV=1410575625520:24:11:8:8345642190829.736.1410575625509:1410572979576; un=15110295667; SUB=_2AkMjT2v9a8NlrAJXnvkRzmLnbI1H-jyQn2ELAn7uJhIyHRgv7kQWqSWFQrzYY-oPOTev4p50rG8H_zniYg..; myuid=1661429500; _s_tentry=-; Apache=8345642190829.736.1410575625509; SWB=usrmd1071; un=15110295667; WBStore=b9261755eac1ef77|undefined; WBtopGlobal_register_version=42c9badfc29f9490; SUS=SID-1661429500-1410589898-GZ-uinzv-bc24acecdf29401d776f638e89658657; SUE=es%3D2b6836c925acdc743a06177a3a9890ae%26ev%3Dv1%26es2%3Dcbecd649943f72ba4001c51326b1d13d%26rs0%3DP7mKGlW66rWEvRgadFvPgw8X6LvkK%252B3WI%252F1YpoD3fOC6VljAjFBlEQKfE91cfmQ%252BBJmbVa9I%252BcDPlzIlp6YE69yNV3D%252BZvYlxRrEgmWjYZuF7acyjWiduDn19BiyVAdndLiJpDlr5EsZQVtDgPM8YY%252BsEhM9Kf6shrGExT5P3IM%253D%26rv%3D0; SUP=cv%3D1%26bt%3D1410589898%26et%3D1410676298%26d%3Dc909%26i%3D8657%26us%3D1%26vf%3D0%26vt%3D0%26ac%3D0%26st%3D0%26uid%3D1661429500%26name%3Dbakerham1982%2540sina.cn%26nick%3D%25E8%2593%259D%25E5%25A4%25A9%26fmp%3D%26lcp%3D2012-06-29%252010%253A12%253A45; ALF=1442125898; SSOLoginState=1410589899";
		String html = client.getPageData(url, "0", "Csina", "item", refer,
				cookie, "");
		html = StringUtil.loadConvert(html);
		LOG.info(html);
	}

}
