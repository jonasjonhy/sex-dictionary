package com.dk.sex.dictionary;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.dk.sex.dictionary.redis.RedisConnector;

@Component
public class DictionaryFactory {

	@Value("${dictionary.type:com.dk.sex.dictionary.DkCommonAsianDictionary}")
	private String type;
	
	@Autowired
	RedisConnector rConnector;

	private Dictionary dictionary;

	public Dictionary yield() {
		if (dictionary == null) {
			if (type != null && !type.equals("")) {
				try {
					dictionary = (Dictionary) Class.forName(type)
							.newInstance();
				} catch (Exception e) {
					// add LOG
					dictionary = new DkCommonAsianDictionary(rConnector);
				}
			} else {
				dictionary = new DkCommonAsianDictionary(rConnector);
			}
		}
		return dictionary;
	}
}
