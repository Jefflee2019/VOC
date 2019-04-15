package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

/**
 * 站点名：Nqqdigi
 * <p>
 * 主要功能：处理生成任务的链接
 * 
 * @author bfd_03
 *
 */
public class NqqvideoListRe implements ReProcessor {
//	private static final Log LOG = LogFactory.getLog(NqqvideoListRe.class);

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace parseFace) {

		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		// 取到解析结果
		Map<String, Object> resultData = result.getParsedata().getData();
		if (resultData != null && !resultData.isEmpty()) {
			String url = null;
			url = unit.getUrl();
			String nextpage = null;
			int pageno = 0;
			if(!url.contains("&cur=")) {
				nextpage = url + "&cur=2";
			} else {
				Pattern p = Pattern.compile("&cur=(\\d+)");
				Matcher m = p.matcher(url);
				while (m.find()) {
					pageno = Integer.valueOf(m.group(1));
				}
				nextpage = url.replace("&cur=" + pageno , "&cur=" + (pageno+1));
			}
			// 做url的处理，获得重定向之后的url
			// https://v.qq.com/x/search/?q=%E5%8D%8E%E4%B8%BA&stag=102&smartbox_ab=&cur=2
			if (nextpage != null && pageno < 100) {
				Map<String, Object> nextpageTask = new HashMap<String, Object>();
				nextpageTask.put("link", nextpage);
				nextpageTask.put("rawlink", nextpage);
				nextpageTask.put("linktype", "newslist");
				resultData.put("nextpage", nextpage);
				List<Map> tasks = (List<Map>) resultData.get("tasks");
				tasks.add(nextpageTask);
			}
		}
		 ParseUtils.getIid(unit, result);
		return new ReProcessResult(processcode, processdata);
	}
}
