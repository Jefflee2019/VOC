package com.bfd.parse.reprocess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.crawler.utils.JsonUtils;
import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

/**
 * 站点名：淘宝首页搜索
 * <P>
 * 主要功能：取得商品名称，url,价格等
 * 
 * @author bfd_01
 * 
 */
public class Etaobao_hwListRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(Etaobao_hwListRe.class);

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		// 取到解析结果
		Map<String, Object> resultData = result.getParsedata().getData();
		String pageUrl = result.getSpiderdata().get("location").toString();
		if (!resultData.isEmpty() && (!pageUrl.contains("hwhonor"))) {
			List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
			Object obj = getList(unit.getPageData());
			Map<String, Object> map = new HashMap<String, Object>();
			try {
				map = (Map<String, Object>) JsonUtils.parseObject(obj
						.toString());
			} catch (Exception e) {
				LOG.error(e);
			}
			List<Map<String, Object>> items = (List<Map<String, Object>>) map
					.get("auctions");
			if (items != null) {
				List<String> itemlink = new ArrayList<String>();
				for (int i = 0; i < items.size(); i++) {
					Map<String, Object> item = new HashMap<String, Object>();
					Map<String, Object> temp = (Map<String, Object>) items
							.get(i);
					String url = getUrl(temp.get("detail_url").toString());
					if (url.contains("https://item.taobao.com")) {
						item.put(Constants.ITEMNAME, temp.get("title")
								.toString().replaceAll("<.*>", ""));
						item.put(Constants.ITEMLINK, url);
						item.put(Constants.REPLY_CNT, temp.get("comment_count"));
						item.put(Constants.QUANTITY, temp.get("view_sales")
								.toString().replace("人收货", "").replace("人付款", ""));
						item.put(Constants.STORENAME, temp.get("nick"));
						data.add(item);
						itemlink.add(url);
					}
				}
				resultData.put(Constants.ITEMS, data);

				// 生成商品页任务
				for (int i = 0; i < itemlink.size(); i++) {
					Map<String, Object> task = new HashMap<String, Object>();
					task.put("link", itemlink.get(i));
					task.put("rawlink", itemlink.get(i));
					task.put("linktype", "eccontent");// 任务为内容页
					if (!resultData.isEmpty()) {
						List<Map<String, Object>> tasks = (List<Map<String, Object>>) resultData
								.get(Constants.TASKS);
						tasks.add(task);
						ParseUtils.getIid(unit, result);
					}
				}
			}
		}
		/* 针对于淘宝店铺搜索处理其下一页字段 */
		if (resultData.containsKey(Constants.NEXTPAGE)) {
			String pageNoStr = (String) resultData.get("pageno");
			int pageNo = Integer.parseInt(pageNoStr);
			String nextPageUrl = null;
			if (pageNo == 1) {
				nextPageUrl = pageUrl + "&pageNo=2";
			} else {
				nextPageUrl = pageUrl.replace("pageNo=" + pageNo, "pageNo="
						+ (pageNo + 1));
			}
			resultData.put(Constants.NEXTPAGE, nextPageUrl);
			List<Map<String, Object>> tasks = (List<Map<String, Object>>) resultData
					.get(Constants.TASKS);
			Map<String, Object> firstTask = tasks.get(0);
			firstTask.put("link", nextPageUrl);
			firstTask.put("rawlink",
					nextPageUrl.replace("http:", "").replace("https:", ""));
		}

		return new ReProcessResult(processcode, processdata);
	}

	private static String getUrl(String str) {
		return "https:" + str.replace("\\u003d", "=").replace("\\u0026", "&");
	}

	/**
	 * 取得商品列表
	 * 
	 * @param pageData
	 * @return
	 */
	private Object getList(String pageData) {
		Object obj = null;
		Pattern p = Pattern.compile("\"msrp_auction\",(.*),\"recommendAuctions\"");
		Matcher m = p.matcher(pageData);
		while (m.find()) {
			obj = m.group(1);
		}
		return "{" + obj + "}";
	}
}
