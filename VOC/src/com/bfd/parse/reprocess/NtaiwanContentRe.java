package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

public class NtaiwanContentRe implements ReProcessor{

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		
		if (resultData != null) {
			//来源
			if(resultData.containsKey(Constants.SOURCE) && resultData.get(Constants.SOURCE) != ""){
				String source = resultData.get(Constants.SOURCE).toString();
				if (source.contains("来源：")) {
					String reg = "来源：([\\S]*)";
					source = this.getCresult(source, reg);
					resultData.put(Constants.SOURCE, source);
				}
			}
			//作者
			if(resultData.containsKey(Constants.AUTHOR) && resultData.get(Constants.AUTHOR) != ""){
				String author = resultData.get(Constants.AUTHOR).toString();
				if (author.contains("编辑：")) {
					String reg = "编辑：([\\S]*)";
					author = this.getCresult(author, reg);
					resultData.put(Constants.AUTHOR, author.replace("]", ""));
				}
				if (author.contains("责任编辑：")) {
					String reg = "责任编辑：([\\S]*)";
					author = this.getCresult(author, reg);
					resultData.put(Constants.AUTHOR, author.replace("]", ""));
				}
				if (author.contains("编辑:")) {
					String reg = "编辑:([\\S]*)";
					author = this.getCresult(author, reg);
					resultData.put(Constants.AUTHOR, author.replace("]", ""));
				}
			}
			//post_time时间
			if(resultData.containsKey(Constants.POST_TIME) && resultData.get(Constants.POST_TIME) != ""){
				String post_time = resultData.get(Constants.POST_TIME).toString();
				String reg = "(\\d{4}[年|-]{1}\\d{2}[月|-]{1}\\d{2}[日|\\s]+[\\S]*:\\d{2})";
				post_time = this.getCresult(post_time, reg).replace("年", "-").replace("月", "-").replace("日", "");
				if (post_time.length() == 16) {
					post_time += ":00";
				}
				resultData.put(Constants.POST_TIME, post_time);
			}
			
		}
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}

	/**
	 * 正则匹配字符串
	 * @param str
	 * @param pattern
	 * @return
	 */
	private String getCresult(String str,String reg){
		Pattern pattern = Pattern.compile(reg);
		Matcher mch = pattern.matcher(str);
		if(mch.find()){
			return mch.group(1);
		}
		return str;
	}

}
