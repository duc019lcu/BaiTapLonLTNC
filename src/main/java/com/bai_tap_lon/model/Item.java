package com.bai_tap_lon.model;
//tạo class cha abstract Item để định hình các class con vd như Art hay Fashion.
public abstract class Item {
    protected String id; //id là 1 thực thể chính của hệ thống nên cho thêm id vô dù trong bản draw k có là hợp lí.
    protected String name;
    protected String description;
    protected double initPrice;
    protected String category;

    public Item(String id, String name, String description, double initPrice, String category){
        this.id=id;
        this.name=name;
        this.description=description;
        this.initPrice=initPrice;
        this.category=category;
    }

    public String getId(){
        return id;
    }
    public void setId(String id){
        this.id=id;
    }
    public String getName(){
        return name;
    }
    public void setName(String name){
        this.name=name;
    }
    public String getDescription(){
        return description;
    }
    public void setDescription(String description){
        this.description=description;
    }
    public double getInitPrice() {
        return initPrice;
    }
    public void setInitPrice(double initPrice) {
        this.initPrice = initPrice;
    }
    public String getCategory() {
        return category;
    }
    public void setCategory(String category) {
        this.category = category;
    }
    public abstract void showDetail();
    public abstract String toCSV();
}
