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

/**
 * 财经网新闻
 * 列表页
 * 后处理插件
 * @author bfd_05
 *
 */
public class NcaijingListRe implements ReProcessor{

	@SuppressWarnings("unused")
	private static final Log LOG = LogFactory.getLog(NcaijingListRe.class);
	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		String url = unit.getUrl();
		//网站75页之后跳回到第一页了
		if(url.contains("pn=750")&&resultData.containsKey(Constants.NEXTPAGE)){
			resultData.remove(Constants.NEXTPAGE);
			List<Map<String, Object>> tasks = (List<Map<String, Object>>) resultData.get(Constants.TASKS);
			for(int i = 0; i < tasks.size();){
				Map<String, Object> task = tasks.get(i);
				if(task.get("linktype").equals("newslist")){
					tasks.remove(task);
					continue;
				}
				i++;
			}
		}
		return new ReProcessResult(SUCCESS, processdata);
	}
}
