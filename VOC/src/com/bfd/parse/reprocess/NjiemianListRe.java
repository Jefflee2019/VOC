package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
/**
 * 站点名：界面网
 * <p>
 * 主要功能：处理下一页
 * @author bfd_01
 *
 */
public class NjiemianListRe implements ReProcessor {
//	private static final Log LOG = LogFactory.getLog(NjiemianListRe.class);
	
	private static final int PAGESIZE = 8;
	
	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {

		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		// 取到解析结果
		Map<String, Object> resultData = result.getParsedata().getData();
		String url = unit.getUrl();
		// https://a.jiemian.com/index.php?m=search&a=index&msg=%E5%8D%8E%E4%B8%BA&type=news
		// https://a.jiemian.com/index.php?m=search&a=index&msg=%E5%8D%8E%E4%B8%BA&type=news&page=2
		int count = 0;
		if (resultData.containsKey(Constants.NEWS_CNT)) {
			String newscnt = resultData.get(Constants.NEWS_CNT).toString();
			Pattern p = Pattern.compile("(\\d+)");
			Matcher m = p.matcher(newscnt);
			while (m.find()) {
				newscnt = m.group(1);
				count = Integer.valueOf(newscnt);
			}
		}
		
		String nextpage = null;
		int pageNo = 1;
		if (!url.contains("&page=") && count > PAGESIZE) {
			nextpage = url + "&page=2";
		} else if (url.contains("&page=")) {
			Pattern p = Pattern.compile("page=(\\d+)");
			Matcher m = p.matcher(url);
			while (m.find()) {
				pageNo = Integer.valueOf(m.group(1));
			}
			
			if (count > pageNo * PAGESIZE) {
				nextpage = url.split("page")[0] + "page=" + (pageNo+1);
			}
		}
		
		List<Map<String, Object>> tasks = (List<Map<String, Object>>) resultData.get(Constants.TASKS);
		if(nextpage != null) {
			Map<String, Object> nextpageTask = new HashMap<String, Object>();
			nextpageTask.put("link", nextpage);
			nextpageTask.put("rawlink", nextpage);
			nextpageTask.put("linktype", "newslist");
			resultData.put("nextpage", nextpage);
			tasks.add(nextpageTask);	// 添加下一页任务
		}
		resultData.remove(Constants.NEWS_CNT);
		return new ReProcessResult(processcode, processdata);
	
	}

}
