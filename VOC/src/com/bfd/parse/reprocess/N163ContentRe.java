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

/**
 * 
 * @author 08
 *
 */
public class N163ContentRe implements ReProcessor{

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		String url = unit.getUrl();
		if (resultData != null) {
			//"post_time": 2018-06-13 07:25:20　来源: 盖世汽车
			if(resultData.containsKey(Constants.POST_TIME) && resultData.get(Constants.POST_TIME) != ""){
				String post_time = resultData.get(Constants.POST_TIME).toString();
				post_time = this.getCresult(post_time, "(\\d+-\\d+-\\d+ \\d+:\\d+:\\d+)");
				resultData.put(Constants.POST_TIME, post_time.trim());
			}
			/**
			 * 评论页
			 */
			List<Map<String, Object>> _tasks = new ArrayList<Map<String, Object>>();
			resultData.put("tasks", _tasks);
			String tempurl = "http://comment.api.163.com/api/v1/products/a2869674571f77b5a0867c3d71db5856/threads/news_id/comments/newList?ibc=newspc&limit=30&showLevelThreshold=72&headLimit=1&tailLimit=2&offset=0";
//			http://zh.house.163.com/18/0718/08/DN023K0C022193SG.html
			String news_id = this.getCresult(url, "/(\\w+).html");
			String commenturl = tempurl.replaceAll("news_id", news_id);
			List tasks = (List) resultData.get("tasks");
			Map<String, Object> task = new HashMap<String, Object>();
			task.put("link", commenturl);
			task.put("rawlink", commenturl);
			task.put("linktype", "newscomment");
			resultData.put("comment_url", commenturl);
			tasks.add(task);
		
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

	
//	http://comment.api.163.com/api/v1/products/a2869674571f77b5a0867c3d71db5856/threads/DKLBMTGG0011819H/comments/newList?ibc=newspc&limit=30&showLevelThreshold=72&headLimit=1&tailLimit=2&offset=0
}
