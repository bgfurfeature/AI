package com.bgfurfeature.callback;

public interface FetcherCallback {
	void onData(Data data) throws Exception;

	void onError(Throwable cause);

}
