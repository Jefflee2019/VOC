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
 * 红豆社区论坛帖子页 
 * 后处理插件
 * @author bfd_05
 *
 */
public class BhongdouPostRe implements ReProcessor{
	
	private static final Pattern NUMPAT = Pattern.compile("\\d+");
	private static final Pattern PATTIME = Pattern.compile("[0-9]{4}-[0-9]{1,2}-[0-9]{1,2}\\s*([0-9]{2}:[0-9]{2})*");
	
	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		String url = unit.getUrl();
		
		if(resultData.containsKey(Constants.TITLE)){
			String title = resultData.get(Constants.TITLE).toString();
			title = title.substring(0, title.indexOf("(您是"));
			title = title.replace("标题:", "");
			resultData.put(Constants.TITLE, title.trim());
		}
		if(resultData.containsKey(Constants.AUTHOR)){
			Object obj = resultData.get(Constants.AUTHOR);
			if(obj instanceof List){
				List<Map<String, Object>> authorList = (List<Map<String, Object>>) obj;
				for(Map<String, Object> author : authorList){
					if(author.containsKey("topic_cnt")){
						regField(author, "topic_cnt");
					}
					if(author.containsKey(Constants.POST_CNT)){
						regField(author, Constants.POST_CNT);
					}
					if(author.containsKey(Constants.EXPERIENCE_CNT)){
						regField(author, Constants.EXPERIENCE_CNT);
					}
					if(author.containsKey(Constants.AUTHOR_LEVEL)){
						String authorLevel = author.get(Constants.AUTHOR_LEVEL).toString();
						authorLevel = authorLevel.replace("头衔：", "");
						author.put(Constants.AUTHOR_LEVEL, authorLevel);
					}
					if(author.containsKey(Constants.REG_TIME)){
						String regTime = author.get(Constants.REG_TIME).toString();
						Matcher mch = PATTIME.matcher(regTime);
						if(mch.find()){
							author.put(Constants.REG_TIME, mch.group().trim());
						}
					}
				}
			}
		}
		if(resultData.containsKey(Constants.NEWSTIME)){
			String newstime = (String) resultData.get(Constants.NEWSTIME);
			Matcher mch = PATTIME.matcher(newstime);
			if(mch.find()){
				resultData.put(Constants.NEWSTIME, mch.group().trim());
			}
		}
		if(resultData.containsKey(Constants.REPLYCOUNT)){
			String replyCount = (String) resultData.get(Constants.REPLYCOUNT);
			Matcher mch = NUMPAT.matcher(replyCount);
			List<String> replyCnts = new ArrayList<String>();
			while(mch.find()){
				replyCnts.add(mch.group().trim());
			}
			if(replyCnts.size() == 2){
				resultData.put(Constants.VIEWS, replyCnts.get(0));
				resultData.put(Constants.REPLYCOUNT, replyCnts.get(1));
			}
			
			
		}
		if(resultData.containsKey(Constants.REPLYS)){
			Object obj = resultData.get(Constants.REPLYS);
			if(obj instanceof List){
				List<Map<String, Object>> replys = (List<Map<String, Object>>) obj;
				if(!resultData.containsKey(Constants.VIEWS)){
					resultData.remove(Constants.CONTENTS);
					resultData.remove(Constants.AUTHOR);
					resultData.remove(Constants.NEWSTIME);
				}
				for(int i = 0; i < replys.size();){
					Map<String, Object> reply = replys.get(i);
					if(reply.containsKey(Constants.REPLY_POST_CNT)){
						regField(reply, Constants.REPLY_POST_CNT);
					}
					if(reply.containsKey("reply_topic_cnt")){
						regField(reply, "reply_topic_cnt");
					}
					if(reply.containsKey("reply_experience_cnt")){
						regField(reply, "reply_experience_cnt");
					}
					if(reply.containsKey(Constants.REPLY_LEVEL)){
						String replyLevel = reply.get(Constants.REPLY_LEVEL).toString();
						replyLevel = replyLevel.replace("头衔：", "");
						reply.put(Constants.AUTHOR_LEVEL, replyLevel);
					}
					if(reply.containsKey(Constants.REPLYDATE)){
						String replyDate = reply.get(Constants.REPLYDATE).toString();
						Matcher mch = PATTIME.matcher(replyDate);
						if(mch.find()){
							reply.put(Constants.REPLYDATE, mch.group().trim());
						}
					}
					if(reply.containsKey(Constants.REPLY_REG_TIME)){
						String replyRegTime = reply.get(Constants.REPLY_REG_TIME).toString();
						Matcher mch = PATTIME.matcher(replyRegTime);
						if(mch.find()){
							reply.put(Constants.REPLY_REG_TIME, mch.group().trim());
						}
					}
					i++;
				}
				nextTask(url, resultData);
			}
		}
		
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}

	private void regField(Map<String, Object> author, String constStr) {
		String  field = (String) author.get(constStr);
		Matcher mch = NUMPAT.matcher(field);
			if(mch.find()){
				author.put(constStr, mch.group().trim());
			}
	}
	
	@SuppressWarnings("unchecked")
	private void nextTask(String url, Map<String, Object> resultData){
		if(resultData.containsKey("pageInfo")){
			String pageInfo = resultData.get("pageInfo").toString();
			resultData.remove("pageInfo");
			Pattern pid = Pattern.compile("viewthread-(\\d+)");
			Matcher pidMch = pid.matcher(url);
			String baseUrl = "http://hongdou.gxnews.com.cn/viewthread-";
			String id = "";
			if(pidMch.find()){
				id = pidMch.group(1);
			}
			else{
				pid = Pattern.compile("t=(\\d+)");
				pidMch = pid.matcher(url);
				if(pidMch.find()){
					id = pidMch.group(1);
				}
			}
			if(!id.isEmpty()){
				baseUrl = baseUrl + id;
			}
			Pattern page = Pattern.compile("第(\\d+)/(\\d+)页");
			Matcher pageMch = page.matcher(pageInfo);
			if(pageMch.find()){
				int pageIndex = Integer.valueOf(pageMch.group(1));
				int totalPage = Integer.valueOf(pageMch.group(2));
				if(pageIndex > 1){
					baseUrl = url.substring(0, url.lastIndexOf("-"));
				}
				if(pageIndex < totalPage){
					Map<String, Object> nextTask = new HashMap<String, Object>();
					String nextpage = new StringBuilder(baseUrl)
					.append("-").append(pageIndex + 1).append(".html").toString();
					List<Map<String, Object>> tasks = (List<Map<String, Object>>) resultData.get("tasks");
					nextTask.put("link", nextpage);
					nextTask.put("rawlink", nextpage);
					nextTask.put("linktype", "bbspost");
					resultData.put("nextpage", nextpage);
					tasks.add(nextTask);
				}
			}
		}
		
	}
}
