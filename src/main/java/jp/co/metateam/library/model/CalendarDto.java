package jp.co.metateam.library.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CalendarDto {

    private String title;

    private int totalCount;

    private List<DateCalendarDto> countAvailableRental;

}
