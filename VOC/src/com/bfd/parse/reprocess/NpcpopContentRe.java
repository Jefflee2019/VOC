package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.ConstantFunc;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

public class NpcpopContentRe implements ReProcessor {

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace parseFace) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String,Object> resultData = result.getParsedata().getData();
		if(resultData.containsKey(Constants.CONTENT) && resultData.containsKey(Constants.TITLE) 
				&& resultData.containsKey(Constants.AUTHOR)){
			String author = (String) resultData.get(Constants.AUTHOR);
			String title = (String) resultData.get(Constants.TITLE);
			String content = (String) resultData.get(Constants.CONTENT);
			content = content.replace(author, "").replace(title, "").
					replaceAll("推荐文章	查看更多(\\s|\\S)*", "").trim();
			resultData.put(Constants.CONTENT, content);
			//2018年05月22日 10:01 出处：泡泡网原创 作者:张前前 分享 QQ空间 新浪微博 腾讯微博 人人网 微信
			Matcher authorM = Pattern.compile("作者:(\\S+)").matcher(author);
			if(authorM.find()){
				author = authorM.group(1);
				resultData.put(Constants.AUTHOR, author);
			}
		}
		if(resultData.containsKey(Constants.POST_TIME)){
			String posttime = (String) resultData.get(Constants.POST_TIME);
			Matcher posttimeM = Pattern.compile("\\S*\\s\\S*").matcher(posttime);
			if(posttimeM.find()){
				posttime = ConstantFunc.getDate(posttimeM.group(0));
				resultData.put(Constants.POST_TIME, posttime);
			}
		}
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
	
}
