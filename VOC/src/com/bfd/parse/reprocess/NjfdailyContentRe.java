package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;

/**
 * 站点名：解放日报 主要功能：处理作者，发表时间，来源
 * 
 * @author bfd_01
 *
 */
public class NjfdailyContentRe implements ReProcessor {
//	private static final Log LOG = LogFactory.getLog(NjfdailyContentRe.class);

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace parseFace) {
		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		// 取到解析结果
		Map<String, Object> resultData = result.getParsedata().getData();

		if (!resultData.isEmpty()) {
			// author
			if (resultData.containsKey(Constants.AUTHOR)) {
				if (resultData.get(Constants.AUTHOR).toString().split("作者：").length > 1) {
					String author = resultData.get(Constants.AUTHOR).toString().split("作者：")[1];
					resultData.put(Constants.AUTHOR, author);
				} 
			}
			
			// source
			if (resultData.containsKey(Constants.SOURCE)) {
				if (resultData.get(Constants.SOURCE).toString().split("稿件来源：").length > 1) {
					String source = resultData.get(Constants.SOURCE).toString().split("稿件来源：")[1].split(" 作者")[0];
					resultData.put(Constants.SOURCE, source);
				}
			}
			
			// post_time
			if (resultData.containsKey(Constants.POST_TIME)) {
				String posttime = resultData.get(Constants.POST_TIME).toString();
				if (posttime.split(" ").length == 3) {
					posttime = resultData.get(Constants.POST_TIME).toString().split("－")[0];
					// 提取日期操作20160720
					String str = posttime.split(" ")[0];
					posttime = str;
				} else {
					// 页面改版修改-2018-10-24
					// 提取发表时间 来源： 上观新闻 作者： 戴辉 2018-10-21 06:28
					String posttimeRex = "\\d+-\\d+-\\d+\\s*\\d+:\\d+(:\\d{1,2})?";
					Matcher match = Pattern.compile(posttimeRex).matcher(posttime);
					if (match.find()) {
						posttime = match.group();
					}
				}
				resultData.put(Constants.POST_TIME, formatDate(posttime));
			}
		}
		return new ReProcessResult(processcode, processdata);
	}

	private String formatDate(String date) {
		if (date.contains("年") && date.contains("月") && date.contains("日")) {
			return date.replace("年", "-").replace("月", "-").replace("日", "");
		}
		return date;
	}
}
