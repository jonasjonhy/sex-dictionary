package com.dk.sex.bg;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DataExtractionController {
	final static public Logger LOG = LoggerFactory
			.getLogger(DataExtractionController.class);

	@Autowired
	private DataExtractor dataExtractor;

	@RequestMapping("/data-extract/all")
	public String extract() {
		try {
			dataExtractor.extract();
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			return "I'm sorry, work not done...";
		}
		return "Ahaaa";
	}
	
	@RequestMapping("/data-extract/brands")
	public String extractBrands() {
		try {
			dataExtractor.extractBrands();
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			return "I'm sorry, work not done...";
		}
		return "Ahaaa";
	}
	
	@RequestMapping("/data-extract/categories")
	public String extractCategories() {
		try {
			dataExtractor.extractCategories();
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			return "I'm sorry, work not done...";
		}
		return "Ahaaa";
	}
	
	@RequestMapping("/data-extract/occations")
	public String extractOccations() {
		try {
			dataExtractor.extractOccations();
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			return "I'm sorry, work not done...";
		}
		return "Ahaaa";
	}
	
	@RequestMapping("/data-extract/sales-point")
	public String extractSalesPoint() {
		try {
			dataExtractor.extractSalesPoint();
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			return "I'm sorry, work not done...";
		}
		return "Ahaaa";
	}	
}
