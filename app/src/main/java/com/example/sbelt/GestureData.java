package com.example.sbelt;

import java.util.Date;

public class GestureData {
    public int amount;
    public Date startDate;
    public Date endDate;

    public GestureData(int amount, Date startDate, Date endDate) {
        this.amount = amount;
        this.startDate = startDate;
        this.endDate = endDate;
    }
    public void setAmount(int amount) {
        this.amount = amount;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public int getAmount() {
        return amount;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }
    public GestureData(){

    }

}
