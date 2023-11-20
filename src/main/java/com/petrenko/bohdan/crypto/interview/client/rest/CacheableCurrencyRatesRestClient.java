package com.petrenko.bohdan.crypto.interview.client.rest;

import java.util.List;

import com.petrenko.bohdan.crypto.interview.model.CurrencyRate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class CacheableCurrencyRatesRestClient {
	@Autowired
	private CurrencyRatesRestClient currencyRatesRestClient;

	@Cacheable(value = "currency_rates_cache")
	public List<CurrencyRate> cachedCurrencyRates() {
		return currencyRatesRestClient.loadCurrencyRates();
	}
}
