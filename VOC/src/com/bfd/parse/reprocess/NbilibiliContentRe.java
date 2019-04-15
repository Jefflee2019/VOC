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
public class NbilibiliContentRe implements ReProcessor{

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		//http://www.bilibili.com/video/av8898580/
		String url = unit.getUrl();
		if (resultData != null) {
			//拼接评论链接 https://api.bilibili.com/x/v2/reply?jsonp=jsonp&pn=1&type=1&oid=7886591&sort=0
			Pattern pattern = Pattern.compile("av(\\d+)");
			Matcher mch = pattern.matcher(url);
			if(mch.find()){
				String topic_id = mch.group(1);
				String sCommUrl = "https://api.bilibili.com/x/v2/reply?jsonp=jsonp&pn=1&type=1&oid=" + topic_id + "&sort=0";
				Map commentTask = new HashMap();
				commentTask.put(Constants.LINK, sCommUrl);
				commentTask.put(Constants.RAWLINK, sCommUrl);
				commentTask.put(Constants.LINKTYPE, "newscomment");
				if (resultData != null && !resultData.isEmpty()) {
					resultData.put(Constants.COMMENT_URL, sCommUrl);
					List<Map> tasks = (List<Map>) resultData.get(Constants.TASKS);
					tasks.add(commentTask);	
				}
			}
		}
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
}
