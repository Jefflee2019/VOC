package com.bfd.parse.reprocess;

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
 * @site:网易手机/数码 (Bmobile163)
 * @function 论坛帖子页后处理插件
 * 
 * @author bfd_02
 *
 */

public class Bmobile163PostRe implements ReProcessor {
	@SuppressWarnings("unused")
	private static final Log LOG = LogFactory.getLog(Bmobile163PostRe.class);

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
		String regex = "";
		if (resultData.containsKey(Constants.REPLYS)) {
			List<Map<String, Object>> replys = (List<Map<String, Object>>) resultData.get(Constants.REPLYS);
			if (replys != null && !replys.isEmpty()) {
				for (int i = 0; i < replys.size(); i++) {
					Map<String, Object> replyData = replys.get(i);

					/**
					 * 楼层数 replyfloor
					 * 
					 * @function 去掉楼层中的 "楼"以及如果帖子页包含楼主，就是1楼即楼主，则删除1楼
					 *           eg:"replyfloor": "30楼 2015-11-02 22:40:28"
					 */
					if (!replyData.containsKey(Constants.REPLYFLOOR)) {
						// 去掉楼主回复层(楼主没有楼层)
						replys.remove(replyData);
						i--;
					} else {
						String oldReplyfloor = replyData.get(Constants.REPLYFLOOR).toString();
						if (!oldReplyfloor.equals("")) {
							regex = "(\\d+)楼\\s*([\\-\\d\\s:]*)";
							Matcher match = Pattern.compile(regex).matcher(oldReplyfloor);
							if (match.find()) {
								int newReplyfloor = Integer.parseInt(match.group(1));
								String replydate = match.group(2);
								replyData.put(Constants.REPLYFLOOR, newReplyfloor);
								replyData.put(Constants.REPLYDATE, replydate);
							}
						}
						
						/**
						 * @param replyContent 回复内容
						 * @param referComments 引用的回复
						 * @param refer_replyfloor 所引用回复人所在楼层
						 * @param refer_comm_username所引用回复人昵称
						 * @param refer_comm_content所引用的回复内容
						 * 
						 * @function 格式化并添加字段
						 *      eg:"replycontent": "引用3楼 送的手机短信 的发言： 手机真的是更新的太快了、我都眼花缭乱了呢。 确实确实", 
	                            eg:"refer_comment": "引用3楼 送的手机短信 的发言： 手机真的是更新的太快了、我都眼花缭乱了呢。", 
						 */

						if (replyData.containsKey("refer_comment")) {
							//存放引用数据的Map
							Map<String,Object> referComments = new HashMap<String,Object>();
							String replyContent = replyData.get(Constants.REPLYCONTENT).toString();
							String referComment = replyData.get("refer_comment").toString();
						    regex ="引用(\\d+)楼\\s*(\\S+)\\s*的发言：\\s*(\\S*)";
							Matcher match = Pattern.compile(regex).matcher(referComment);
							if(match.find()) {
								String referReplyFloor = match.group(1);
								String referComUsername = match.group(2).trim();
								String referComContent = match.group(3).trim();
								referComments.put(Constants.REFER_REPLYFLOOR, referReplyFloor);
								referComments.put(Constants.REFER_COMM_USERNAME, referComUsername);
								referComments.put(Constants.REFER_COMM_CONTENT, referComContent);
								replyData.put(Constants.REFER_COMMENTS, referComments);
							}
							replyContent = replyContent.replace(referComment, "");
							replyData.put(Constants.REPLYCONTENT, replyContent);
							replyData.remove("refer_comment");
						}
						/**
						 * @param reply_post_cnt回复人发帖数
						 *             eg:"reply_post_cnt":"发帖: 0 篇"
						 * @param reply_forum_score回复人论坛积分
						 *             eg:"reply_forum_score":"积分: 32740"
						 * @function 格式化字段
						 */
						
						if (replyData.containsKey(Constants.REPLY_POST_CNT)) {
							String replyPostCnt = replyData.get(Constants.REPLY_POST_CNT).toString();
							regex = "(\\d+)\\s*篇";
							if(replyPostCnt.contains("篇")) {
							replyPostCnt = toPattern(regex, replyPostCnt);
							replyData.put(Constants.REPLY_POST_CNT, Integer.parseInt(replyPostCnt));
							}else {
								replyData.remove(Constants.REPLY_POST_CNT);
							}
						}

						if (replyData.containsKey(Constants.REPLY_FORUM_SCORE)) {
							String replyForumScore = replyData.get(Constants.REPLY_FORUM_SCORE).toString();
							regex = "积分:\\s*([\\d\\-]+)";
							replyForumScore = toPattern(regex, replyForumScore);
							if(!replyForumScore.equals("")){
								replyData.put(Constants.REPLY_FORUM_SCORE, Integer.parseInt(replyForumScore));
							}

						}
					}
				}
				resultData.put(Constants.REPLYS, replys);
			}
		}


		/**
		 * @param newstime 发帖时间
		 *        eg："newstime":"楼主 2015-11-02 15:38:16"
		 */
		if (resultData.containsKey(Constants.NEWSTIME)) {
			String oldNewstime = (String) resultData.get(Constants.NEWSTIME);
			if (!oldNewstime.equals("")) {
				regex = "([\\d\\-\\s:]+)";
				oldNewstime = toPattern(regex, oldNewstime);
				resultData.put(Constants.NEWSTIME, oldNewstime);
			}
		}

		/**
		 * @param cate 帖子路径
		 *           
		 * @function 去掉路径中多余的"" 
		 *       eg:"cate":["","网易首页","网易手机", "网易手机论坛", "活动专区"]
		 */
		if (resultData.containsKey(Constants.CATE)) {
			List<String> cate = (List<String>) resultData.get(Constants.CATE);
			cate.remove(0);
			resultData.put(Constants.CATE, cate);
		}

		/**
		 * @param author 作者信息
		 *           
		 * @function forum_score and post_cnt 格式化 
		 * 			 eg："forum_score":"积分: 32740"
		 *           eg："post_cnt":"发帖: 0 篇"
		 * 
		 */
		if (resultData.containsKey(Constants.AUTHOR)) {
			List<Map<String, Object>> author = (List<Map<String, Object>>) resultData.get(Constants.AUTHOR);
			Map<String, Object> authorData = author.get(0);
			if (authorData.containsKey(Constants.FORUM_SCORE)) {
				String forumScore = authorData.get(Constants.FORUM_SCORE).toString();
				regex = "(\\d+)";
				forumScore = toPattern(regex, forumScore);
				authorData.put(Constants.FORUM_SCORE, Integer.parseInt(forumScore));
			}
			if (authorData.containsKey(Constants.POST_CNT)) {
				String postCnt = authorData.get(Constants.POST_CNT).toString();
				regex = "(\\d+)";
				postCnt = toPattern(regex, postCnt);
				authorData.put(Constants.POST_CNT, Integer.parseInt(postCnt));
			}
		}

		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}

	private String toPattern(String regex, String data) {
		Pattern ptn = Pattern.compile(regex);
		Matcher match = ptn.matcher(data);
		String need = "";
		if (match.find()) {
			need = match.group(1);
		}
		return need;
	}
}