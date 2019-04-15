package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.ConstantFunc;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

/**
 * @site:站长之家 (Bzhanzhang)
 * @function 论坛帖子页后处理插件
 * 
 * @author bfd_02
 *
 */

public class BzhanzhangPostRe implements ReProcessor {
	@SuppressWarnings("unused")
	private static final Log LOG = LogFactory.getLog(BzhanzhangPostRe.class);

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = new HashMap<String, Object>();
		// 解析数据
		resultData = result.getParsedata().getData();
		if (resultData == null) {
			return null;
		}

		if (resultData.containsKey(Constants.REPLYS)) {
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> replys = (List<Map<String, Object>>) resultData.get(Constants.REPLYS);
			if (replys != null && !replys.isEmpty()) {
				int replyFloor = 2;
				for (int i = 0; i < replys.size(); i++) {
					Map<String, Object> replyData = replys.get(i);

					/**
					 * 楼层数 replyfloor": "热门"，对于部分帖子的2、3、4楼层显示异常进行处理
					 * 
					 * @function 格式化楼层字段
					 */
					if (replyData.containsKey(Constants.REPLYFLOOR)) {
						String oldReplyfloor = (String) replyData.get(Constants.REPLYFLOOR);
						
						if (!oldReplyfloor.equals("") && oldReplyfloor.contains("热门")) {
							oldReplyfloor = oldReplyfloor.replace("热门", ""+replyFloor).trim();
							replyFloor = Integer.parseInt(oldReplyfloor);
							replyData.put(Constants.REPLYFLOOR, replyFloor);
							replyFloor++;
						}else if(!oldReplyfloor.equals("") && oldReplyfloor.contains("楼")) {
							oldReplyfloor = oldReplyfloor.replace("楼", "");
							replyFloor = Integer.parseInt(oldReplyfloor);
							replyData.put(Constants.REPLYFLOOR, replyFloor);
						}
					}

					/**
					 * @param replyCOntent 回复人昵称
					 * @param replyDate 回复时间
					 * @function 格式化replyCOntent和replyDate
					 * 发表时间  replyDate:"ldrqmy ：2015-9-18 16:49:34"
					 */
					if (replyData.containsKey(Constants.REPLYDATE)) {
						String replyDate = (String) replyData.get(Constants.REPLYDATE);
						if (!replyDate.equals("")) {
							int index = replyDate.indexOf("：");
							//回复人昵称
							String replyusername = replyDate.substring(0,index).trim();
							//回复时间
							replyDate = replyDate.substring(index);
							replyDate = ConstantFunc.convertTime(replyDate);
							replyData.put(Constants.REPLYUSERNAME, replyusername);
							replyData.put(Constants.REPLYDATE, replyDate);
							
						}
					}
				}
				resultData.put(Constants.REPLYS, replys);
			}
		}

		/**
		 * @param newstime 发表时间
		 * @function 发表时间 标准化 "newstime": "发布：2015-9-18 12:25"
		 */
		if (resultData.containsKey(Constants.NEWSTIME)) {
			String oldNewstime = (String) resultData.get(Constants.NEWSTIME);
			if (!oldNewstime.equals("")) {
				String newstime = oldNewstime.replace("发布：", "");
				newstime = ConstantFunc.convertTime(newstime);
				resultData.put(Constants.NEWSTIME, newstime);
			}
		}
		
		/**
		 * @param partake_cnt 参与人数
		 * @function 格式化 partake_cnt "131 位用户参与讨论"
		 */
		if(resultData.containsKey(Constants.PARTAKE_CNT)) {
			String oldPartakeCnt = resultData.get(Constants.PARTAKE_CNT).toString();
			int index = oldPartakeCnt.indexOf("位用户");
			int partakeCnt = Integer.parseInt(oldPartakeCnt.substring(0,index).trim());
			resultData.put(Constants.PARTAKE_CNT, partakeCnt);
		}
		
		/**
		 * @param view_cnt 浏览人数
		 * @function 格式化 view_cnt "14527 次浏览"
		 */
		if(resultData.containsKey(Constants.VIEW_CNT)) {
			String oldViewCnt = resultData.get(Constants.VIEW_CNT).toString();
			int index = oldViewCnt.indexOf("次浏览");
			int viewCnt = Integer.parseInt(oldViewCnt.substring(0,index).trim());
			resultData.put(Constants.VIEW_CNT, viewCnt);
		}
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
}