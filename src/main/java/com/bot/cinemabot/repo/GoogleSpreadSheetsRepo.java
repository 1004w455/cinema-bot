package com.bot.cinemabot.repo;

import com.bot.cinemabot.model.MessageFormat;
import com.bot.cinemabot.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;
import org.springframework.util.LinkedMultiValueMap;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

@Slf4j
@Repository
public class GoogleSpreadSheetsRepo {
	private String url = "https://script.google.com/macros/s/AKfycbzGf6aN5oRmIahIo_Pnup5Q95fJ4nA0PUvjikEoPjD-tCa9D_g/exec";

	@Async
	public void save(final MessageFormat mf) {
		try {
			LinkedMultiValueMap data = new LinkedMultiValueMap();
			data.add("key", mf.getKey());
			data.add("platform", mf.getPlatform());
			data.add("title", mf.getTitle());
			data.add("dateRange", mf.getDateRange());
			data.add("price", mf.getPrice());
			data.add("buyUrl", URLEncoder.encode(mf.getBuyUrl(), "UTF-8"));
			String response = Utils.restTemplate.postForObject(url, data, String.class);
			log.info(String.format("save googleSheet: %s", Utils.gson.toJson(data)));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
}
