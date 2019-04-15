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
 * 心声社区（华为家事版块）
 * 论坛帖子页 
 * 后处理插件
 * @author bfd_05
 *
 */
public class BxinshengPostRe implements ReProcessor{

	@SuppressWarnings("unused")
	private static final Log LOG = LogFactory.getLog(BxinshengPostRe.class);
	private static final Pattern PNUM = Pattern.compile("\\d+");
	private static final Pattern PATTIME = Pattern.compile("[0-9]{4}-[0-9]{1,2}-[0-9]{1,2}.*[0-9]{2}:[0-9]{2}(?=\\b)");
	
	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		
		String url = unit.getTaskdata().get("url").toString();
		String[] urls = url.split("&p=");
		int totalPage = 1;
		if(resultData.containsKey(Constants.REPLYCOUNT)){
			int replyCount = Integer.valueOf((String)resultData.get(Constants.REPLYCOUNT));
			//帖子一页60楼
			totalPage = replyCount%60 == 0 ? replyCount/60 : replyCount/60 + 1;
		}
		
		int pageIndex = 1;
		if(urls.length > 1){
			pageIndex = Integer.valueOf(urls[1]);
			if(pageIndex > 1){
				resultData.remove(Constants.CONTENTS);
				resultData.remove(Constants.AUTHOR);
				resultData.remove(Constants.NEWSTIME);
			}
		}
		if(pageIndex < totalPage){
			String nextpage = urls[0] + "&p=" + (pageIndex + 1);
			resultData.put(Constants.NEXTPAGE, nextpage);
			Map<String, Object> nextMap = new HashMap<String, Object>();
			nextMap.put("link", nextpage);
			nextMap.put("rawlink", nextpage);
			nextMap.put("linktype", "bbspost");
			resultData.put("nextpage", nextpage);
			List<Map<String, Object>> tasks = null;
			if(resultData.containsKey("tasks")){
				tasks = (List<Map<String, Object>>) resultData.get("tasks");
			}
			else {
				tasks = new ArrayList<Map<String, Object>>();
				resultData.put("tasks", tasks);
			}
			tasks.add(nextMap);
		}
		
		if(resultData.containsKey(Constants.NEWSTIME)){
			parseByReg(resultData, Constants.NEWSTIME, PATTIME);
		}
		if(resultData.containsKey(Constants.FORUM_SCORE)){
			parseByReg(resultData, Constants.FORUM_SCORE, PNUM);
		}
		if(resultData.containsKey(Constants.REPLYS)){
			List<Map<String, Object>> replys = (List<Map<String, Object>>) resultData.get(Constants.REPLYS);
			for (Map<String, Object> reply : replys) {
				if (reply.containsKey(Constants.REPLYDATE)) {
					parseByReg(reply, Constants.REPLYDATE, PATTIME);
				}
				if (reply.containsKey(Constants.REPLYFLOOR)) {
					parseByReg(reply, Constants.REPLYFLOOR, PNUM);
				}
				if(reply.containsKey(Constants.REPLY_FORUM_SCORE)){
					parseByReg(reply, Constants.REPLY_FORUM_SCORE, PNUM);
				}
			}
		}
//		LOG.info("url:" + unit.getUrl() + ".after reprocess rs is "
//				+ JsonUtil.toJSONString(resultData));
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
	
	public void parseByReg(Map<String, Object> resultData, String conststr, Pattern p){
		String  resultStr = (String) resultData.get(conststr);
		Matcher mch = p.matcher(resultStr);
		if(mch.find()){
			resultStr = mch.group(0);
		}
		resultData.put(conststr, resultStr.trim());
	}
}
