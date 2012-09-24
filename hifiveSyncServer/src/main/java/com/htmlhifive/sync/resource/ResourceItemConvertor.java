package com.htmlhifive.sync.resource;

public interface ResourceItemConvertor<T> {

	T convert(Object itemObj, Class<T> to);
}
