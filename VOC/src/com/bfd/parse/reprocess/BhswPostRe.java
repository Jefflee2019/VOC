package com.bfd.parse.reprocess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.ConstantFunc;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

/**
 * 华商网论坛帖子页 
 * 后处理插件
 * @author bfd_05
 *
 */
public class BhswPostRe implements ReProcessor{
	
	private static final Pattern numPat = Pattern.compile("\\d+");
	private static final Pattern pageIndexPat = Pattern.compile("page-(\\d+).html");
	private static final Pattern urlRoot = Pattern.compile("http://bbs.hsw.cn/read-htm-tid-\\d+");
	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		String url = unit.getUrl();
		if(resultData.containsKey(Constants.CONTENTS)){
			String contents = (String) resultData.get(Constants.CONTENTS);
			resultData.put(Constants.CONTENTS, ConstantFunc.replaceBlank(contents));
		}
		if(resultData.containsKey(Constants.NEWSTIME)){
			String newstime = (String) resultData.get(Constants.NEWSTIME);
			newstime = newstime.replace("发表于:", "");
			newstime = ConstantFunc.convertTime(newstime);
			resultData.put(Constants.NEWSTIME, newstime.trim());
		}
		if(resultData.containsKey(Constants.AUTHOR)){
			List<Map<String, Object>> author = (List<Map<String, Object>>) resultData.get(Constants.AUTHOR);
			Map<String, Object> map = author.get(0);
			String fans_cntStr = (String) map.get(Constants.FANS_CNT);
			String  attention_cnt= fans_cntStr.substring(fans_cntStr.indexOf("(")+1, fans_cntStr.indexOf(")"));
			String fans_cnt= fans_cntStr.substring(fans_cntStr.lastIndexOf("(")+1, fans_cntStr.lastIndexOf(")"));
			map.put(Constants.FANS_CNT, fans_cnt.trim());
			map.put("attention_cnt", attention_cnt.trim());
		}
		if(resultData.containsKey(Constants.REPLYS)){
			Object obj = resultData.get(Constants.REPLYS);
			if(obj instanceof List){
				List<Map<String, Object>> replys = (List<Map<String, Object>>) obj;
				if(!resultData.containsKey(Constants.TITLE)){
					resultData.remove(Constants.CONTENTS);
					resultData.remove(Constants.AUTHOR);
					resultData.remove(Constants.NEWSTIME);
				}
				else {
					replys.remove(0);
				}
				for(int i = 0; i < replys.size();){
					Map<String, Object> reply = replys.get(i);
					if(reply.containsKey(Constants.REPLYFLOOR)){
						String replyfloor = (String) reply.get(Constants.REPLYFLOOR);
						replyfloor = replyfloor.replace("沙发", "1")
								.replace("板凳", "2")
								.replace("地板", "3")
								.replace("#", "");
						reply.put(Constants.REPLYFLOOR, replyfloor.trim());
					}
					if(reply.containsKey(Constants.REPLYDATE)){
						String replyDate = (String) reply.get(Constants.REPLYDATE);
						replyDate = replyDate.replace("发表于:", "");
						replyDate = ConstantFunc.convertTime(replyDate);
						reply.put(Constants.REPLYDATE, replyDate.trim());
					}
					if(reply.containsKey(Constants.REPLY_FANS_CNT)){
						String fans_cntStr = (String) reply.get(Constants.REPLY_FANS_CNT);
						String attention_cnt = fans_cntStr.substring(fans_cntStr.indexOf("(")+1, fans_cntStr.indexOf(")"));
						String fans_cnt = fans_cntStr.substring(fans_cntStr.lastIndexOf("(")+1, fans_cntStr.lastIndexOf(")"));
						reply.put(Constants.REPLY_FANS_CNT, fans_cnt.trim());
						reply.put("reply_attention_cnt", attention_cnt.trim());
					}
					i++;
				}
			}
		}
		if(resultData.containsKey(Constants.VIEW_CNT)){
			Matcher mch = numPat.matcher(resultData.get(Constants.VIEW_CNT).toString());
			if(mch.find()){
				resultData.put(Constants.VIEW_CNT, mch.group());
			}
		}
		if(resultData.containsKey(Constants.REPLYCOUNT)){
			Matcher mch = numPat.matcher(resultData.get(Constants.REPLYCOUNT).toString());
			if(mch.find()){
				resultData.put(Constants.REPLYCOUNT, mch.group());
			}
		}
		if(resultData.containsKey("totalpage")){
			Matcher totalMch = numPat.matcher(resultData.get("totalpage").toString());
			int totalPage = 1;
			if(totalMch.find()){
				totalPage = Integer.valueOf(totalMch.group()) ;
			}
			resultData.put("totalpage", totalPage);
			Matcher mch = pageIndexPat.matcher(url);
			int pageIndex = 1;
			if(mch.find()){
				pageIndex = Integer.valueOf(mch.group(1));
			}
			if(pageIndex < totalPage){
				
				Map<String, Object> nextMap = new HashMap<String, Object>();
				StringBuilder nextpage = new StringBuilder();
				Matcher urlMch = urlRoot.matcher(url);
				if(urlMch.find()){
					nextpage.append(urlMch.group()).append("-page-")
					.append(pageIndex+1).append(".html");
				}
				if(!nextpage.toString().isEmpty()){
					nextMap.put("link", nextpage.toString());
					nextMap.put("rawlink", nextpage.toString());
					nextMap.put("linktype", "bbspost");
					List<Map<String, Object>> tasks = null;
					if(resultData.containsKey(Constants.TASKS)){ 
						tasks= (List<Map<String, Object>>) resultData.get(Constants.TASKS);
					}
					else {
						tasks = new ArrayList<Map<String, Object>>();
						resultData.put(Constants.TASKS, tasks);
					}
					resultData.put("nextpage", nextpage.toString());
					tasks.add(nextMap);
				}
			}
		}
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
}
