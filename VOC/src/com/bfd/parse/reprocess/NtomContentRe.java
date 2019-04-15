package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

/**
 * @site:TOM (Ntom)
 * @function 新闻内容页后处理插件-处理内容和发表时间
 * 
 * @author bfd_02
 *
 */

public class NtomContentRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(NtomContentRe.class);

	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace parseFace) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = new HashMap<String, Object>();
		// 解析数据
		resultData = result.getParsedata().getData();
		if (resultData == null) {
			LOG.warn("未找到解析数据");
			return null;
		}
		
		//处理发表时间
		//post_time 时间：2017-05-16 21:21
		if(resultData.containsKey(Constants.POST_TIME)) {
			String postTime = resultData.get(Constants.POST_TIME).toString();
			if(postTime.contains("时间：")) {
				postTime = postTime.replace("时间：", "");
			}	
			resultData.put(Constants.POST_TIME, postTime);
		}
		
		//处理内容
		//模板内容中附带有其他无关内容
		if(resultData.containsKey(Constants.CONTENT)) {
			String tem = resultData.get(Constants.CONTENT).toString();
			String content = resultData.containsKey("keyword")?tem.replace((String)resultData.get("keyword"), ""):tem;
			content = content.replace((String)resultData.get("title"), "");
			content = content.trim();
			resultData.put(Constants.CONTENT, content);
			resultData.remove("keyword");
		}
		

		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}

}
