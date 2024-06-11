package jp.co.metateam.library.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jp.co.metateam.library.constants.Constants;
import jp.co.metateam.library.model.BookMst;
import jp.co.metateam.library.model.Stock;
import jp.co.metateam.library.model.StockDto;
import jp.co.metateam.library.repository.BookMstRepository;
//import jp.co.metateam.library.repository.RentalManageRepository;
import jp.co.metateam.library.repository.StockRepository;
import jp.co.metateam.library.model.CalendarDto;
import jp.co.metateam.library.model.DailyDuplication;

@Service
public class StockService {
    private final BookMstRepository bookMstRepository;
    private final StockRepository stockRepository;

    @Autowired
    public StockService(BookMstRepository bookMstRepository, StockRepository stockRepository) {
        this.bookMstRepository = bookMstRepository;
        this.stockRepository = stockRepository;
    }

    @Transactional
    public List<Stock> findAll() {
        List<Stock> stocks = this.stockRepository.findByDeletedAtIsNull();

        return stocks;
    }

    @Transactional
    public List<Stock> findStockAvailableAll() {
        List<Stock> stocks = this.stockRepository.findByDeletedAtIsNullAndStatus(Constants.STOCK_AVAILABLE);

        return stocks;
    }

    @Transactional
    public Stock findById(String id) {
        return this.stockRepository.findById(id).orElse(null);
    }

    @Transactional
    public void save(StockDto stockDto) throws Exception {
        try {
            Stock stock = new Stock();
            BookMst bookMst = this.bookMstRepository.findById(stockDto.getBookId()).orElse(null);
            if (bookMst == null) {
                throw new Exception("BookMst record not found.");
            }

            stock.setBookMst(bookMst);
            stock.setId(stockDto.getId());
            stock.setStatus(stockDto.getStatus());
            stock.setPrice(stockDto.getPrice());

            // データベースへの保存
            this.stockRepository.save(stock);
        } catch (Exception e) {
            throw e;
        }
    }

    @Transactional
    public void update(String id, StockDto stockDto) throws Exception {
        try {
            Stock stock = findById(id);
            if (stock == null) {
                throw new Exception("Stock record not found.");
            }

            BookMst bookMst = stock.getBookMst();
            if (bookMst == null) {
                throw new Exception("BookMst record not found.");
            }

            stock.setId(stockDto.getId());
            stock.setBookMst(bookMst);
            stock.setStatus(stockDto.getStatus());
            stock.setPrice(stockDto.getPrice());

            // データベースへの保存
            this.stockRepository.save(stock);
        } catch (Exception e) {
            throw e;
        }
    }

    public List<Object> generateDaysOfWeek(int year, int month, LocalDate startDate, int daysInMonth) {
        List<Object> daysOfWeek = new ArrayList<>();
        for (int dayOfMonth = 1; dayOfMonth <= daysInMonth; dayOfMonth++) {
            LocalDate date = LocalDate.of(year, month, dayOfMonth);
            DateTimeFormatter formmater = DateTimeFormatter.ofPattern("dd(E)", Locale.JAPANESE);
            daysOfWeek.add(date.format(formmater));
        }

        return daysOfWeek;
    }

    public List<CalendarDto> generateValues(Integer year, Integer month, Integer daysInMonth) {
        /*
         * FIXME ここで各書籍毎の日々の在庫を生成する処理を実装する
         * 書籍マスタから全件取得 repositoryを呼ぶ(リスト化)
         */
        List<CalendarDto> values = new ArrayList<>();
        List<BookMst> countBylendableBooks = this.bookMstRepository.findAll();

        /*
         * 書籍分ループ
         * 取得した書籍名を「書籍名」に表示
         */
        for (int i = 0; i < countBylendableBooks.size(); i++) {
            BookMst book = countBylendableBooks.get(i);

            List<Stock> stockCount = this.stockRepository.findByBookMstIdAndStatus(book.getId(),
                    Constants.STOCK_AVAILABLE);

            CalendarDto calendarDto = new CalendarDto();
            calendarDto.setTitle(book.getTitle());
            calendarDto.setTotalCount(stockCount.size());

            List<DailyDuplication> dailyDuplication = new ArrayList<>();

            // 日付ごとの在庫数
            for (int dayOfMonth = 1; dayOfMonth <= daysInMonth; dayOfMonth++) {
                Calendar cl = Calendar.getInstance();
                cl.set(Calendar.YEAR, year);
                cl.set(Calendar.MONTH, month - 1);
                cl.set(Calendar.DATE, dayOfMonth);
                Date date = new Date();
                date = cl.getTime();

                DailyDuplication dailyList = new DailyDuplication();
                List<Object[]> stockList = stockRepository.calender(book.getId(), date);

                dailyList.setExpectedRentalOn(date);

                dailyList.setStockId(stockList.isEmpty() ? null : stockList.get(0)[0].toString());
                dailyList.setDailyCount(stockList.size());

                dailyDuplication.add(dailyList);

            }

            calendarDto.setCountAvailableRental(dailyDuplication);
            values.add(calendarDto);
        }

        return values;
    }

}