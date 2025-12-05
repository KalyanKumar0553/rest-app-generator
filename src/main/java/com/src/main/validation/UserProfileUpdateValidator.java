package com.src.main.validation;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.src.main.dto.UserProfileRequestDTO;
import com.src.main.model.UserInfo;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class UserProfileUpdateValidator implements ConstraintValidator<ValidUserProfileUpdate, UserProfileRequestDTO> {

    @Override
    public boolean isValid(UserProfileRequestDTO dto, ConstraintValidatorContext context) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        UserInfo user = (UserInfo) request.getAttribute("AUTH_USER");
        if (user == null) {
            return true; // Cannot validate without user context
        }
        boolean valid = true;
        // email validation based on user’s existing profile
        if (user.getEmail() != null) {
            if (dto.getEmail() == null || dto.getEmail().isEmpty()) {
                addError(context, "email", "Email cannot be blank");
                valid = false;
            } else if (!dto.getEmail().equals(user.getEmail())) {
                addError(context, "email", "User cannot update registered email");
                valid = false;
            }
        }
        return valid;
    }

    private void addError(ConstraintValidatorContext ctx, String field, String message) {
        ctx.disableDefaultConstraintViolation();
        ctx.buildConstraintViolationWithTemplate(message)
                .addPropertyNode(field)
                .addConstraintViolation();
    }
}
