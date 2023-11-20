package com.petrenko.bohdan.crypto.interview.exception;

public class CurrencyRatesUnavailableException extends RuntimeException {
	public CurrencyRatesUnavailableException(String message, Exception rootCause) {
		super(message, rootCause);
	}
}
