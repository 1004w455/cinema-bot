package com.bot.cinemabot.scheduler;

import com.bot.cinemabot.Telegram;
import com.bot.cinemabot.model.megabox.MegaboxResponse;
import com.bot.cinemabot.model.megabox.MegaboxTicket;
import com.bot.cinemabot.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
//@Component
@RestController
public class MegaboxScheduler {

	@Autowired
	private Telegram telegram;

	@Value("${bot.cinema.megabox.api}")
	private String megabox;
	@Value("${bot.cinema.megabox.detailUrl}")
	private String detailUrl;

	private List<MegaboxTicket> cache1p1Tickets;

	@PostConstruct
	public void init() throws IOException {
		List<MegaboxTicket> allTickets =  getMegaboxTickets();
		List<MegaboxTicket> onePlusOneTickets = filtered1p1Tickets(allTickets);
		cache1p1Tickets = Collections.synchronizedList(onePlusOneTickets);
		telegram.sendMessageToBot("메가박스\n모든 관람권: %s\n1+1 관람권: %s",
				allTickets.size(), onePlusOneTickets.size());
	}

//	@Scheduled(initialDelay = 1_000, fixedDelayString = "5000")
//	public void aJob() throws IOException {
//		getMegaboxData();
//	}

	@GetMapping("/list")
	private List<MegaboxTicket> getMegaboxTickets() {
		LinkedMultiValueMap data = new LinkedMultiValueMap();
		data.add("_command", "Store.getList");
		data.add("siteCode", "43");
		data.add("majorCode", "03");
		data.add("minorCode", "00");
		data.add("size", "small");
		String response = Utils.restTemplate.postForObject(megabox, data, String.class);
		MegaboxResponse res = Utils.gson.fromJson(response, MegaboxResponse.class);
		return res.convertTickets(detailUrl);
	}

	@GetMapping("/detail/{itemCode}")
	private MegaboxTicket getDetailInfo(@PathVariable String itemCode) throws IOException {
		Document megaboxDocument = Jsoup.connect(detailUrl + itemCode).get();
		String date = megaboxDocument.select("#content .good_info .date time").text();
		String price = megaboxDocument.select("#content #displayPrice").val();
		String name = megaboxDocument.select("#form_pay input[name=itemName]").val();
		return new MegaboxTicket(name, price, date, detailUrl + itemCode, false);
	}

	private List<MegaboxTicket> filtered1p1Tickets(List<MegaboxTicket> tickets) {
		return tickets.stream()
				.filter(t -> t.getName().contains("1+1"))
				.collect(Collectors.toList());
	}

}

