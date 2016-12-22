package com.dk.sex.controller;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dk.sex.dictionary.DkCommonAsianDictionary;
import com.dk.sex.dictionary.Term;
import com.dk.sex.dictionary.redis.RedisConnector;
import com.lambdaworks.redis.RedisConnection;

@RestController
public class KeywordController {
	final static private Logger LOG = LoggerFactory
			.getLogger(KeywordController.class);

	@Autowired
	private RedisConnector connector;

	@Deprecated
	@RequestMapping("/dictionary/pickedup")
	public List<Term> showPickedupTerms() {
		return DkCommonAsianDictionary.pickedUpTermSet;
	}

	@Deprecated
	@RequestMapping("/dictionary/notbalanced")
	public List<Term> showNotBalancedTerms() {
		return DkCommonAsianDictionary.notBalancedTermSet;
	}

	@Deprecated
	@RequestMapping("/dictionary/lessref")
	public List<Term> showLessRefTerms() {
		return DkCommonAsianDictionary.lessRefTermSet;
	}

	@Deprecated
	@RequestMapping("/dictionary/discarded")
	public List<Term> showDiscardedTerms() {
		return DkCommonAsianDictionary.discardedTermSet;
	}

	@RequestMapping("/keyword/applied")
	public Set<String> showApplied() {
		RedisConnection<String, Object> conn = connector.getDictionaryConn();
		Set<String> appliedTermSet = (Set<String>) conn.get("dk_dict");
		connector.freeDictionaryConn(conn);
		return appliedTermSet;
	}

	@RequestMapping(value = "/keyword/update", consumes = "application/json")
	public String update(@RequestBody Terms2Update terms2Update) {
		LOG.debug(terms2Update.toString());
		RedisConnection<String, Object> conn = connector.getDictionaryConn();
		Set<String> appliedTermSet = (Set<String>) conn.get("dk_dict");
		if (appliedTermSet != null) {
			if (terms2Update != null && terms2Update.getTerms2Del() != null) {
				for (String term2Del : terms2Update.getTerms2Del()) {
					if (appliedTermSet.contains(term2Del)) {
						appliedTermSet.remove(term2Del);
					}
				}
			}
		} else {
			appliedTermSet = new HashSet<String>();
		}
		if (terms2Update != null && terms2Update.getTerms2Add() != null) {
			appliedTermSet.addAll(terms2Update.getTerms2Add());
		}
		conn.set("dk_dict", appliedTermSet);
		conn.set("update_ts", System.currentTimeMillis());
		connector.freeDictionaryConn(conn);
		return "updated";
	}

	@RequestMapping("/keyword/download")
	public void download() {
		
	}

	@RequestMapping("/keyword/upload")
	public void upload() {
		
	}
}
