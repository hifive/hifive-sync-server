package com.htmlhifive.sync.resource;

import org.springframework.stereotype.Service;

import com.htmlhifive.sync.commondata.CommonData;
import com.htmlhifive.sync.exception.ConflictException;

/**
 * ロックエラー発生時の更新戦略、競合判定ロジックの実装.<br>
 * 更新を行わずクライアントに解決を求めます.<br>
 *
 * @author kishigam
 */
@Service
public class ClientResolvingStrategy implements UpdateStrategy {

	/**
	 * ConflictExceptionをスローし、クライアントにサーバ状態を返します.
	 */
	@Override
	public <E> E resolveConflict(SyncRequestHeader requestHeader, E clientElement, CommonData common, E serverElement)
			throws ConflictException {

		throw new ConflictException("resource element has updated.", new SyncResponse<>(common, serverElement));
	}
}
