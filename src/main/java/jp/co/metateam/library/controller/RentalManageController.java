package jp.co.metateam.library.controller;
 
import java.util.List;
import java.util.Optional;
 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.FileEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
 
import jp.co.metateam.library.service.AccountService;
import jp.co.metateam.library.service.RentalManageService;
import jp.co.metateam.library.service.StockService;
import lombok.extern.log4j.Log4j2;
import jp.co.metateam.library.model.RentalManage;
import jp.co.metateam.library.model.RentalManageDto;
 
import jp.co.metateam.library.model.Account;
import jp.co.metateam.library.model.Stock;
 
import jp.co.metateam.library.values.RentalStatus;
import jp.co.metateam.library.values.StockStatus;
 
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.validation.Valid;
 
import java.nio.charset.StandardCharsets;
//ここ追加インポート
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date; //これインポートしたらDate型のエラー解消した
import org.springframework.transaction.annotation.Transactional;//(5/14)
import jp.co.metateam.library.repository.RentalManageRepository;
import org.springframework.web.bind.annotation.RequestParam;
import java.net.URLEncoder;
 
 
 
 
 
 
 
/**
 * 貸出管理関連クラスß
 */
@Log4j2
@Controller
public class RentalManageController {
 
    private final AccountService accountService;
    private final RentalManageService rentalManageService;
    private final StockService stockService;
    private final RentalManageRepository rentalManageRepository; // 追加
   
    
    @Autowired
    public RentalManageController(
    
        AccountService accountService,
        RentalManageService rentalManageService,
        StockService stockService,
        RentalManageRepository rentalManageRepository // 追加
    ) {
       
        this.accountService = accountService;
        this.rentalManageService = rentalManageService;
        this.stockService = stockService;
        this.rentalManageRepository = rentalManageRepository; // 追加
    }
 
    /**
     * 貸出一覧画面初期表示
     * @param model　//Modelオブジェクトが引数として渡されています。SpringMVCが提供するモデルオブジェクト。
     * @return //メソッドの戻り値に対する説明を記述する
     */
    
    @GetMapping("/rental/index")
    
    public String index(Model model) {

            List<RentalManage> RentalManageList = this.rentalManageService.findAll();

            model.addAttribute("rentalManageList", RentalManageList);

            return "rental/index";
    }
   
    @GetMapping("/rental/add")
    public String add(Model model) {
 
            List <Stock> stockList = this.stockService.findAll();
            List <Account> accounts = this.accountService.findAll();

            model.addAttribute("accounts", accounts);
            model.addAttribute("stockList", stockList);
            model.addAttribute("rentalStatus", RentalStatus.values());
    
            if (!model.containsAttribute("rentalManageDto")) {
                model.addAttribute("rentalManageDto", new RentalManageDto());
            }
    
        return "rental/add";
    }
    //URL（貸出一覧画面）へのデータ登録を行う
    @PostMapping("/rental/add")
    public String save(@Valid @ModelAttribute RentalManageDto rentalManageDto, BindingResult result, RedirectAttributes ra) {
        try {

            if (result.hasErrors()) {
                throw new Exception("Validation error.");
            }
            Optional<String> a = rentalManageService.rentalAble(rentalManageDto.getStockId(), new java.sql.Date(rentalManageDto.getExpectedRentalOn().getTime()),  new java.sql.Date(rentalManageDto.getExpectedReturnOn().getTime()));
            if (a.isPresent()){
                FieldError fieldError = new FieldError("rentalManageDto","status",a.get());
                result.addError(fieldError);

            }
            // 登録処理
            this.rentalManageService.save(rentalManageDto);
            return "redirect:/rental/index";
        } catch (Exception e) {
            
            log.error(e.getMessage());
    
            ra.addFlashAttribute("rentalManageDto", rentalManageDto);
            ra.addFlashAttribute("org.springframework.validation.BindingResult.rentalManageDto", result);
            
            return "redirect:/rental/add";
        }

    }

    @GetMapping("/rental/{id}/edit")
    public String edit(@PathVariable("id") String id, Model model, @RequestParam(name = "errorMessage", required = false) String errorMessage) {
            List<RentalManage> rentalManageList = this.rentalManageService.findAll();
            List<Account> accounts = this.accountService.findAll();
            List<Stock> stockList = this.stockService.findAll();
    
            model.addAttribute("accounts", accounts);
            model.addAttribute("stockList", stockList);
            model.addAttribute("rentalStatus", RentalStatus.values());
            model.addAttribute("rentalManageList", rentalManageList);
            model.addAttribute("rentalStockStatus", StockStatus.values());
        
            if (errorMessage != null) {
                model.addAttribute("errorMessage", errorMessage);
            }
 
            if (!model.containsAttribute("rentalManageDto")) {
                RentalManageDto rentalManageDto = new RentalManageDto();
                Long idLong = Long.parseLong(id);
                RentalManage rentalManage = this.rentalManageService.findById(idLong);
    
                rentalManageDto.setEmployeeId(rentalManage.getAccount().getEmployeeId());
                rentalManageDto.setId(rentalManage.getId());
                rentalManageDto.setExpectedRentalOn(rentalManage.getExpectedRentalOn());
                rentalManageDto.setExpectedReturnOn(rentalManage.getExpectedReturnOn());
                rentalManageDto.setStatus(rentalManage.getStatus());
                rentalManageDto.setStockId(rentalManage.getStock().getId());
    
                model.addAttribute("rentalManageDto", rentalManageDto);
            }
    
            return "rental/edit";
    }

