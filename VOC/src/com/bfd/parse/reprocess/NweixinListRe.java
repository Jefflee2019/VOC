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


public class NweixinListRe implements ReProcessor{

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace parseFace) {
		Map<String, Object> processdata = new HashMap<String, Object>(16);
		Map<String,Object> resultData = result.getParsedata().getData();
		List<Map<String,Object>> items = (List<Map<String, Object>>) resultData.get(Constants.ITEMS);
		List<Map<String,Object>> tasks = (List<Map<String, Object>>) resultData.get(Constants.TASKS);
		for (Map<String, Object> map : tasks) {
			String link = (String) map.get("link");
			String rawlink = (String) map.get("rawlink");
			map.put("link", link.replaceAll("&times", "&amp;times"));
			map.put("rawlink", rawlink.replaceAll("&times", "&amp;times"));
		}
//		http://mp.weixin.qq.com/s?src=11×tamp=1533693818&ver=1047&signature=FPyS*l5bNjubEGdEor47CtGyueRJgxDqDAk3BIj7vkqmPGzGmOnpr2wTsmVrt71xsNVSQvqLzzvF4AjrQnlgy3FFJUKhvrbKiLgvYLBMY0-ZOasfga8Kj5Kh6-IvbrqT&new=1
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}

}
