package com.netflix.karyon.ws.rs.test;

public class UserName {

    private String first;
    private String last;
    
    public UserName(String first, String last) {
        this.first = first;
        this.last = last;
    }
    
    public String getFirst() {
        return first;
    }

    public void setFirst(String first) {
        this.first = first;
    }

    public String getLast() {
        return last;
    }

    public void setLast(String last) {
        this.last = last;
    }
    
}
