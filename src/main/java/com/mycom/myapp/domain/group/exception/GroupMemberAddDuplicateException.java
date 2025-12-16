package com.mycom.myapp.domain.group.exception;

public class GroupMemberAddDuplicateException extends RuntimeException{
    public GroupMemberAddDuplicateException(String message) {
        super(message);
    }

    public GroupMemberAddDuplicateException(String message, Throwable cause) {
        super(message, cause);
    }
}
