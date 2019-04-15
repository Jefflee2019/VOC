package com.bfd.parse.reprocess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
/**
 * 站点名：21cn
 * <p>
 * 主要功能：处理重定向
 * @author bfd_01
 *
 */
public class N21cnListRe implements ReProcessor {
	// private static final Log LOG = LogFactory.getLog(N21cnListRe.class);

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {

		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		// 取到解析结果
		Map<String, Object> resultData = result.getParsedata().getData();
		if (resultData != null && !resultData.isEmpty()) {
			List<Map<String, Object>> list = (List<Map<String, Object>>) resultData
					.get("tasks");
			List<Map<String, Object>> temp = new ArrayList<Map<String, Object>>();
			for (int i = 0; i < list.size(); i++) {
				Map<String, Object> map = (Map<String, Object>) list.get(i);
				String link = null;
				if ("newscontent".equals(map.get("linktype"))) {
					if (map.get("link").toString().contains("/cb/")) {
						link = map.get("link").toString()
								.replace("/cb/", "/a/");
						map.put("link", link);
					}
					if (map.get("link").toString().contains("/zhnb/")) {
						link = map.get("link").toString()
								.replace("/zhnb/", "/a/");
						map.put("link", link);
					}
					if (!map.get("link").toString()
							.contains("finance.21cn.com")
							&& !map.get("link").toString()
									.contains("it.21cn.com")
							&& !map.get("link").toString()
									.contains("news.21cn.com")) {
						list.remove(i);
					}
				}
				temp.add(map);
			}
			resultData.put("tasks", temp);

			if (!unit.getPageData().contains("下一页")) {
				resultData.remove("nextpage");
				List tasks = (List) resultData.get(Constants.TASKS);
				for (int i = 0; i < tasks.size(); i++) {
					Map map = (Map) tasks.get(i);
					if ("newslist".equals(map.get("linktype"))) {
						tasks.remove(i);
						break;
					}
				}
			}
		}
		return new ReProcessResult(processcode, processdata);
	}
}
