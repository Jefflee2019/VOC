package com.bfd.parse.reprocess;

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
 * 
 * @author bfd_05
 * 
 */
public class Eamazon_mexicoListRe implements ReProcessor {

	@SuppressWarnings("unused")
	private static final Log LOG = LogFactory.getLog(Eamazon_mexicoListRe.class);

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		if(resultData.containsKey(Constants.ITEMS)){
			List<Map<String, Object>> items = (List<Map<String, Object>>) resultData.get(Constants.ITEMS);
			for(Map<String, Object> item : items){
				if(item.containsKey("itemprice")){
					String itemprice = (String) item.get("itemprice");
//					$2,314.57
					itemprice = this.getCresult(itemprice, "(\\$[\\d|,|.]+)");
					item.put("itemprice", itemprice);
				}
			}
		}
		resultData.put("country", "AE");
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
