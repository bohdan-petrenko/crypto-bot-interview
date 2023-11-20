package com.petrenko.bohdan.crypto.interview.client.rest;

import java.net.SocketTimeoutException;
import java.util.List;

import com.petrenko.bohdan.crypto.interview.exception.CurrencyRatesUnavailableException;
import com.petrenko.bohdan.crypto.interview.model.CurrencyRate;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class CurrencyRatesRestClient {
	@Value("${currency.rates.api.retries.count}")
	private int currencyRatesApiRetriesCount;
	@Value("${currency.rates.api.url}")
	private String currencyRatesApiUrl;
	@Value("${currency.rates.api.method}")
	private String currencyRatesApiMethod;
	@Autowired
	private RestTemplate restTemplate;

	public List<CurrencyRate> loadCurrencyRates() {
		HttpMethod httpMethod = HttpMethod.valueOf(currencyRatesApiMethod.toUpperCase());
		ParameterizedTypeReference<List<CurrencyRate>> responseType = new ParameterizedTypeReference<>() {
		};
		return exchangeWithHandledErrors(currencyRatesApiUrl, httpMethod, responseType);
	}

	private <T> T exchangeWithHandledErrors(String url, HttpMethod method, ParameterizedTypeReference<T> responseType) {
		int allowedRetries = currencyRatesApiRetriesCount;
		ResponseEntity<T> responseEntity = null;
		for (int retry = 0; retry < allowedRetries; retry++) {
			try {
				responseEntity = restTemplate.exchange(url, method, null, responseType);
				break;
			} catch (Exception exception) {
				if (exception instanceof ResourceAccessException
						&& exception.getCause() instanceof SocketTimeoutException) {
					log.warn("{} Retry of {} for {} call to {} due to SocketTimeoutException",
							retry, allowedRetries, method, url);
				} else {
					log.error("{} Retry of {} for {} call to {} failed with exception",
							retry, allowedRetries, method, url, exception);
					throw new CurrencyRatesUnavailableException("Unhandled exception while get currency rates", exception);
				}
			}
		}
		if (responseEntity.getStatusCode().isError()) {
			log.error("{} Request {} failed with status {} and body {}", method, url, responseEntity.getStatusCode(), responseEntity.getBody());
			throw new CurrencyRatesUnavailableException("Currency rates retrieval failed with unhandled status code " + responseEntity.getStatusCode() , null);
		}
		return responseEntity.getBody();
	}
}
