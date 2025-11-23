package com.src.main.service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.src.main.exception.OTPException;
import com.src.main.model.Otp;
import com.src.main.model.OtpAttempt;
import com.src.main.model.UserInfo;
import com.src.main.repository.OtpAttemptRepository;
import com.src.main.repository.OtpRepository;
import com.src.main.repository.UserInfoRepository;
import com.src.main.util.AppUtils;
import com.src.main.util.RequestStatus;

@Service
public class OTPService {
	private static final int MAX_ATTEMPTS = 35;
	private static final int OTP_VALIDITY_MINUTES = 3;

	@Autowired
	private OtpRepository otpRepository;

	@Autowired
	private OtpAttemptRepository otpAttemptRepository;

	@Autowired
	private UserInfoRepository userRepository;

	public boolean canSendOtp(String username) {
		LocalDateTime startOfToday = LocalDateTime.now().toLocalDate().atStartOfDay();
		LocalDateTime endOfToday = LocalDateTime.now().toLocalDate().atTime(LocalTime.MAX);
		Optional<Otp> latestOTP = otpRepository.findFirstByUsernameAndCreatedAtBetweenOrderByCreatedAtDesc(username,startOfToday, endOfToday);
		Optional<OtpAttempt> latestOTPAttempt = otpAttemptRepository.findByUsernameAndCreatedAtBetweenOrderByCreatedAtDesc(username,startOfToday, endOfToday);
		if (latestOTPAttempt.isPresent()) {
			if (latestOTPAttempt.get().getAttempts() >= MAX_ATTEMPTS) {
				throw new OTPException(RequestStatus.OTP_LIMIT_EXCEED_ERROR);
			}

			LocalDateTime otpSentAt = latestOTP.get().getCreatedAt();
			LocalDateTime nextOtpAt = otpSentAt.plusMinutes(OTP_VALIDITY_MINUTES);
			LocalDateTime currentTime =  LocalDateTime.now();
			
			boolean canSendOTP = nextOtpAt.isBefore(currentTime);
			if (!canSendOTP) {
				throw new OTPException(RequestStatus.OTP_TIME_LIMIT_ERROR,AppUtils.formatSecondsToHMString(ChronoUnit.SECONDS.between(currentTime,nextOtpAt)));
			}
		}
		return true;
	}

	public void recordOtpAttempt(String username) {
		LocalDateTime startOfToday = LocalDateTime.now().toLocalDate().atStartOfDay();
		LocalDateTime endOfToday = LocalDateTime.now().toLocalDate().atTime(LocalTime.MAX);
		Optional<OtpAttempt> otpAttemptOpt = otpAttemptRepository.findByUsernameAndCreatedAtBetweenOrderByCreatedAtDesc(username,startOfToday, endOfToday);
		if (otpAttemptOpt.isPresent()) {
			OtpAttempt otpAttempt = otpAttemptOpt.get();
			otpAttempt.setAttempts(otpAttempt.getAttempts() + 1);
			otpAttemptRepository.save(otpAttempt);
		} else {
			OtpAttempt otpAttempt = new OtpAttempt();
			otpAttempt.setUsername(username);
			otpAttempt.setCreatedAt(LocalDateTime.now());
			otpAttempt.setAttempts(1);
			otpAttemptRepository.save(otpAttempt);
		}
	}

	public void saveOtp(String username, String otp) {
		Otp otpEntity = new Otp();
		otpEntity.setUsername(username);
		otpEntity.setOtp(otp);
		otpEntity.setCreatedAt(LocalDateTime.now());
		otpRepository.save(otpEntity);
	}

	public boolean verifyOtp(String username, String otp) {
		Optional<Otp> otpOpt = otpRepository.findFirstByUsernameOrderByCreatedAtDesc(username);
		if (otpOpt.isPresent()) {
			Otp otpEntity = otpOpt.get();
			if (otpEntity.getOtp().equals(otp)) {
				Optional<UserInfo> user = userRepository.findByEmailOrMobile(username, username);
				user.ifPresent(u -> {
					u.setEnabled(true);
					userRepository.save(u);
					otpRepository.deleteAllByUsername(username);
					otpAttemptRepository.deleteAllByUsername(username);
				});
				return true;
			} else {
				throw new OTPException(RequestStatus.OTP_VERIFICATION_FAIL);
			}
		} else {
			throw new RuntimeException();
		}
	}
}
