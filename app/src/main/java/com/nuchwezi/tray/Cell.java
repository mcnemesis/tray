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

    @Override
    public int hashCode(){
        return  getMoment().hashCode() * getItem().hashCode();
    }

    @Override
    public boolean equals(Object obj){
        if (obj instanceof Cell) {
            Cell pp = (Cell) obj;
            return (pp.getItem().equals(this.getItem()) && (pp.getMoment().equals(this.getMoment())));
        } else {
            return false;
        }
    }
}
