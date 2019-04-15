package com.bfd.parse.util;

public class SortTest {
	public static void main(String[] args) {
		int[] a = { 49, 38, 65, 23, 89, 45, 133, 343, 13, 53 };
		System.out.println("排序前：");
		for(int i=0;i<a.length;i++) {
			System.out.println(a[i]+" ");
		}
//		insertSort(a);
		binarySort(a);
	}

	private static void binarySort(int[] a) {
		for (int i = 0; i < a.length; i++) {
			int tem = a[i];
			int left = 0;
			int right = i - 1;
			int mid = 0;
			while (left < right) {
				mid = (left + right) / 2;
				if (tem < a[mid]) {
					right = mid - 1;
				} else {
					left = mid + 1;
				}
			}
			for (int j = i - 1; j >= left; j--) {
				a[j + 1] = a[i];
			}
			if (left != 1) {
				a[left] = tem;
			}
		}
		System.out.println();
		System.out.println("排序之后：");
		for (int i = 0; i < a.length; i++) {
			System.out.println(a[i] + " ");
		}
	}

	private static void insertSort(int[] a) {
		for (int i = 0; i < a.length; i++) {
			System.out.println(a[i] + " ");
		}
		for (int i = 1; i < a.length; i++) {
			int tem = a[i];// 待插入元素
			int j;
			for (j = i - 1; j >= 0; j--) {
				if (a[j] > tem) {
					a[j + 1] = a[j];// 将大于tem的向后移一位
				} else {
					break;
				}
			}
			a[j + 1] = tem;
		}
		System.out.println();
		System.out.println("排序之后：");
		for (int i = 0; i < a.length; i++) {
			System.out.println(a[i] + " ");
		}
	}
}
