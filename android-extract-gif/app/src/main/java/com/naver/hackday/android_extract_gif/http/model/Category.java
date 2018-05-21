package com.naver.hackday.android_extract_gif.http.model;

import java.util.ArrayList;

public class Category {
    private String name;
    private int categoryNo;
    private boolean isOpen;
    private ArrayList<Category> subCategories;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCategoryNo() {
        return categoryNo;
    }

    public void setCategoryNo(int categoryNo) {
        this.categoryNo = categoryNo;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        isOpen = open;
    }

    public ArrayList<Category> getSubCategories() {
        return subCategories;
    }

    public void setSubCategories(ArrayList<Category> subCategories) {
        this.subCategories = subCategories;
    }

    @Override
    public String toString() {
        return getName();
    }
}