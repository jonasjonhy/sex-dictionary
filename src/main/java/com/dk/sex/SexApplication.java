package com.dk.sex;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.dk.sex.bg.DataExtractor;

@SpringBootApplication
public class SexApplication {
	
	@Autowired
	private DataExtractor dataExtractor;

    public static void main(String[] args) {
        SpringApplication.run(SexApplication.class, args);
    }
}
