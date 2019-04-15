package com.bfd.parse.reprocess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

/**
 * @site:华为荣耀直营店(工商银行融e购)(Eicbc)
 * @function 商品详情页后处理插件，处理商品名称和商品编码
 * 
 * @author bfd_02
 *
 */

public class EicbcContentRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(EicbcContentRe.class);

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = new HashMap<String, Object>();
		// 解析数据
		resultData = result.getParsedata().getData();
		if (resultData == null) {
			LOG.warn("解析数据不存在");
			return null;
		}
		/**
		 * @param itemname
		 * @function 格式化itemname,去掉前面页面上没有的"优惠分期" 
		 * eg:优惠分期华为 荣耀6 移动4GTDD-LTE/TD-SCDMA/GSM（32GB存储） 手机
		 */
		if (resultData.containsKey(Constants.ITEMNAME)) {
			String oldItemname = resultData.get(Constants.ITEMNAME).toString();
			String newItemname = oldItemname.replace("优惠分期", "");
			resultData.put(Constants.ITEMNAME, newItemname);
		}

		/**
		 * @param item_num
		 * @function 处理item_num
		 *  eg: 商品编号：0000154920
		 */
		if (resultData.containsKey(Constants.ITEM_NUM)) {
			String oldItemNum = resultData.get(Constants.ITEM_NUM).toString();
			String newItemNum = oldItemNum.replace("商品编号：", "").trim();
			resultData.put(Constants.ITEM_NUM, newItemNum);
		}

		/**
		 * @param replyCnt
		 * @function 从页面源码添加replyCnt
		 */

		// 获得页面源码
		String pageData = unit.getPageData();
		if (pageData.contains("</span>人评价<")) {
			Pattern ptn = Pattern.compile(">(\\d+)</span>人评价<");
			Matcher match = ptn.matcher(pageData);
			if (match.find()) {
				int replyCnt = Integer.parseInt(match.group(1));
				resultData.put(Constants.REPLY_CNT, replyCnt);
			}
		}

		/**
		 * @param smallImg
		 * @function 从页面源码添加smallImg
		 */
		if (resultData.containsKey(Constants.SMALL_IMG)) {
			List<String> smallImg = new ArrayList<String>();
			// 清空空字符串集合
			 smallImg.clear();
			if (pageData.contains("/></a></li>")) {
				String reg = "<img src=\"(http://image\\d+.mall.icbc.com.cn/[/_\\w\\d]+.jpg)\"\\s+alt=\"图片不存在\"\\s+title=\"产品缩略图\"/></a></li>";
				Pattern ptn = Pattern.compile(reg);
				Matcher match = ptn.matcher(pageData);
				while (match.find()) {
					String url = match.group(1);
					smallImg.add(url);
				}
				resultData.put(Constants.SMALL_IMG, smallImg);
			}
		}
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
}
