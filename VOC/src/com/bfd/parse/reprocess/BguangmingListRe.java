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
 * 站点名：光明网
 * <p>
 * 主要功能：处理回复数
 * @author bfd_01
 *
 */
public class BguangmingListRe implements ReProcessor {
//	private static final Log LOG = LogFactory.getLog(BguangmingListRe.class);
	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {

		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		// 取到解析结果
		Map<String, Object> resultData = result.getParsedata().getData();
		if (resultData != null && !resultData.isEmpty()) {
			if (resultData.containsKey(Constants.ITEMS)) {
				List<Map<String,Object>> items = (List<Map<String,Object>>) resultData.get(Constants.ITEMS);
				for (int i = 0; i < items.size(); i++) {
					int replyCnt = 0;
					Map<String,Object> item = (Map<String,Object>) items.get(i);
					Pattern iidPatter = Pattern.compile("(\\d+).*");
					Matcher match = iidPatter.matcher(item.get(
							Constants.REPLY_CNT).toString());
					while (match.find()) {
						replyCnt = Integer.valueOf(match.group(1));
					}
					item.put(Constants.REPLY_CNT, replyCnt);
				}
			}
			
			if (resultData.containsKey(Constants.TASKS)) {
				List<Map<String,Object>> list = (List<Map<String,Object>>)resultData.get(Constants.TASKS);
				for (int i=0;i< list.size();i++) {
					Map<String,Object> map = list.get(i);
					if ("bbspost".equals(map.get("linktype"))) {
						String id = urlFormat(map.get("link").toString());
						map.put("link", "http://bbs.gmw.cn/thread-" + id + "-1-1.html");
					}
				}
			}
			
		}
		return new ReProcessResult(processcode, processdata);
	}
	
	private String urlFormat(String url) {
		// http://bbs.gmw.cn/forum.php?mod=viewthread&tid=3437476&highlight=%E5%8D%8E%E4%B8%BA
		Pattern p = Pattern.compile("tid=(\\d+)");
		Matcher m = p.matcher(url);
		while (m.find()) {
			return m.group(1);
		}
		return null;
	}
}
