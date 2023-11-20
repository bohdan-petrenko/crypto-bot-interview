package com.petrenko.bohdan.crypto.interview.dao.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(name="user_state")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserStateEntity {
	@Id
	@Column(name="telegram_chat_id", nullable=false, unique = true)
	private long telegramChatId;
	@Column(name="start_time_utc", nullable=false)
	private long startTime;
	@Column(name="start_currency_rates", length=160_000, nullable=false)
	private String currencyRatesAtStartJson;
}
