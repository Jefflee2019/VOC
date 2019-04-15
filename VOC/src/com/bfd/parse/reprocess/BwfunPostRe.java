package com.bfd.parse.reprocess;

import java.util.ArrayList;
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
 * @site:C114 (B114)
 * @function 处理帖子页中的多余字段
 * 
 * @author bfd_04
 *
 */

public class BwfunPostRe implements ReProcessor {
//	private static final Log LOG = LogFactory.getLog(BwfunListRe.class);

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace arg2) {
		Map<String, Object> resultData = result.getParsedata().getData();
		Map<String, Object> processdata = new HashMap<String, Object>();
		if (resultData != null && !resultData.isEmpty()) {
			if (resultData.containsKey(Constants.NEWSTIME)) {
				String time = resultData.get(Constants.NEWSTIME).toString();
				time = ConstantFunc.convertTime(time.replace("发表于 ", ""));
				resultData.put(Constants.NEWSTIME, time);
				
			}
			if (resultData.containsKey(Constants.CONTENTS)) {
				String content = resultData.get(Constants.CONTENTS).toString();
				if (content.contains("马上注册，享受积分奖励和更多功能，让您轻松玩转社区。 您需要 登录 才可以下载或查看，没有帐号？注册 x")) {
					content = content.replace("马上注册，享受积分奖励和更多功能，让您轻松玩转社区。 您需要 登录 才可以下载或查看，没有帐号？注册 x", "");
					resultData.put(Constants.CONTENTS, content);
				}
			}
			if (resultData.containsKey(Constants.REPLYS)) {
				List list = new ArrayList();
				list = (List)resultData.get(Constants.REPLYS);
				
				if (((Map)list.get(0)).containsKey(Constants.REPLYFLOOR)) {
					if ("楼主".equals(((Map)list.get(0)).get(Constants.REPLYFLOOR))) {
						list.remove(0);
					} else {
						resultData.remove(Constants.CONTENTS);
						resultData.remove(Constants.AUTHOR);
						resultData.remove(Constants.NEWSTIME);
					}
				}
				
				for (int i=0;i<list.size();i++) {
					Map map = (Map)list.get(i);
					if (map.containsKey(Constants.REPLYDATE)) {
						String replydata = map.get(Constants.REPLYDATE).toString();
						replydata = replydata.replace("发表于 ", "");
						map.put(Constants.REPLYDATE, ConstantFunc.convertTime(replydata));
					}
					

					
					if (map.containsKey(Constants.REPLYFLOOR)) {
						String replyfloor = map.get(Constants.REPLYFLOOR)
								.toString();
						replyfloor = replyfloor.replace("沙发", "2")
								.replace("板凳", "3").replace("地板", "4")
								.replace("地下室", "5").replace("楼主", "")
								.replace("楼", "").replace("#", "").replace("推荐", "0");
						map.put(Constants.REPLYFLOOR, replyfloor);
					}
				}
			}
		}
		
		
//		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
}
