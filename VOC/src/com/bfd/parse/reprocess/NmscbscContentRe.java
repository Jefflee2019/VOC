package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.Map;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
/**
 * 站点名：移动通讯网
 * <p>
 * 主要功能：处理字段中的多余信息
 * @author bfd_01
 *
 */
public class NmscbscContentRe implements ReProcessor {
	
	
	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {

		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		// 取到解析结果
		Map<String, Object> resultData = result.getParsedata().getData();
		// 发布: 2017-04-21 05:37 | 作者: | 来源: OFweek 光通讯网讯 | 浏览:203次 | 字体: 小 中 大
		String temp = null;
		if (resultData.containsKey(Constants.AUTHOR)) {
			temp = resultData.get(Constants.AUTHOR).toString();
		}
		
		if (temp != null && temp.split("\\|").length >= 4) {
			String posttime = temp.split("\\|")[0];
			String author = temp.split("\\|")[1];
			String source = temp.split("\\|")[2];
			String viewcnt = temp.split("\\|")[3];
			viewcnt = viewcnt.replace("浏览:", "").replace("次", "").trim();
			resultData.put(Constants.AUTHOR, author.replace("作者:", "").trim());
			resultData.put(Constants.SOURCE, source.replace("来源:", "").trim());
			resultData.put(Constants.POST_TIME, posttime.replace("发布:", "").trim());
			resultData.put(Constants.VIEW_CNT, Integer.valueOf(viewcnt));
			
		}
		
		return new ReProcessResult(processcode, processdata);
	}

}
