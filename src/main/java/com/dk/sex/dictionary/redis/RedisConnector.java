package com.dk.sex.dictionary.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.lambdaworks.redis.ClientOptions;
import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisConnection;
import com.lambdaworks.redis.RedisConnectionPool;
import com.lambdaworks.redis.RedisURI;

@Component
public class RedisConnector {
	static private RedisClient dictionaryDbClient;
	static private RedisConnectionPool<RedisConnection<String, Object>> dictionaryConnPool;
	static private RedisClient synonymDbClient;
	static private RedisConnectionPool<RedisConnection<String, Object>> synonymConnPool;

	@Autowired
	public RedisConnector(
			@Value("${dictionary.redis.url}") final String dictionaryDbUrl,
			@Value("${synonym.redis.url}") final String synonymDbUrl) {
		if (dictionaryDbClient == null)
			dictionaryDbClient = new RedisClient(
					RedisURI.create(dictionaryDbUrl));
		if (dictionaryConnPool == null)
			dictionaryConnPool = dictionaryDbClient.pool(
					new SerializedObjectCodec(), 5, 20);
		if (synonymDbClient == null)
			synonymDbClient = new RedisClient(
					RedisURI.create(synonymDbUrl));
		if (synonymConnPool == null)
			synonymConnPool = synonymDbClient.pool(
					new SerializedObjectCodec(), 5, 20);
	}

	public RedisConnection<String, Object> getDictionaryConn() {
		if (dictionaryConnPool != null)
			return dictionaryConnPool.allocateConnection();
		return null;
	}

	public void freeDictionaryConn(RedisConnection<String, Object> conn) {
		if (dictionaryConnPool != null)
			dictionaryConnPool.freeConnection(conn);
	}
	
	public RedisConnection<String, Object> getSynonymConn() {
		if (synonymConnPool != null)
			return synonymConnPool.allocateConnection();
		return null;
	}

	public void freeSynonymConn(RedisConnection<String, Object> conn) {
		if (synonymConnPool != null)
			synonymConnPool.freeConnection(conn);
	}

	@Override
	public void finalize() {
		if (dictionaryConnPool != null)
			dictionaryConnPool.close();
		if (dictionaryDbClient != null)
			dictionaryDbClient.shutdown();
		if (synonymConnPool != null)
			synonymConnPool.close();
		if (synonymDbClient != null)
			synonymDbClient.shutdown();
	}
}
