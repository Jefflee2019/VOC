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
 * 站点：手机之家
 * 功能：新闻评论页后处理时间
 * @author bfd_05
 */

public class NimobileCommentRe implements ReProcessor {
//	private static final Log LOG = LogFactory.getLog(NimobileCommentRe.class);
	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		
		if(resultData.containsKey(Constants.COMMENTS)){
			List<Map<String, Object>> comms = (List<Map<String, Object>>) resultData.get(Constants.COMMENTS);
			for(Map<String, Object> comm : comms){
				if(comm.containsKey(Constants.COMMENT_TIME)){
					String commentTime = (String) comm.get(Constants.COMMENT_TIME);
					//正常时间的不进行
					commentTime = ConstantFunc.convertTime(commentTime); 
					comm.put(Constants.COMMENT_TIME, commentTime);
				}
			}
		}
		ParseUtils.getIid(unit, result);
//		LOG.info("url:" + unit.getUrl() + ".after reprocess rs is "
//				+ JsonUtil.toJSONString(resultData));
		return new ReProcessResult(SUCCESS, processdata);
	}
}
