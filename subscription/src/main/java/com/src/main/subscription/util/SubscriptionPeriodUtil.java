package com.src.main.subscription.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;

import com.src.main.subscription.entity.SubscriptionFeatureEntity;
import com.src.main.subscription.enums.ResetPolicy;

public final class SubscriptionPeriodUtil {

	public record PeriodWindow(String key, LocalDateTime start, LocalDateTime end) {
	}

	private SubscriptionPeriodUtil() {
	}

	public static PeriodWindow resolveWindow(SubscriptionFeatureEntity feature, LocalDateTime now) {
		ResetPolicy policy = feature.getResetPolicy() == null ? ResetPolicy.NEVER : feature.getResetPolicy();
		LocalDate date = now.toLocalDate();
		return switch (policy) {
			case MONTHLY -> {
				LocalDate start = date.withDayOfMonth(1);
				LocalDate end = start.plusMonths(1).minusDays(1);
				yield new PeriodWindow(
						"%04d-%02d".formatted(start.getYear(), start.getMonthValue()),
						start.atStartOfDay(),
						end.atTime(23, 59, 59));
			}
			case HALF_YEARLY -> {
				int half = date.getMonthValue() <= 6 ? 1 : 2;
				Month startMonth = half == 1 ? Month.JANUARY : Month.JULY;
				LocalDate start = LocalDate.of(date.getYear(), startMonth, 1);
				LocalDate end = start.plusMonths(6).minusDays(1);
				yield new PeriodWindow(
						date.getYear() + "-H" + half,
						start.atStartOfDay(),
						end.atTime(23, 59, 59));
			}
			case YEARLY -> {
				LocalDate start = date.withDayOfYear(1);
				LocalDate end = start.plusYears(1).minusDays(1);
				yield new PeriodWindow(
						String.valueOf(start.getYear()),
						start.atStartOfDay(),
						end.atTime(23, 59, 59));
			}
			case NEVER -> new PeriodWindow("LIFETIME", LocalDate.of(1970, 1, 1).atStartOfDay(), LocalDateTime.of(9999, 12, 31, 23, 59, 59));
		};
	}
}
