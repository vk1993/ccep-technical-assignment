package com.bayer.healthgoal.utlity;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class Utility {

    public static LocalDate toLocalDate(Date date) {
        return date == null ? null : date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

}
