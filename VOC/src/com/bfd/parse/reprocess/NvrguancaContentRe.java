package com.bfd.parse.reprocess;

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
 * 
 * @author 08
 *
 */
public class NvrguancaContentRe implements ReProcessor{

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		String url = unit.getUrl();
		if (resultData != null) {
//			"author": "作者：admin", 
			if(resultData.containsKey(Constants.AUTHOR) && resultData.get(Constants.AUTHOR) != ""){
				String author = resultData.get(Constants.AUTHOR).toString().replaceAll("作者：", "");
				resultData.put(Constants.AUTHOR, author.trim());
			}
//	        "source": "来源：雷锋网", 
			if(resultData.containsKey(Constants.SOURCE) && resultData.get(Constants.SOURCE) != ""){
				String source = resultData.get(Constants.SOURCE).toString().replaceAll("来源：", "");
				resultData.put(Constants.SOURCE, source.trim());
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
