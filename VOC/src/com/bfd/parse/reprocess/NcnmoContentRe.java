package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.Map;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

/**
 * @site：手机中国
 * @function：新闻内容页后处理
 * @author bfd_04
 *
 */
public class NcnmoContentRe implements ReProcessor {

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace parseFace) {
		Map<String, Object> resultData = new HashMap<String, Object>();
		Map<String, Object> processdata = new HashMap<String, Object>();
		resultData = result.getParsedata().getData();
		if (resultData != null && !resultData.isEmpty()) {
			// "手机中国 【原创】 作者：张爽 2016-01-26 11:14"
			// "华为 作者：华为 2012-02-27 00:29"
			// "手机中国 【原创】 作者：王雨 2018-06-21 05:30"
			if (resultData.containsKey(Constants.SOURCE)) {
				String source = resultData.get(Constants.SOURCE).toString();
				String[] splitArr = source.split("作者：");
				source = splitArr[0].trim();
				String author = splitArr[1].trim().split(" ")[0].trim();
				String postTime = splitArr[1].trim().split(" ")[1].trim();

				// deal with source
				if (!source.equals("")) {
					resultData.put(Constants.SOURCE, source);
				} else {
					resultData.remove(Constants.SOURCE);
				}

				// deal with author
				if (!author.equals("")) {
					resultData.put(Constants.AUTHOR, author);
				} else {
					resultData.remove(Constants.AUTHOR);
				}

				// deal with post_time
				if (!postTime.equals("")) {
					resultData.put(Constants.POST_TIME, postTime);
				} else {
					resultData.remove(Constants.POST_TIME);
				}
			}

			// //deal with comment
			/*Map<String, Object> commentTask = new HashMap<String, Object>();
			String url = unit.getUrl();
			String pageData = unit.getPageData();
			Matcher match1 = Pattern.compile("kindid=(\\d+)").matcher(pageData);
			Matcher match2 = Pattern.compile("(\\d+).html").matcher(url);
			if (match1.find() && match2.find()) {
				String kindId = match1.group(1);
				String articleId = match2.group(1);
				StringBuilder sb = new StringBuilder();
				sb.append("http://comments.cnmo.com/comments2012.php?")
				  .append("kindid=")
				  .append(kindId)
				  .append("&articleid=")
				  .append(articleId);
				String commUrl = sb.toString();
				commentTask.put("link", commUrl);
				commentTask.put("rawlink", commUrl);
				commentTask.put("linktype", "newscomment");
				resultData.put(Constants.COMMENT_URL, commUrl);
				if (resultData.containsKey("tasks")) {
					@SuppressWarnings("unchecked")
					List<Map<String, Object>> tasks = (List<Map<String, Object>>) resultData.get("tasks");
					tasks.add(commentTask);
				}
			}*/
			ParseUtils.getIid(unit, result);
		}
		return new ReProcessResult(SUCCESS, processdata);
	}
}