    @PostMapping("/rental/{id}/edit")
    public String update(@PathVariable("id") Long id, @Valid @ModelAttribute RentalManageDto rentalManageDto, BindingResult result, Model model,RedirectAttributes ra) {
        try {
            Optional<String> b = rentalManageService.editrentalAble(rentalManageDto.getStockId(), rentalManageDto.getId(),new java.sql.Date(rentalManageDto.getExpectedReturnOn().getTime()),  new java.sql.Date(rentalManageDto.getExpectedRentalOn().getTime()));
            if (b.isPresent()){
                FieldError fieldError = new FieldError("rentalManageDto","status",b.get());
                result.addError(fieldError);
                throw new Exception("Validetion error");
            }
            // バリデーションエラーチェック
            if (result.hasErrors()) {
                model.addAttribute("errorMessage", "入力内容にエラーがあります");
                // バリデーションエラーがある場合は編集画面に戻る
                List<Stock> stockList = this.stockService.findStockAvailableAll();
                List<Account> accounts = this.accountService.findAll();
                model.addAttribute("accounts", accounts);
                model.addAttribute("stockList", stockList);
                model.addAttribute("rentalStatus", RentalStatus.values());
                return "rental/edit";
                
            }

            // 貸出情報を取得
            RentalManage rentalManage = this.rentalManageService.findById(id);
            if (rentalManage == null) {
                model.addAttribute("errorMessage", "指定された貸出情報が見つかりません");
                // 貸出情報が見つからない場合は編集画面に戻る
                List<Stock> stockList = this.stockService.findStockAvailableAll();
                List<Account> accounts = this.accountService.findAll();
                model.addAttribute("accounts", accounts);
                model.addAttribute("stockList", stockList);
                model.addAttribute("rentalStatus", RentalStatus.values());
                return "rental/edit";
            }
   
            // 貸出情報のステータスをチェック
            String statusErrorMessage = rentalManageDto.isValidStatus(rentalManage.getStatus());
            if (statusErrorMessage != null) {
                model.addAttribute("errorMessage", statusErrorMessage);
                // ステータスが無効な場合は編集画面に戻る
                List<Stock> stockList = this.stockService.findStockAvailableAll();
                List<Account> accounts = this.accountService.findAll();
                model.addAttribute("accounts", accounts);
                model.addAttribute("stockList", stockList);
                model.addAttribute("rentalStatus", RentalStatus.values());
                return "rental/edit";
            }
    
            // 貸出予定日のバリデーションチェック
            if (rentalManage.getStatus() == RentalStatus.RENT_WAIT.getValue() &&
                rentalManageDto.getStatus() == RentalStatus.RENTAlING.getValue()){
            if (!rentalManageDto.isValidRentalDate()) {

                model.addAttribute("errorMessage", "貸出予定日は現在の日付に設定してください");
                List<Stock> stockList = this.stockService.findStockAvailableAll();
                List<Account> accounts = this.accountService.findAll();
                model.addAttribute("accounts", accounts);
                model.addAttribute("stockList", stockList);
                model.addAttribute("rentalStatus", RentalStatus.values());
                result.addError(new FieldError("errorMessage", "status","貸出予定日は現在の日付に設定してください"));
                throw new Exception("Validetion error");
            }
            //返却予定日のバリデーションチェック
            }else if (rentalManage.getStatus() == RentalStatus.RENTAlING.getValue() &&
                      rentalManageDto.getStatus() == RentalStatus.RETURNED.getValue()) {
            if(!rentalManageDto.isValidReturnDate()) {

                model.addAttribute("errorMessage", "返却予定日は現在の日付に設定してください");
                List<Stock> stockList = this.stockService.findStockAvailableAll();
                List<Account> accounts = this.accountService.findAll();
                model.addAttribute("accounts", accounts);
                model.addAttribute("stockList", stockList);
                model.addAttribute("rentalStatus", RentalStatus.values());
                return "rental/edit";
    }
}
            // 更新処理
            this.rentalManageService.update(id, rentalManageDto);
            return "redirect:/rental/index";
        } catch (Exception e) {
            // エラーが発生した場合の処理
            log.error("更新処理中にエラーが発生しました: " + e.getMessage());
            ra.addFlashAttribute("rentalManageDto", rentalManageDto);
            ra.addFlashAttribute("org.springframework.validation.BindingResult.rentalManageDto", result);
            ra.addFlashAttribute("errorMessage", "更新処理中にエラーが発生しました");
            return String.format("redirect:/rental/%s/edit", id);
        }
    }
}