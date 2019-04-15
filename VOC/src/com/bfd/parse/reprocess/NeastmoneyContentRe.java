package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;
import com.bfd.parse.entity.ConstantFunc;
import com.bfd.parse.entity.Constants;

/**
 * 站点名：Neastmoney
 * 
 * 标准化部分字段 以及给出评论页地址
 * 
 * @author bfd_06
 */
public class NeastmoneyContentRe implements ReProcessor {
	
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		
		if(resultData.containsKey(Constants.EDITOR)){
			String editor = (String) resultData.get(Constants.EDITOR);
			formatAttr(Constants.EDITOR,editor, resultData);
			// AUTHOR
			if (editor.contains("作者")) {
				formatAttr(Constants.AUTHOR,editor, resultData);
			}
		}
		
		// "source": "来源： 人民网",
		if(resultData.containsKey(Constants.SOURCE)){
			String source = resultData.get(Constants.SOURCE).toString();
			source = source.replace("来源：", " ").trim();
			resultData.put(Constants.SOURCE, source);
		}
		//"post_time": "2018年05月23日 09:56"
		if(resultData.containsKey(Constants.POST_TIME)){
			String postTime = resultData.get(Constants.POST_TIME).toString();
			postTime = ConstantFunc.getDate(postTime);
			resultData.put(Constants.POST_TIME, postTime);
		}
		
	   /**
	    * 添加评论页链接
	    */
		if (resultData.containsKey(Constants.REPLY_CNT)&&resultData.containsKey("post_id")) {
			Object obj = resultData.get(Constants.REPLY_CNT);
			if(obj instanceof Integer){
				int reply_cnt = (int) resultData.get(Constants.REPLY_CNT);
				if (reply_cnt > 0) {
					addCommentUrl(unit, resultData, result,(int)resultData.get("post_id"));
				}
			} else {
				int reply_cnt = Integer.parseInt((String) resultData.get(Constants.REPLY_CNT));
				if (reply_cnt > 0) {
					addCommentUrl(unit, resultData, result,(int)resultData.get("post_id"));
				}
			}
		}

		return new ReProcessResult(processcode, processdata);
	}

	public void formatAttr(String keyName, String value,
			Map<String, Object> result) {
		switch (keyName) {
		case Constants.EDITOR:
			int index = value.indexOf("编辑");
			value = value.substring(index + 3);
			if (value.equals("")) {
				result.remove(keyName);
			} else {
				result.put(keyName, value);
			}
			break;
		case Constants.AUTHOR:
			int indexA = value.indexOf("作者");
			int indexE = value.indexOf("编辑");
			int indexS = value.indexOf("来源");
			if(indexS!=-1){
				value = value.substring(indexA+3,indexS-1);
			} else {
				value = value.substring(indexA+3,indexE-1);
			}
			if (value.equals("")) {
				result.remove(keyName);
			} else {
				result.put(keyName, value);
			}
			break;
		default:
			break;
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void addCommentUrl(ParseUnit unit, Map<String, Object> resultData, ParseResult result, int topicId) {
			String commentUrl = "http://iguba.eastmoney.com/interf/reply.aspx?callback=jQuery1830010378390684160355_1457575969896&action=getreplys&topicid="
					+ topicId + "&thispage=1&pagecount=20&sort=-1";
			Map<String, String> commentTask = new HashMap<String, String>();
			commentTask.put("link", commentUrl);
			commentTask.put("rawlink", commentUrl);
			commentTask.put("linktype", "newscomment");
			resultData.put("comment_url", commentUrl);
			List<Map> tasks = (List<Map>) resultData.get("tasks");
			tasks.add(commentTask);
			ParseUtils.getIid(unit, result);
	}

}
