//ServiceはControllerから呼び出され、必要なデータの取得や更新、処理などを行う
package jp.co.metateam.library.service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.sql.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import io.micrometer.common.util.StringUtils;
import jakarta.validation.Valid;
import jp.co.metateam.library.model.Account;
import jp.co.metateam.library.model.RentalManage;
import jp.co.metateam.library.model.RentalManageDto;
import jp.co.metateam.library.model.Stock;
import jp.co.metateam.library.repository.AccountRepository;
import jp.co.metateam.library.repository.RentalManageRepository;
import jp.co.metateam.library.repository.StockRepository;
import jp.co.metateam.library.values.RentalStatus;




@Service
public class RentalManageService {

    private final AccountRepository accountRepository;
    private final RentalManageRepository rentalManageRepository;
    private final StockRepository stockRepository;

     @Autowired
    public RentalManageService(
        AccountRepository accountRepository,
        RentalManageRepository rentalManageRepository,
        StockRepository stockRepository
    ) {
        this.accountRepository = accountRepository;
        this.rentalManageRepository = rentalManageRepository;
        this.stockRepository = stockRepository;
    }

    @Transactional
    public List <RentalManage> findAll() {
        List <RentalManage> rentalManageList = this.rentalManageRepository.findAll();

        return rentalManageList;
    }

    @Transactional
    public RentalManage findById(Long id) {
        return this.rentalManageRepository.findById(id).orElse(null);
    }

    @Transactional 
    public void save(RentalManageDto rentalManageDto) throws Exception {
        try {
            Account account = this.accountRepository.findByEmployeeId(rentalManageDto.getEmployeeId()).orElse(null);
            if (account == null) {
                throw new Exception("Account not found.");
            }

            Stock stock = this.stockRepository.findById(rentalManageDto.getStockId()).orElse(null);
            if (stock == null) {
                throw new Exception("Stock not found.");
            }

            RentalManage rentalManage = new RentalManage();
            rentalManage = setRentalStatusDate(rentalManage, rentalManageDto.getStatus());

            rentalManage.setAccount(account);
            rentalManage.setExpectedRentalOn(rentalManageDto.getExpectedRentalOn());
            rentalManage.setExpectedReturnOn(rentalManageDto.getExpectedReturnOn());
            rentalManage.setStatus(rentalManageDto.getStatus());
            rentalManage.setStock(stock);
            
            // データベースへの保存
            this.rentalManageRepository.save(rentalManage);
        } catch (Exception e) {
            throw e;
        }
    }

    @Transactional
    public void update(Long id, RentalManageDto rentalManageDto) throws Exception {
       
       
        try {
            //アカウント情報を取得(指定された社員番号と一致するアカウント情報を検索し、結果を返す)
            Account account = this.accountRepository.findByEmployeeId(rentalManageDto.getEmployeeId()).orElse(null);
            if (account == null) {
                throw new Exception("Rental record not found.");
            }
            //在庫管理情報を取得（指定された値と一致する在庫管理情報を検索し、結果を返す）
            Stock stock = this.stockRepository.findById(rentalManageDto.getStockId()).orElse(null);
            if (stock == null) {
                throw new Exception("Rental record not found.");
            }
            //在庫管理番号を取得（指定された値と一致する在庫管理番号を検索し、結果を返す）
            RentalManage rentalManage = this.rentalManageRepository.findById(id).orElse(null);
            if (rentalManage == null) {
                throw new Exception("RentalManage record not found.");
            }
                //日付設定をupdateする
                setRentalStatusDate(rentalManage, rentalManageDto.getStatus());

                //レンタル情報の更新
                rentalManage.setId(rentalManage.getId());
                rentalManage.setAccount(account);
                rentalManage.setExpectedRentalOn(rentalManageDto.getExpectedRentalOn());
                rentalManage.setExpectedReturnOn(rentalManageDto.getExpectedReturnOn());
                rentalManage.setStatus(rentalManageDto.getStatus());
                rentalManage.setStock(stock);

            // データベースへの保存
            this.rentalManageRepository.save(rentalManage);
        } catch (Exception e) {
            throw e;
        }
    }
    //レンタルステータスに関連する日付を設定する
    private RentalManage setRentalStatusDate(RentalManage rentalManage, Integer status) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        
        if (status == RentalStatus.RENTAlING.getValue()) {
            //レンタル開始日時が設定
            rentalManage.setRentaledAt(timestamp);
        } else if (status == RentalStatus.RETURNED.getValue()) {
            //レンタル返却日時が設定
            rentalManage.setReturnedAt(timestamp);
        } else if (status == RentalStatus.CANCELED.getValue()) {
            //レンタルキャンセル日時が設定
            rentalManage.setCanceledAt(timestamp);
        }    

        return rentalManage;
    }
   
    @Transactional
    public Optional<String> rentalAble(String stockId, Date expected_return_on, Date expected_rental_on){
        if(rentalManageRepository.count(stockId) == 0){
          return Optional.of("この本は貸し出せません");
        }
        if(rentalManageRepository.addtest(stockId) != rentalManageRepository.addwhetherDay(stockId, expected_return_on,expected_rental_on) ){
            return Optional.of("この本は貸し出せません");
        }
        return Optional.empty();
    }

    public Optional<String> editrentalAble(String stockId, Long id, Date expected_return_on, Date expected_rental_on){
        if(rentalManageRepository.count(stockId) == 0){
          return Optional.of("この本は貸し出せません");
        }
        if(rentalManageRepository.whetherDay(stockId, id,expected_return_on,expected_rental_on) != rentalManageRepository.test(stockId, id)){
            return Optional.of("この本は貸し出せません");
        }
        return Optional.empty();
    }
}
    
