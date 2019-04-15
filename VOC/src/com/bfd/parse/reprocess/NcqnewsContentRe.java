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

public class NcqnewsContentRe implements ReProcessor{
	
	private static final Pattern PATTIME = Pattern.compile("[0-9]{4}.[0-9]{1,2}.[0-9]{1,2}日*\\s*([0-9]{2}:[0-9]{2})*(:[0-9]{2})*");
	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		List<Map<String, Object>> tasks = (List<Map<String, Object>>) resultData.get("tasks");
		
		String url = unit.getUrl();	
		String pageData = unit.getPageData();
		//\\w+
		Pattern p = null;
		Matcher mch = null;
		if(resultData.containsKey(Constants.CONTENT) 
				&& resultData.containsKey(Constants.TITLE)
				&& resultData.containsKey(Constants.SOURCE)
				&& resultData.containsKey(Constants.POST_TIME)){
			String content = resultData.get(Constants.CONTENT).toString();
			String title = resultData.get(Constants.TITLE).toString();
			String source = resultData.get(Constants.SOURCE).toString();
			String posttime = resultData.get(Constants.POST_TIME).toString();
			if(content.startsWith(title)){
				content = content.substring(title.length()).trim();
			}
			if(content.startsWith(source)){
				content = content.substring(source.length()).trim();
			}
			if(content.startsWith(posttime)){
				content = content.substring(posttime.length()).trim();
			}
			int endIndex = content.indexOf("责任编辑：") != -1 ? content.indexOf("责任编辑：") :  content.indexOf("编辑：");
			if(endIndex != -1){
				content = content.substring(0, endIndex);
			}
			resultData.put(Constants.CONTENT, content);
		}
		if(resultData.containsKey(Constants.SOURCE)){
			String source = resultData.get(Constants.SOURCE).toString();
			int index = source.indexOf("来源：");
			if(index > -1){
				source = source.substring(index + 3);
				resultData.put(Constants.SOURCE, source);
			}
		}
		else{
			p = Pattern.compile("var get_laiyuan = '(.*)';");
			mch = p.matcher(pageData);
			Pattern pts = Pattern.compile("var ts\\s*=\\s*'(.*)'");
			Matcher mts = pts.matcher(pageData);
			String source = "";
			if(mch.find()){
				source = mch.group(1);
			}
			else if (mts.find()){
				source = mts.group(1);
			}
			int start = source.indexOf(">");
			if(start > 0 && source.indexOf("<", start) > 0){
				source = source.substring(source.indexOf(">") + 1, source.indexOf("<", start));
				resultData.put(Constants.SOURCE, source);
			}
		}
		if(resultData.containsKey(Constants.POST_TIME)){
			String postTime = resultData.get(Constants.POST_TIME).toString();
			Matcher mchPostTime = PATTIME.matcher(postTime);
			if(mchPostTime.find()){
				resultData.put(Constants.POST_TIME, mchPostTime.group());
			}
		}
		if(resultData.containsKey("editor")){
			String editor = resultData.get("editor").toString();
			editor = editor.replace("责任编辑：", "").replace("编辑：","").replace("[", "").replace("]", "");
			resultData.put("editor", editor.trim());
		}
		//内容页下一页
		if (resultData.containsKey("nextpage1")) {
			String nextpage = resultData.get("nextpage1").toString();
			resultData.remove("nextpage1");
			p = Pattern.compile("\\d+");
			mch = p.matcher(nextpage);
			StringBuilder sb = new StringBuilder();
			while(mch.find()){
				sb.append(mch.group()).append(" ");
			}
			String[] pages = sb.toString().split(" ");
			p = Pattern.compile("content_(\\d+)_(\\d+).htm");
			mch = p.matcher(url);
			String urlBase = url.substring(0, url.indexOf(".htm"));
			int pageIndex = 1;
			if(mch.find()){
				pageIndex = Integer.valueOf(mch.group(2));
				urlBase = url.substring(0, url.lastIndexOf("_"));
			}
			if(pageIndex < pages.length){
				//下一页
				nextpage = new StringBuilder(urlBase).append("_")
				.append(pageIndex + 1).append(".htm").toString();
				resultData.put(Constants.NEXTPAGE, nextpage);
				Map<String, Object> nextMap = new HashMap<>();
				nextMap.put("link", nextpage);
				nextMap.put("rawlink", nextpage);
				nextMap.put("linktype", "newscontent");
				tasks.add(nextMap);
			}
		}
		String clientID = "";
		//http://www.cqnews.net/js/changyan.js clientID是从js里面返回的，用json插件获取clientID
		if(resultData.containsKey("clientID")){
			clientID = resultData.get("clientID").toString();
			resultData.remove("clientID");
		}
		if(pageData.contains("条评论")){
			p = Pattern.compile("content_(\\d+).htm");
			mch = p.matcher(url);
			String topicsID = "";
			if (mch.find()) {
				topicsID = mch.group(1);
			}
			if(!topicsID.equals("") && !clientID.equals("") ){
				Map<String, Object> commentTask = new HashMap<>();
				String commUrl = "http://changyan.sohu.com/node/html?client_id=%s&topicsid=%s";
				commUrl = String.format(commUrl, clientID, topicsID);
				commentTask.put(Constants.LINK, commUrl);
				commentTask.put(Constants.RAWLINK, commUrl);
				commentTask.put(Constants.LINKTYPE, "newscomment");
				if(tasks == null){
					tasks = new ArrayList<>();
					resultData.put(Constants.TASKS, tasks);
				}
				tasks.add(commentTask);
				resultData.put(Constants.COMMENT_URL, commUrl);
			}
		}
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
}
