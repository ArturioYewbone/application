package com.example.myapplication;

public class Item {
    private String shName;
    private String name;
    private float price;
    private boolean favor;
    public Item(String n, String sn, float p, boolean f){
        this.shName = sn;
        this.name = n;
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
