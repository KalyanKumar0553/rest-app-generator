package com.src.main.subscription.service.impl;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.src.main.subscription.dto.SubscriptionAuditCommand;
import com.src.main.subscription.dto.SubscriptionAuditResponse;
import com.src.main.subscription.entity.SubscriptionAuditLogEntity;
import com.src.main.subscription.repository.SubscriptionAuditLogRepository;
import com.src.main.subscription.service.SubscriptionAuditService;
import com.src.main.subscription.util.SubscriptionMapperUtil;

@Service
public class SubscriptionAuditServiceImpl implements SubscriptionAuditService {
	private final SubscriptionAuditLogRepository auditLogRepository;

	@Override
	@Transactional
	public void logEvent(SubscriptionAuditCommand command) {
		SubscriptionAuditLogEntity entity = new SubscriptionAuditLogEntity();
		entity.setTenantId(command.getTenantId());
		entity.setSubscription(command.getSubscription());
		entity.setEventType(command.getEventType());
		entity.setOldPlan(command.getOldPlan());
		entity.setNewPlan(command.getNewPlan());
		entity.setOldStatus(command.getOldStatus());
		entity.setNewStatus(command.getNewStatus());
		entity.setActorType(command.getActorType());
		entity.setActorId(command.getActorId());
		entity.setReason(command.getReason());
		entity.setPayloadJson(command.getPayloadJson());
		auditLogRepository.save(entity);
	}

	@Override
	@Transactional(readOnly = true)
	public List<SubscriptionAuditResponse> getAuditHistory(Long tenantId) {
		return auditLogRepository.findAllByTenantIdOrderByCreatedAtDesc(tenantId).stream().map(SubscriptionMapperUtil::toAuditResponse).toList();
	}

	public SubscriptionAuditServiceImpl(final SubscriptionAuditLogRepository auditLogRepository) {
		this.auditLogRepository = auditLogRepository;
	}
}
