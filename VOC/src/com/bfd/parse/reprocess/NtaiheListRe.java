package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.entity.Constants;
import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.reprocess.ReProcessResult;
import com.bfd.parse.reprocess.ReProcessor;
import com.bfd.parse.util.ParseUtils;
import com.bfd.parse.utils.urlUtil;

/**
 * 站点名：太和网
 * 功能：处理翻页
 * @author bfd01
 *
 */
public class NtaiheListRe implements ReProcessor {

	private static final Log log = LogFactory.getLog(NtaiheListRe.class);
	private static final Pattern p = Pattern.compile("PageNo=(\\d+)");
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace parseFace) {
		int processcode = 0;
		
		Map<String, Object> processdata = new HashMap<String, Object>();
		// 得到模板解析结果
		Map<String, Object> resultData = result.getParsedata().getData();
		try {
			if (!resultData.isEmpty() && resultData.size() > 0) {
				// http://www.taihe.net/News/Search.aspx?PageNo=1&keyword=%E5%8D%8E%E4%B8%BA
				String url = unit.getUrl();
				int pageIndex = 1;
				Matcher m = p.matcher(url);
				while (m.find()) {
					pageIndex = Integer.valueOf(m.group(1));
				}
				String nextpage = null;
				nextpage = url.replace("PageNo=" + pageIndex, "PageNo="
						+ (pageIndex + 1));
				if (nextpage != null) {
					resultData.put(Constants.NEXTPAGE, nextpage);
					Map<String, Object> nextpageTask = new HashMap<String, Object>();
					nextpageTask.put("link", nextpage);
					nextpageTask.put("rawlink", nextpage);
					nextpageTask.put("linktype", "newslist");
					List<Map<String, Object>> tasks = (List<Map<String, Object>>) resultData
							.get("tasks");
					tasks.add(nextpageTask);
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
			log.info( this.getClass().getName() + "reprocess exception...");
			processcode = 1;
		}
		ParseUtils.getIid(unit, result);
		// 解析结果返回值 0代表成功
		return new ReProcessResult(processcode, processdata);
	}
}
