package com.example.myapplication;

public class Item {
    private String shName;
    private String name;
    private float price;
    private boolean favor;
    public Item(String sn, String n, float p, boolean f){
        this.shName = n;
        this.name = sn;
        this.price = p;
        this.favor = f;
    }
    public Item(String sn){
        this.shName = sn;
    }
    public String getName(){
        return name;
    }
    public String getshName(){
        return shName;
    }
    public float getPrice(){
        return price;
    }
    public boolean getFavor(){
        return favor;
    }
    public void setFavor(boolean f){
        this.favor = f;
    }
}
