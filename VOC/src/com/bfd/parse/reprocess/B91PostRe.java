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
 * 站点名：91门户
 * <p>
 * 主要功能：处理楼层数，发表时间，回复时间，回复内容
 * @author bfd_01
 *
 */
public class B91PostRe implements ReProcessor {
//	private static final Log LOG = LogFactory.getLog(B91PostRe.class);

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		// 取到解析结果
		Map<String, Object> resultData = result.getParsedata().getData();
		if (!resultData.isEmpty()) {
			// 删除1楼
			if (resultData.containsKey(Constants.VIEW_CNT)) {
				List<String> list = (List<String>)resultData.get(Constants.REPLYS);
				list.remove(0);
			} else {
				if (resultData.containsKey(Constants.AUTHOR)) {
					resultData.remove(Constants.AUTHOR);
				}
				if (resultData.containsKey(Constants.CONTENTS)) {
					resultData.remove(Constants.CONTENTS);
				}
				if (resultData.containsKey(Constants.NEWSTIME)) {
					resultData.remove(Constants.NEWSTIME);
				}
			}
			if (resultData.containsKey(Constants.REPLYS)) {
				List<Object> replys = (List<Object>) resultData
						.get(Constants.REPLYS);
				for (Object obj : replys) {
					if (obj instanceof Map) {
						Map<String, Object> reply = (Map<String, Object>) obj;
						// 处理楼层数
						if (reply.containsKey(Constants.REPLYFLOOR)) {
							String replyfloor = reply.get(Constants.REPLYFLOOR)
									.toString();
							replyfloor = replyfloor.replace("楼主", "1")
									.replace("沙发", "2").replace("板凳", "3")
									.replace("地板", "4")
									.replace("楼", "").replace("#", "");
							reply.put(Constants.REPLYFLOOR, replyfloor);
						}
						// 回复时间
						if (reply.containsKey(Constants.REPLYDATE)) {
							String newstime = reply
									.get(Constants.REPLYDATE).toString();
							reply.put(Constants.REPLYDATE,
									newstime.split("发表于 ")[1]);
						}
					}
				}
			}
			// newstime
			if (resultData.containsKey(Constants.NEWSTIME)) {
				String newstime = resultData.get(Constants.NEWSTIME).toString();
				//发表于 2016-3-7 19:38:25 98 28739
				Pattern p = Pattern.compile("(\\d+-\\d+-\\d+ \\d+:\\d+:\\d+)");
				Matcher m = p.matcher(newstime);
				while (m.find()) {
					newstime = m.group(1);
				}
				resultData.put(Constants.NEWSTIME, newstime);
			}
		}
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(processcode, processdata);
	}

}
