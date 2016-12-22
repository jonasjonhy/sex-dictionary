package com.dk.sex.dictionary.redis;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;

import com.lambdaworks.redis.codec.RedisCodec;

public class SerializedObjectCodec extends RedisCodec<String, Object> {

	@Override
	public String decodeKey(ByteBuffer bytes) {
		return bytes.toString();
	}

	@Override
	public Object decodeValue(ByteBuffer bytes) {
		try {
			byte[] array = new byte[bytes.remaining()];
			bytes.get(array);
			ObjectInputStream is = new ObjectInputStream(
					new ByteArrayInputStream(array));
			return is.readObject();
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public byte[] encodeKey(String key) {
		return key.getBytes();
	}

	@Override
	public byte[] encodeValue(Object value) {
		try {
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			ObjectOutputStream os = new ObjectOutputStream(bytes);
			os.writeObject(value);
			return bytes.toByteArray();
		} catch (IOException e) {
			return null;
		}
	}

}
