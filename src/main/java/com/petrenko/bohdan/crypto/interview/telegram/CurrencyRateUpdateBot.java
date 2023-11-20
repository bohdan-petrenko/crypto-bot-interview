package com.petrenko.bohdan.crypto.interview.telegram;

import com.petrenko.bohdan.crypto.interview.exception.SubscribedUsersLimitExceededException;
import com.petrenko.bohdan.crypto.interview.exception.UserAlreadySubscribedException;
import com.petrenko.bohdan.crypto.interview.service.UserSubscriber;
import com.petrenko.bohdan.crypto.interview.service.UserUnsubscriber;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CurrencyRateUpdateBot extends TelegramLongPollingBot {
	@Value("${telegram.bot.username}")
	private String botName;
	@Value("${telegram.bot.token}")
	private String botToken;
	@Value("${application.rate.change.threshold.percent}")
	private int rateChangeThresholdPercent;
	@Autowired
	private UserSubscriber userSubscriber;
	@Autowired
	private UserUnsubscriber userUnsubscriber;


	@Override
	public String getBotUsername() {
		return botName;
	}

	@Override
	public String getBotToken() {
		return botToken;
	}

	@Override
	public void onUpdateReceived(Update update) {
		log.info("Update received");
		if (!update.hasMessage() || !update.getMessage().hasText()) {
			return;
		}
		String messageText = update.getMessage().getText();
		long chatId = update.getMessage().getChatId();

		switch (messageText) {
			case "/start":
				onStart(chatId);
				break;
			case "/stop":
				onStop(chatId);
				break;

			default:
				sendMessage(chatId, "Unknown command");
		}

	}

	private void onStart(Long chatId) {
		String answer;
		try {
			userSubscriber.subscribeUser(chatId);
		} catch (UserAlreadySubscribedException exception) {
			answer = "Subscription failed. You are already subscribed";
		} catch (SubscribedUsersLimitExceededException exception) {
			answer = "Subscription failed. Reached maximum amount of subscribed users. Please wait a bit and try again";
		} catch (Exception exception) {
			answer = "Subscription failed due to internal errors";
		}
		answer = "Subscription successful. Wait for updates when currency rate will be changed more than in "
				+ rateChangeThresholdPercent + "%";

		sendMessage(chatId, answer);
	}

	private void onStop(Long chatId) {
		String answer;
		try {
			userUnsubscriber.unsubscribeUser(chatId);
		} catch (Exception exception) {
			answer = "Unsubscription failed due to internal errors";
		}
		answer = "Unsubscription successful";

		sendMessage(chatId, answer);
	}

	public void sendMessage(Long chatId, String textToSend){
		SendMessage sendMessage = new SendMessage();
		sendMessage.setChatId(String.valueOf(chatId));
		sendMessage.setText(textToSend);
		try {
			execute(sendMessage);
		} catch (TelegramApiException exception) {
			log.error("Can't send message '{}' to telegram chat {}", textToSend, chatId, exception);
		}
	}
}
