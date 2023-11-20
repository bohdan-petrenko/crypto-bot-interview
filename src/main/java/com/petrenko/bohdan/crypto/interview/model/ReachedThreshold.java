package com.petrenko.bohdan.crypto.interview.model;

import lombok.Value;

@Value
public class ReachedThreshold {
	CurrencyRate rateAtStart;
	CurrencyRate rateAtTheMoment;
	int differencePercent;
}
