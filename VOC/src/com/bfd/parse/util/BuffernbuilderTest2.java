package com.bfd.parse.util;

public class BuffernbuilderTest2 {
	public static void main(String[] args) {
		testString();
		testStrBuffer();
		testStrBuilder();
	}
	/**
	 * String  StringBuffer  Stringbuilder
	 */
	public static void testString() {
		Long start = System.currentTimeMillis();
		String str = null;
		for(int i=0;i<20000;i++) {
			str = str+i+',';
			System.out.println(str);
		}
		System.out.println(System.currentTimeMillis() - start);
	}
	
	public static void testStrBuffer() {
		Long start = System.currentTimeMillis();
		StringBuffer buffer = new StringBuffer();
		for(int i=0;i<20000;i++) {
			 buffer.append(i+',');
		}
		System.out.println(System.currentTimeMillis() - start);
	}
	
	public static void testStrBuilder() {
		Long start = System.currentTimeMillis();
		StringBuilder builder = new StringBuilder();
		for(int i=0;i<20000;i++) {
			builder.append(i+',');
		}
		System.out.println(System.currentTimeMillis() - start);
	}
}



