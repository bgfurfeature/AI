package com.bgfurfeature.callback;

public class MyFetcher implements Fetcher {
	final Data data;

	public MyFetcher(Data data) {
		System.out.println("调用MyFetcher的构造函数");
		this.data = data;
	}

	/**
	 * 此方法接受一个对象
	 */
	public void fetchData(FetcherCallback callback) {
		try {
			//正常情况
			System.out.println("调用fetchData方法正常");
			callback.onData(data);  // 回调操作

		} catch (Exception e) {
			//报错情况
			System.out.println("调用fetchData方法异常");

			callback.onError(e);  //回调操作

		}
	}

}
