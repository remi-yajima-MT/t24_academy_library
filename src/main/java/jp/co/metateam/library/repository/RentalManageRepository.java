package jp.co.metateam.library.repository;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jp.co.metateam.library.model.RentalManage;

@Repository
public interface RentalManageRepository extends JpaRepository<RentalManage, Long> {
    List<RentalManage> findAll();

	Optional<RentalManage> findById(Long id);

    @Query("SELECT COUNT(*) FROM Stock WHERE id = ?1 AND status = 0")
    Integer count(String id);

    @Query("SELECT COUNT(*) FROM RentalManage WHERE stock.id = ?1 AND status IN (0,1)")
    Integer addtest(String stockId);
    
    @Query("SELECT COUNT(*) FROM RentalManage WHERE stock.id = ?1 AND status IN (0,1) AND (expectedRentalOn > ?2 OR expectedReturnOn < ?3)")
    Integer addwhetherDay(String stockId, Date expected_return_on, Date expected_rental_on);
    
    @Query("SELECT COUNT(*) FROM RentalManage WHERE stock.id = ?1 AND id != ?2 AND status IN (0,1) ")
    Integer test(String stockId, Long id);
    
    @Query("SELECT COUNT(*) FROM RentalManage WHERE stock.id = ?1 AND id != ?2 AND status IN (0,1) AND (expectedRentalOn > ?3 OR expectedReturnOn < ?4)")
    Integer whetherDay(String stockId, Long id, Date expected_return_on, Date expected_rental_on);


}
/* 
    @Query("select rm"
            + " from RentalManage rm " + " where (rm.status = 0 or rm.status = 1)"
            + " and rm.stock.id = ?1 "
            + " and rm.id <> ?2")
        List<RentalManage> findByStockIdAndStatusIn(String Id,Long rentalId);
 //保存
    @Query("select rm"
            + " from RentalManage rm " + " where (rm.status = 0 or rm.status = 1)"
            + " and rm.stock.id = ?1 ")
    List<RentalManage> findByStockIdAndStatusIn(String Id);

/*
 * String Id　１２３４５～rentalIdを取得
 * Long rentalId　編集したものを除外
 * 保存のLong rentalIdでは２３４５を取得  
 * 
 * Integer:数列　String:文字列
 */