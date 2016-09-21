package com.hortonworks.iot.simulator.events;

public class Product {
	private String productId;
	private String itemCategory;
	private String subClass;
	private String itemName;
	private String manufacturer;
	private String price;
	
	public Product(){}
	
	public Product(String productId, String itemCategory, String subClass, String manufacturer, String itemName, String price){
		this.productId = productId;
		this.itemCategory = itemCategory;
		this.subClass = subClass;
		this.manufacturer = manufacturer;
		this.itemName = itemName;
		this.price = price;
	}

	public String getItemCategory() {
		return itemCategory;
	}

	public void setItemCategory(String itemCategory) {
		this.itemCategory = itemCategory;
	}

	public String getItemName() {
		return itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	public String getManufacturer() {
		return manufacturer;
	}

	public void setManufacturer(String manufacturer) {
		this.manufacturer = manufacturer;
	}

	public String getSubClass() {
		return subClass;
	}

	public void setSubClass(String subClass) {
		this.subClass = subClass;
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	public String getProductId() {
		return productId;
	}

	public void setProductId(String productId) {
		this.productId = productId;
	}
}