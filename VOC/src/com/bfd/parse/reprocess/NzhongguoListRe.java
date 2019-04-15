package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
/**
 * 站点名：中国网
 * <p>
 * 主要功能：处理下一页，items，模板中用发表时间标出了搜索结果数
 * @author bfd_01
 *
 */
public class NzhongguoListRe implements ReProcessor {

	// private static final Log LOG = LogFactory.getLog(NzhongguoListRe.class);

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		// 取到解析结果
		Map<String, Object> resultData = result.getParsedata().getData();
		if (!unit.getPageData().contains("下一页") && !resultData.isEmpty()) {
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
		return new ReProcessResult(processcode, processdata);
	}
}
