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
 * @site：E都市
 * @function：新闻内容页后处理
 * @author bfd_04
 *
 */
public class NedushiContentRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(NedushiContentRe.class);

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace parseFace) {
		int processcode = 0;
		Map<String, Object> resultData = new HashMap<String, Object>();
		Map<String, Object> processdata = new HashMap<String, Object>();
		resultData = result.getParsedata().getData();
		if (resultData != null && !resultData.isEmpty()) {

			/**
			 * "source": "2016年03月08日 21:34 编辑：无邪 来源：新浪科技",
			 */
			if (resultData.containsKey(Constants.SOURCE)) {
				String source = resultData.get(Constants.SOURCE).toString();
				if (source.contains("来源")) {
					String sourceRex = "来源\\S\\s*(\\S*)";
					source = getRex(source, sourceRex);
				}
				resultData.put(Constants.SOURCE, source);
			}
			/**
			 * "author": "2016年03月08日 21:34 编辑：无邪 来源：新浪科技"
			 */
			if (resultData.containsKey(Constants.AUTHOR)) {
				String author = resultData.get(Constants.AUTHOR).toString();
				if (author.contains("编辑")) {
					String authorRex = "编辑\\S\\s*(\\S*)";
					author = getRex(author, authorRex);
				}
				resultData.put(Constants.AUTHOR, author);
			}
			/**
			 * "post_time": "2016年03月08日 21:34 编辑：无邪 来源：新浪科技", "post_time":
			 * "2016-04-12 10:50 新浪科技"
			 */
			if (resultData.containsKey(Constants.POST_TIME)) {
				String postTime = resultData.get(Constants.POST_TIME).toString();
				String rex = "(\\d{4}\\S\\d{2}\\S\\d{2}\\S\\s*\\d{2}:\\d{2})";
				Matcher match = Pattern.compile(rex).matcher(postTime);
				if (match.find()) {
					postTime = match.group(1);
				}
				resultData.put(Constants.POST_TIME, postTime.replace("年", "-").replace("月", "-").replace("日", ""));
			}
			// //deal with comment
			String url = unit.getUrl();
			if (url.contains("bdt/detail")) {
				Map<String, Object> commentTask = new HashMap<String, Object>();
				try {
					String comm_url = url;
					commentTask.put("link", comm_url);
					commentTask.put("rawlink", comm_url);
					commentTask.put("linktype", "newscomment");
					resultData.put(Constants.COMMENT_URL, comm_url);
					if (resultData.containsKey("tasks")) {
						List<Map<String, Object>> tasks = (List<Map<String, Object>>) resultData.get("tasks");
						tasks.add(commentTask);
					}
				} catch (Exception e) {
					LOG.debug(e.toString());
				}
			}
		}

		ParseUtils.getIid(unit, result);
		return new ReProcessResult(processcode, processdata);
	}

	public String getRex(String resource, String rex) {
		String result = null;
		Matcher match = Pattern.compile(rex).matcher(resource);
		if (match.find()) {
			result = match.group(1);
		}
		return result;
	}
}
