package com.petrenko.bohdan.crypto.interview.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.petrenko.bohdan.crypto.interview.client.rest.CurrencyRatesRestClient;
import com.petrenko.bohdan.crypto.interview.dao.UserStateRepository;
import com.petrenko.bohdan.crypto.interview.dao.converter.UserStateConverter;
import com.petrenko.bohdan.crypto.interview.model.CurrencyRate;
import com.petrenko.bohdan.crypto.interview.model.ReachedThreshold;
import com.petrenko.bohdan.crypto.interview.model.ReachedThresholdsForUser;
import com.petrenko.bohdan.crypto.interview.model.UserState;
import com.petrenko.bohdan.crypto.interview.telegram.CurrencyRateUpdateBot;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@NoArgsConstructor
@AllArgsConstructor
public class UserNotifier {
	@Value("${application.rate.change.threshold.percent}")
	private int rateChangeThresholdPercent;
	@Autowired UserStateRepository userStateRepository;
	@Autowired UserStateConverter userStateConverter;
	@Autowired CurrencyRatesRestClient currencyRatesRestClient;
	@Autowired CurrencyRateUpdateBot currencyRateUpdateBot;

	@Scheduled(fixedDelayString = "${application.refresh.duration.millis}")
	public void notifyUsers() {
		List<CurrencyRate> currencyRates = currencyRatesRestClient.loadCurrencyRates();
		List<UserState> userStates = StreamSupport.stream(userStateRepository.findAll().spliterator(), true)
				.map(userStateConverter::toModel)
				.collect(Collectors.toList());
		List<ReachedThresholdsForUser> usersWithReachedThresholds = userStates.stream()
				.map(userState -> thresholdsIfReached(currencyRates, userState))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(Collectors.toList());
		usersWithReachedThresholds.forEach(this::notifyUser);
	}

	Optional<ReachedThresholdsForUser> thresholdsIfReached(List<CurrencyRate> currencyRatesAtTheMoment, UserState userState) {
		Map<String, CurrencyRate> ratesAtTheMoment = currencyRatesAtTheMoment.stream()
				.collect(Collectors.toMap(CurrencyRate::getSymbol, Function.identity()));
		Map<String, CurrencyRate> ratesAtStart = userState.getCurrencyRatesAtStart().stream()
				.collect(Collectors.toMap(CurrencyRate::getSymbol, Function.identity()));
		List<ReachedThreshold> reachedThresholds = ratesAtTheMoment.keySet().stream()
				.map(symbol -> thresholdIfReached(ratesAtTheMoment.get(symbol), ratesAtStart.get(symbol)))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(Collectors.toList());
		return Optional.of(reachedThresholds)
				.filter(Predicate.not(List::isEmpty))
				.map(thresholds -> new ReachedThresholdsForUser(userState.getTelegramChatId(), thresholds));
	}

	Optional<ReachedThreshold> thresholdIfReached(CurrencyRate rateAtTheMoment, CurrencyRate rateAtStart) {
		double priceAtTheMoment = rateAtTheMoment.getPrice();
		double priceAtStart = rateAtStart.getPrice();
		double difference = priceAtTheMoment - priceAtStart;
		return Optional.of(difference / priceAtStart)
				.map(percentage -> percentage * 100)
				.map(Math::abs)
				.map(Math::round)
				.map(Long::intValue)
				.filter(percentage -> percentage >= rateChangeThresholdPercent)
				.map(differencePercent -> new ReachedThreshold(rateAtStart, rateAtTheMoment, differencePercent));
	}

	private void notifyUser(ReachedThresholdsForUser reachedThresholdsForUser) {
		long chatId = reachedThresholdsForUser.getTelegramChatId();
		for (ReachedThreshold threshold : reachedThresholdsForUser.getReachedThresholds()) {
			currencyRateUpdateBot.sendMessage(chatId, buildMessage(threshold));
		}
	}

	private String buildMessage(ReachedThreshold threshold) {
		return String.format("For currency %s price changed in %s%% from %s to %s",
				threshold.getRateAtStart().getSymbol(),
				threshold.getDifferencePercent(),
				threshold.getRateAtStart().getPrice(),
				threshold.getRateAtTheMoment().getPrice());
	}
}
