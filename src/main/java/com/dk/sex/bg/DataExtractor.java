package com.dk.sex.bg;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.dk.sex.dictionary.Dictionary;
import com.dk.sex.dictionary.DictionaryFactory;
import com.dk.sex.dictionary.redis.RedisConnector;
import com.lambdaworks.redis.RedisConnection;

@Component
public class DataExtractor implements CommandLineRunner {
	public static final Logger LOG = LoggerFactory
			.getLogger(DataExtractor.class);

	@Autowired
	private JdbcTemplate jdbcTemplate;
	@Autowired
	private DictionaryFactory dictionaryFactory;
	@Autowired
	private RedisConnector rConnector;
	@Value("${redis.keywords_db}")
	private Integer db;
	

	private String goodsExtractionSql = null;
	private String brandsExtractionSql = null;
	private String categoriesExtractionSql = null;
	private String occationsExtractionSql = null;

	public DataExtractor() {
		ClassLoader currentClassLoader = getClass().getClassLoader();
		File goodsExtractionSqlFile = new File(currentClassLoader.getResource(
				"bg_goods_extraction.sql").getFile());
		goodsExtractionSql = loadSql(goodsExtractionSqlFile);
		File brandsExtractionSqlFile = new File(currentClassLoader.getResource(
				"bg_brands_extraction.sql").getFile());
		brandsExtractionSql = loadSql(brandsExtractionSqlFile);
		File categoriesExtractionSqlFile = new File(currentClassLoader
				.getResource("bg_categories_extraction.sql").getFile());
		categoriesExtractionSql = loadSql(categoriesExtractionSqlFile);
		File occationsExtractionSqlFile = new File(currentClassLoader
				.getResource("bg_occations_extraction.sql").getFile());
		occationsExtractionSql = loadSql(occationsExtractionSqlFile);
	}

	private String loadSql(File file) {
		Scanner scanner = null;
		StringBuilder sb = new StringBuilder();
		try {
			scanner = new Scanner(file);
			while (scanner.hasNextLine()) {
				sb.append(scanner.nextLine()).append("\n");
			}
		} catch (FileNotFoundException e) {
			LOG.error("sql file not found");
		} finally {
			if (scanner != null) {
				scanner.close();
			}
		}
		return sb.toString();
	}

	@Override
	public void run(String... args) {
		LOG.debug("Data extractor is ready!");
	}

	public void extract() throws Exception {
		if (goodsExtractionSql != null && !goodsExtractionSql.equals("")) {
			Dictionary dictionary = dictionaryFactory.yield();
			extractGoods(0, dictionary);
			dictionary.relexiconize();
		}
		if (brandsExtractionSql != null && !brandsExtractionSql.equals("")) {
			extractBrands();
		}
	}

	private void extractGoods(int start, Dictionary dictionary) {
		List<Good> goods = jdbcTemplate.query(goodsExtractionSql,
				new Object[] { new Integer(start) }, new RowMapper<Good>() {
					@Override
					public Good mapRow(ResultSet rs, int arg1)
							throws SQLException {
						return new Good(rs.getInt("id"), rs
								.getString("productCode"), rs
								.getString("productName"), rs
								.getString("brandName"), rs
								.getString("categoryName"));
					}
				});

		int nextStart = 0;
		int i = 0;
		for (Good good : goods) {
			dictionary.addRawTerm(good.getGoodName());
			if (i == goods.size() - 1) {
				nextStart = good.getId();
			}
			i++;
		}

		if (goods != null && goods.size() > 0) {
			extractGoods(nextStart, dictionary);
		}
	}

	void extractBrands() {
		List<String> brands = jdbcTemplate.query(brandsExtractionSql,
				new RowMapper<String>() {

					@Override
					public String mapRow(ResultSet rs, int arg1)
							throws SQLException {
						return rs.getString("cname");
					}
				});
		Set<String> brands2Apply = new HashSet<String>();
		for (String brand : brands) {
			StringBuilder sb = new StringBuilder();
			String[] brandSnippets = brand.split(" ");
			for (String brandSnippet : brandSnippets) {
				if (brandSnippet != " ") {
					int commonAsianCharacterNum = 0;
					char[] charArray = brandSnippet.toCharArray();
					for (char c : charArray) {
						if (isCommonAsianCharacter(c)) {
							commonAsianCharacterNum++;
						}
					}
					// to confirm that snippet is composed mostly by common
					// Asian characters and the snippet is not a single
					// character
					if (((float) commonAsianCharacterNum
							/ (float) brandSnippet.length() >= 0.5)
							&& (brandSnippet.length() > 1)) {
						sb.append(brandSnippet);
					}
				}
			}
			String brand2Apply = sb.toString();
			if (brand2Apply.length() > 1) {
				brands2Apply.add(brand2Apply);
			}
		}
		System.out.println(brands2Apply);
		if (brands2Apply.size() > 0) {
			RedisConnection conn = rConnector.getDictionaryConn();
			conn.select(1);
			Set<String> dictionary = (Set<String>) conn.get("dk_dict");
			dictionary.addAll(brands2Apply);
			conn.set("dk_dict", dictionary);
		}
	}

