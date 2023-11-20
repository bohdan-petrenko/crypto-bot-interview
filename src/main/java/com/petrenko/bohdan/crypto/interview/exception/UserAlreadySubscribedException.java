package com.petrenko.bohdan.crypto.interview.exception;

public class UserAlreadySubscribedException extends RuntimeException {
	public UserAlreadySubscribedException(long telegramChatId) {
		super("User already subscribed " + telegramChatId);
	}
}
