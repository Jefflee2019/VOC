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
 * @site:一生一世网-新闻 (Nmylove1314)
 * @function 新闻内容页后处理插件拼接评论链接
 * 
 * @author bfd_02
 *
 */

public class Nmylove1314ContentRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(Nmylove1314ContentRe.class);

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
		 * @param comment_url评论链接
		 * 
		 * @function 需要在页面拼接出评论链接，将与rawlink,linktype放入tasks中
		 */

		/**
		 * http://coral.qq.com/article/1263577794/comment?commentid=0&reqnum=10
		 */

		// 获取新闻内容页源码
		String pageData = unit.getPageData();
		// 存放评论任务的map
		Map<String, Object> commentTask = new HashMap<String, Object>();
		try {
			String comm_url_head = "http://coral.qq.com/article/";
			String comm_url_back = "/comment?commentid=0&reqnum=10";
			//正则匹配出源码中的itemId
			String regex = "href=\"http://coral.qq.com/(\\d+)\"";
			Matcher match = Pattern.compile(regex).matcher(pageData);
			if(match.find()) {
				String itemId = match.group(1);
			String comm_url = comm_url_head + itemId + comm_url_back;
			commentTask.put(Constants.LINK, comm_url);
			commentTask.put(Constants.RAWLINK, comm_url);
			commentTask.put(Constants.LINKTYPE, "newscomment");
			LOG.info("url:" + unit.getUrl() + "taskdata is " + commentTask.get(Constants.LINK)
					+ commentTask.get(Constants.RAWLINK) + commentTask.get(Constants.LINKTYPE));
			if (resultData != null && resultData.size() > 0) {
				resultData.put(Constants.COMMENT_URL, comm_url);
				List<Map> tasks = (List<Map>) resultData.get(Constants.TASKS);
				tasks.add(commentTask);
			}
			}
		} catch (Exception e) {
			LOG.error("excuteParse error");
		}

		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
}