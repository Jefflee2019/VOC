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
 * 电子产品世界新闻详情页
 * 后处理插件
 * @author bfd_05
 */
public class NeepwContentRe implements ReProcessor{
//	private static final Log LOG = LogFactory.getLog(NeepwContentRe.class);
	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace arg2) {
		Map<String, Object> resultData = result.getParsedata().getData();
		Map<String, Object> processdata = new HashMap<String, Object>();
		
		if (resultData.containsKey(Constants.AUTHOR)) {
			String author = resultData.get(Constants.AUTHOR).toString();
			author = author.substring(author.indexOf("作者：") + 3, author.length());
			resultData.put(Constants.AUTHOR, author);
		}
		if (resultData.containsKey(Constants.SOURCE)) {
			String source = resultData.get(Constants.SOURCE).toString();
			source = source.substring(source.indexOf("来源：") + 3, source.length());
			resultData.put(Constants.SOURCE, source);
		}
		if (resultData.containsKey(Constants.POST_TIME)) {
			String posttime = resultData.get(Constants.POST_TIME).toString();
			posttime = posttime.substring(posttime.indexOf("时间：") + 3, posttime.length());
			resultData.put(Constants.POST_TIME, posttime);
		}
		String url = unit.getUrl();
		Pattern p = Pattern.compile("(\\d+).htm");
		Matcher mch = p.matcher(url);
		if (mch.find()) {
			String iid = mch.group(1);
			String commentURl = "http://www.eepw.com.cn/comments/list/id/%s";
			commentURl = String.format(commentURl, iid);
			Map<String, Object> commentTask = new HashMap<String, Object>();
			commentTask.put("link", commentURl);
			commentTask.put("rawlink", commentURl);
			commentTask.put("linktype", "newscomment");//任务为列表页
			resultData.put(Constants.COMMENT_URL, commentURl);
			List<Map<String, Object>> tasks = (List<Map<String, Object>>) resultData.get("tasks");
			if(tasks == null){
				tasks = new ArrayList<Map<String, Object>>();
				resultData.put("tasks", tasks);
			}
			tasks.add(commentTask);
		}
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}

}
