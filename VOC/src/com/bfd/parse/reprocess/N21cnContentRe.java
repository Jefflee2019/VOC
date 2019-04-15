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
 * 站点名：21cn
 * <p>
 * 主要功能：处理author字段,评论链接
 * @author bfd_01
 *
 */
public class N21cnContentRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(N21cnContentRe.class);
	private static final Pattern PATTIME = Pattern.compile("[0-9]{4}-[0-9]{1,2}-[0-9]{1,2}\\s[0-9]{2}:[0-9]{2}(:[0-9]{2})?");
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		// 取到解析结果
		Map<String, Object> resultData = result.getParsedata().getData();
		if (!resultData.isEmpty()) {
			if (resultData.containsKey(Constants.AUTHOR)) {
				String author = resultData.get(Constants.AUTHOR).toString();
				resultData.put(Constants.AUTHOR, author.split("作者：")[1]);
			}
			if(resultData.containsKey(Constants.POST_TIME)){
				parseByReg(resultData, Constants.POST_TIME, PATTIME);
			}

			String url = result.getSpiderdata().get("location").toString();
			// 评论链接
			Map<String, Object> commentTask = new HashMap<String, Object>();
			String urlHead = "http://review.21cn.com/review/list.do?operationId=0&contentId=";
			String topicid = findIid(url);
			String comUrl = urlHead + topicid
					+ "&pageNo=1&pageSize=10&sys=cms";
			commentTask.put("link", comUrl);
			commentTask.put("rawlink", comUrl);
			commentTask.put("linktype", "newscomment");
			LOG.info("url:" + url + "taskdata is " + commentTask.get("link")
					+ commentTask.get("rawlink")
					+ commentTask.get("linktype"));
			if (!resultData.isEmpty()) {
				resultData.put(Constants.COMMENT_URL, comUrl);
				List<Map> tasks = (List<Map>) resultData.get("tasks");
				tasks.add(commentTask);
			}
			// 后处理插件加上iid
			ParseUtils.getIid(unit, result);
		}
		return new ReProcessResult(processcode, processdata);
	}

	/**
	 * 取到iid，拼出url
	 * @param url
	 * @return
	 */
	private String findIid(String url) {
		Pattern iidPatter = Pattern.compile("(\\d+).shtml");
		Matcher match = iidPatter.matcher(url);
		while (match.find()) {
			return match.group(1);
		}
		return null;
	}
	
	public void parseByReg(Map<String, Object> resultData, String key, Pattern p){
		String resultStr = (String) resultData.get(key);
		Matcher mch = p.matcher(resultStr);
		if(mch.find()){
			resultStr = mch.group(0);
		}
		resultData.put(key, resultStr.trim());
	}
	
}
