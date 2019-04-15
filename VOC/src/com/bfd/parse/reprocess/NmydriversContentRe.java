package com.bfd.parse.reprocess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
 * 驱动之家新闻内容页
 * 后处理插件
 * @author bfd_05
 *
 */
public class NmydriversContentRe implements ReProcessor{
	@SuppressWarnings("unused")
	private static final Log LOG = LogFactory.getLog(NmydriversContentRe.class);
	private static final Pattern PATTIME = Pattern.compile("[0-9]{4}.[0-9]{1,2}.[0-9]{1,2}.*[0-9]{2}:[0-9]{2}:[0-9]{2}(?=\\b)");
	private static final Pattern COMMENT_URLPAT = Pattern.compile("http://comment8.mydrivers.com/review/(\\d+)-\\d+.htm");
	private static final Pattern NEXTPAT = Pattern.compile("<a target=.* href=\"\\d+_(\\d+).htm\">下一页</a>");
//	http://comment8.mydrivers.com/review/295074-2.htm 有些打开的链接是\d+-2
	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace arg2) {
		Map<String, Object> resultData = result.getParsedata().getData();
		Map<String, Object> processdata = new HashMap<String, Object>();
		String pageData = unit.getPageData();
		
		if(resultData.containsKey(Constants.CONTENT)){
			Object obj = resultData.get(Constants.CONTENT);
			if(obj instanceof List){
				List<Object> contents = (List<Object>) obj; 
				StringBuilder sb = new StringBuilder();
				for(Object content : contents){
					sb.append(content);
				}
				resultData.put(Constants.CONTENT, sb.toString());
			}
		}
		if(resultData.containsKey(Constants.POST_TIME)){
			String postTime = (String) resultData.get(Constants.POST_TIME);
			Matcher mch = PATTIME.matcher(postTime);
			if(mch.find()){
				postTime  = mch.group(0);
				resultData.put(Constants.POST_TIME, postTime.trim());
			}
		}
		
		if(resultData.containsKey(Constants.SOURCE)){
			String source = resultData.get(Constants.SOURCE).toString();
			String s = null;
			if (source.startsWith("出处")) {
				s = source.split(" ")[1];
			}
			resultData.put(Constants.SOURCE, (s == null?source:s.trim()));
		}
		
		if(resultData.containsKey(Constants.AUTHOR)){
			String author= (String) resultData.get(Constants.AUTHOR);
			if(author.indexOf("编辑：") != -1){
				int startIndex = author.indexOf("编辑：") + 3;
				author = author.substring(startIndex, author.indexOf(" ", startIndex));
				resultData.put(Constants.AUTHOR,  author.trim());
			}
		}
		String commUrl = "http://comment8.mydrivers.com/ReviewAjax.aspx?Tid=%s&Cid=1&page=1";
		List<Map<String, Object>> tasks = new ArrayList<Map<String, Object>>();
		Matcher iidMch = COMMENT_URLPAT.matcher(pageData);
		if(iidMch.find()){
			String iid = iidMch.group(1);
			commUrl = String.format(commUrl, iid);
			Map<String, Object> taskMap = new HashMap<String, Object>();
			taskMap.put("link", commUrl);
			taskMap.put("rawlink", commUrl);
			taskMap.put("linktype", "newscomment");
			tasks.add(taskMap);
			resultData.put(Constants.COMMENT_URL, commUrl);
		}
		Matcher nextMch  = NEXTPAT.matcher(pageData);
		if(nextMch.find()){
			String url = unit.getTaskdata().get("url").toString();
			String[] urls = url.split("_");
			int pageIndex = 0;
			if(urls.length > 1){
				pageIndex = Integer.valueOf(urls[1].substring(0, urls[1].length() - 5));
			}
			String nextpage = urls[0] + "_" + (pageIndex + 1) + ".html";
			resultData.put(Constants.NEXTPAGE, nextpage);
			Map<String, Object> taskMap = new HashMap<String, Object>();
			taskMap.put("link", nextpage);
			taskMap.put("rawlink", nextpage);
			taskMap.put("linktype", "newscontent");
			tasks.add(taskMap);
		}
		resultData.put("tasks", tasks);
		ParseUtils.getIid(unit, result);
//		LOG.info("url:" + unit.getUrl() + ".after reprocess rs is "
//				+ JsonUtil.toJSONString(resultData));
		return new ReProcessResult(SUCCESS, processdata);
	}

}
