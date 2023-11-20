package com.petrenko.bohdan.crypto.interview.exception;

//todo create base application exception and extend it instead of RuntimeException
public class SubscribedUsersLimitExceededException extends RuntimeException {
	public SubscribedUsersLimitExceededException(int limit) {
		super("Exceeded subscribed users limit " + limit);
	}
}
