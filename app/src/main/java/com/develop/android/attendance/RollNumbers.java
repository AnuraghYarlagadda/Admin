package com.develop.android.attendance;

public class RollNumbers {
    String rollnum;

    public RollNumbers(String rollnum) {
        this.rollnum = rollnum;
    }

    public RollNumbers() {
    }
    public String getRollnum() {
        return rollnum;
    }

    public void setRollnum(String rollnum) {
        this.rollnum = rollnum;
    }


    @Override
    public boolean equals(Object obj) {
        if(obj==null)
        {
            return false;
        }
        else if (!(obj instanceof RollNumbers)) {
            return false;
        } else {
            return (((RollNumbers) obj).getRollnum().equals(this.getRollnum()));
        }
    }
}
