package com.bfd.parse.test;

import java.util.List;
import java.util.Map;

import com.bfd.crawler.utils.FileUtil;
import com.bfd.parse.util.JsonUtil;

public class Test {

	public static void main(String[] args) {

		String str = FileUtil.readFromFile2Str("d:\\test.txt");
		try {
			List<Object> rss = (List)JsonUtil.parseObject(str);
			Map map = (Map)rss.get(0);
			Map parsedata = (Map)map.get("parsedata");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
