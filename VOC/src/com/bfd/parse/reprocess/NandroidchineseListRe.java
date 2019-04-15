package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;
/**
 * 站点名：安卓中文网
 * <p>
 * 主要功能：处理下一页
 * @author bfd_01
 *
 */
public class NandroidchineseListRe implements ReProcessor {
//	private static final Log LOG = LogFactory.getLog(NandroidchineseListRe.class);
	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {

		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		// 取到解析结果
		Map<String, Object> resultData = result.getParsedata().getData();
		if (!unit.getPageData().contains("下一页") && !resultData.isEmpty()) {
			List<Map<String,Object>> list = (List<Map<String,Object>>)resultData.get("tasks");
			for (int i=0;i<list.size();i++) {
				Map<String,Object> map = (Map<String,Object>)list.get(i);
				if ("newslist".equals(map.get("linktype"))) {					
					list.remove(i);
				}
			}
			resultData.remove("nextpage");
		}
		return new ReProcessResult(processcode, processdata);
	
	}

}
