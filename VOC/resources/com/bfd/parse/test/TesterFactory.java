package com.bfd.parse.test;

import com.bfd.parse.client.URLNormalizerClient;

public class TesterFactory {
	private static URLNormalizerClient normalizer; // 所有的tester共用同一个归一化链接
	static {
		normalizer = new URLNormalizerClient();
	}

	public static Tester getTester(String testType) {
		if ("DomTmpl".equalsIgnoreCase(testType)) {
			return new DomTmplTester(normalizer);
		} else if ("DomPath".equalsIgnoreCase(testType)) {
			return new DomPathTester(normalizer);
		} else if ("JudgeRule".equalsIgnoreCase(testType)) {
			return new JudgeRuleTester();
		} else if ("BfdItem".equalsIgnoreCase(testType)) {
			return new BfdItemTester(normalizer);
		}
		return null;
	}
}
