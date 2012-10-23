package com.htmlhifive.sync.resource.lock;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.htmlhifive.sync.exception.LockException;
import com.htmlhifive.sync.resource.ResourceQueryConditions;
import com.htmlhifive.sync.resource.common.ResourceItemCommonData;
import com.htmlhifive.sync.resource.common.ResourceItemCommonLockData;
import com.htmlhifive.sync.resource.common.ResourceItemCommonLockDataRepository;
import com.htmlhifive.sync.service.SyncCommonData;

/**
 * リソースアイテム共通データの共有ロックのみ行うロック戦略実装.<br>
 * TODO 次期バージョンにて実装予定
 *
 * @author kishigam
 */
@SuppressWarnings("deprecation")
@Deprecated
@Service
public class ResourceItemCommonLockStrategy implements LockStrategy {

	/**
	 * リソースアイテム共通ロックデータのリポジトリ.<br>
	 * TODO: 次期バージョンにて実装予定
	 */
	@SuppressWarnings("unused")
	@Resource
	private ResourceItemCommonLockDataRepository repository;

	/**
	 * リソースアイテムの現在のロック状態をチェックし、自身以外のロックトークンで排他ロックされている場合{@link LockException}をスローします.<br>
	 *
	 * @param syncCommon 同期共通データ
	 * @param itemCommon リソースアイテム共通データ
	 * @return リソースアイテム共通ロックデータ
	 */
	@Override
	public void checkReadLockStatus(SyncCommonData syncCommon, ResourceItemCommonData itemCommon) {
		// TODO 自動生成されたメソッド・スタブ

	}

	/**
	 * リソースアイテムの現在のロック状態をチェックし、自身のロックトークンで指定されたロック状態となっていない場合{@link LockException}をスローします.<br>
	 *
	 * @param syncCommon 同期共通データ
	 * @param itemCommon リソースアイテム共通データ
	 * @param required チェックするロック状態タイプ
	 * @return リソースアイテム共通ロックデータ
	 */
	@Override
	public void checkWriteLockStatus(SyncCommonData syncCommon, ResourceItemCommonData itemCommon,
			ResourceLockStatusType required) {
		// TODO 自動生成されたメソッド・スタブ

	}

	/**
	 * 1件のリソースアイテムを指定されたロック状態でロックします.
	 *
	 * @param syncCommon 同期共通データ
	 * @param itemCommon リソースアイテム共通データ
	 * @param lockStatus ロック状態
	 * @return リソースアイテム共通ロックデータ
	 * @throws LockException ロックが取得できなかったとき
	 */
	@Override
	public ResourceItemCommonLockData lock(SyncCommonData syncCommon, ResourceItemCommonData itemCommon,
			ResourceLockStatusType lockStatus) throws LockException {

		// TODO: 次期バージョンにて実装予定
		return null;
	}

	/**
	 * Mapで指定されたすべてのリソースアイテムを指定されたロック状態でロックします.
	 *
	 * @param syncCommon 同期共通データ
	 * @param itemCommonList リソースアイテム共通データのリスト
	 * @param lockStatus ロック状態
	 * @return リソースアイテム共通ロックデータ
	 * @throws LockException ロックが取得できなかったとき
	 */
	@Override
	public ResourceItemCommonLockData lock(SyncCommonData syncCommon, List<ResourceItemCommonData> itemCommonList,
			ResourceLockStatusType lockStatus) throws LockException {

		// TODO: 次期バージョンにて実装予定
		return null;
	}

	/**
	 * リソースアイテム共通データのリストに含まれる対象アイテムのうち、クエリの条件を満たすリソースアイテムを、指定されたロック状態でロックします.
	 *
	 * @param syncCommon 同期共通データ
	 * @param itemCommonList リソースアイテム共通データのリスト
	 * @param query クエリ
	 * @param lockStatus ロック状態
	 * @return クエリの条件を満たすリソースアイテム共通ロックデータのリスト
	 * @throws LockException ロックが取得できなかったとき
	 */
	@Override
	public List<ResourceItemCommonLockData> lock(SyncCommonData syncCommon,
			List<ResourceItemCommonData> itemCommonList, List<ResourceQueryConditions> query,
			ResourceLockStatusType lockStatus) throws LockException {

		// TODO: 次期バージョンにて実装予定
		return null;
	}

	/**
	 * 指定されたリソースアイテムのロックを開放します.<br>
	 * ロック状態は{@link ResourceLockStatusType#UNLOCK} になります.
	 *
	 * @param syncCommon 同期共通データ
	 * @param itemCommon リソースアイテム共通データ
	 */
	@Override
	public void unlock(SyncCommonData syncCommon, ResourceItemCommonData commonData) {

		// TODO: 次期バージョンにて実装予定
	}
}
