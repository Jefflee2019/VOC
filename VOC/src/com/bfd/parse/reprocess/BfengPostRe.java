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
 * @ClassName: BfengContentRe
 * @author: taihua.li
 * @date: 2019年3月20日 下午3:36:15
 * @Description:TODO(处理威锋网帖子页 发帖时间、楼层等)
 */
public class BfengPostRe implements ReProcessor {

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace parseFace) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();

		/*
		 * @function 主贴部分处理
		 * 
		 * @element newstime、contents
		 */
		// newstime 处理发表时间，去噪。对于"几分钟前"等模糊时间在预处理里面处理
		if (resultData.containsKey(Constants.NEWSTIME)) {
			getNewstime(resultData);
		}

		// contents 去掉有些帖子中带有的编辑时间相关信息
		if (resultData.containsKey(Constants.CONTENTS)) {
			getContents(resultData);
		}

		/**
		 * date:2019-03-26 16:56 nextpage 下一页链接变化太多，没法首页正则化，不能增量翻页。
		 * 需要改成手动拼接，不再依赖模板
		 */
		if (resultData.containsKey(Constants.REPLYCOUNT)) {
			int replyCount = Integer.parseInt(resultData.get(Constants.REPLYCOUNT).toString());
			if (replyCount > 10) {
				List<Map<String, Object>> tasks = (List<Map<String, Object>>) resultData.get(Constants.TASKS);
				String url = unit.getUrl();
				String nextpage = null;
				Matcher match = Pattern.compile("page=(\\d+)").matcher(url);
				if (match.find()) {
					int pageno = Integer.parseInt(match.group(1));
					if ((double) replyCount / 10 > pageno) {
						nextpage = url.replace("page=" + pageno, "page=" + (pageno + 1));
					}
				} else {
					nextpage = url + "?page=2";
				}
				Map<String, Object> nextpageTask = new HashMap<String, Object>();
				nextpageTask.put(Constants.LINK, nextpage);
				nextpageTask.put(Constants.RAWLINK, nextpage);
				nextpageTask.put(Constants.LINKTYPE, "bbspost");
				tasks.add(nextpageTask);
				resultData.put(Constants.NEXTPAGE, nextpage);
			}
		}

		/**
		 * @function 回帖部分处理
		 * 
		 */
		if (resultData.containsKey(Constants.REPLYS)) {
			List<Map<String, Object>> replys = (List<Map<String, Object>>) resultData.get(Constants.REPLYS);
			if (replys != null && !replys.isEmpty()) {
				// 首页回帖部分去掉楼主层回复,楼主已经当作者处理
				if (replys.get(0).get(Constants.REPLYFLOOR).toString().contains("楼主")) {
					replys.remove(0);
				} else {
					// 除了首页，其它页去掉主贴相关信息
					resultData.remove(Constants.NEWSTIME);
					resultData.remove(Constants.CONTENTS);
					resultData.remove(Constants.AUTHOR);
				}
				// 回帖去噪
				getreplys(replys);
			}
		}

		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}

	/**
	 * @Title: getreplys
	 * @Description: TODO(回帖部分的楼层数提取和回帖时间去噪)
	 * @param: @param replys
	 * @return: void
	 * @throws
	 */
	private void getreplys(List<Map<String, Object>> replys) {
		for (int i = 0; i < replys.size(); i++) {
			Map<String, Object> reply = replys.get(i);
			if (reply.containsKey(Constants.REPLYFLOOR)) {
				String replyfloor = reply.get(Constants.REPLYFLOOR).toString();
				// 楼层数提取
				replyfloor = replyfloor.substring(0, replyfloor.indexOf("楼"));
				reply.put(Constants.REPLYFLOOR, Integer.parseInt(replyfloor));

				// 回帖时间
				if (reply.containsKey(Constants.REPLYDATE)) {
					String replydate = reply.get(Constants.REPLYDATE).toString();
					if (replydate.contains("发表于")) {
						reply.put(Constants.REPLYDATE, replydate.replace("发表于", "").trim());
					}
				}
			}
		}
	}

	/**
	 * @Title: getContents
	 * @Description: TODO(清理回帖中带有时间编辑部分，以免对后端数据去重造成干扰)
	 * @param: @param resultData
	 * @return: void
	 * @throws
	 */
	private void getContents(Map<String, Object> resultData) {
		String contents = resultData.get(Constants.CONTENTS).toString();
		if (contents.contains("本帖最后由")) {
			contents = contents.replaceAll("本帖最后由.*编辑\\s*", "");
			resultData.put(Constants.CONTENTS, contents);

		}
	}

	/**
	 * @Title: getNewstime
	 * @Description: TODO(发表时间去噪)
	 * @param: @param resultData
	 * @return: void
	 * @throws
	 */
	private void getNewstime(Map<String, Object> resultData) {
		String newstime = resultData.get(Constants.NEWSTIME).toString();
		// 处理发表时间，去噪。对于"几分钟前"等模糊时间在预处理里面处理
		if (newstime.contains("发表于")) {
			newstime = newstime.replace("发表于", "").trim();
			resultData.put(Constants.NEWSTIME, newstime);
		}
	}

}
