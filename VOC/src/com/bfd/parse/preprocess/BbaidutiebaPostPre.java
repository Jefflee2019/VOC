package com.bfd.parse.preprocess;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.crawler.utils.JsonUtils;
import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;

public class BbaidutiebaPostPre implements PreProcessor {
	private static final Log LOG = LogFactory.getLog(BbaidutiebaPostPre.class);

	private static final Pattern patternnews = Pattern.compile("<div class=\"l_post j_l_post l_post_bright noborder\\s*\"\\s*data-field='(.*?)'\\s*?>");//楼主
	private static final Pattern patternreplys = Pattern.compile("<div class=\"l_post j_l_post l_post_bright\\s*\"\\s*data-field='(.*?)'\\s*?>");//回复者
	
	//处理下一页
	private static final Pattern patternnextpage = Pattern.compile("<li class=\"l_pager pager_theme_4 pb_list_pager\">[\\s\\S]*?</li>");//下一页li
	private static final Pattern patternnextpageA = Pattern.compile("<a href=.*?\">下一页</a>");//下一页a
	
	//处理会员认证
	private static final Pattern patternauth = Pattern.compile("\\<span class\\=\\\"j_icon_slot old_icon_size\\\" [^\\>]+ title\\=\\\"([^\\\\\\\"]+)\\\" [^\\>]+>");//reply_author_auth
	
	@Override
	public boolean process(ParseUnit unit, ParserFace parseFace) {
		String pageData = unit.getPageData();
		Matcher matcher = patternnews.matcher(pageData);
		Matcher matcherreply = patternreplys.matcher(pageData);
		Matcher matcherreplynpage = patternnextpage.matcher(pageData);//下一页li
		Matcher matcheauth = patternauth.matcher(pageData);
		
		
		if (matcheauth.find()) {
			try {
				String attrs = matcheauth.group(0);
				String title = matcheauth.group(1);
				
				String posttab = "<div class=\"auths\">"+title+"</div>";
				pageData = pageData.replace(attrs, posttab);
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
		
//		if (matcherreplynpage.find()) {
//			try {
//				String attrs = matcherreplynpage.group(0);
//				Matcher matcherreplynpageA = patternnextpageA.matcher(attrs);//下一页a
//				if(matcherreplynpageA.find()){
//					String nextpage = matcherreplynpageA.group(0);
//					String posttab = "<div class=\"nextpage_new\">"+nextpage+"</div>";
//					pageData = pageData.replace(nextpage, posttab);
//				}
//			} catch (Exception e) {
//				// TODO: handle exception
//				e.printStackTrace();
//			}
//		}
		
		
		//时间，楼层  楼主 下一页
		if (matcher.find()) {
			String attr_line = matcher.group(0);
			String attrs = matcher.group(1);
			try {
				attrs = attrs.replace("&quot;", "\"");
				Map<String,Object> om = JsonUtils.parseObject(attrs) ;
					if(om.containsKey("content")){
						Map<String,Object> contents = (Map<String, Object>) om.get("content");
						// 正则匹配到全部数据
						String content = "";
						// 时间
						String date = "";
						String reply_floor = "";//楼层post_index
						if(contents.containsKey("post_no")){
							System.out.println(contents.get("post_no"));
							reply_floor = contents.get("post_no").toString();
						}
						if(contents.containsKey("date")){
							date = contents.get("date").toString();
						}
						if(!"".equals(reply_floor) && !"".equals(date) ) {
							String replyFloorTag = "<div class=\"reply_floor\">" + reply_floor + "</div>";
							String dateTage = "<div class=\"date\">" + date + "</div>";
							pageData = pageData.replace(attr_line, attr_line + replyFloorTag + dateTage);
						}
					}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
			
				
				//时间，楼层  回复者
				while(matcherreply.find()) {
					String replysLine = matcherreply.group(0);
					String replyattrs = matcherreply.group(1);
					try {
						replyattrs = replyattrs.replace("&quot;", "\"");
						Map<String,Object> om = JsonUtils.parseObject(replyattrs) ;
							if(om.containsKey("content")){
								Map<String,Object> contents = (Map<String, Object>) om.get("content");
								// 正则匹配到全部数据
								String content = "";
								// 时间
								String date = "";
								String reply_floor = "";//楼层post_index
								if(contents.containsKey("post_no")){
									reply_floor = contents.get("post_no").toString();
								}
								if(contents.containsKey("date")){
									date = contents.get("date").toString();
								}
								if(!"".equals(reply_floor) && !"".equals(date) ) {
									if(!"".equals(reply_floor) && !"".equals(date) ) {
										String replyFloorTag = "<div class=\"reply_floor\">" + reply_floor + "</div>";
										String dateTage = "<div class=\"date\">" + date + "</div>";
										pageData = pageData.replace(replysLine, replysLine + replyFloorTag + dateTage);
									}
								}
							}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}
				unit.setPageData(pageData);
				unit.setPageBytes(pageData.getBytes());
				unit.setPageEncode("utf8");
				return true;
		}
	}

