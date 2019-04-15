package com.bfd.parse.facade;

import com.bfd.crawler.tmptask.Iparser;
import com.bfd.parse.facade.autoparse.AutoParser;
import com.bfd.parse.facade.jspageparse.JsPageParser;
import com.bfd.parse.facade.tmplparse.TmplParser;

public class ParserFactory {

	public static IParse jsParser = new JsPageParser();
	public static IParse tmplParser = new TmplParser();
	public static IParse autoParser = new AutoParser();
	public static IParse getParser(int parserType){
		if(parserType == IParse.parser_tmpl){
			return tmplParser;
		}else if(parserType == IParse.parser_js){
			return jsParser;
		}else if(parserType == IParse.parser_auto){
			return autoParser;
		}
		return null;
	}
}
