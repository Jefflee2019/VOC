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
 * @site:新浪网-论坛
 * @function 论坛帖子页后处理插件
 * 
 * @author bfd_02
 *
 */

public class BsinaPostRe implements ReProcessor {
	@SuppressWarnings("unused")
	private static final Log LOG = LogFactory.getLog(BsinaPostRe.class);
	private static final String REG = "\\S+\\s+(\\d+)\\s+\\S+：(\\d+)\\s+\\S+：([\\-\\d]+)\\s+\\S+";

	@SuppressWarnings({ "unchecked", "rawtypes" })
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
					 * 楼层数 replyfloor": "2楼
					 * 
					 * @function 去掉非数字符号
					 */
					if (replyData.containsKey(Constants.REPLYFLOOR)) {
						String oldReplyfloor = (String) replyData.get(Constants.REPLYFLOOR);
						// 去掉 与主贴信息重复的1楼
						if (!oldReplyfloor.equals("") && oldReplyfloor.equals("1楼")) {
							replys.remove(i);
							i--;
						}
						Pattern pattern = Pattern.compile("(\\d+)(.*)");
						Matcher matcher = pattern.matcher(oldReplyfloor);
						if (matcher.find()) {
							String newReplyfloor = oldReplyfloor.replace(matcher.group(), matcher.group(1));
							replyData.put(Constants.REPLYFLOOR, newReplyfloor);
						}
					}

					/**
					 * 回复时间 "replydate": "发表于：2015-07-13 14:34"
					 */
					if (replyData.containsKey(Constants.REPLYDATE)) {
						String oldReplydate = (String) replyData.get(Constants.REPLYDATE);
						if (!oldReplydate.equals("")) {
							String newReplydate = oldReplydate.replace("发表于：", "").trim();
							replyData.put(Constants.REPLYDATE, newReplydate);

						}
					}

					/**
					 * @function deal post_cnt、essence_cnt、reg_time
					 *           "reply_post_cnt"
					 *           :"发帖 59 精华：0 注册时间：2014-7-28 发短消息"
					 *           "reply_essence_cnt"
					 *           :"发帖 59 精华：0 注册时间：2014-7-28 发短消息"
					 *           "reply_reg_time"
					 *           :"发帖 59 精华：0 注册时间：2014-7-28 发短消息"
					 */

					if (replyData.containsKey(Constants.REPLY_POST_CNT)
							| replyData.containsKey(Constants.REPLY_ESSENCE_CNT)
							| replyData.containsKey(Constants.REPLY_REG_TIME)) {
						String replyPostCnt = replyData.get(Constants.REPLY_POST_CNT).toString();
						String replyEssenceCnt = replyData.get(Constants.REPLY_ESSENCE_CNT).toString();
						String replyRegTime = replyData.get(Constants.REPLY_REG_TIME).toString();
						Pattern ptn = Pattern.compile(REG);
						Matcher match = ptn.matcher(replyPostCnt);
						if (match.find()) {
							replyPostCnt = match.group(1);
							replyEssenceCnt = match.group(2);
							replyRegTime = match.group(3);
							replyData.put(Constants.REPLY_POST_CNT, Integer.parseInt(replyPostCnt));
							replyData.put(Constants.REPLY_ESSENCE_CNT, Integer.parseInt(replyEssenceCnt));
							replyData.put(Constants.REPLY_REG_TIME, replyRegTime);
						} else {
							replyData.put(Constants.REPLY_POST_CNT, 0);
							replyData.put(Constants.REPLY_ESSENCE_CNT, 0);
							replyData.put(Constants.REPLY_REG_TIME, "");
						}
					}

				}
				resultData.put(Constants.REPLYS, replys);
			}
		}

		/**
		 * @function deal postCnt、essenceCnt、regTime
		 * 
		 */
		if (resultData.containsKey(Constants.AUTHOR)) {
			List<Map> authorlist = (List) resultData.get(Constants.AUTHOR);
			Map author = authorlist.get(0);
			if (author.containsKey(Constants.POST_CNT) | author.containsKey(Constants.ESSENCE_CNT)
					| author.containsKey(Constants.REG_TIME)) {
				String postCnt = author.get(Constants.POST_CNT).toString();
				String essenceCnt = author.get(Constants.ESSENCE_CNT).toString();
				String regTime = author.get(Constants.REG_TIME).toString();
				Pattern ptn = Pattern.compile(REG);
				Matcher match = ptn.matcher(postCnt);
				if (match.find()) {
					postCnt = match.group(1);
					essenceCnt = match.group(2);
					regTime = match.group(3);
					author.put(Constants.POST_CNT, Integer.parseInt(postCnt));
					author.put(Constants.ESSENCE_CNT, Integer.parseInt(essenceCnt));
					author.put(Constants.REG_TIME, regTime);
				}else {
					author.put(Constants.POST_CNT, 0);
					author.put(Constants.ESSENCE_CNT, 0);
					author.put(Constants.REG_TIME, "");
				}

			}
			resultData.put(Constants.AUTHOR, authorlist);
		}
		/**
		 * 删掉 首页外的主贴重复信息：contents、author、newstime
		 */
		// 获取当前页url
		String currentUrl = unit.getUrl();
		Pattern ptn = Pattern.compile("page=(\\d+)");
		Matcher match = ptn.matcher(currentUrl);
		if (match.find()) {
			int currentPage = Integer.parseInt(match.group(1));
			if (currentPage > 1) {
				if (resultData.containsKey(Constants.CONTENTS)) {
					resultData.remove(Constants.CONTENTS);
				}
				if (resultData.containsKey(Constants.AUTHOR)) {
					resultData.remove(Constants.AUTHOR);
				}
				if (resultData.containsKey(Constants.NEWSTIME)) {
					resultData.remove(Constants.NEWSTIME);
				}
			}
		}

		/**
		 * 发表时间 标准化 "newstime": "发表于：2015-07-13 14:34"
		 */
		if (resultData.containsKey(Constants.NEWSTIME)) {
			String oldNewstime = (String) resultData.get(Constants.NEWSTIME);
			if (!oldNewstime.equals("")) {
				String newstime = oldNewstime.replace("发表于：", "").trim();
				resultData.put(Constants.NEWSTIME, newstime);
			}
		}

		/**
		 * 路径 cate:科技论坛 » 4G话题讨论 » 华为新品邀请函，好神秘的赶脚~ 去掉"»"
		 */
		if (resultData.containsKey(Constants.CATE)) {
			Object oldCate = resultData.get(Constants.CATE);
			if (oldCate instanceof List) {
				ArrayList oldCateList = (ArrayList) oldCate;
				String oldCatestr = oldCateList.get(0).toString();
				ArrayList newCateList = new ArrayList<String>();
				if (!oldCatestr.equals("") && oldCatestr.contains("»")) {
					String[] newCate = oldCatestr.split("»");
					for (int i = 0; i < newCate.length; i++) {
						newCateList.add(newCate[i].trim());
					}
					resultData.put(Constants.CATE, newCateList);
				}
			}
		}
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}

}
