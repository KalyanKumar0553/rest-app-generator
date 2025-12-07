package com.src.main.exception;

import com.src.main.util.RequestStatus;

public class ProjectFetchException extends AbstractRuntimeException {

	public ProjectFetchException(RequestStatus error,Object... msgParams) {
        super(error.getCode(),error.getDescription(msgParams));
    }
}
