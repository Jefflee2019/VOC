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
 * @site：硅谷动力-论坛(Benet)
 * @function 处理内容页会随所在列表页页数变化情况
 * @author bfd_02
 *
 */
public class BenetListRe implements ReProcessor {

	private static final Log LOG = LogFactory.getLog(BenetListRe.class);

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace parseFace) {
		int processcode = 0;

		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
//http://bbs.enet.com.cn/forum.php?mod=viewthread&tid=2675386&extra=page%3D1%26filter%3Dlastpost%26orderby%3Dlastpost%26orderby%3Dlastpost
		// 处理tasks里面url
		List<Map<String, Object>> taskList = (List<Map<String, Object>>) resultData.get(Constants.TASKS);
		if (taskList != null && !taskList.isEmpty()) {
			for (int i = 0; i < taskList.size(); i++) {
				Map taskMap = taskList.get(i);
				if(taskMap.containsKey(Constants.LINK)) {
					String link = taskMap.get(Constants.LINK).toString();
					if(link.contains("&extra")) {
//						int index = link.indexOf("&extra");
//						link = link.substring(0,index);
						link = link.split("&extra")[0];
						LOG.info("the current linkUrl is:" + link);
						taskMap.put(Constants.LINK, link);
						taskMap.put(Constants.RAWLINK, link);
					}
				}
			}
		}
		
		List<Map<String, Object>> itemList = (List<Map<String, Object>>) resultData.get(Constants.ITEMS);
		if (itemList != null && !itemList.isEmpty()) {
			for (int i = 0; i < itemList.size(); i++) {
				Map itemMap = itemList.get(i);
				if(itemMap.containsKey(Constants.ITEMLINK)) {
					Map linkMap = (Map)itemMap.get(Constants.ITEMLINK);
					if(linkMap.containsKey(Constants.LINK)) {
						String link = linkMap.get(Constants.LINK).toString();
						if(link.contains("&extra")) {
							link = link.split("&extra")[0];
							linkMap.put(Constants.LINK, link);
							linkMap.put(Constants.RAWLINK, link);
						}
					}
				}
			}
		}
		
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(processcode, processdata);
	}

}
