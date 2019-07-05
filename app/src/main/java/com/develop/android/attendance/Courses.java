package com.develop.android.attendance;

import java.util.ArrayList;

public class Courses {
    String courseName;
    String courseFaculty;
    String mobileNumber;
    String venue;
    ArrayList<String> emails;
    String year;

    public Courses() {
    }

    public Courses(String courseName, String courseFaculty, ArrayList<String> emails, String year,String mobileNumber,String venue) {
        this.courseName = courseName;
        this.courseFaculty = courseFaculty;
        this.emails = emails;
        this.year = year;
        this.mobileNumber=mobileNumber;
        this.venue=venue;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getCourseFaculty() {
        return courseFaculty;
    }

    public void setCourseFaculty(String courseFaculty) {
        this.courseFaculty = courseFaculty;
    }

    public ArrayList<String> getEmails() {
        return emails;
    }

    public void setEmails(ArrayList<String> emails) {
        this.emails = emails;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

}
