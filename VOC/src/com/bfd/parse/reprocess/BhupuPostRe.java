package com.bfd.parse.reprocess;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.ConstantFunc;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

/**
 * @site:虎扑社区 (Bhupu)
 * @function 帖子页post请求，需要带cookie,不然会重定向到主页
 * 
 * @author bfd_02
 *
 */

public class BhupuPostRe implements ReProcessor {

	private static final Log LOG = LogFactory.getLog(BhupuPostRe.class);

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = new HashMap<String, Object>();
		// 解析数据
		resultData = result.getParsedata().getData();
		if (resultData == null) {
			return null;
		}

		// 获取当前页url
		String currentUrl = unit.getUrl();
		// 下载带cookie的数据
		String postData = htmlDownPostData(currentUrl);
		// 解析postData
		Document data = Jsoup.parse(postData);
		// 获取标题
		getTitle(resultData, data);
		// 获取作者
		getAuthor(resultData, data);
		// 回复数、亮数及下一页
		getReplyAndLightcount(resultData, data, currentUrl);
		// 提取主贴内容
		getContent(resultData, data);
		// 发帖时间
		getNewstime(resultData, data);
		// 获取浏览数
		getViewCnt(resultData, currentUrl);
		// 回帖部分
		getReply(resultData, data);
		// 去掉第二页以后的发表时间、作者、主贴内容
		if (currentUrl.contains("-")) {
			resultData.remove(Constants.NEWSTIME);
			resultData.remove(Constants.AUTHOR);
			resultData.remove(Constants.CONTENTS);
		}

		 ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}

	/**
	 * @param replycount
	 *            lightcount
	 */
	private static int getReplyNLightRex(String replycount, String reg) {
		int replyCnt = 0;
		Matcher match = Pattern.compile(reg).matcher(replycount);
		if (match.find()) {
			replyCnt = Integer.parseInt(match.group(1));
		}
		return replyCnt;
	}

	/**
	 * @param resultData
	 * @param element
	 *            标题
	 */
	private static void getTitle(Map<String, Object> resultData, Document data) {
		String title = data.getElementById("j_data").text();
//		String title = data.title();
		resultData.put(Constants.TITLE, title);
	}

	/**
	 * @param resultData
	 * @param element
	 *            发帖人
	 */
	private static void getAuthor(Map<String, Object> resultData, Document data) {
		List<Map<String, Object>> authorlist = new ArrayList<Map<String, Object>>();
		Map<String, Object> authorMap = new HashMap<String, Object>();
		String authorEle = data.select("div.subhead").text();
		String authorReg = "由\\s*(\\S*)\\s*发表";
		Matcher authorMatch = Pattern.compile(authorReg).matcher(authorEle);
		if (authorMatch.find()) {
			String author = authorMatch.group(1);
			//去除全角空格
			if (author.contains(" ")) {
				author = author.substring(1, author.length() - 1);
			}
			authorMap.put(Constants.AUTHORNAME, author);
			authorlist.add(authorMap);
			resultData.put(Constants.AUTHOR, authorlist);
		}
	}

	/**
	 * @param resultData
	 * @param element
	 *            回复数 和 下一页 链接及任务
	 */
	@SuppressWarnings("unchecked")
	private static void getReplycountAndNextpage(Map<String, Object> resultData, int replyCount, String url) {
		// 页面
		int pageno = 1;
		// 当前页码
		String pagenoRex = "-(\\d+).html";
		pageno = getRex(url, pagenoRex);
		// 下一页
		if ((double) replyCount / 20 > pageno) {
			String nextpage = null;
			// 当前页码是第一页时
			if (pageno == 1) {
				nextpage = url.replaceAll(".html", "-2.html");
			} else {
				nextpage = url.replace("-" + pageno + ".html", "-" + (pageno + 1) + ".html");
			}
			List<Map<String, Object>> tasks = (List<Map<String, Object>>) resultData.get(Constants.TASKS);
			Map<String, Object> nextpageTask = new HashMap<String, Object>();
			nextpageTask.put(Constants.LINK, nextpage);
			nextpageTask.put(Constants.RAWLINK, nextpage);
			nextpageTask.put(Constants.LINKTYPE, "bbspost");
			if (resultData != null) {
				resultData.put(Constants.NEXTPAGE, nextpage);
				tasks.add(nextpageTask);
			}
		}
	}

	/**
	 * @param url
	 * @param pagenoRex
	 */
	private static int getRex(String url, String pagenoRex) {
		int pageno = 0;
		Matcher match = Pattern.compile(pagenoRex).matcher(url);
		if (match.find()) {
			pageno = Integer.parseInt(match.group(1));
		} else {
			pageno = 1;
		}
		return pageno;
	}

	/**
	 * @param resultData
	 * @param element
	 *            亮数
	 */
	private static void getReplyAndLightcount(Map<String, Object> resultData, Document data, String url) {
		String replyNLight = data.select("span.browse>span").text();
		// 回复数
		String replyReg = "(\\d+)回复";
		int replycount = getReplyNLightRex(replyNLight, replyReg);
		resultData.put(Constants.REPLYCOUNT, replycount);
		// 获取下一页任务
		getReplycountAndNextpage(resultData, replycount, url);

		// 亮数
		String lightReg = "/(\\d+)亮";
		int lightCnt = getReplyNLightRex(replyNLight, lightReg);
		resultData.put("lightcount", lightCnt);
	}

	/**
	 * @param resultData
	 * @param element
	 *            回帖部分
	 */
	private static void getReply(Map<String, Object> resultData, Document data) {
		// 存放回帖部分
		List<Map<String, Object>> replys = new ArrayList<Map<String, Object>>();
		Elements replyelements = data.select("div.floor-show  ");
		if (!replyelements.isEmpty()) {
			for (int i = 0; i < replyelements.size(); i++) {
				Map<String, Object> temMap = new HashMap<String, Object>();
				Element replyEle = replyelements.get(i);
				// 回复楼层
				String floornum = replyEle.select("a.floornum").text();
				if (floornum.equals("楼主")) {
					replyelements.remove(replyEle);
					i--;
					continue;
				}
				if (floornum.contains("楼")) {
					int floor = Integer.parseInt(floornum.replace("楼", ""));
					temMap.put(Constants.REPLYFLOOR, floor);
				}

				// 回帖人
				String replyusername = replyEle.select("a.u").text();
				temMap.put(Constants.REPLYUSERNAME, replyusername);

				// 回贴时间
				String replyDate = replyEle.select("span.stime").eq(0).text();
				temMap.put(Constants.REPLYDATE, replyDate);

				// 回帖亮数
				String lightReg = "\\(<span\\s*class=\"stime\">(\\d+)</span>\\)";
				int replyLightcount = getLightcountRex(replyEle, lightReg);
				temMap.put("replyLightcount", replyLightcount);

				// 回帖内容
				String replycontent = replyEle.select("td").text();
				// 去掉引用部分
				String refercontent = replyEle.getElementsByTag("blockquote").text();
				if (replycontent.contains(refercontent)) {
					replycontent = replycontent.replace(refercontent, "");
				}
				if (replycontent.contains("发自") && replycontent.contains("虎扑")) {
					replycontent = replycontent.replaceAll("发自\\S*虎扑\\S*", "").trim();
				}
				temMap.put(Constants.REPLYCONTENT, replycontent);

				replys.add(temMap);
			}
			resultData.put(Constants.REPLYS, replys);
		}
	}

	/**
	 * @param replyEle
	 * @param lightReg
	 * 		replyLightcount 回帖亮数
	 */
	private static int getLightcountRex(Element replyEle, String lightReg) {
		int lightcount = 0;
		Matcher match = Pattern.compile(lightReg).matcher(replyEle.toString());
		if (match.find()) {
			lightcount = Integer.parseInt(match.group(1));
		}
		return lightcount;
	}

	/**
	 * @param resultData
	 * @param element
	 *            主贴内容
	 */
	private static void getContent(Map<String, Object> resultData, Document data) {
		String contents = data.select("div.quote-content").text();
		if (contents.contains("发自") && contents.contains("虎扑")) {
			contents = contents.replaceAll("发自\\S*虎扑\\S*", "");
		}
		resultData.put(Constants.CONTENTS, contents);
	}

	/**
	 * @param resultData
	 * @param element
	 *            发帖时间
	 */
	private static void getNewstime(Map<String, Object> resultData, Document data) {
		String newstime = data.select("div.floor-show  ").eq(0).select("span.stime").text();
		if (!newstime.equals("")) {
			newstime = ConstantFunc.convertTime(newstime);
		}
		resultData.put(Constants.NEWSTIME, newstime);
	}

	/**
	 * @param resultData
	 * @param element
	 *            浏览数
	 */
	private static void getViewCnt(Map<String, Object> resultData, String url) {
		// 帖子页：https://bbs.hupu.com/24057016.html
		// 浏览数：https://msa.hupu.com/thread_hit?tid=24057016
		Matcher match = Pattern.compile("/(\\d+).html").matcher(url);
		if (match.find()) {
			String tid = match.group(1);
			String viewUrl = "https://msa.hupu.com/thread_hit?tid=" + tid;
			String viewCnt = htmlDownPostData(viewUrl);
			resultData.put(Constants.VIEW_CNT, viewCnt);
		}
	}

	/**
	 * 
	 * @param url
	 * @return postData 带cookie的数据
	 */
	private static String htmlDownPostData(String url) {
		String postData = null;
		CloseableHttpClient client = HttpClients.createDefault();
		HttpGet request = new HttpGet(url);

		request.setHeader(
				"Cookie",
				"_dacevid3=d2ca4059.ffd5.c5e3.41d0.083c7bdfb4a9; __gads=ID=260ecae302402cac:T=1540266072:S=ALNI_MZOiACIDJ9ifhwuHSOpEWgX1fHJeA; PHPSESSID=487bf9528e61d0c9717d3d411b805761; shihuo_target_common_go_go=1; __dacevst=40da2d20.00fe260f|1540887084402; _cnzz_CV30020080=buzi_cookie%7Cd2ca4059.ffd5.c5e3.41d0.083c7bdfb4a9%7C-1; Hm_lvt_39fc58a7ab8a311f2f6ca4dc1222a96e=1540266285,1540343343,1540348024; Hm_lpvt_39fc58a7ab8a311f2f6ca4dc1222a96e=1540885285; _fmdata=%2FQsL8YC7eay%2FS%2FsmDWc0lR1gxy0QQM8xUdUxrxIgaPYBfDh0sVmN71XsqbhFOXeY5WimkQbk0xz2Ms2RV6gHNmqDg%2FYgDG7MCTzvCgJ041M%3D");
		request.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		request.setHeader("Accept-Encoding", "gzip, deflate, sdch, br");
		request.setHeader("User-Agent",
				"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36");
		CloseableHttpResponse response = null;
		try {
			response = client.execute(request);
			postData = EntityUtils.toString(response.getEntity(), "utf8");

		} catch (Exception e) {
			LOG.warn("httprequest download failed" + url);
		} finally {
			// 释放连接
			if (null != response) {
				try {
					response.close();
					client.close();
				} catch (IOException e) {
					LOG.warn("释放连接出错");
					e.printStackTrace();
				}
			}
		}
		return postData;
	}
}