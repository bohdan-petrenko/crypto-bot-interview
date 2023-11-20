package com.petrenko.bohdan.crypto.interview.model;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UserState {
	long telegramChatId;
	LocalDateTime startTime;
	List<CurrencyRate> currencyRatesAtStart;
}
