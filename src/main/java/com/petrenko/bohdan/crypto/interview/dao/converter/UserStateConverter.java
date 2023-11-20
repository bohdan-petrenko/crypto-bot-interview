package com.petrenko.bohdan.crypto.interview.dao.converter;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petrenko.bohdan.crypto.interview.dao.entity.UserStateEntity;
import com.petrenko.bohdan.crypto.interview.model.CurrencyRate;
import com.petrenko.bohdan.crypto.interview.model.UserState;
import lombok.SneakyThrows;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserStateConverter {
	@Autowired ObjectMapper objectMapper;

	@SneakyThrows
	public UserState toModel(UserStateEntity entity) {
		return UserState.builder()
				.telegramChatId(entity.getTelegramChatId())
				.startTime(Instant.ofEpochMilli(entity.getStartTime()).atZone(ZoneId.systemDefault()).toLocalDateTime())
				.currencyRatesAtStart(objectMapper.readValue(entity.getCurrencyRatesAtStartJson(), new TypeReference<List<CurrencyRate>>() {}))
				.build();
	}

	@SneakyThrows
	public UserStateEntity toEntity(UserState model) {
		UserStateEntity userStateEntity = new UserStateEntity();
		userStateEntity.setTelegramChatId(model.getTelegramChatId());
		userStateEntity.setStartTime(model.getStartTime().toEpochSecond(ZoneOffset.UTC));
		userStateEntity.setCurrencyRatesAtStartJson(objectMapper.writeValueAsString(model.getCurrencyRatesAtStart()));
		return userStateEntity;
	}
}
