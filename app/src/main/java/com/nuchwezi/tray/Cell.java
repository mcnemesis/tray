package com.nuchwezi.tray;

import java.util.Date;

class Cell {
    String item;
    Date moment;

    public  Cell(Date moment, String item) {
        this.item = item;
        this.moment = moment;
    }

    public String getItem(){ return  this.item; }
    public Date getMoment() { return  this.moment; }
}
