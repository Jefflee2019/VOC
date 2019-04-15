package com.bfd.parse.reprocess;

import java.util.ArrayList;
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


public class NyoukuListRe implements ReProcessor{

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace parseFace) {
		Map<String, Object> processdata = new HashMap<String, Object>(16);
		Map<String,Object> resultData = result.getParsedata().getData();
//		List<Map<String,Object>> items = (List<Map<String, Object>>) resultData.get(Constants.ITEMS);
		List<Map<String,Object>> tasks = (List<Map<String, Object>>) resultData.get(Constants.TASKS);
		List<Map<String,Object>> tasks_r = new ArrayList<Map<String,Object>>();
//		for (int i = 0; i < items.size(); i++) {
//			Map item = items.get(i);
//			String posttime = (String) item.get("posttime");
//			String post_time = this.getCresult(posttime, "(\\d+-\\d+-\\d+)");
//			String author = this.getCresult(posttime, "上传者:(\\S*)");
//			String title = (String) item.get("title");
//			Map task = tasks.get(i);
//			if (item.size() != tasks.size()) {//没有下一页
//				task = tasks.get(i + 1);
//			}
//			if (task.get("link").toString().contains("i.youku.com")) {
//				continue;
//			}
//			String link = (String) task.get("link") + "?post_time=" + post_time + "&title=" + title + "&author=" + author;
//			String rawlink = (String) task.get("rawlink") + "?post_time=" + post_time + "&title=" + title + "&author=" + author;
//			task.put("link", link);
//			task.put("rawlink", rawlink);
//			
//		}
//		for (Map<String, Object> map : items) {
//			map.remove("posttime");
//		}
		//删除掉列表视频
		for (Map<String, Object> map : tasks) {
			String link = (String) map.get("link");
			if (link.contains("v.youku.com") || link.contains("serach_video")) {
				tasks_r.add(map);
			}
		}
		resultData.put("tasks", tasks_r);
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
