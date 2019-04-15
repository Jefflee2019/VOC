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
public class BkoolshareContentRe implements ReProcessor{

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		String url = unit.getUrl();
		if (resultData != null) {
			//newstime 发表于 2017-9-6 10:04:41
			if(resultData.containsKey(Constants.NEWSTIME) && resultData.get(Constants.NEWSTIME) != ""){
				String newstime = resultData.get(Constants.NEWSTIME).toString().replaceAll("发表于 ", "");
//				newstime = this.getCresult(newstime, "(\\d+-\\d+-\\d+ \\d+:\\d+:\\d+)");
				newstime = ConstantFunc.convertTime(newstime);
				resultData.put(Constants.NEWSTIME, newstime.trim());
			}
			List replys = (List) resultData.get("replys");
			for (Object object : replys) {
				Map reply = (Map) object;
				//replydate
				if(reply.containsKey(Constants.REPLYDATE) && reply.get(Constants.REPLYDATE) != ""){
					String replydate = reply.get(Constants.REPLYDATE).toString().replaceAll("发表于 ", "");
//					replydate = this.getCresult(replydate, "(\\d+-\\d+-\\d+ \\d+:\\d+:\\d+)");
					replydate = ConstantFunc.convertTime(replydate);
					reply.put(Constants.REPLYDATE, replydate.trim());
				}
			}
			if (url.contains("page=") && !"1".equals(this.getCresult(url, "page=(\\d+)"))) {
				//去掉主贴内容和发表时间
				resultData.remove(Constants.NEWSTIME);
				resultData.remove(Constants.CONTENTS);
			} else {
				//去掉一楼主贴内容
				replys.remove(0);
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
