package com.android.weether;

/**
 * Class for weather object.
 * @author Muaz Rahman
 */
public class Weather {

    private String weekday;
    private String monthname;
    private String day;
    private String year;

    private String conditions;
    private String icon;

    private String tempHighF;
    private String tempLowF;
    private String tempHighC;
    private String tempLowC;


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

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getConditions() {
        return conditions;
    }

    public void setConditions(String conditions) {
        this.conditions = conditions;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getTempHighF() {
        return tempHighF;
    }

    public void setTempHighF(String tempHighF) {
        this.tempHighF = tempHighF;
    }

    public String getTempLowF() {
        return tempLowF;
    }

    public void setTempLowF(String tempLowF) {
        this.tempLowF = tempLowF;
    }

    public String getTempHighC() {
        return tempHighC;
    }

    public void setTempHighC(String tempHighC) {
        this.tempHighC = tempHighC;
    }

    public String getTempLowC() {
        return tempLowC;
    }

    public void setTempLowC(String tempLowC) {
        this.tempLowC = tempLowC;
    }

}
