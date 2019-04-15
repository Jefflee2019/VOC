package com.bfd.parse.reprocess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

/**
 * @site:Oppo官方论坛(Boppo)
 * @function 论坛帖子页后处理插件
 * 
 * @author bfd_02
 *
 */

public class BoppoPostRe implements ReProcessor {
	@SuppressWarnings("unused")
	private static final Log LOG = LogFactory.getLog(BoppoPostRe.class);

	@SuppressWarnings({ "unchecked" })
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
			List<Map<String, Object>> replys = (List<Map<String, Object>>) resultData.get(Constants.REPLYS);
			if (replys != null && !replys.isEmpty()) {
				for (int i = 0; i < replys.size(); i++) {
					Map<String, Object> replyData = replys.get(i);

					/**
					 * 楼层数 replyfloor": "2# 如果帖子页包含楼主，就是1楼即楼主，则删除1楼
					 * 
					 * @function 去掉 "#"
					 */
					if (replyData.containsKey(Constants.REPLYFLOOR)) {
						String replyFloor = (String) replyData.get(Constants.REPLYFLOOR);
						// 去掉楼主回复层
						if (!replyFloor.equals("") && replyFloor.equals("沙发")) {
							replyData.put(Constants.REPLYFLOOR, "2");
						} else if (!replyFloor.equals("") && replyFloor.equals("板凳")) {
							replyData.put(Constants.REPLYFLOOR, "3");
						} else if (!replyFloor.equals("") && replyFloor.equals("地板")) {
							replyData.put(Constants.REPLYFLOOR, "4");
						} else if (!replyFloor.equals("") && replyFloor.contains("楼主")) {
							replyData.put(Constants.REPLYFLOOR, "1");
						} else if (!replyFloor.equals("") && replyFloor.contains("楼")) {
							replyFloor = replyFloor.replace("楼", "").trim();
							replyData.put(Constants.REPLYFLOOR, replyFloor);
						} 
					} else {
						// 没有楼层，说明是楼主层
						replys.remove(replyData);
						i--;
					}

					/**
					 * 回复时间 replydate:"发表于 2015-2-11 00:23"
					 */
					if (replyData.containsKey(Constants.REPLYDATE)) {
						String replyDate = (String) replyData.get(Constants.REPLYDATE);
						if (!replyDate.equals("")) {
							replyDate = replyDate.replace("发表于", "");
							replyData.put(Constants.REPLYDATE, replyDate);
						}
					}
				}
				resultData.put(Constants.REPLYS, replys);
			}
		}

		/**
		 * 发表时间 标准化 "newstime": "发表于 2015-6-2 11:33:05"
		 */
		if (resultData.containsKey(Constants.NEWSTIME)) {
			String newsTime = (String) resultData.get(Constants.NEWSTIME);
			if (!newsTime.equals("")) {
				newsTime = newsTime.replace("发表于", "").trim();
				resultData.put(Constants.NEWSTIME, newsTime);
			}
		}

		/**
		 * 路径 标准化 "cate":
		 * "您现在的位置： 版块 › 玩机专区 › O粉玩机 玩机教程 【OPPO自带】教你使用OPPO手机免费发短信--OPPO社区"
		 */
		if (resultData.containsKey(Constants.CATE)) {
			String cate = (String) resultData.get(Constants.CATE);
			if (!cate.equals("")) {
				cate = cate.replace("您现在的位置：", "").trim();
				String[] cateArr = cate.split("›");
				// 去掉各cate中的空格
				List<Object> list = new ArrayList<Object>();
				for (int i = 0; i < cateArr.length; i++) {
					list.add(cateArr[i].trim());
				}
				resultData.put(Constants.CATE, list);
			}
		}

		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
}