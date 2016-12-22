package com.dk.sex.bg;

public class Good {
	private int id;
	private String goodCode;
	private String goodName;
	private String brandName;
	private String categoryName;

	public Good(int id, String goodCode, String goodName, String brandName,
			String categoryName) {
		this.id = id;
		this.goodCode = goodCode;
		this.goodName = goodName;
		this.brandName = brandName;
		this.categoryName = categoryName;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getGoodCode() {
		return goodCode;
	}

	public void setGoodCode(String goodCode) {
		this.goodCode = goodCode;
	}

	public String getGoodName() {
		return goodName;
	}

	public void setGoodName(String goodName) {
		this.goodName = goodName;
	}

	public String getBrandName() {
		return brandName;
	}

	public void setBrandName(String brandName) {
		this.brandName = brandName;
	}

	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Good [id=").append(id).append(", goodCode=")
				.append(goodCode).append(", goodName=").append(goodName)
				.append(", brandName=").append(brandName)
				.append(", categoryName=").append(categoryName).append("]");
		return builder.toString();
	}

}
