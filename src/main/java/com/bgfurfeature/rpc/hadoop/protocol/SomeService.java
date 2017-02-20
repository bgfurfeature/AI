package com.bgfurfeature.rpc.hadoop.protocol;

/**
 * 定义
 * 统一客户端和服务端
 * 所提供的服务类型
 * 一种类似基于某个服务的通信协议
 * 一种类似基于某个服务的通信协议
 * 客户端： 知道这个协议的存在
 * 服务端： 提供给协议的实现
 */
public interface SomeService {

	public long versionID = Long.MAX_VALUE;

	public String heartBeat(String name);
}
