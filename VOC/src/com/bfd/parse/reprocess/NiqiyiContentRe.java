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
public class NiqiyiContentRe implements ReProcessor{

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		String resourceData = unit.getPageData();
		String url = unit.getUrl();
		if (resultData != null) {
			List<Map<String, Object>> rtasks = (List<Map<String, Object>>) resultData.get(Constants.TASKS);
			//data-qitancomment-tvid="9629576309" 
			String reg = "data-qitancomment-tvid=\"(\\d+)";
			String reg1 = "post_time=(.+)";
			String post_time = this.getCresult(url, reg1);
			resultData.put(Constants.POST_TIME, post_time);
			String vid = this.getCresult(resourceData, reg);
			String templateUrl = "http://api-t.iqiyi.com/qx_api/comment/get_video_comments?aid=0&albumid=temp_vid&categoryid=30&cb=fnsucc&escape=true&is_video_page=true&need_reply=true&need_subject=true&need_total=1&page=1&page_size=10&page_size_reply=3&qitan_comment_type=1&qitancallback=fnsucc&qitanid=0&reply_sort=hot&sort=add_time&tvid=temp_vid";
			if (vid != null) {
				String commentUrl = templateUrl.replaceAll("temp_vid", vid);
				/**
				 * 加上评论页链接
				 */
				Map<String, Object> rtask = new HashMap<String, Object>();
				rtask.put("link", commentUrl);
				rtask.put("rawlink", commentUrl);
				rtask.put("linktype", "newscomment");
				rtasks.add(rtask);
				resultData.put(Constants.COMMENT_URL, commentUrl);
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
		return null;
	}

}
