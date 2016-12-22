package com.dk.sex.dictionary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

//import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dk.sex.controller.Terms2Update;
import com.dk.sex.dictionary.redis.RedisConnector;
import com.lambdaworks.redis.RedisConnection;

public class DkCommonAsianDictionary implements Dictionary {
	final static public Logger LOG = LoggerFactory
			.getLogger(DkCommonAsianDictionary.class);
	final static private int MAX_STEP = 2;

	private Map<Integer, Map<String, Integer>> termRefMap = new HashMap<Integer, Map<String, Integer>>();
	private int totalTerms = 0;
	static public List<Term> pickedUpTermSet = new ArrayList<Term>();
	// frequently referenced but sub-term reference times are not balanced
	static public List<Term> notBalancedTermSet = new ArrayList<Term>();
	// less referenced but sub-term reference times are balance;
	static public List<Term> lessRefTermSet = new ArrayList<Term>();
	static public List<Term> discardedTermSet = new ArrayList<Term>();

	private RedisConnector connector;

	public DkCommonAsianDictionary(RedisConnector connector) {
		this.connector = connector;
	}

	@Override
	public void addRawTerm(String rawTerm) {
		if (rawTerm != null && !rawTerm.equals("")) {
			char[] charArray = rawTerm.toCharArray();
			for (int step = 1; step <= MAX_STEP; step++) {
				// for (int step = 1; step <= charArray.length; step++) {
				for (int start = 0; start + step <= charArray.length; start++) {
					StringBuffer sb = new StringBuffer();
					int increment = 0;
					while (increment < step
							&& start + increment < charArray.length) {
						if (isCommonAsianCharacter(charArray[start + increment]))
							sb.append(charArray[start + increment]);
						increment++;
					}
					if (sb.toString().length() < step)
						continue;
					Map<String, Integer> fixedLengthTermRefMap = termRefMap
							.get(step);
					if (fixedLengthTermRefMap == null) {
						fixedLengthTermRefMap = new HashMap<String, Integer>();
					}
					if (!fixedLengthTermRefMap.keySet().contains(sb.toString())) {
						fixedLengthTermRefMap
								.put(sb.toString(), new Integer(1));
					} else {
						fixedLengthTermRefMap.put(sb.toString(),
								fixedLengthTermRefMap.get(sb.toString()) + 1);
					}
					termRefMap.put(step, fixedLengthTermRefMap);
					if (step == MAX_STEP)
						totalTerms++;
				}
			}
		}
	}

	private boolean isCommonAsianCharacter(char c) {
		if (c >= 0x4E00 && c <= 0x9FFF) {
			return true;
		}
		return false;
	}

	private void show() {
		for (Term term : pickedUpTermSet) {
			LOG.debug(term.toString());
		}
		LOG.debug("--------------------");

		for (Term term : notBalancedTermSet) {
			LOG.debug(term.toString());
		}
		LOG.debug("--------------------");

		for (Term term : lessRefTermSet) {
			LOG.debug(term.toString());
		}
		LOG.debug("--------------------");

		for (Term term : discardedTermSet) {
			LOG.debug(term.toString());
		}
		LOG.debug("--------------------");
		LOG.debug(termRefMap.get(MAX_STEP).size() == pickedUpTermSet.size()
				+ notBalancedTermSet.size() + lessRefTermSet.size()
				+ discardedTermSet.size() ? "OK" : "damn");
		LOG.debug("total:" + termRefMap.get(MAX_STEP).size());
		LOG.debug("pickedup:" + pickedUpTermSet.size());
		LOG.debug("not balanced:" + notBalancedTermSet.size());
		LOG.debug("lessref:" + lessRefTermSet.size());
		LOG.debug("discarded:" + discardedTermSet.size());
	}

	@Override
	public void relexiconize() {
		relexiconize2CharsTerm();
	}

	private void relexiconize2CharsTerm() {
		Set<String> terms2Cache = new HashSet<String>();
		Map<String, Integer> _2CharsTermRefMap = termRefMap.get(MAX_STEP);
		Map<String, Integer> charRefMap = termRefMap.get(1);
		for (String _2CharsTerm : _2CharsTermRefMap.keySet()) {
			float _1stCharRefTime = (float) charRefMap.get(_2CharsTerm
					.substring(0, 1));
			float _2ndCharRefTime = (float) charRefMap.get(_2CharsTerm
					.substring(1));
			float subTermRefDiffRatio = _1stCharRefTime > _2ndCharRefTime ? _1stCharRefTime
					/ _2ndCharRefTime
					: _2ndCharRefTime / _1stCharRefTime;
			float freqence = (float) _2CharsTermRefMap.get(_2CharsTerm)
					/ (float) totalTerms;
			if (freqence > 0.001f) {
				if (Math.round(subTermRefDiffRatio) == 1) {
					pickedUpTermSet.add(new Term(_2CharsTerm, _2CharsTermRefMap
							.get(_2CharsTerm), subTermRefDiffRatio, true));
					terms2Cache.add(_2CharsTerm);
				} else {
					if (Math.round(subTermRefDiffRatio) >= 10) {
						// quite unbalanced, also need to be discarded
						discardedTermSet.add(new Term(_2CharsTerm,
								_2CharsTermRefMap.get(_2CharsTerm),
								subTermRefDiffRatio, false));
					} else {
						notBalancedTermSet.add(new Term(_2CharsTerm,
								_2CharsTermRefMap.get(_2CharsTerm),
								subTermRefDiffRatio, false));
					}
				}
			} else {
				if (Math.round(subTermRefDiffRatio) == 1) {
					lessRefTermSet.add(new Term(_2CharsTerm, _2CharsTermRefMap
							.get(_2CharsTerm), subTermRefDiffRatio, false));
				} else {
					discardedTermSet.add(new Term(_2CharsTerm,
							_2CharsTermRefMap.get(_2CharsTerm),
							subTermRefDiffRatio, false));
				}
			}
		}
		RedisConnection conn = connector.getDictionaryConn();
		conn.set("dk_dict", terms2Cache);
		connector.freeDictionaryConn(conn);
	}

	@Override
	public boolean isIncluded(String str) {
		for (Term term : pickedUpTermSet) {
			if (term.getPayload().equals(str) && term.isApplied()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Set<String> getAppliedTermSet() {
		RedisConnection<String, Object> conn = connector.getDictionaryConn();
		Set<String> appliedTermSet = (Set<String>) conn.get("dk_dict");
		connector.freeDictionaryConn(conn);
		return appliedTermSet;
	}

	@Override
	public void updateAppliedTermSet(Terms2Update terms2Update) {
		RedisConnection<String, Object> conn = connector.getDictionaryConn();
		Set<String> appliedTermSet = (Set<String>)conn.get("dk_dict");
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
	}
}
