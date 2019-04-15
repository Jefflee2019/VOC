package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;

/**
 * 站点名：it168(论坛)
 * 
 * 主要功能：处理发表时间，正文，回复（回复内容，回复时间）
 * 
 * @author bfd_03
 *
 */
public class Bit168PostRe implements ReProcessor {

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;

		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();

		if (resultData != null && !resultData.isEmpty()) {
			//回复（回复内容，回复时间）
			if (resultData.containsKey(Constants.REPLYS)) {
				stringToMap(resultData, unit, Constants.REPLYS);
			}
			//正文
			if (resultData.containsKey(Constants.CONTENTS)) {
				stringToMap(resultData, unit, Constants.CONTENTS);
			}
			//发表时间
			if (resultData.containsKey(Constants.NEWSTIME)) {
				stringToMap(resultData, unit, Constants.NEWSTIME);
			}
			//作者
			if (resultData.containsKey(Constants.AUTHOR)) {
				stringToMap(resultData, unit, Constants.AUTHOR);
			}
			

		}

		return new ReProcessResult(processcode, processdata);
	}

	@SuppressWarnings("unchecked")
	public static void stringToMap(Map<String, Object> resultData, ParseUnit unit, String key) {
		String url = unit.getUrl();
		int curPage = 0;
		Pattern pattern = Pattern.compile("http://jiyouhui.it168.com/thread-\\d+-(\\d+)-1.html");
		Matcher matcher = pattern.matcher(url);
		if(matcher.find()){
			curPage = Integer.parseInt(matcher.group(1));
		}else{
			curPage = 1; //默认当前页面第一页
		}
		
		if(key.equals(Constants.AUTHOR)){
			if(curPage > 1){
				resultData.remove(Constants.AUTHOR);
				resultData.remove(Constants.CONTENTS);
				resultData.remove(Constants.NEWSTIME);
			}
		}
		
		if (key.equals(Constants.REPLYS)) {
			List<Map<String, String>> listReplys = (List<Map<String, String>>) resultData.get(key);
			Map<String, String> mapReplys = null;		
			
			for (int i = 0; i < listReplys.size();) {
				if (listReplys.get(i) instanceof Map) {
					mapReplys = listReplys.get(i);
					String replyfloor = mapReplys
							.containsKey(Constants.REPLYFLOOR) ? mapReplys.get(
							Constants.REPLYFLOOR).toString() : "";
					String replydate = mapReplys
							.containsKey(Constants.REPLYDATE) ? mapReplys.get(
							Constants.REPLYDATE).toString() : "";
					String replycontent = mapReplys
							.containsKey(Constants.REPLYCONTENT) ? mapReplys
							.get(Constants.REPLYCONTENT).toString() : "";

					if (replyfloor.equals("楼主")) {
						listReplys.remove(i);
						continue;
					}

					if (!replycontent.equals("")) {
						int index = -1;
						if ((index = replycontent.indexOf(": ")) >= 0) {
							replycontent = replycontent.substring(index + 1)
									.trim();
						}
						/*
						 * index = -1; if((index = replycontent.indexOf("编辑"))
						 * >= 0){ replycontent =
						 * replycontent.substring(index+2).replace("\t",
						 * "  ").trim(); }
						 */
					}
					if (!replydate.equals("")) {
						int index = -1;
						if ((index = replydate.indexOf("发表于")) >= 0) {
							replydate = replydate.substring(index + 3).trim();
						}
					}
					if (!replyfloor.equals("")) {
						int index = -1;
						if ((index = replyfloor.indexOf("#")) >= 0) {
							replyfloor = replyfloor.substring(0, index).trim();
						}
						if (replyfloor.equals("楼主")) {
							replyfloor = "1";
						} else if (replyfloor.equals("沙发")) {
							replyfloor = "2";
						} else if (replyfloor.equals("板凳")) {
							replyfloor = "3";
						} else if (replyfloor.equals("地板")) {
							replyfloor = "4";
						}
					}
					mapReplys.put(Constants.REPLYDATE, replydate);
					mapReplys.put(Constants.REPLYCONTENT, replycontent);
					mapReplys.put(Constants.REPLYFLOOR, replyfloor);
				}
				i++;
			}
			
			resultData.put(Constants.REPLYS, listReplys);
		}
		
		if (key.equals(Constants.CONTENTS)) {
			String contents = (String)resultData.get(key);
			int index = -1;
			if((index = contents.indexOf("编辑")) >= 0){
				contents = contents.substring(index+2).replace("\t", "  ").trim();
			}
			resultData.put(Constants.CONTENTS, contents);
		}
		if(key.equals(Constants.NEWSTIME)){
			String replydate = (String)resultData.get(key);
			if (!replydate.equals("")) {
				int index = -1;
				if ((index = replydate.indexOf("发表于")) >= 0) {
					replydate = replydate.substring(index + 3)
							.trim();
				}
			}
			resultData.put(Constants.NEWSTIME, replydate);
		}

	}

}
