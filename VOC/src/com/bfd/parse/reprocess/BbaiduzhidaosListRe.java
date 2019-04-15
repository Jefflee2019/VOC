package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;


public class BbaiduzhidaosListRe implements ReProcessor{
	private static final Log LOG = LogFactory.getLog(BbaiduzhidaosListRe.class);

	@SuppressWarnings("unchecked")
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace parseFace) {
		int processcode = 0;
		Map<String,Object> processdata = new HashMap<String,Object>();
		try {
			Map<String,Object> resultData = result.getParsedata().getData();
			List<Map<String,Object>> items = (List<Map<String,Object>>) resultData.get("items");
			List<Map<String,Object>> tasks = (List<Map<String,Object>>) resultData.get("tasks");
			for (Iterator<Map<String, Object>> iterator = items.iterator(); iterator.hasNext();) {
				Map<String,Object> item = (Map<String, Object>) iterator.next();
				if (item.containsKey("reply_cnt")) {
					item.put("reply_cnt", Integer.valueOf(-1024));
				}
				String link = null;
				String posttime = null;
				if(item.containsKey("posttime")){
					posttime = (String) item.get("posttime");
					posttime = posttime.split(" ")[0];
				}
				Map<String,String> itemlink = null;
				if(item.containsKey("itemlink")){
					itemlink = (Map<String, String>) item.get("itemlink");
					link = itemlink.get("link");
				}
				/*for(Iterator<Map<String, Object>> iteratorT = tasks.iterator(); iteratorT.hasNext();){
					Map<String,Object> task = (Map<String, Object>) iteratorT.next();
					if(task.get("linktype").equals("bbspost") && task.get("link").equals(link)){
						link = link + "&newstime=" + posttime;
						task.put("link", link);
						task.put("rawlink", link);
					}
				}*/
				for (Map<String, Object> map : tasks) {
					if(map.get("linktype").equals("bbspost") && map.get("link").equals(link)){
						link = link + "&newstime=" + posttime;
						map.put("link", link);
						map.put("rawlink", link);
						itemlink.put("link", link);
						itemlink.put("rawlink", link);
					}
				}
			}
		} catch (Exception e) {
			LOG.error("the result is null");
		}
		return new ReProcessResult(processcode, processdata);
	}

}
