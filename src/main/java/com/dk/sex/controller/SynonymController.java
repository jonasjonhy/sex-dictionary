package com.dk.sex.controller;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dk.sex.dictionary.redis.RedisConnector;
import com.lambdaworks.redis.RedisConnection;

@RestController
public class SynonymController {
	final static private Logger LOG = LoggerFactory
			.getLogger(SynonymController.class);

	@Autowired
	private RedisConnector connector;

	@RequestMapping(value = "/synonym/append")
	public String append(@RequestBody List<String> synonymList) {
		if (synonymList != null && synonymList.size() > 0) {
			RedisConnection<String, Object> conn = connector.getSynonymConn();
			for (String synonym : synonymList) {
				conn.lpush("synonyms", synonym);
			}
			conn.set("update_ts", System.currentTimeMillis());
			connector.freeSynonymConn(conn);
			return "synonyms appended";
		}
		return "no synonyms";
	}

	@RequestMapping(value = "/synonym/delete")
	public String delete(@RequestBody List<String> synonymList) {
		if (synonymList != null && synonymList.size() > 0) {
			RedisConnection<String, Object> conn = connector.getSynonymConn();
			for (String synonym : synonymList) {
				conn.lrem("synonyms", 0, synonym);
			}
			conn.set("update_ts", System.currentTimeMillis());
			connector.freeSynonymConn(conn);
			return "synonyms removed";
		}
		return "no synonyms";
	}

	@RequestMapping("/synonym/page/{pageNo}")
	public Page<String> getPage(@PathVariable Integer pageNo,
			@RequestParam(defaultValue = "10") Integer pageSize) {
		RedisConnection<String, Object> conn = connector.getSynonymConn();
		long totalSynoes = conn.llen("synonyms");
		int totalPages = (int) (totalSynoes % pageSize <= 0 ? totalSynoes
				/ pageSize : totalSynoes / pageSize + 1);
		if (pageNo <= 0 || pageNo > totalPages)
			pageNo = 1;
		long start = (pageNo - 1) * pageSize;
		List<Object> synoes = conn.lrange("synonyms", start, start
				+ pageSize - 1);
		List<String> synoesInPage = new ArrayList<String>();
		for (Object syno : synoes) {
			synoesInPage.add((String) syno);
		}
		connector.freeSynonymConn(conn);
		Page<String> p = new Page<String>();
		p.setCurrentPage(pageNo);
		p.setTotalPages(totalPages);
		p.setContent(synoesInPage);
		return p;
	}
}
