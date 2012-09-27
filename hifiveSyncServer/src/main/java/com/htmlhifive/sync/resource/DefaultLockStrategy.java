package com.htmlhifive.sync.resource;

import com.htmlhifive.sync.common.ResourceItemCommonData;
import com.htmlhifive.sync.exception.LockException;

public class DefaultLockStrategy implements LockStrategy {

	@Override
	public boolean lock(ResourceItemCommonData commonData) throws LockException {
		// TODO 自動生成されたメソッド・スタブ
		return false;
	}

	@Override
	public boolean isLocked() {
		// TODO 自動生成されたメソッド・スタブ
		return false;
	}

	@Override
	public void unlock(ResourceItemCommonData commonData) {
		// TODO 自動生成されたメソッド・スタブ

	}

}
