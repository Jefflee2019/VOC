package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.ConstantFunc;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
/**
 * 站点名：花粉俱乐部
 * <p>
 * 主要功能：处理发帖时间
 * @author bfd_01
 *
 */
public class BhuafenListRe implements ReProcessor {

	@Override
	@SuppressWarnings("unchecked")
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		// 取到解析结果
		Map<String, Object> resultData = result.getParsedata().getData();
		if (resultData != null && !resultData.isEmpty()) {
			if(resultData.containsKey(Constants.ITEMS)) {
				List<Map<String,Object>> list = (List<Map<String,Object>>)resultData.get(Constants.ITEMS);
				for (int i=0;i<list.size();i++) {
					Map<String,Object> map = (Map<String,Object>)list.get(i);
					if (map.containsKey(Constants.POSTTIME)) {
						String posttime = map.get(Constants.POSTTIME).toString();
						posttime = ConstantFunc.convertTime(posttime);
						map.put(Constants.POSTTIME, posttime);
					}
				}
			}
			String url = unit.getUrl();
			// 默认排序，即最新回复排序，限制翻页深度为10页
			if (url.contains("http://cn.club.vmall.com/forum-")) {
				int pageNum = 1;
				Pattern p = Pattern.compile("-(\\d+).html");
				Matcher m = p.matcher(url);
				while (m.find()) {
					pageNum = Integer.valueOf(m.group(1));
				}
				//设置翻页深度
				if (pageNum > 9) {
					if (resultData.containsKey(Constants.NEXTPAGE)) {
						resultData.remove(Constants.NEXTPAGE);
					}
					if (resultData.containsKey(Constants.TASKS)) {
						List task = (List)resultData.get(Constants.TASKS);
						for (int i=0;i<task.size();i++) {
							Map map = (Map)task.get(i);
							if("bbspostlist".equals(map.get("linktype"))) {
								task.remove(i);
								break;
							}
						}
					}
					
				}
			} else {
				// 按最新发贴排序，只抓第一页
				if (resultData.containsKey(Constants.NEXTPAGE)) {
					resultData.remove(Constants.NEXTPAGE);
				}
				if (resultData.containsKey(Constants.ITEMS)) {
					List items = (List)resultData.get(Constants.ITEMS);
					for (int i=0;i<items.size();i++) {
						Map map = (Map) items.get(i);
						if (map.containsKey("itemlink")) {
							Map itemlink = (Map)map.get("itemlink");
							if (itemlink.containsKey("link")) {
								String temp = getPostLink(itemlink.get("link").toString());
								String rawlink = getPostRawlink(itemlink.get("link").toString());
								itemlink.put("link", temp);
								itemlink.put("rawlink",rawlink);
							}
						}
						
					}
				}
				
				if (resultData.containsKey(Constants.TASKS)) {
					List tasks = (List) resultData.get(Constants.TASKS);
					for (int i=0;i<tasks.size();i++) {
						Map map = (Map)tasks.get(i);
						if("bbspostlist".equals(map.get("linktype"))) {
							tasks.remove(i);
							break;
						}
					}
					for (int i=0;i<tasks.size();i++) {
						Map task = (Map) tasks.get(i);
						if (task.containsKey("link")) {
							String link = getPostLink(task.get("link").toString());
							String rawlink = getPostRawlink(task.get("link").toString());
							task.put("link", link);
							task.put("rawlink", rawlink);
						}
					}
				}
			}
			
		}
		return new ReProcessResult(processcode, processdata);
	}
	
	private String getPostLink(String str) {
		String url = null;
		String iid = null;
		// http://cn.club.vmall.com/viewthreaduni-12775109-filter-author-orderby-dateline-page-1-1.html
		Pattern p = Pattern.compile("viewthreaduni-(\\d+)-filter");
		Matcher m = p.matcher(str);
		while (m.find()) {
			iid = m.group(1);
		}
		if (iid != null) {
			// http://cn.club.vmall.com/thread-12775235-1-1.html
			url = "http://cn.club.vmall.com/thread-" + iid + "-1-1.html";
		}
		return url;
	}
	private String getPostRawlink(String str) {
		String rawlink = null;
		String iid = null;
		// http://cn.club.vmall.com/viewthreaduni-12775109-filter-author-orderby-dateline-page-1-1.html
		Pattern p = Pattern.compile("viewthreaduni-(\\d+)-filter");
		Matcher m = p.matcher(str);
		while (m.find()) {
			iid = m.group(1);
		}
		if (iid != null) {
			// http://cn.club.vmall.com/thread-12775235-1-1.html
			rawlink = "thread-" + iid + "-1-1.html";
		}
		return rawlink;
	}
}
