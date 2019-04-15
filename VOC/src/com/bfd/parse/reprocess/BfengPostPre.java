package com.bfd.parse.reprocess;

import java.io.UnsupportedEncodingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.preprocess.PreProcessor;

/**
 * @site 硅谷动力-论坛(Benet)
 * @function 去掉列表页中结构不同的部分
 * @author bfd_02
 *
 */
public class BfengPostPre implements PreProcessor {
	private static final Log LOG = LogFactory.getLog(BfengPostPre.class);

	@Override
	public boolean process(ParseUnit unit, ParserFace parseFace) {
		String pageData = unit.getPageData();

		pageData = parsePageData(pageData);

		unit.setPageData(pageData);
		try {
			unit.setPageBytes(pageData.getBytes("utf-8"));
		} catch (UnsupportedEncodingException e) {
			LOG.warn("Charset transform error");
		}
		unit.setPageEncode("utf8");
		return true;
	}

	/**   
	 * @Title: parsePageData   
	 * @Description: TODO(1、去掉回帖内容中的引用，2、提取准确的发帖时间替换模糊时间)   
	 * @param: @param pageData
	 * @param: @return      
	 * @return: String      
	 * @throws   
	 */ 
	public String parsePageData(String pageData) {
		// 去掉回帖中的引用部分，以免对回帖内容造成干扰
		if (pageData.contains("<div class=\"quote\">")) {
			String regex = "<div class=\"quote\">.*?</div>";
			pageData = pageData.replaceAll(regex, "");
		}
		//提取准确的发帖时间替换模糊时间
		Document doc = Jsoup.parse(pageData);
		//提取出时间模糊的标签体
		Elements postEles = doc.select("em[id~=authorposton]");
		if(postEles != null && !postEles.isEmpty()) {
			for(Element posttimeEle:postEles) {
				//提取标签
				Elements posttimetags = posttimeEle.getElementsByTag("span");
				if(posttimetags.isEmpty()) {
					continue;
				}
				Element posttimetag = posttimetags.get(0);
				String posttime = posttimeEle.select("span").get(0).attr("title");
				posttimetag.html(posttime);
			}
		}
		
		pageData = doc.html();
		return pageData;
	}

}
