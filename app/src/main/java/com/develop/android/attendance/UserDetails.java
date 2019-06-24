package com.develop.android.attendance;

public class UserDetails {
    String year;
    String courseName;

    public UserDetails() {
    }

    public UserDetails(String year, String courseName) {
        this.year = year;
        this.courseName = courseName;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }
}
