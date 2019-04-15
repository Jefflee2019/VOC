package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

/**
 * 站点名：一号店
 * 
 * 主要功能：过滤商品编号及拼接评论链接
 * 
 * @author bfd_03
 *
 */
public class Eyhd_hwContentRe implements ReProcessor {

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();

		// 商品编号: 商品编号0587757720
		if (resultData.containsKey(Constants.ITEM_NUM)) {
			String itemNum = resultData.get(Constants.ITEM_NUM).toString();
			int index = itemNum.indexOf("商品编号");
			if (index >= 0) {
				itemNum = itemNum.substring(index + 4).trim();
			}

			resultData.put(Constants.ITEM_NUM, itemNum);

			// 拼接评论链接(评论获取方式改为模板获取)，itemid来源于商品编码
			getCommentTask(resultData, itemNum);
			ParseUtils.getIid(unit, result);
		}
		return new ReProcessResult(SUCCESS, processdata);
	}

	@SuppressWarnings("unchecked")
	private void getCommentTask(Map<String, Object> resultData, String itemNum) {
		// http://item.yhd.com/squ/comment/getCommentDetail.do?productId=3942714
		// &pagenationVO.currentPage=1&pagenationVO.preCurrentPage=0&pagenationVO.rownumperpage=10&filter.commentFlag=0&filter.sortType=6
		StringBuffer sb = new StringBuffer();
		sb.append("http://item.yhd.com/squ/comment/getCommentDetail.do?productId=");
		sb.append(itemNum);
		sb.append("&pagenationVO.currentPage=1&pagenationVO.preCurrentPage=0&pagenationVO.rownumperpage=10&filter.commentFlag=0&filter.sortType=6");
		Map<String, String> commentTask = new HashMap<String, String>();
		String sCommUrl = sb.toString();
		commentTask.put(Constants.LINK, sCommUrl);
		commentTask.put(Constants.RAWLINK, sCommUrl);
		commentTask.put(Constants.LINKTYPE, "eccomment");
		if (resultData != null && !resultData.isEmpty()) {
			resultData.put(Constants.COMMENT_URL, sCommUrl);
			List<Map<String, String>> tasks = (List<Map<String, String>>) resultData.get(Constants.TASKS);
			tasks.add(commentTask);
		}
	}
}
