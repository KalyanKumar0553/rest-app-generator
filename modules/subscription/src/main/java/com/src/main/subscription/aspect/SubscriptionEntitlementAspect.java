package com.src.main.subscription.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import com.src.main.subscription.exception.FeatureNotAvailableException;
import com.src.main.subscription.exception.QuotaExceededException;
import com.src.main.subscription.security.SubscriptionTenantResolver;
import com.src.main.subscription.service.EntitlementService;
import com.src.main.subscription.service.UsageTrackingService;

@Aspect
@Component
@ConditionalOnProperty(prefix = "app.subscription", name = "aspect-enabled", havingValue = "true", matchIfMissing = true)
public class SubscriptionEntitlementAspect {
	private final SubscriptionTenantResolver tenantResolver;
	private final EntitlementService entitlementService;
	private final UsageTrackingService usageTrackingService;

	@Around("@annotation(requireFeature)")
	public Object requireFeature(ProceedingJoinPoint joinPoint, RequireFeature requireFeature) throws Throwable {
		Long tenantId = tenantResolver.resolveRequiredTenantId();
		if (!entitlementService.hasFeature(tenantId, requireFeature.value())) {
			throw new FeatureNotAvailableException(requireFeature.value());
		}
		return joinPoint.proceed();
	}

	@Around("@annotation(requireEntitlement)")
	public Object requireEntitlement(ProceedingJoinPoint joinPoint, RequireEntitlement requireEntitlement) throws Throwable {
		Long tenantId = tenantResolver.resolveRequiredTenantId();
		if (!entitlementService.hasFeature(tenantId, requireEntitlement.featureCode())) {
			throw new FeatureNotAvailableException(requireEntitlement.featureCode());
		}
		if (requireEntitlement.checkQuota() && !usageTrackingService.hasRemainingQuota(tenantId, requireEntitlement.featureCode(), requireEntitlement.requestedUnits())) {
			throw new QuotaExceededException(requireEntitlement.featureCode());
		}
		return joinPoint.proceed();
	}

	public SubscriptionEntitlementAspect(final SubscriptionTenantResolver tenantResolver, final EntitlementService entitlementService, final UsageTrackingService usageTrackingService) {
		this.tenantResolver = tenantResolver;
		this.entitlementService = entitlementService;
		this.usageTrackingService = usageTrackingService;
	}
}
