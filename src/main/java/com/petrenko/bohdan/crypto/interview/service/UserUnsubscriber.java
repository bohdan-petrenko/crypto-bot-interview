package com.petrenko.bohdan.crypto.interview.service;

import com.petrenko.bohdan.crypto.interview.dao.UserStateRepository;
import jakarta.transaction.Transactional;
import lombok.SneakyThrows;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserUnsubscriber {
	@Autowired UserStateRepository userStateRepository;

	@SneakyThrows
	@Transactional
	public void unsubscribeUser(long telegramChatId) {
		userStateRepository.deleteById(telegramChatId);
	}
}
