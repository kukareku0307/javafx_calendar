package com.example.kursach;

import java.time.ZonedDateTime;

public class CalendarActivity {
    private final ZonedDateTime date;
    private final String note;

    public CalendarActivity(ZonedDateTime date, String note) {
        this.date = date;
        this.note = note;
    }

    @Override
    public String toString() {
        return "CalenderActivity{" +
                "date=" + date +
                ", clientName='" + note + '\'' +

                '}';
    }
}