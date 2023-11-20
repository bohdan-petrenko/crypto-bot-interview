package com.petrenko.bohdan.crypto.interview.model;

import java.util.List;

import lombok.Value;

@Value
public class ReachedThresholdsForUser {
	long telegramChatId;
	List<ReachedThreshold> reachedThresholds;
}