	private boolean isCommonAsianCharacter(char c) {
		if (c >= 0x4E00 && c <= 0x9FFF) {
			return true;
		}
		return false;
	}

	void extractCategories() {
		List<String> categories = jdbcTemplate.query(categoriesExtractionSql,
				new RowMapper<String>() {

					@Override
					public String mapRow(ResultSet rs, int arg1)
							throws SQLException {
						return rs.getString("category");
					}
				});
		System.out.println(categories);
		// we assume all categories are Chinese, so we won't perform language
		// checking.
		Set<String> categories2Apply = new HashSet<String>();
		// extract adjective part of categories which end with nouns like
		// 【裤】,【鞋】 and so on, full list of these kind of nouns are in the
		// nouns.txt
		for (String category : categories) {
			int nounIndex = 0;
			if ((nounIndex = category.indexOf("裤")) == category.length() - 1
					|| (nounIndex = category.indexOf("裙")) == category.length() - 1
					|| (nounIndex = category.indexOf("鞋")) == category.length() - 1
					|| (nounIndex = category.indexOf("靴")) == category.length() - 1
					|| (nounIndex = category.indexOf("袜")) == category.length() - 1
					|| (nounIndex = category.indexOf("包")) == category.length() - 1
					|| (nounIndex = category.indexOf("笔")) == category.length() - 1
					|| (nounIndex = category.indexOf("本")) == category.length() - 1
					|| (nounIndex = category.indexOf("杯")) == category.length() - 1) {
				System.out.println("we will extract part of the category ["
						+ category + "]!");
				LOG.debug("we will extract part of the category [?]!", category);
				String adjectivePart = category.substring(0, nounIndex);
				if (adjectivePart.length() > 1 && !adjectivePart.equals("其他")
						&& !adjectivePart.equals("其它")) {
					categories2Apply.add(adjectivePart);
				}
				continue;
			}
			if (category.endsWith("子")) {
				System.out.println("we will discard the category [" + category
						+ "]!");
				LOG.debug("we will discard the category [?]!", category);
				continue;
			}
			categories2Apply.add(category);
		}
		// deal with categories with a two-words noun
		Set<String> categories2Remove = new HashSet<String>();
		outterCategoryLoop: for (String longerCategory2Apply : categories2Apply) {
			if (longerCategory2Apply.length() == 3) {
				for (String shorterCategory2Apply : categories2Apply) {
					if (longerCategory2Apply.contains(shorterCategory2Apply)
							&& !longerCategory2Apply
									.equals(shorterCategory2Apply)) {
						// If we don't discard these categories, they will put a
						// non-ideal affection on tokenization.
						System.out.println("we will discard the category ["
								+ longerCategory2Apply + "]!");
						LOG.debug("we will discard the category [?]!",
								longerCategory2Apply);
						categories2Remove.add(longerCategory2Apply);
						continue outterCategoryLoop;
					}
				}
			}
		}
		categories2Apply.removeAll(categories2Remove);
		System.out.println("Categories 2 apply:");
		System.out.println(categories2Apply.toString());
		LOG.debug("Categories 2 apply:");
		LOG.debug(categories2Apply.toString());
		if (categories2Apply.size() > 0) {
			RedisConnection conn = rConnector.getDictionaryConn();
			conn.select(1);
			Set<String> dictionary = (Set<String>) conn.get("dk_dict");
			dictionary.addAll(categories2Apply);
			conn.set("dk_dict", dictionary);
		}
	}

	void extractOccations() {
		List<String> occations = jdbcTemplate.query(occationsExtractionSql,
				new RowMapper<String>() {

					@Override
					public String mapRow(ResultSet rs, int arg1)
							throws SQLException {
						return rs.getString("attr_value");
					}
				});
		if (occations.size() > 0) {
			RedisConnection conn = rConnector.getDictionaryConn();
			conn.select(1);
			Set<String> dictionary = (Set<String>) conn.get("dk_dict");
			dictionary.addAll(occations);
			conn.set("dk_dict", dictionary);
		}
	}

	void extractSalesPoint() throws Exception {
		RedisConnection conn = rConnector.getDictionaryConn();
		conn.select(1);
		Set<String> dictionary = (Set<String>) conn.get("dk_dict");
		Scanner scanner = null;
		try {
			scanner = new Scanner(new File(getClass().getClassLoader()
					.getResource("bg_sales_point.txt").getFile()));
			while (scanner.hasNextLine()) {
				dictionary.add(scanner.nextLine());
			}
		} catch (Exception e) {
			throw e;
		} finally {
			if (scanner != null) {
				scanner.close();
			}
		}
		conn.set("dk_dict", dictionary);
	}
}
