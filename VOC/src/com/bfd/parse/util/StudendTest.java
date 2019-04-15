package com.bfd.parse.util;

public class StudendTest {

	public static void main(String[] args) {
		Student s = new Student();
		s.setAge(102);
		s.setName("Tom");
		s.setAddress("China");
		System.out.println("age="+s.getAge()+" "+"name="+s.getName()+" "+"address="+s.getAddress()+" ");
	}
}
