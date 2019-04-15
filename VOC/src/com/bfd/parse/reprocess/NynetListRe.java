package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
/**
 * 站点名：北京青年报
 * 主要功能：处理下一页
 * @author bfd_01
 *
 */
public class NynetListRe implements ReProcessor {

//	private static final Log LOG = LogFactory.getLog(NynetListRe.class);

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		// 取到解析结果
		Map<String, Object> resultData = result.getParsedata().getData();
		if (!resultData.isEmpty()) {
			String url = unit.getUrl();
			if (url.contains("&p=74&")) {
				List<Map<String, Object>> map = (List<Map<String, Object>>) resultData
						.get("tasks");
				for (int i = 0; i < map.size(); i++) {
					if ((map.get(i)).get("linktype").equals("newslist")) {
						map.remove(i);
						resultData.remove(Constants.NEXTPAGE);
						break;
					}
				}
			}
		}
		return new ReProcessResult(processcode, processdata);
	}
}
