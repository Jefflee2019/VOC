package com.bfd.parse.preprocess;

import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;

public class NpcpopListPre implements PreProcessor {
	private static final Log LOG = LogFactory.getLog(NpcpopListPre.class);
	//处理下一页
	private static final Pattern PATTERNNEXTPAGE = Pattern.compile("<div class=\"page\">[\\s\\S]*?</div>");//下一页li
	private static final Pattern PATTERNNEXTPAGEA = Pattern.compile("<li><a href=.*?>下一页</a></li>");//下一页a
	
	@Override
	public boolean process(ParseUnit unit, ParserFace parseFace) {
		String pageData = unit.getPageData();
		Matcher matcherreplynpage = PATTERNNEXTPAGE.matcher(pageData);//下一页li
		
		if (matcherreplynpage.find()) {
			try {
				String attrs = matcherreplynpage.group(0);
				attrs = attrs.replaceAll("&nbsp;", "\n");
				Matcher matcherreplynpageA = PATTERNNEXTPAGEA.matcher(attrs);//下一页a
				if(matcherreplynpageA.find()){
					String nextpage = matcherreplynpageA.group(0);
					String posttab = "<div class=\"nextpage_new\">"+nextpage+"</div>";
					pageData = pageData.replace(nextpage, posttab);
				}
			} catch (Exception e) {
				LOG.warn("excuteParse error");
			}
		}
				unit.setPageData(pageData);
				try {
					unit.setPageBytes(pageData.getBytes("utf-8"));
				} catch (UnsupportedEncodingException e) {
					LOG.error("charset transform error");
				}
				unit.setPageEncode("utf8");
				return true;
		}
	}

