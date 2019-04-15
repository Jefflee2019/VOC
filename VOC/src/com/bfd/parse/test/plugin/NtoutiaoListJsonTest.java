package com.bfd.parse.test.plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.json.NbilibiliListJson;
import com.bfd.parse.json.NtoutiaoListJson;
import com.bfd.parse.util.JsonUtil;

public class NtoutiaoListJsonTest {
	public static void main(String[] args) throws Exception {
		testDemo();
	}

	public static void testDemo() throws Exception {
		// 要抓取链接
		String url1 = "https://search.bilibili.com/api/search?search_type=video&keyword=%E5%8D%8E%E4%B8%BA&from_source=banner_search&spm_id_from=333.338.banner_link.1&page=1&order=pubdate";
		// 动态数据
		 List<String> ajaxlist = new ArrayList<String>();
		 ajaxlist.add(url1);
		// 生成解析使用的bean
		Map<String, Object> map = CreateParseUnit.createMap("test", "test", 100, 100, url1, "utf8", null, ajaxlist);
		// 生成解析需要结构
		ParseUnit unit = ParseUnit.fromMap(map, System.currentTimeMillis());
		// 模板
//		String tmplStr = FileUtils.readFileToString(new File(""));
		// 解析输出字段
		String outputFiled = "";
		ParserFace face = new ParserFace("test");
		// 执行解析流程
		// 参数分别是 预处理插件,json插件,后处理插件,模板,ParseFace,输出字段
		ParsePlugin testPlugin = new ParsePlugin(null, new NbilibiliListJson(), null, null, face, outputFiled);
		// ParsePlugin testPlugin = new ParsePlugin(null, null, null, tmplStr,
		// face, outputFiled);
		// 输出结果
		System.out.println(JsonUtil.toJSONString(testPlugin.parse(unit)));
	}
}
