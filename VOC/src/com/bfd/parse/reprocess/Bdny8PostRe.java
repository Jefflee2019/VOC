package com.bfd.parse.reprocess;

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
 * 南阳论坛帖子页 
 * 后处理插件
 * @author bfd_05
 *
 */
public class Bdny8PostRe implements ReProcessor{

//	private static final Log LOG = LogFactory.getLog(B373PostRe.class);
	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
				
		if(resultData.containsKey(Constants.CONTENTS)){
			String contents = (String) resultData.get(Constants.CONTENTS);
			resultData.put(Constants.CONTENTS, ConstantFunc.replaceBlank(contents));
		}
		if(resultData.containsKey(Constants.NEWSTIME)){
			String newstime = (String) resultData.get(Constants.NEWSTIME);
			newstime = newstime.replace("发表于", "");
			newstime = ConstantFunc.convertTime(newstime);
			resultData.put(Constants.NEWSTIME, newstime.trim());
		}
		if(resultData.containsKey(Constants.REPLYS)){
			Object obj = resultData.get(Constants.REPLYS);
			if(obj instanceof List){
				List<Map<String, Object>> replys = (List<Map<String, Object>>) obj;
				for(int i = 0; i < replys.size();){
					Map<String, Object> reply = replys.get(i);
					if(reply.containsKey(Constants.REPLYFLOOR)){
						String replyfloor = (String) reply.get(Constants.REPLYFLOOR);
						replyfloor = replyfloor.replace("沙发", "2")
								.replace("板凳", "3")
								.replace("地板", "4")
								.replace("#", "");
						reply.put(Constants.REPLYFLOOR, replyfloor.trim());
					}
					if(reply.containsKey(Constants.REPLYDATE)){
						String replyDate = (String) reply.get(Constants.REPLYDATE);
						replyDate = replyDate.replace("发表于", "");
						replyDate = ConstantFunc.convertTime(replyDate);
						reply.put(Constants.REPLYDATE, replyDate.trim());
					}
					i++;
				}
			}
		}
		ParseUtils.getIid(unit, result);
//		LOG.info("url:" + unit.getUrl() + ".after reprocess rs is "
//				+ JsonUtil.toJSONString(resultData));
		return new ReProcessResult(SUCCESS, processdata);
	}
}
