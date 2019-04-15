package com.bfd.parse.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.client.URLNormalizerClient;

public class DomPathTester implements Tester {

	private static final Log LOG = LogFactory.getLog(DomPathTester.class);

	private URLNormalizerClient normalizer;

	public DomPathTester(URLNormalizerClient normalizer) {
		this.normalizer = normalizer;
	}

	@Override
	public TestResponse test(TestRequest request) {
		return null;
	}
}
