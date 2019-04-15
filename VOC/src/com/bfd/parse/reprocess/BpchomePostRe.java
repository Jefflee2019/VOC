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
 * 电脑之家论坛帖子页 
 * 后处理插件
 * @author bfd_05
 *
 */
public class BpchomePostRe implements ReProcessor{

	@SuppressWarnings("unused")
	private static final Log LOG = LogFactory.getLog(BpchomePostRe.class);
	private static final Pattern PNUM = Pattern.compile("\\d+");
	private static final Pattern PATTIME = Pattern.compile("(?<=\\D)[0-9]{4}.[0-9]{1,2}.[0-9]{1,2}.*[0-9]{2}:[0-9]{2}(?=\\b)");
	private static final Pattern PAGEINDEXPAT = Pattern.compile("thread-\\d+-(\\d+)-\\d+.html");
	
	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace arg2) {
 		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		String url = (String) unit.getTaskdata().get("url");
		Matcher mch = PAGEINDEXPAT.matcher(url);
		if (mch.find()) {
			int pageIndex = Integer.valueOf(mch.group(1));
			if(pageIndex > 1){
				resultData.remove(Constants.CONTENTS);
				resultData.remove(Constants.NEWSTIME);
				resultData.remove(Constants.AUTHOR);
			}
		}
		//路径最后一个dom结构不同，不能通过模板取全，需要取出来处理">"
		if(resultData.containsKey(Constants.CATE)){
			List<String> cates = (List<String>) resultData.get(Constants.CATE);
			String cateStr = cates.get(0);
			if (!cateStr.equals("") && cateStr.contains(">")) {
				String[] cateArr = cateStr.split(">");
				List<String> newCates =  new ArrayList<String>();
				for(String cate : cateArr){
					newCates.add(cate.trim());
				}
				resultData.put(Constants.CATE, newCates);
			}
		}
		
		if(resultData.containsKey(Constants.REPLYS)){
			Object obj = resultData.get(Constants.REPLYS);
			if(obj instanceof List){
				List<Map<String, Object>> replys = (List<Map<String, Object>>) obj;
				for(int i = 0; i < replys.size();){
					Map<String, Object> reply = replys.get(i);
					if(reply.containsKey(Constants.REPLYFLOOR)){
						String replyfloor = (String) reply.get(Constants.REPLYFLOOR);
						if(replyfloor != null && replyfloor.contains("楼主")){
							replys.remove(i);
							continue;
						}
						else{
							parseByReg(reply, Constants.REPLYFLOOR, PNUM);
						}
					}
					if(reply.containsKey(Constants.REPLYDATE)){
						parseByReg(reply, Constants.REPLYDATE, PATTIME);
					}
					i++;
				}
			}
		}
		if(resultData.containsKey(Constants.NEWSTIME)){
			parseByReg(resultData, Constants.NEWSTIME, PATTIME);
		}
		if(resultData.containsKey(Constants.REPLYCOUNT)){
			parseByReg(resultData, Constants.REPLYCOUNT, PNUM);
		}
//		LOG.info("url:" + unit.getUrl() + ".after reprocess rs is "
//				+ JsonUtil.toJSONString(resultData));
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
	
	public void parseByReg(Map<String, Object> dataMap, String conststr, Pattern p){
		String  resultStr = (String) dataMap.get(conststr);
		Matcher mch = p.matcher(resultStr);
		if(mch.find()){
			resultStr = mch.group(0);
		}
		dataMap.put(conststr, resultStr);
	}
}
