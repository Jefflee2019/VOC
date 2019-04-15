package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
/**
 * 站点名：Nitworld
 * 
 * 主要功能：处理作者，发表时间，来源
 * 
 * @author bfd_03
 *
 */
public class NitworldContentRe implements ReProcessor {
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		Map<String, Object> resultData = new HashMap<String,Object>();
		Map<String, Object> processdata = new HashMap<String, Object>();
		resultData = result.getParsedata().getData();
		// 日期：2015-03-08 作者：新闻更新 出处：中关村在线
		// 2009-6-25 10:38:06 文/leon 出处：IT.com.cn(IT世界网)
		if (resultData != null) {
			// 来源
			if (resultData.containsKey(Constants.SOURCE)) {
				String source = (String) resultData.get(Constants.SOURCE);
				int index = -1;
				if ((index = source.indexOf("出处：")) >= 0) {
					source = source.substring(index).replace("出处：", "");
				}
				resultData.put(Constants.SOURCE, source);
			}
			// 发表时间
			if (resultData.containsKey(Constants.POST_TIME)) {
				String sPostTime = (String) resultData.get(Constants.POST_TIME);
				String regEx="[0-9]{4}.[0-9]{1,2}.[0-9]{1,2}\\s*([0-9]{2}:[0-9]{2}:[0-9]{2})?"; 
		        Pattern pattern = Pattern.compile(regEx);  
		        Matcher matcher = pattern.matcher(sPostTime);  
		        if(matcher.find()){  
		        	sPostTime = matcher.group();
		        }
				resultData.put(Constants.POST_TIME, sPostTime.trim());
			}
			// 作者
			if (resultData.containsKey(Constants.AUTHOR)) {
				String author = (String) resultData.get(Constants.AUTHOR);
				int index = -1;
				if ((index = author.indexOf("作者：")) >= 0) {
					int indexEnd = author.indexOf("出处：");
					author = author.substring(index,indexEnd).replace("作者：", "").trim();
				}
				if((index = author.indexOf("文/")) >= 0){
					int indexEnd = author.indexOf("出处：");
					author = author.substring(index,indexEnd).replace("文/", "").trim();
				}
 				resultData.put(Constants.AUTHOR, author);
			}
		} 
		return new ReProcessResult(SUCCESS, processdata);
	}
}