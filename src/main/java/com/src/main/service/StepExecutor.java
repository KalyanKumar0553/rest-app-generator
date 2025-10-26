package com.src.main.service;


import org.springframework.statemachine.ExtendedState;

import com.src.main.dto.StepResult;

public interface StepExecutor {
    StepResult execute(ExtendedState data) throws Exception;
}