package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.ConstantFunc;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
/**
 * 站点名：光明网
 * 主要功能：楼层数，发表时间，回复时间
 * @author bfd_01
 *
 */
public class BguangmingPostRe implements ReProcessor {
//	private static final Log LOG = LogFactory.getLog(BguangmingPostRe.class);

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		// 取到解析结果
		Map<String, Object> resultData = result.getParsedata().getData();

		if (!resultData.isEmpty()) {
			
			if (resultData.containsKey(Constants.REPLYS)) {
				List<Object> replys = (List<Object>) resultData.get("replys");
				// 处理主贴信息
				if (((Map<String,Object>)replys.get(0)).get(Constants.REPLYFLOOR).equals("1")) {
					replys.remove(0);
					dealTopic(resultData);
					
				} else {
					// 删除主贴相关内容
					resultData.remove(Constants.NEWSTIME);
					resultData.remove(Constants.CONTENTS);
					resultData.remove(Constants.AUTHOR);
				}
				dealReplys(resultData);
				}
		}
		return new ReProcessResult(processcode, processdata);
	}
	
	/**
	 * 处理主贴内容
	 * @param resultData
	 */
	private void dealTopic(Map<String,Object> resultData) {
		if (resultData.containsKey(Constants.NEWSTIME)) {
			String newstime = resultData.get(Constants.NEWSTIME).toString();
			newstime = newstime.replace("发表于 ", "");
			resultData.put(Constants.NEWSTIME, ConstantFunc.convertTime(newstime));
		}
	}
	
	/**
	 * 处理作者的签到字段
	 * @param resultData
	 */
	@SuppressWarnings("unchecked")
	private void dealReplys(Map<String,Object> resultData) {
		if (resultData.containsKey(Constants.REPLYS)) {
			List<Map<String,Object>> replysList = (List<Map<String,Object>>) resultData.get(Constants.REPLYS);
			for (int i = 0; i < replysList.size(); i++) {
				Map<String,Object> replys = (Map<String,Object>) replysList.get(i);
				String replydate = replys.get(Constants.REPLYDATE).toString();
				replydate = replydate.replace("发表于 ", "");
				replys.put(Constants.REPLYDATE,
						ConstantFunc.convertTime(replydate));
				if (replys.get(Constants.REPLYCONTENT).toString()
						.startsWith(": ")) {
					replys.put(Constants.REPLYCONTENT,
							replys.get(Constants.REPLYCONTENT).toString()
									.substring(2));
				}
			}
		}
	}
}
