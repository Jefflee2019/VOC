package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.ConstantFunc;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;
/**
 * 站点名：我爱卡
 * <p>
 * 主要功能：处理生成任务的链接
 * @author bfd_01
 *
 */
public class N51creditListRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(N51creditListRe.class);
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		// 取到解析结果
		Map<String, Object> resultData = result.getParsedata().getData();
		if (resultData != null && !resultData.isEmpty()) {
			//做url的截取，避免url中每次都变得部分导致列表页一直刷
			if(resultData.containsKey(Constants.TASKS)){
				String pagedata = null;
				pagedata = unit.getPageData();
				String url = null;
				url = unit.getUrl();
				List<Map<String, Object>> tasks = (List<Map<String, Object>>) resultData.get(Constants.TASKS);
				ConstantFunc.decodeLink(tasks, pagedata);
				// 处理下一页链接
				if (pagedata.contains(">下一页<") && url.contains("www.so.com")) {
					ConstantFunc.getNextpage(resultData, url, tasks);
				}
			}
			
		}
		 ParseUtils.getIid(unit, result);
		return new ReProcessResult(processcode, processdata);
	}
}
