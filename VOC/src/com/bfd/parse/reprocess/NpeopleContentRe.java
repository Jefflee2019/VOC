package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

/**
 * @site:人民网-新闻 (Npeople)
 * @function 新闻内容页后处理插件
 * 
 * @author bfd_02
 *
 */

public class NpeopleContentRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(NpeopleContentRe.class);

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = new HashMap<String, Object>();

		// 解析数据
		resultData = result.getParsedata().getData();
		if (resultData == null) {
			LOG.warn("未找到解析数据");
			return null;
		}
		
		/**
		 * @param author文章作者
		 * @function 格式化
		 *  eg1:"author": "（责任编辑：财经实习、刘阳)" 
		 *  eg2:"author": "刘育英"
		 *  eg3:"author": "（责任编辑：苏楠）"
		 *  eg4:"author":"2012年03月27日10:17 傅雲威 手機看新聞"
		 */
		if(resultData.containsKey(Constants.AUTHOR)) {
			String author = resultData.get(Constants.AUTHOR).toString();
			Pattern ptn = Pattern.compile("");
			Matcher match = ptn.matcher(author);
			if(author.contains("责任编辑")) {
				ptn = Pattern.compile("责任编辑：(\\S+)[\\)\\）]");
			    match = ptn.matcher(author);
			}else if(author.contains("责编")) {
				ptn = Pattern.compile("责编：(\\S+)[\\)\\）]");
			    match = ptn.matcher(author);
			}else if(!author.contains("来源")&&author.contains("年")&author.contains("月")&author.contains("日")) {
				ptn = Pattern.compile("\\d+\\s+(\\S+)\\s+");
			    match = ptn.matcher(author);
			}else {
				ptn = Pattern.compile("(\\S*)");
			    match = ptn.matcher(author);
			}
			if(match.find()) {
				author = match.group(1).trim();
				resultData.put(Constants.AUTHOR, author);
			}
		}
		
		/**
		 * @param source 来源
		 * @function 格式化来源字段
		 *   eg1: 来源：和讯网
		 *   eg2: 《新京报》
		 *   eg3: 和讯网
		 */
		if(resultData.containsKey(Constants.SOURCE)) {
			String source = resultData.get(Constants.SOURCE).toString();
			Pattern ptn = Pattern.compile("");
			Matcher match = ptn.matcher(source);
			if(source.contains("来源")) {
				ptn = Pattern.compile("来源：(\\s*)\\s*");
				match = ptn.matcher(source);
			}else if(source.contains("《")&source.contains("》")) {
				ptn = Pattern.compile("《(\\S+)》");
				match = ptn.matcher(source);
			}else {
				ptn = Pattern.compile("(\\S*)");
				match = ptn.matcher(source);
			}
			if(match.find()) {
				source = match.group(1).trim();
				resultData.put(Constants.SOURCE, source);
			}
		}
		
		/**
		 * @param post_time 发表时间
		 * @function 格式化发表时间
		 *  eg:2007年07月18日09:43 來源：人民網－《人民日報》
		 */
		if(resultData.containsKey(Constants.POST_TIME)) {
			String postTime = resultData.get(Constants.POST_TIME).toString();
			Matcher match = Pattern.compile("(\\d+\\S\\d+\\S\\d+\\S\\d+:\\d+)\\s*").matcher(postTime);
			if(match.find()) {
				postTime = match.group(1).trim();
				resultData.put(Constants.POST_TIME, postTime);
			}
		}
		
		
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
}