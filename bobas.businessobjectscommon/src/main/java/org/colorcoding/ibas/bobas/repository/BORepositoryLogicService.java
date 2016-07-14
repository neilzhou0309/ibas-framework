package org.colorcoding.ibas.bobas.repository;

import org.colorcoding.ibas.bobas.MyConfiguration;
import org.colorcoding.ibas.bobas.approval.ApprovalException;
import org.colorcoding.ibas.bobas.approval.ApprovalFactory;
import org.colorcoding.ibas.bobas.approval.IApprovalData;
import org.colorcoding.ibas.bobas.approval.IApprovalProcess;
import org.colorcoding.ibas.bobas.approval.IApprovalProcessManager;
import org.colorcoding.ibas.bobas.bo.IBOStorageTag;
import org.colorcoding.ibas.bobas.core.IBusinessObjectBase;
import org.colorcoding.ibas.bobas.core.SaveActionsEvent;
import org.colorcoding.ibas.bobas.core.SaveActionsType;
import org.colorcoding.ibas.bobas.logics.BusinessLogicsFactory;
import org.colorcoding.ibas.bobas.logics.IBusinessLogicsChain;
import org.colorcoding.ibas.bobas.logics.IBusinessLogicsManager;

/**
 * 业务仓库服务，带业务逻辑处理
 * 
 * 
 * @author niuren.zhu
 *
 */
public class BORepositoryLogicService extends BORepositoryService {

	@Override
	public boolean actionsNotification(SaveActionsEvent event) {
		try {
			// 审批流程相关
			if (event.getType() == SaveActionsType.before_adding || event.getType() == SaveActionsType.before_deleting
					|| event.getType() == SaveActionsType.before_updating) {
				// 先执行审批逻辑，可能对bo的状态有影响
				if (!MyConfiguration.getConfigValue(MyConfiguration.CONFIG_ITEM_BO_DISABLED_BUSINESS_APPROVAL, false)) {
					// 触发审批流程
					this.triggerApprovals(event.getBO());
				}
			}
			// 业务逻辑相关
			if (event.getType() != SaveActionsType.before_adding && event.getType() != SaveActionsType.updated) {
				// 最后执行业务逻辑，因为要求状态可用
				if (!MyConfiguration.getConfigValue(MyConfiguration.CONFIG_ITEM_BO_DISABLED_BUSINESS_LOGICS, false)) {
					// 执行业务逻辑
					this.runLogics(event.getType(), event.getBO());
				}

			}
			// 运行基类方法
			return super.actionsNotification(event);
		} catch (Exception e) {
			throw new ActionsEventException(e);
		}
	}

	/**
	 * 触发审批流程
	 * 
	 * @param type
	 *            操作类型
	 * @param bo
	 *            业务数据
	 * @throws ApprovalException
	 */
	private void triggerApprovals(IBusinessObjectBase bo) throws ApprovalException {
		if (!(bo instanceof IApprovalData)) {
			// 业务对象不是需要审批的数据，退出处理
			return;
		}
		IApprovalProcessManager apManager = ApprovalFactory.createManager();
		IApprovalProcess approvalProcess = apManager.checkProcess((IApprovalData) bo);
		if (approvalProcess != null) {
			// 创建了流程实例
			// 保存流程实例，使用当前仓库以保证事务完整
			approvalProcess.checkToSave(this.getCurrentUser());// 检查用户是否有权限保存
			approvalProcess.save(this.getRepository());
		}
	}

	/**
	 * 执行业务逻辑
	 * 
	 * @param type
	 *            操作类型
	 * @param bo
	 *            业务数据
	 */
	private void runLogics(SaveActionsType type, IBusinessObjectBase bo) {
		String transId = null;// 事务链标记
		if (bo instanceof IBOStorageTag) {
			IBOStorageTag tagBO = (IBOStorageTag) bo;
			transId = bo.isNew() ? tagBO.getCreateActionId() : tagBO.getUpdateActionId();
		}
		IBusinessLogicsManager logicsManager = BusinessLogicsFactory.createManager();
		IBusinessLogicsChain logicsChain = logicsManager.getChain(transId);
		if (logicsChain == null) {
			// 没有已存在的，创建个新的
			logicsChain = logicsManager.createChain(transId);
			// 传递仓库
			logicsChain.useRepository(this.getRepository());
		}
		// 执行逻辑
		if (type == SaveActionsType.added || type == SaveActionsType.updated) {
			// 新建数据，正向逻辑
			logicsChain.forwardLogics(bo);
		} else if (type == SaveActionsType.before_deleting || type == SaveActionsType.before_updating) {
			// 删除更新数据前，反向逻辑
			logicsChain.reverseLogics(bo);
		}
	}
}
