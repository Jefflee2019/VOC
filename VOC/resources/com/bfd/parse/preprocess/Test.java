package com.bfd.parse.preprocess;

import java.io.UnsupportedEncodingException;

import com.bfd.crawler.utils.EncodeUtil;
import com.bfd.crawler.utils.FileUtil;

public class Test {

	public static void main(String[] args) throws UnsupportedEncodingException {
		byte[] bytes = FileUtil.readFromFile("/home/ian/test.html");
		U u = new U();
		u.bytes = bytes;
		test(u);

	}

	public static void test(U u) throws UnsupportedEncodingException {
		String charset = EncodeUtil.getHtmlEncode(u.bytes);
		u.charset = charset;
		u.data = new String(u.bytes, charset);
		u.data = u.data.replaceAll("title", "");
		u.charset = "utf-8";
		u.bytes = u.data.getBytes();
	}
}

class U {
	public byte[] bytes;
	public String data;
	public String charset;
}
