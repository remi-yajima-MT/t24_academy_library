package jp.co.metateam.library.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
/*
 * 利用可能在庫数とそれに紐づく貸出予定日と在庫管理番号
 * 日付ごとの在庫数のリンク押下時に貸出予定日と在庫管理番号をセットするため
 */
public class DateCalendarDto {

    private String stockId;

    private Date expectedRentalOn;

    private Integer dailyCount;
}
