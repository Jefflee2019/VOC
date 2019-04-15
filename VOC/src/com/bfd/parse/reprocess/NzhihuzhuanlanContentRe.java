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
 * @site:知乎专栏 (Nzhihuzhuanlan)
 * @function 拼接评论链接
 * 
 * @author bfd_02
 *
 */

public class NzhihuzhuanlanContentRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(NzhihuzhuanlanContentRe.class);

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
		 * 发表时间
		 */
		if(resultData.containsKey("post_time")) {
			String postTime = resultData.get("post_time").toString();
			postTime = postTime.replace("编辑于", "").trim();
			resultData.put(Constants.POST_TIME, postTime);
		}
		
		
		/**
		 * @param commUrl评论链接
		 * @function 需要在页面拼接出评论链接，将与rawlink,linktype放入tasks中
		 */
		Map<String, Object> commentTask = new HashMap<String, Object>();
		String url = unit.getUrl();
		Matcher match = Pattern.compile("p/(\\d+)").matcher(url);
		if (match.find()) {
			String itemId = match.group(1);
			String commUrl = new StringBuffer()
					.append("https://www.zhihu.com/api/v4/articles/")
					.append(itemId)
					.append("/comments?include=data%5B*%5D.author%2Ccontent%2Cvoting%2Cvote_count&order=reverse&limit=20&offset=0")
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
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
}