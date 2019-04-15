package com.bfd.parse.reprocess;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.ConstantFunc;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

/**
 * 
 * @author bfd_05
 * 
 */
public class BdzwwwListRe implements ReProcessor {

	private static final Log LOG = LogFactory.getLog(BdzwwwListRe.class);
	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		//做url的替换，避免url中每次都变得部分导致列表页一直刷
		String pagedata = null;
		if (resultData != null && !resultData.isEmpty()) {
			pagedata = unit.getPageData();
			String url = null;
			url = unit.getUrl();
			// 做url的处理，获得重定向之后的url
			if (resultData.containsKey(Constants.TASKS)) {
				List<Map<String, Object>> tasks = (List<Map<String, Object>>) resultData
						.get(Constants.TASKS);
				decodeLink(tasks, pagedata);
				// 处理下一页链接
				if (pagedata.contains(">下一页<") && url.contains("www.so.com")) {
					getNextpage(resultData, url, tasks);
				}
			}
			List<Map> items = (List<Map>) resultData.get(Constants.ITEMS);
			decodeLinkitems(items, pagedata);
		}
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}

	private void getNextpage(Map<String, Object> resultData, String url, List<Map<String, Object>> tasks) {
		String nextpage = null;
		Map<String, Object> nextpageTask = new HashMap<String, Object>();
		String oldPageNum = getPage(url);
		if (oldPageNum.equals("0")) {
			nextpage = url + "&pn=2";
		} else {
			int pageNum = Integer.valueOf(oldPageNum) + 1;
			nextpage = url.replace("&pn=" + oldPageNum, "&pn=" + pageNum);
		}

		nextpageTask.put("link", nextpage);
		nextpageTask.put("rawlink", nextpage);
		nextpageTask.put("linktype", "bbspostlist");
		resultData.put("nextpage", nextpage);
		tasks.add(nextpageTask);
	}

	/**
	 * 处理生成新闻内容页的链接,针对使用360搜索的列表页
	 * 
	 * @param tasks
	 * @param pagedata
	 */
	private void decodeLink(List<Map<String, Object>> tasks, String pagedata) {
		for (int i = 0; i < tasks.size(); i++) {
			if (tasks.get(i).containsKey(Constants.LINK)
					&& "bbspost".equals(tasks.get(i).get("linktype").toString())) {
				tasks.remove(i);
				i--;
			}
		}

		List<String> dataurl = new ArrayList<String>();
		try {
			Pattern p = Pattern.compile("data-url=\"(\\S+)\"");
			Matcher m = p.matcher(pagedata);
			while (m.find()) {
				if (dataurl.contains(m.group(1))) {
					continue;
				} else {
					dataurl.add(m.group(1));
				}
			}
			for (int i = 0; i < dataurl.size(); i++) {
				if (dataurl.get(i).toString().contains("360") || dataurl.get(i).toString().contains("so.com")) {
					dataurl.remove(i);
					i--;
				}
			}

			for (int i = 0; i < dataurl.size(); i++) {
				Map<String, Object> content = new HashMap<String, Object>();
				content.put(Constants.LINK, dataurl.get(i));
				content.put(Constants.RAWLINK, dataurl.get(i));
				content.put(Constants.LINKTYPE, "bbspost");
				tasks.add(content);
			}

		} catch (Exception e) {
		}
	}
	
	private void decodeLinkitems(List<Map> tasks, String pagedata) {
		tasks.removeAll(tasks);
		List<String> dataurl = new ArrayList<String>();
		try {
			Pattern p = Pattern.compile("data-url=\"(\\S+)\"");
			Matcher m = p.matcher(pagedata);
			while (m.find()) {
				if (dataurl.contains(m.group(1))) {
					continue;
				} else {
					dataurl.add(m.group(1));
				}
			}
			for (int i = 0; i < dataurl.size(); i++) {
				if (dataurl.get(i).toString().contains("360") || dataurl.get(i).toString().contains("so.com")) {
					dataurl.remove(i);
					i--;
				}
			}

			for (int i = 0; i < dataurl.size(); i++) {
				long now = new Date().getTime();
				String posttime = null;
				posttime = ConstantFunc.convertTime(String.valueOf(now/1000));
				Map<String, Object> content = new HashMap<String, Object>();
				Map<String, Object> itemlink = new HashMap<String, Object>();
				itemlink.put(Constants.LINK, dataurl.get(i));
				itemlink.put(Constants.RAWLINK, dataurl.get(i));
				itemlink.put(Constants.LINKTYPE, "bbspost");
				content.put("itemlink", itemlink);
				content.put(Constants.POSTTIME, posttime);
				content.put(Constants.REPLY_CNT, -1024);
				tasks.add(content);
			}

		} catch (Exception e) {
		}
	}

	// 获取页数
	private String getPage(String url) {
		Pattern iidPatter = Pattern.compile("&pn=(\\d+)");
		Matcher match = iidPatter.matcher(url);
		if (match.find()) {
			return match.group(1);
		} else {
			return "0";
		}
	}
}
