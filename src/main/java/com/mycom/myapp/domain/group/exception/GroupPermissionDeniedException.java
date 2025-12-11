package com.mycom.myapp.domain.group.exception;

// 그룹 소유자(OWNER)가 아닌 사용자가 그룹을 수정, 삭제하려고 할 때 던지는 예외
public class GroupPermissionDeniedException extends RuntimeException {
    public GroupPermissionDeniedException(String message) {
        super(message);
    }

    public GroupPermissionDeniedException(String message, Throwable cause) {
        super(message, cause);
    }
}
