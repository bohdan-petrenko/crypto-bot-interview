package com.petrenko.bohdan.crypto.interview.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petrenko.bohdan.crypto.interview.client.rest.CacheableCurrencyRatesRestClient;
import com.petrenko.bohdan.crypto.interview.dao.UserStateRepository;
import com.petrenko.bohdan.crypto.interview.dao.entity.UserStateEntity;
import com.petrenko.bohdan.crypto.interview.exception.SubscribedUsersLimitExceededException;
import com.petrenko.bohdan.crypto.interview.exception.UserAlreadySubscribedException;
import jakarta.transaction.Transactional;
import lombok.SneakyThrows;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class UserSubscriber {
	@Value("${application.max.active.users}")
	private int maxActiveUsers;
	@Autowired UserStateRepository userStateRepository;
	@Autowired CacheableCurrencyRatesRestClient cacheableCurrencyRatesRestClient;
	@Autowired ObjectMapper objectMapper;

	@SneakyThrows
	@Transactional
	public void subscribeUser(long telegramChatId) {
		if (userStateRepository.count() > maxActiveUsers) {
			throw new SubscribedUsersLimitExceededException(maxActiveUsers);
		}
		if (userStateRepository.existsById(telegramChatId)) {
			throw new UserAlreadySubscribedException(telegramChatId);
		}
		UserStateEntity userStateEntity = new UserStateEntity();
		userStateEntity.setTelegramChatId(telegramChatId);
		userStateEntity.setStartTime(System.currentTimeMillis());
		String currencyRatesJson = objectMapper.writeValueAsString(cacheableCurrencyRatesRestClient.cachedCurrencyRates());
		userStateEntity.setCurrencyRatesAtStartJson(currencyRatesJson);
		userStateRepository.save(userStateEntity);
	}
}
