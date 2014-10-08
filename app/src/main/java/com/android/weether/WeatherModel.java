package com.android.weether;

/**
 * Class for weather object.
 * @author Muaz Rahman
 */
public class WeatherModel {

    private String weekday;
    private String monthname;
    private int day;
    private int year;

    private String conditions;
    private String iconURL;

    private int tempHighF;
    private int tempLowF;
    private int tempHighC;
    private int tempLowC;


    public String getWeekday() {
        return weekday;
    }

    public void setWeekday(String weekday) {
        this.weekday = weekday;
    }

    public String getMonthname() {
        return monthname;
    }

    public void setMonthname(String monthname) {
        this.monthname = monthname;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getConditions() {
        return conditions;
    }

    public void setConditions(String conditions) {
        this.conditions = conditions;
    }

    public String getIconURL() {
        return iconURL;
    }

    public void setIconURL(String icon) {
        this.iconURL = icon;
    }

    public int getTempHighF() {
        return tempHighF;
    }

    public void setTempHighF(int tempHighF) {
        this.tempHighF = tempHighF;
    }

    public int getTempLowF() {
        return tempLowF;
    }

    public void setTempLowF(int tempLowF) {
        this.tempLowF = tempLowF;
    }

    public int getTempHighC() {
        return tempHighC;
    }

    public void setTempHighC(int tempHighC) {
        this.tempHighC = tempHighC;
    }

    public int getTempLowC() {
        return tempLowC;
    }

    public void setTempLowC(int tempLowC) {
        this.tempLowC = tempLowC;
    }

}
