package com.htmlhifive.sync.resource;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.persistence.PessimisticLockException;

import org.springframework.stereotype.Service;

import com.htmlhifive.sync.common.ResourceItemCommonData;
import com.htmlhifive.sync.common.ResourceItemCommonLockData;
import com.htmlhifive.sync.common.ResourceItemCommonLockDataRepository;
import com.htmlhifive.sync.exception.LockException;
import com.htmlhifive.sync.service.SyncCommonData;

/**
 * リソースアイテム共通データの共有ロックのみ行うロック戦略実装.
 *
 * @author kishigam
 */
@Service
public class ResourceItemCommonLockStrategy implements LockStrategy {

	/**
	 * リソースアイテム共通ロックデータのリポジトリ.
	 */
	@Resource
	private ResourceItemCommonLockDataRepository repository;

	@Override
	public <T> void lock(SyncCommonData syncCommon, ResourceItemCommonData itemCommon, T item,
			ResourceLockModeType lockMode) throws LockException {

		// TODO 自動生成されたメソッド・スタブ
		try {

			ResourceItemCommonLockData lockData = repository.findByIdAndLockMode(itemCommon.getId(), lockMode);

			if (lockData == null) {

				lockData = new ResourceItemCommonLockData();
				lockData.setStorageId(syncCommon.getStorageId());
			}

			if (lockData == null || lockData.getStorageId().equals(syncCommon.getStorageId())) {

				// ロック可能
				lockData.setLockedTime(syncCommon.getSyncTime());
				lockData.setLockMode(lockMode);
				lockData.setId(itemCommon.getId());

				repository.save(lockData);
			}

			throw new LockException("locked by : " + lockData.getStorageId());

		} catch (PessimisticLockException e) {
			throw new LockException(e);
		}

	}

	@Override
	public <T> void lock(SyncCommonData syncCommon, Map<T, ResourceItemCommonData> itemMap,
			ResourceLockModeType lockMode) throws LockException {
		// TODO 自動生成されたメソッド・スタブ

	}

	@Override
	public <T> List<ResourceItemCommonData> lock(SyncCommonData syncCommon,
			List<ResourceItemCommonData> commonDataList, List<ResourceQueryConditions> query,
			ResourceLockModeType lockMode) throws LockException {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public <T> boolean isLocked(SyncCommonData syncCommon, ResourceItemCommonData commonData, T item,
			ResourceLockModeType lockMode) {
		// TODO 自動生成されたメソッド・スタブ
		return false;
	}

	@Override
	public <T> boolean canRead(SyncCommonData syncCommon, ResourceItemCommonData commonData, T item) {
		// TODO 自動生成されたメソッド・スタブ
		return false;
	}

	@Override
	public <T> boolean canWrite(SyncCommonData syncCommon, ResourceItemCommonData commonData, T item) {
		// TODO 自動生成されたメソッド・スタブ
		return false;
	}

	@Override
	public <T> void unlock(SyncCommonData syncCommon, ResourceItemCommonData commonData, T item) {
		// TODO 自動生成されたメソッド・スタブ

	}
}
