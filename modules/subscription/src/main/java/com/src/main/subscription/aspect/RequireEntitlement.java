package com.src.main.subscription.aspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireEntitlement {
	String featureCode();
	long requestedUnits() default 1L;
	boolean checkQuota() default false;
}
