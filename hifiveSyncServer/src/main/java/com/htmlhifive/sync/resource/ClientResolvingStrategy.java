package com.htmlhifive.sync.resource;

import org.springframework.stereotype.Service;

import com.htmlhifive.sync.exception.ItemUpdatedException;

/**
 * 更新を行わず、クライアントに判断を求める更新戦略実装クラス.<br>
 *
 * @author kishigam
 */
@Service
public class ClientResolvingStrategy implements UpdateStrategy {

	/**
	 * 競合を解決せず、ItemUpdateExceptionをスローします.
	 */
	@Override
	public <T> T resolveConflict(ResourceItemWrapper clientItemWrapper, ResourceItemWrapper serverItemWrapper,
			Class<T> resourceItemType) throws ItemUpdatedException {

		throw new ItemUpdatedException("return client to resolve.");
	}
}
