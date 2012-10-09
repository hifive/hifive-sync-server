package com.htmlhifive.sync.resource;

import java.util.List;

import javax.annotation.Resource;
import javax.persistence.PessimisticLockException;

import org.springframework.stereotype.Service;

import com.htmlhifive.sync.common.ResourceItemCommonData;
import com.htmlhifive.sync.common.ResourceItemCommonLockData;
import com.htmlhifive.sync.common.ResourceItemCommonLockDataRepository;
import com.htmlhifive.sync.exception.LockException;
import com.htmlhifive.sync.service.SyncCommonData;

/**
 * リソースアイテム共通データの共有ロックのみ行うロック戦略実装.<br>
 * TODO 次期バージョンにて実装予定
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
	public void lock(SyncCommonData syncCommon, ResourceItemCommonData itemCommon, ResourceLockStatusType lockStatus)
			throws LockException {

		try {

			ResourceItemCommonLockData lockData = repository.findByIdAndStatus(itemCommon.getId(), lockStatus);

			if (lockData == null) {

				lockData = new ResourceItemCommonLockData();
				lockData.setToken(syncCommon.getLockToken());
			}

			if (lockData == null || lockData.getToken().equals(syncCommon.getLockToken())) {

				// ロック可能
				lockData.setLimitOfTime(syncCommon.getSyncTime());
				lockData.setStatus(lockStatus);
				lockData.setId(itemCommon.getId());

				repository.save(lockData);
			}

			throw new LockException("locked by : " + lockData.getToken());

		} catch (PessimisticLockException e) {
			throw new LockException(e);
		}

	}

	@Override
	public void lock(SyncCommonData syncCommon, List<ResourceItemCommonData> itemCommonList,
			ResourceLockStatusType lockStatus) throws LockException {

	}

	@Override
	public List<ResourceItemCommonData> lock(SyncCommonData syncCommon, List<ResourceItemCommonData> commonDataList,
			List<ResourceQueryConditions> query, ResourceLockStatusType lockStatus) throws LockException {
		return null;
	}

	@Override
	public ResourceLockStatusType getCurrentlockStatus(SyncCommonData syncCommon, ResourceItemCommonData commonData) {
		return ResourceLockStatusType.UNLOCK;
	}

	@Override
	public void unlock(SyncCommonData syncCommon, ResourceItemCommonData commonData) {
	}
}
