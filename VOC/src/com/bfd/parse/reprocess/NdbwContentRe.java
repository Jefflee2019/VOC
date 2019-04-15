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
import com.bfd.parse.util.ParseUtils;

/**
 * 
 * @author 08
 *
 */
public class NdbwContentRe implements ReProcessor{

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		String url = unit.getUrl();
		if (resultData != null) {
			//来源
			if(resultData.containsKey(Constants.SOURCE) && resultData.get(Constants.SOURCE) != ""){
				String source = resultData.get(Constants.SOURCE).toString();
				if (source.contains("来源：")) {
					String reg = "来源：\\s*(\\S+)";
					source = this.getCresult(source, reg);
					resultData.put(Constants.SOURCE, source.trim());
				}
			}
			//作者
			if(resultData.containsKey(Constants.AUTHOR) && resultData.get(Constants.AUTHOR) != ""){
				String author = resultData.get(Constants.AUTHOR).toString();
				if (author.contains("编辑：") && !author.contains("作者：")) {
					String reg = "编辑：\\s*(\\S*)";
					author = this.getCresult(author, reg);
					resultData.put(Constants.AUTHOR, author.replace("）", "").trim());
				}
				if (!author.contains("编辑：") && author.contains("作者：")) {
					resultData.put(Constants.AUTHOR, author.replace("作者：", "").trim());
				}
				if (author.contains("编辑：") && author.contains("作者：")) {
					String reg = "作者：(\\S*)";
					author = this.getCresult(author, reg);
					resultData.put(Constants.AUTHOR, author.trim());
				}
			}
			//post_time时间
			if(resultData.containsKey(Constants.POST_TIME) && resultData.get(Constants.POST_TIME) != ""){
				String post_time = resultData.get(Constants.POST_TIME).toString();
				String reg = "(\\d{4}-\\d{2}-\\d{2} \\S+:\\d{2})";
				post_time = this.getCresult(post_time, reg);
				resultData.put(Constants.POST_TIME, post_time.trim());
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
