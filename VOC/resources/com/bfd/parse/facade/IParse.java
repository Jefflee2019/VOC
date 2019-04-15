package com.bfd.parse.facade;

public interface IParse {

	public static final int parser_tmpl = 0;
	public static final int parser_js = 1;
	public static final int parser_auto = 2;
	/**
	 * 
	 * @param jsonStr  传入的参数，包含页面源码和配置参数
	 * @return
	 */
	public Object parse(String jsonStr);
	
}
