package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;
/**
 * 站点名：泡泡网-论坛
 * <p>
 * 主要功能：处理下一页
 * @author bfd_01
 *
 */
public class BpcpopListRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(BpcpopListRe.class);

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		// 取到解析结果
		Map<String, Object> resultData = result.getParsedata().getData();
		String next = unit.getPageData();
		if (!resultData.isEmpty()) {
			
			if (resultData.containsKey(Constants.ITEMS)) {
				List<Map<String,Object>> items = (List<Map<String,Object>>)resultData.get(Constants.ITEMS);
				for (int i=0;i<items.size();i++) {
					// 处理帖子发表时间
					String posttime = ((Map<String,Object>)items.get(i)).get(Constants.POSTTIME).toString();
					((Map<String,Object>)items.get(i)).put(Constants.POSTTIME, posttime.split(" ")[1]);
					// 添加回复数字段
					((Map<String,Object>)items.get(i)).put(Constants.REPLY_CNT, -1024);
				}
			}
			
			if (next.contains("下一页")) {
				String url = unit.getUrl();
				String urlHead = "http://so.pcpop.com/bbs?q=";
				String urlEnd = "&ie=utf-8";
				String key = url.substring(url.indexOf("bbs?q=")+6,url.indexOf("&ie="));
				String nextpage = null;
				// 每页显示多少数据
				int pageSize = 20;
				// 判断是不是第一页
				if (url.endsWith("utf-8")) {
					nextpage = urlHead + key + urlEnd + "&st=" + pageSize;
				} else {
					String num = url.split("&st=")[1];
					int nextnum = Integer.valueOf(num) + 20;
					nextpage = urlHead + key + urlEnd +"&st=" + nextnum;
				}
				// 处理下一页链接
				Map<String, Object> nextpageTask = new HashMap<String, Object>();
				nextpageTask.put("link", nextpage);
				nextpageTask.put("rawlink", nextpage);
				nextpageTask.put("linktype", "bbspostlist");
				LOG.info("url:" + url + "taskdata is " + nextpageTask.get("link")
						+ nextpageTask.get("rawlink")
						+ nextpageTask.get("linktype"));
				if (!resultData.isEmpty()) {
					resultData.put("nextpage", nextpage);
					List<Map<String,Object>> tasks = (List<Map<String,Object>>) resultData.get("tasks");
					tasks.add(nextpageTask);
				}
			}
			// 后处理插件加上iid
			ParseUtils.getIid(unit, result);
		}
		return new ReProcessResult(processcode, processdata);
	}

}
