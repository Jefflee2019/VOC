package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.Map;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;
/**
 * 	@site：光电新闻
 * 	@function：新闻内容页后处理
 * 	@author bfd_04
 *
 */
public class NofweekContentRe implements ReProcessor {
//	private static final Log LOG = LogFactory.getLog(NofweekContentRe.class);
	@SuppressWarnings("rawtypes")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		Map<String, Object> resultData = new HashMap<String,Object>();
		Map<String, Object> processdata = new HashMap<String, Object>();
		resultData = result.getParsedata().getData();
		if (resultData != null) {
			
			/**
			 *  "source": "来源： 新浪", 
			 */
			 if(resultData.containsKey(Constants.SOURCE)) {
				 String source = resultData.get(Constants.SOURCE).toString();
				 source = source.replace("来源：", "").trim();
				 resultData.put(Constants.SOURCE, source);
			 }
			/**
			 *  "brief": "导读： 在"
			 */
			 if(resultData.containsKey(Constants.BRIEF)) {
				 String brief = resultData.get(Constants.BRIEF).toString();
				 brief = brief.replace("导读：", "").trim();
				 resultData.put(Constants.BRIEF, brief);
			 } 
			 /**
			  * "author": "作者：刘文忞"
			  */
			 if(resultData.containsKey(Constants.AUTHOR)) {
				 String author = resultData.get(Constants.AUTHOR).toString();
				 author = author.replace("作者：", "").trim();
				 resultData.put(Constants.AUTHOR, author);
			 } 
			 /**
			  * 处理comment_url
			  */
			 if(resultData.containsKey(Constants.COMMENT_URL)) {
				 Map commUrlMap = (Map)resultData.get(Constants.COMMENT_URL);
				 String commUrl = "";
				 if(commUrlMap != null && commUrlMap.containsKey("link")) {
					 commUrl = commUrlMap.get("link").toString();
				 } else {
					 commUrl = "";
				 }
				 resultData.put(Constants.COMMENT_URL, commUrl);
			 } 
		}
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
}
