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
 * @site:随身数码影音-新闻 (Nimp3)
 * @function 新闻内容页后处理插件处理字段和拼接评论链接
 * 
 * @author bfd_02
 *
 */

public class Nimp3ContentRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(Nimp3ContentRe.class);

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = new HashMap<String, Object>();

		// 解析数据
		resultData = result.getParsedata().getData();
		if (resultData == null) {
			return null;
		}

		/**
		 * @param author
		 * @function 作者字段去噪 作者：mydrivers
		 */

		if (resultData.containsKey(Constants.AUTHOR)) {
			String author = resultData.get(Constants.AUTHOR).toString();
			if (author.contains("作者：")) {
				author = author.replace("作者：", "").trim();
			}
			resultData.put(Constants.AUTHOR, author);
		}

		/**
		 * @param comment_url评论链接
		 * @function 需要在页面拼接出评论链接，将与rawlink,linktype放入tasks中
		 *           http://www.imp3.net/article-74337-1.html
		 *           http://www.imp3.net/portal.php?mod=comment&id=74337
		 * 
		 */

		// 通过评论数控制：是否生成评论任务
		int replyCnt = 0;
		if (resultData.containsKey(Constants.REPLY_CNT)) {
			replyCnt = Integer.parseInt(resultData.get(Constants.REPLY_CNT).toString());
		}
		if (replyCnt > 0) {
			String url = unit.getUrl();
			Matcher match = Pattern.compile("article-(\\d+)-").matcher(url);
			Map<String, Object> commentTask = new HashMap<String, Object>();
			if (match.find()) {
				String guid = match.group(1);
				// 存放评论任务的map
				// 附带的url需要解码
				String commUrl = new StringBuffer("http://www.imp3.net/portal.php?mod=comment&id=").append(guid)
						.toString();
				commentTask.put(Constants.LINK, commUrl);
				commentTask.put(Constants.RAWLINK, commUrl);
				commentTask.put(Constants.LINKTYPE, "newscomment");
				LOG.info("url:" + unit.getUrl() + "taskdata is " + commentTask.get(Constants.LINK)
						+ commentTask.get(Constants.RAWLINK) + commentTask.get(Constants.LINKTYPE));
				if (resultData != null && !resultData.isEmpty()) {
					resultData.put(Constants.COMMENT_URL, commUrl);
					List<Map> tasks = (List<Map>) resultData.get(Constants.TASKS);
					tasks.add(commentTask);
				}

			}
		}
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
}