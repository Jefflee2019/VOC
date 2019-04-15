package com.bfd.parse.reprocess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

/**
 * 
 * @author bfd_05
 */
public class NworkercnContentRe implements ReProcessor{
	
	private static Pattern p = Pattern.compile("_(//d+).html");
	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		String pageData = unit.getPageData();
		Pattern totalPat = Pattern.compile("共(\\d+)页");
		Matcher totalMch = totalPat.matcher(pageData);
		if(totalMch.find()){
			int totalPage = Integer.valueOf(totalMch.group(1));
			String url = unit.getUrl();
			Matcher mch = p.matcher(url);
			int page = 1;
			String urlBase = url.substring(0, url.indexOf(".shtml"));
			if(mch.find()){
				page = Integer.valueOf(mch.group(1));
				urlBase = url.substring(0, url.indexOf("-"));
			}
			if(page < totalPage){
				String nextpage = new StringBuilder(urlBase).append("-")
						.append(page + 1).append(".shtml").toString();
				Map<String, Object> nextTask = new HashMap<String, Object>();
				nextTask.put("link", nextpage);
				nextTask.put("rawlink", nextpage);
				nextTask.put("linktype", "newscontent");//任务为列表页
				resultData.put(Constants.NEXTPAGE, nextpage);
				List<Map<String, Object>> tasks = (List<Map<String, Object>>) resultData.get("tasks");
				if(tasks == null){
					tasks = new ArrayList<Map<String, Object>>();
					resultData.put("tasks", tasks);
				}
				tasks.add(nextTask);
			}
		}
		if(resultData.containsKey("editor")){
			String editor = resultData.get("editor").toString();
			editor = editor.substring(editor.indexOf("编辑") + 3);
			editor = editor.replace("[", "")
					.replace("]", "")
					.replace(":", "")
					.replace("：", "");
			resultData.put("editor", editor.trim());
		}
		if(resultData.containsKey(Constants.SOURCE)){
			String source =  resultData.get(Constants.SOURCE).toString();
			source = source.substring(source.indexOf("来源：") + 3);
			resultData.put(Constants.SOURCE, source.trim());
		}
		if(resultData.containsKey(Constants.CONTENT)){
			Object obj = resultData.get(Constants.CONTENT);
			if(obj instanceof List){
				List<String> contents = (List<String>) obj;
				StringBuilder sb = new StringBuilder();
				for(String content : contents){
					sb.append(content);
				}
				resultData.put(Constants.CONTENT, sb.toString());
			}
		}
		if(resultData.containsKey(Constants.POST_TIME)){
			String posttime = resultData.get(Constants.POST_TIME).toString();
			if(posttime.contains("来源")){
				Pattern p = Pattern.compile("[0-9]{4}-[0-9]{1,2}-[0-9]{1,2}\\s*([0-9]{2}:[0-9]{2})*");
				Matcher mch = p.matcher(posttime);
				if(mch.find()){
					posttime = mch.group();
					resultData.put(Constants.POST_TIME, posttime);
				}
			}
		}
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}

}
