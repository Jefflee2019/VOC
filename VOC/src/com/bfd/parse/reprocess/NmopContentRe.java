package com.bfd.parse.reprocess;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;
import com.bfd.parse.entity.Constants;

/**
 * 站点名：Nmop
 * 
 * 功能：拼接评论URL
 * 
 * @author bfd_06
 */
public class NmopContentRe implements ReProcessor {
	private static final Pattern PATTERN_URLID = Pattern.compile("\\d+");

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace parseFace) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String,Object> resultData = result.getParsedata().getData();
		List<Map<String,Object>> taskList = null;
		if(resultData.containsKey(Constants.TASKS)){
			taskList = (List<Map<String, Object>>) resultData.get(Constants.TASKS);
		}else{
			taskList = new ArrayList<Map<String,Object>>();
		}
		String url = unit.getUrl();
		Matcher urlIdM = PATTERN_URLID.matcher(url);
		String urlId = null;
		while(urlIdM.find()){
			urlId = urlIdM.group(0);
			urlId = urlId.substring(urlId.length()-9,urlId.length());
			System.out.println(urlId);
		}
		if(resultData.containsKey(Constants.POST_TIME)){
			String posttime = (String) resultData.get(Constants.POST_TIME);
			System.out.println(posttime);
			SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			try {
				Date date = sdf.parse(posttime);
				Long dateLong = date.getTime();
				dateLong = dateLong / 1000;
				String id = dateLong + urlId;
				String commetnUrl = 
						"http://comment.mop.com/mopcommentapi/dzh/replylist/api/v170828/replyat/offset/asc/" + id + "/0/100";
				Map<String,Object> map = new HashMap<>();
				map.put(Constants.LINK, commetnUrl);
				map.put(Constants.RAWLINK, commetnUrl);
				map.put(Constants.LINKTYPE, "newscomment");
				taskList.add(map);
				resultData.put(Constants.COMMENT_URL, commetnUrl);
				resultData.put(Constants.TASKS, taskList);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}

}
