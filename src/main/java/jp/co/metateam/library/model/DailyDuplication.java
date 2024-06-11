package jp.co.metateam.library.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class DailyDuplication {

    private String stockId;

    private Date expectedRentalOn;

    private Integer dailyCount;
}
