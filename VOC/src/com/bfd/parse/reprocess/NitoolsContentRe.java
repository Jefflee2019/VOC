package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.Iterator;
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
 * 站点：Nitools
 * 功能：内容页后处理插件
 * @author dph 2017年11月15日
 *
 */
public class NitoolsContentRe implements ReProcessor{
	private static final Log LOG = LogFactory.getLog(NitoolsContentRe.class);
	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace parseFace) {
		Map<String, Object> processdata = new HashMap<String, Object>(16);
		Map<String,Object> resultData = result.getParsedata().getData();
		String title = null;
		String posttime = null;
		String content = null;
		//"author": "责任编辑：兔小编",
		if(resultData.containsKey(Constants.AUTHOR)){
			String author = (String) resultData.get(Constants.AUTHOR);
			author = author.replace("责任编辑：", "").trim();
			resultData.put(Constants.AUTHOR, author);
		}
		
		// "你也在等iPhone 5S？iPhone需求降至冰点 2013-05-11更新 标签： 以下为文章全文：	根据 ...
		//内容页包含标题、时间和标签
		if(resultData.containsKey(Constants.CONTENT)){
//			String content = (String) resultData.get(Constants.CONTENT);
			List<Map<String,Object>> contentMList = (List<Map<String, Object>>) resultData.get(Constants.CONTENT);
			Iterator<?> iList = contentMList.iterator();
			while(iList.hasNext()){
				try {
					content = (String) iList.next();
					posttime = (String) resultData.get(Constants.POST_TIME);
					title = (String) resultData.get(Constants.TITLE);
					content = content.replaceAll(title+" "+posttime+" 标签：\\S*\\s+", "").trim();
					resultData.put(Constants.CONTENT, content);
				} catch (Exception e) {
					LOG.debug(e.toString());
				}
			}
		}
		//"post_time": "2013-05-11更新"
		if(resultData.containsKey(Constants.POST_TIME)){
			posttime = posttime.replace("更新", "").trim();
			resultData.put(Constants.POST_TIME, posttime);
		}
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}

}
