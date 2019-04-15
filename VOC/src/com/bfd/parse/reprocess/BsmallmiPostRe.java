package com.bfd.parse.reprocess;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.ConstantFunc;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;
/**
 * 站点名：泡泡网-论坛
 * <p>
 * 主要功能：处理楼层数，发表时间，回复时间，回复内容
 * @author bfd_01
 *
 */
public class BsmallmiPostRe implements ReProcessor {
//	private static final Log LOG = LogFactory.getLog(BpcpopPostRe.class);

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		// 取到解析结果
		Map<String, Object> resultData = result.getParsedata().getData();
		if (!resultData.isEmpty()) {
			//格式化newstime
			if (resultData.containsKey(Constants.NEWSTIME)) {
				String newstime = resultData.get(Constants.NEWSTIME).toString().trim();
				if (newstime.matches("\\d+-\\d+ \\d+:\\d+:\\d+")) {
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
					newstime = sdf.format(new Date()) + "-" + newstime;
				}else{
					newstime = ConstantFunc.convertTime(newstime);
				}
				resultData.put(Constants.NEWSTIME, newstime);
			}
			if (resultData.containsKey(Constants.REPLYS)) {
				List<Object> replys = (List<Object>) resultData
						.get(Constants.REPLYS);
				for (Object obj : replys) {
					if (obj instanceof Map) {
						Map<String, Object> reply = (Map<String, Object>) obj;
						// 处理楼层数
						if (reply.containsKey(Constants.REPLYFLOOR)) {
							String replyfloor = reply.get(Constants.REPLYFLOOR).toString();
							replyfloor = replyfloor.replace("沙发", "1")
									.replace("板凳", "2")
									.replace("地板", "3")
									.replace("#", "");
							reply.put(Constants.REPLYFLOOR, replyfloor);
						}
						// 回复时间 replydate
						if (reply.containsKey(Constants.REPLYDATE)) {
							String replydate = reply.get(Constants.REPLYDATE).toString().trim();
							if (replydate.matches("\\d+-\\d+ \\d+:\\d+:\\d+")) {
								SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
								replydate = sdf.format(new Date()) + "-" + replydate;
							}else{
								replydate = ConstantFunc.convertTime(replydate);
							}
							reply.put(Constants.REPLYDATE, replydate);
						}
					}
				}
			}
		}
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(processcode, processdata);
	}

}
