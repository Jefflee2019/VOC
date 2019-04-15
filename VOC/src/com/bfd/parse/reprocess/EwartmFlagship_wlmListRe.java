package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.Iterator;
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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

/**
 * 站点名：(京东)沃尔玛官方旗舰店 cid：EwartmFlagship_wlm 主要功能：补充列表页后30商品、处理下一页及修改商品页任务链接
 * 
 * @author lth
 *
 */
public class EwartmFlagship_wlmListRe implements ReProcessor {

	private static final Log LOG = LogFactory.getLog(EwartmFlagship_wlmListRe.class);

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();

		/**
		 * 拼接不能标定的后30个商品的列表页链接
		 * 
		 * @pageindex 页码
		 * @keyword 关键字
		 * @showItems 前30商品itemid
		 */

		// https://search.jd.com/Search?keyword=E4%B8%AA%E9%85%8D%E4%BB%B6&enc=utf-8&page=1&qrst=1&psort=4
		String url = unit.getUrl();
		int pageindex = Integer.parseInt(getRegex("&page=(\\d+)", url));
		String keyword = getRegex("keyword=(\\S*)&enc", url);

		String showItems = null;
		StringBuffer itemsb = new StringBuffer();
		List<Map<String, Object>> items = (List<Map<String, Object>>) resultData.get("items");
		List<Map<String, Object>> tasks = (List<Map<String, Object>>) resultData.get("tasks");
		if (tasks != null && !tasks.isEmpty()) {
			for (Map<String, Object> taskMap : tasks) {
				if (taskMap.get("linktype").equals("wlmEcList")) {
					continue;
				} else {
					String link = taskMap.get("rawlink").toString();
					String itemid = getRegex("(\\d+).html", link);
					itemsb.append(itemid).append(",");
				}
			}
			if (itemsb.length() != 0) {
				itemsb = itemsb.deleteCharAt(itemsb.length() - 1);
				showItems = itemsb.toString();
			}
		}

		// 拼接后30个的商品link
		StringBuffer sb = new StringBuffer();
		String last30Link = sb.append("https://search.jd.com/Search/s_new.php?keyword=").append(keyword)
				.append("&enc=utf-8&qrst=1&rt=1&stop=1&vt=2&stock=1&page=").append(pageindex + 1)
				.append("&30&scrolling=y&log_id=1516773164.37617&tpl=1_M&show_items=").append(showItems).toString();
		/**
		 * 单独请求后30个商品，提取数据补充在resultData
		 */
		HttpClientBuilder builder = HttpClientBuilder.create();
		builder.setUserAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36");
		HttpClient client = builder.build();
		HttpGet request = new HttpGet(last30Link);
		request.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		request.setHeader("Accept-Encoding", "gzip, deflate, sdch, br");
		request.setHeader("Accept-Language", "zh-CN,zh;q=0.8");
		request.setHeader("Connection", "keep-alive");
		request.setHeader("Host", "search.jd.com");
		HttpResponse response;
		try {
			response = client.execute(request);
			String pageData = EntityUtils.toString(response.getEntity(), "utf-8");
			Document doc = Jsoup.parse(pageData);
			Elements eles = doc.select("li.gl-item");
			Iterator it = eles.iterator();
			while (it.hasNext()) {
				Element element = (Element) it.next();
				// 价格
				String priceEle = element.select("div.p-price>strong>i").text();
				// 商品url
				String urlEle = element.select("div.p-name").select("div.p-name-type-2").select("a").attr("href");
				// 补充完整链接
				if (!urlEle.contains("http")) {
					urlEle = "https:" + urlEle;
				}
				// 商品 title
				String titleEle = element.select("div.p-name").select("div.p-name-type-2").select("em").text();

				// 后30商品组装tasks
				Map<String, Object> taskMap = new HashMap<String, Object>();
				taskMap.put("link", urlEle);
				taskMap.put("rawlink", urlEle);
				taskMap.put("linktype", "wlmEcContent");

				// 后30商品组装items
				Map<String, Object> itemMap = new HashMap<String, Object>();
				Map<String, Object> linkMap = new HashMap<String, Object>();
				linkMap.put("link", urlEle);
				linkMap.put("rawlink", urlEle);
				linkMap.put("linktype", "wlmEcContent");
				itemMap.put("itemlink", linkMap);
				itemMap.put("itemprice", priceEle);
				itemMap.put("itemname", titleEle);

				// items和tasks合并
				items.add(itemMap);
				tasks.add(taskMap);
			}
		} catch (Exception e) {
			LOG.error("httprequest download failed" + last30Link);
		}

		// 区别于京东，绕过消重
		if (tasks != null && !tasks.isEmpty()) {
			for (Map task : tasks) {
				if (task.get("linktype").equals("wlmEcList")) {
					continue;
				} else {
					String link = task.get("link").toString();
					String linkChange = link.concat("?w=w");
					task.put("link", linkChange);
					task.put("rawlink", linkChange);
				}
			}
		}
			/**
			 * 处理下一页翻页
			 */
			//页面上的页码(不同于url的页码),只判断是否翻页，不参与链接页码的计算
			int pageno = Integer.parseInt(resultData.get("pageno").toString());
			if (resultData.containsKey("pagecount")) {
				int pagecount = Integer.parseInt(resultData.get("pagecount").toString());
				if (pagecount > pageno) {
					getNextpage(url, resultData, pageindex);
				}
				resultData.remove("pagecount");
			} else {
				// 当商品数改版导致获取失败时，下一页通过当页商品数和每页商品数(60)比较判断，考虑到参杂广告改55
				LOG.error("商品数获取出错或改版，" + "cid is EwartmFlagship_wlmListRe,and url is " + url);
				if (resultData.containsKey("items")) {
					if (items.size() >= 55) {
						getNextpage(url, resultData, pageindex);
					}
				}
			}
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}

	private String getRegex(String regex, String url) {
		Matcher match = Pattern.compile(regex).matcher(url);
		String result = null;
		if (match.find()) {
			result = match.group(1);
		}
		return result;
	}

	/**
	 * @param unit
	 * @param resultData
	 */
	@SuppressWarnings("unchecked")
	private void getNextpage(String url, Map<String, Object> resultData, int currPageNo) {
		String nextPage = url.replace("&page=" + currPageNo, "&page=" + (currPageNo + 2));
		Map<String, Object> nextpageTask = new HashMap<String, Object>();
		nextpageTask.put(Constants.LINK, nextPage);
		nextpageTask.put(Constants.RAWLINK, nextPage);
		nextpageTask.put(Constants.LINKTYPE, "wlmEcList");
		resultData.put(Constants.NEXTPAGE, nextPage);
		List<Map<String, Object>> tasks = (List<Map<String, Object>>) resultData.get(Constants.TASKS);
		tasks.add(nextpageTask);
	}
}
