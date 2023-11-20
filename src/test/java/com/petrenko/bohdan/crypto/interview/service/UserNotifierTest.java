package com.petrenko.bohdan.crypto.interview.service;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.petrenko.bohdan.crypto.interview.client.rest.CurrencyRatesRestClient;
import com.petrenko.bohdan.crypto.interview.dao.UserStateRepository;
import com.petrenko.bohdan.crypto.interview.dao.converter.UserStateConverter;
import com.petrenko.bohdan.crypto.interview.model.CurrencyRate;
import com.petrenko.bohdan.crypto.interview.model.ReachedThreshold;
import com.petrenko.bohdan.crypto.interview.model.ReachedThresholdsForUser;
import com.petrenko.bohdan.crypto.interview.model.UserState;
import com.petrenko.bohdan.crypto.interview.telegram.CurrencyRateUpdateBot;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

public class UserNotifierTest {
	private int rateChangeThresholdPercent = 20;
	@Mock
	UserStateRepository userStateRepository;
	@Mock
	UserStateConverter userStateConverter;
	@Mock
	CurrencyRatesRestClient currencyRatesRestClient;
	@Mock
	CurrencyRateUpdateBot currencyRateUpdateBot;

	private UserNotifier target;

	@BeforeEach
	public void beforeEach() {
		target = new UserNotifier(rateChangeThresholdPercent, userStateRepository, userStateConverter, currencyRatesRestClient, currencyRateUpdateBot);
	}

	@Test
	public void testThresholdReachedFor20PercentDecrease() {
		String symbol = "USD/UAH";
		CurrencyRate rateAtTheMoment = new CurrencyRate(symbol, 80);
		CurrencyRate rateAtStart = new CurrencyRate(symbol, 100);

		Optional<ReachedThreshold> actual = target.thresholdIfReached(rateAtTheMoment, rateAtStart);

		ReachedThreshold expected = new ReachedThreshold(rateAtStart, rateAtTheMoment, 20);
		Assertions.assertTrue(actual.isPresent());
		Assertions.assertEquals(expected, actual.get());
	}

	@Test
	public void testThresholdReachedFor20PercentIncrease() {
		String symbol = "USD/UAH";
		CurrencyRate rateAtTheMoment = new CurrencyRate(symbol, 120);
		CurrencyRate rateAtStart = new CurrencyRate(symbol, 100);

		Optional<ReachedThreshold> actual = target.thresholdIfReached(rateAtTheMoment, rateAtStart);

		ReachedThreshold expected = new ReachedThreshold(rateAtStart, rateAtTheMoment, 20);
		Assertions.assertTrue(actual.isPresent());
		Assertions.assertEquals(expected, actual.get());
	}

	@Test
	public void testThresholdReachedForHugeIncrease() {
		String symbol = "USD/UAH";
		CurrencyRate rateAtTheMoment = new CurrencyRate(symbol, 120_000_000);
		CurrencyRate rateAtStart = new CurrencyRate(symbol, 100);

		Optional<ReachedThreshold> actual = target.thresholdIfReached(rateAtTheMoment, rateAtStart);

		ReachedThreshold expected = new ReachedThreshold(rateAtStart, rateAtTheMoment, 119_999_900);
		Assertions.assertTrue(actual.isPresent());
		Assertions.assertEquals(expected, actual.get());
	}

	@Test
	public void testThresholdReachedForHugeDecrease() {
		String symbol = "USD/UAH";
		CurrencyRate rateAtTheMoment = new CurrencyRate(symbol, 1);
		CurrencyRate rateAtStart = new CurrencyRate(symbol, 100);

		Optional<ReachedThreshold> actual = target.thresholdIfReached(rateAtTheMoment, rateAtStart);

		ReachedThreshold expected = new ReachedThreshold(rateAtStart, rateAtTheMoment, 99);
		Assertions.assertTrue(actual.isPresent());
		Assertions.assertEquals(expected, actual.get());
	}

	@Test
	public void testThresholdNotReachedForDecrease() {
		String symbol = "USD/UAH";
		CurrencyRate rateAtTheMoment = new CurrencyRate(symbol, 90);
		CurrencyRate rateAtStart = new CurrencyRate(symbol, 100);

		Optional<ReachedThreshold> actual = target.thresholdIfReached(rateAtTheMoment, rateAtStart);

		Assertions.assertTrue(actual.isEmpty());
	}

	@Test
	public void testThresholdNotReachedForIncrease() {
		String symbol = "USD/UAH";
		CurrencyRate rateAtTheMoment = new CurrencyRate(symbol, 110);
		CurrencyRate rateAtStart = new CurrencyRate(symbol, 100);

		Optional<ReachedThreshold> actual = target.thresholdIfReached(rateAtTheMoment, rateAtStart);

		Assertions.assertTrue(actual.isEmpty());
	}

	@Test
	public void testThresholdsIsReached() {
		int telegramChatId = 1;
		List<CurrencyRate> currencyRatesAtTheMoment = List.of(
				new CurrencyRate("USD/UAH", 120),
				new CurrencyRate("EUR/UAH", 120)
		);
		UserState userState = UserState.builder()
				.telegramChatId(telegramChatId)
				.startTime(LocalDateTime.MAX)
				.currencyRatesAtStart(List.of(
						new CurrencyRate("USD/UAH", 100),
						new CurrencyRate("EUR/UAH", 100)
				))
				.build();

		Optional<ReachedThresholdsForUser> actual = target.thresholdsIfReached(currencyRatesAtTheMoment, userState);

		ReachedThresholdsForUser expected = new ReachedThresholdsForUser(telegramChatId, List.of(
				new ReachedThreshold(new CurrencyRate("USD/UAH", 100), new CurrencyRate("USD/UAH", 120), 20),
				new ReachedThreshold(new CurrencyRate("EUR/UAH", 100), new CurrencyRate("EUR/UAH", 120), 20)
		));
		Assertions.assertTrue(actual.isPresent());
		Assertions.assertEquals(expected, actual.get());
	}

	@Test
	public void testThresholdsIfNotReached() {
		int telegramChatId = 1;
		List<CurrencyRate> currencyRatesAtTheMoment = List.of(
				new CurrencyRate("USD/UAH", 110),
				new CurrencyRate("EUR/UAH", 110)
		);
		UserState userState = UserState.builder()
				.telegramChatId(telegramChatId)
				.startTime(LocalDateTime.MAX)
				.currencyRatesAtStart(List.of(
						new CurrencyRate("USD/UAH", 100),
						new CurrencyRate("EUR/UAH", 100)
				))
				.build();

		Optional<ReachedThresholdsForUser> actual = target.thresholdsIfReached(currencyRatesAtTheMoment, userState);

		Assertions.assertTrue(actual.isEmpty());
	}
}
