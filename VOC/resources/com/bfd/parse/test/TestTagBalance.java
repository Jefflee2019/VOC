package com.bfd.parse.test;

import org.apache.commons.lang.StringEscapeUtils;
import org.jsoup.Jsoup;

//import com.prnasia.mediawatch.common.utilities.MyCrawler;


public class TestTagBalance {

	public static void main(String[] args) {
		String html = ""+
				" <textarea>"+
				" <div class=\"deal_tit\">"+
				"     <h1>商品介绍</h1>"+
				"     <span class=\"tit_icon\">INTRODUCE</span>"+
				"  </div>"+
				"  <div class=\"intro_wrap deal_com\">"+
				"       颜色鲜艳可爱，将你的琐碎凌乱又常用之物，收纳其中使您的生活整洁有序。相信您的生活会更加惬意充满情调."+
				"    </div>"+
				" </textarea>"+
				"";
//		html = new MyCrawler().get("http://pop.jumei.com/i/deal/df140604p642609.html");
		System.out.println(html);
		System.out.println("-----------------");
		String doc = Jsoup.parse(html).html();
		System.out.println(doc);
		
//		String strHTMLInput = "<P>MyName<P>";
//        String strEscapeHTML = StringEscapeUtils.escapeHtml(doc);
        String strUnEscapeHTML = StringEscapeUtils.unescapeHtml(doc);
//        System.out.println("Escaped HTML >>> " + strEscapeHTML);
        System.out.println("UnEscaped HTML >>> " + strUnEscapeHTML);
	}

}
