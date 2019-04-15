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
 * 中华网新闻
 * 新闻内容页
 * 后处理插件
 * @author bfd_05
 *
 */
public class NzhonghuaContentRe implements ReProcessor{

	@SuppressWarnings("unused")
	private static final Log LOG = LogFactory.getLog(NzhonghuaContentRe.class);
	private static final Pattern IIDPAT = Pattern.compile("(\\d+).html");
	private static final Pattern PATTIME = Pattern.compile("[0-9]{4}-[0-9]{1,2}-[0-9]{1,2}\\s([0-9]{2}:[0-9]{2})*");
	private static final Pattern PATSOURCE = Pattern.compile("[0-9]{2}:[0-9]{2} (.+) 参与评论");
	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		
		List<Map<String, Object>> tasks = (List<Map<String, Object>>) resultData.get("tasks");
		String pageData = unit.getPageData();
		String url = unit.getTaskdata().get("url").toString();
		Matcher iidMch = IIDPAT.matcher(url);
		if(pageData.contains("javascript:CHINA_COMMENT.gotoendpagec()") && iidMch.find()){
			String commUrl = "http://pl.china.com/CommentInfoAction.json?processID=listNewsComment&order=desc&newsobjectid=%s&channelcode=wap&pageindex=0&typeobjectid=news&clienttype=0&self=1&key=N_F_P_wap_%s";
			String iid = iidMch.group(1);
			commUrl = String.format(commUrl, iid, iid);
			Map<String, Object> taskMap = new HashMap<String, Object>();
			taskMap.put("link", commUrl);
			taskMap.put("rawlink", commUrl);
			taskMap.put("linktype", "newscomment");
			if(tasks == null){
				tasks = new ArrayList<>();
				resultData.put(Constants.TASKS, tasks);
			}
			tasks.add(taskMap);
			resultData.put(Constants.COMMENT_URL, commUrl);
		}
		if(!resultData.containsKey(Constants.NEXTPAGE)&&
				(pageData.contains("下一页") || pageData.contains("下页"))){
			Pattern p = Pattern.compile("\\d+_(\\d+).html");
			Matcher m = p.matcher(url);
			int pageIndex = 0;
			String nextpage = "";
			if(m.find()){
				pageIndex = Integer.valueOf(m.group(1));
				nextpage = url.substring(0, url.lastIndexOf("_")) + "_" + (pageIndex + 1) + ".html";
			}
			else {
				nextpage = url.substring(0, url.lastIndexOf(".")) + "_" + (pageIndex + 1) + ".html";
			}
			Map<String, Object> taskMap = new HashMap<String, Object>();
			taskMap.put("link", nextpage);
			taskMap.put("rawlink", nextpage);
			taskMap.put("linktype", "newscontent");
			if(tasks == null){
				tasks = new ArrayList<>();
				resultData.put(Constants.TASKS, tasks);
			}
			tasks.add(taskMap);
			resultData.put(Constants.NEXTPAGE, taskMap);
		}
		if (resultData.containsKey(Constants.CATE)){ 
			Object obj = resultData.get(Constants.CATE);
			if(obj instanceof String){
				String cateStr = obj.toString().replace("当前位置：", "");
				String[] cateArr = new String[]{};
				if(cateStr.indexOf(">>") != -1){
					cateArr = cateStr.split(">>");
				}
				else if(cateStr.indexOf(">") != -1){
					cateArr = cateStr.split(">");
				}
				List<String> newCates = new ArrayList<String>(); 
				for(String cate : cateArr){
					newCates.add(cate.trim());
				}
				resultData.put(Constants.CATE, newCates);
			}
		}
		String time_sourceStr = (String) resultData.get(Constants.POST_TIME);
		if(resultData.containsKey(Constants.POST_TIME)){
			parseByReg(resultData, Constants.POST_TIME, time_sourceStr, PATTIME, 0);
		}
		if(!resultData.containsKey(Constants.SOURCE) && resultData.containsKey(Constants.POST_TIME)){
			parseByReg(resultData, Constants.SOURCE, time_sourceStr, PATSOURCE, 1);
		}
		if(resultData.containsKey(Constants.CONTENT)){
			String content = resultData.get(Constants.CONTENT).toString();
			if(resultData.containsKey("contents")){
				Object obj = resultData.get("contents");
				if(obj instanceof List){
					List<String> contents = (List<String>) obj;
					StringBuilder sb = new StringBuilder();
					for(String c : contents){
						sb.append(c);
					}
					sb.append(content);
					resultData.remove("contents");
					resultData.put(Constants.CONTENT, sb.toString());
				}
			}
			
		}
		
		//图片新闻下一页会跳到别的组图
		if(resultData.containsKey(Constants.NEXTPAGE)){
			Object obj = resultData.get(Constants.NEXTPAGE);
			String nextpage = "";
			if(obj instanceof String){
				nextpage = obj.toString();
			}
			if(obj instanceof Map){
				Map<String, Object> nextMap = (Map<String, Object>) obj;
				nextpage = nextMap.get("link").toString();
			}
			if(!nextpage.contains("_")){
				resultData.remove(Constants.NEXTPAGE);
				for(int i = 0; i < tasks.size();){
					Map<String,Object> task = tasks.get(i);
					if(task.get("link").equals(nextpage)){
						tasks.remove(task);
						continue;
					}
					i++;
				}
			}
		}
//		LOG.info("url:" + unit.getUrl() + ".after reprocess rs is "
//				+ JsonUtil.toJSONString(resultData));
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
	
	public void parseByReg(Map<String, Object> resultData, String key, String resultStr, Pattern p, int resultIndex){
		Matcher mch = p.matcher(resultStr);
		if(mch.find()){
			resultStr = mch.group(resultIndex);
		}
		resultData.put(key, resultStr.trim());
	}
}
