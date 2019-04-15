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
 * 站点名：百事网
 * <p>
 * 主要功能：处理生成新闻的链接，去掉不是新闻正文的其他链接
 * @author bfd_01
 *
 */
public class Npc841ListRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(Npc841ListRe.class);
	
	
	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {

		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		// 取到解析结果
		Map<String, Object> resultData = result.getParsedata().getData();
		if (resultData.containsKey(Constants.TASKS)) {
			List tasks = (List)resultData.get(Constants.TASKS);
			
			for (int i = 0; i < tasks.size(); i++) {
				Map map = (Map) tasks.get(i);
				if ("newscontent".equals(map.get("linktype"))
						&& (!map.get("link").toString().endsWith("html"))) {
					tasks.remove(i);
					i--;
				} else if ("newscontent".equals(map.get("linktype"))
						&& map.get("link").toString().endsWith(".html")
						&& (!map.get("link").toString().contains("all.html"))) {
					String link = map.get("link").toString();
					map.put("link", link.replaceAll("_(\\d+).html", "_all.html"));
				}
			}
			resultData.put(Constants.TASKS, tasks);
		}
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(processcode, processdata);
	
	}

}
