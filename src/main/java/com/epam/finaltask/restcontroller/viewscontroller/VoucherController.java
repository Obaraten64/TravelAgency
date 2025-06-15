package com.epam.finaltask.restcontroller.viewscontroller;

import com.epam.finaltask.dto.VoucherDTO;
import com.epam.finaltask.dto.request.ChangeHotStatusVoucherRequest;
import com.epam.finaltask.dto.request.ChangeStatusVoucherRequest;
import com.epam.finaltask.dto.request.CreateVoucherRequest;
import com.epam.finaltask.dto.request.UpdateVoucherRequest;
import com.epam.finaltask.model.HotelType;
import com.epam.finaltask.model.TourType;
import com.epam.finaltask.model.TransferType;
import com.epam.finaltask.model.VoucherStatus;
import com.epam.finaltask.restcontroller.VoucherRestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;

@Controller
@RequestMapping(path = "/vouchers")
@RequiredArgsConstructor
@Log4j2
public class VoucherController {
    private final VoucherRestController voucherRestController;

    @GetMapping("/search/{searchType}")
    public ModelAndView searchView(@PathVariable("searchType") String searchType) {
        ModelAndView modelAndView = new ModelAndView("/tour/search");

        modelAndView.addObject("type", searchType.toLowerCase());

        return modelAndView;
    }

	@GetMapping(path = "/")
    public ModelAndView getVouchers(@RequestParam(name = "pageSize", defaultValue = "5", required = false) Integer pageSize,
                                    @RequestParam(name = "pageNumber", defaultValue = "0", required = false) Integer pageNumber) {
        ModelAndView modelAndView = new ModelAndView("/tour/dashboard");
        log.info("Request to get all vouchers from frontend");
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        //obtain tours
        Page<VoucherDTO> vouchers = voucherRestController.getVouchers(pageable);
        //configure model
        String linkStarter = "?pageSize=" + pageSize;
        modelAndView.addObject("linkPrevious",
                 linkStarter +
                        "&pageNumber=" + (pageNumber - 1));
        modelAndView.addObject("linkForward",
                linkStarter +
                        "&pageNumber=" + (pageNumber + 1));
        modelAndView.addObject("vouchers", vouchers);
        log.info("Obtained all vouchers for frontend");

        return modelAndView;
    }
    @GetMapping(path = "/user")
    public ModelAndView getVouchersByUser(@RequestParam("id") UUID userId,
                                          @RequestParam(name = "pageSize", defaultValue = "5", required = false) Integer pageSize,
                                          @RequestParam(name = "pageNumber", defaultValue = "0", required = false) Integer pageNumber) {
        ModelAndView modelAndView = new ModelAndView("/tour/dashboard");
        log.info("Request to get all vouchers from frontend by user id: {}", userId);
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        //obtain tours
        Page<VoucherDTO> vouchers = voucherRestController.getVouchersByUser(userId, pageable);
        //configure model
        configureModelAndView(modelAndView, "id",
                userId.toString(), pageable, vouchers);
        log.info("Obtained all vouchers for frontend by user id: {}", userId);

        return modelAndView;
    }
    @GetMapping(path ="/tour")
    public ModelAndView getVouchersByTourType(@RequestParam("tour") String tourType,
                                                  @RequestParam(name = "pageSize", defaultValue = "5", required = false) Integer pageSize,
                                                  @RequestParam(name = "pageNumber", defaultValue = "0", required = false) Integer pageNumber) {
        ModelAndView modelAndView = new ModelAndView("/tour/dashboard");
        log.info("Request to get all vouchers from frontend by tour type: {}", tourType);
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        //obtain tours
        Page<VoucherDTO> vouchers = voucherRestController.getVouchersByTourType(tourType, pageable);
        //configure model
        configureModelAndView(modelAndView, "tour",
                tourType, pageable, vouchers);
        log.info("Obtained all vouchers for frontend by tour type: {}", tourType);

        return modelAndView;
    }
    @GetMapping(path = "/transfer")
    public ModelAndView getVouchersByTransferType(@RequestParam("transfer") String transferType,
                                                      @RequestParam(name = "pageSize", defaultValue = "5", required = false) Integer pageSize,
                                                      @RequestParam(name = "pageNumber", defaultValue = "0", required = false) Integer pageNumber) {
        ModelAndView modelAndView = new ModelAndView("/tour/dashboard");
        log.info("Request to get all vouchers from frontend by transfer type: {}", transferType);
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        //obtain tours
        Page<VoucherDTO> vouchers = voucherRestController.getVouchersByTransferType(transferType, pageable);
        //configure model
        configureModelAndView(modelAndView, "transfer",
                transferType, pageable, vouchers);
        log.info("Obtained all vouchers for frontend by transfer type: {}", transferType);

        return modelAndView;
    }
    @GetMapping(path = "/price")
    public ModelAndView getVouchersByPrice(@RequestParam("price") Double price,
                                               @RequestParam(name = "pageSize", defaultValue = "1", required = false) Integer pageSize,
                                               @RequestParam(name = "pageNumber", defaultValue = "0", required = false) Integer pageNumber) {
        ModelAndView modelAndView = new ModelAndView("/tour/dashboard");
        log.info("Request to get all vouchers from frontend by price: {}", price);
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        //obtain tours
        Page<VoucherDTO> vouchers = voucherRestController.getVouchersByPrice(price, pageable);
        //configure model
        configureModelAndView(modelAndView, "price",
                String.valueOf(price), pageable, vouchers);
        log.info("Obtained all vouchers for frontend by price: {}", price);

        return modelAndView;
    }
    @GetMapping(path = "/hotel")
    public ModelAndView getVouchersByHotelType(@RequestParam("hotel") String hotelType,
                                                   @RequestParam(name = "pageSize", defaultValue = "5", required = false) Integer pageSize,
                                                   @RequestParam(name = "pageNumber", defaultValue = "0", required = false) Integer pageNumber) {
        ModelAndView modelAndView = new ModelAndView("/tour/dashboard");
        log.info("Request to get all vouchers from frontend by hotel type: {}", hotelType);
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        //obtain tours
        Page<VoucherDTO> vouchers = voucherRestController.getVouchersByHotelType(hotelType, pageable);
        //configure model
        configureModelAndView(modelAndView, "hotel",
                hotelType, pageable, vouchers);
        log.info("Obtained all vouchers for frontend by hotel type: {}", hotelType);

        return modelAndView;
    }

    @GetMapping("/create")
    public ModelAndView createVoucherView() {
        ModelAndView modelAndView = new ModelAndView("/tour/create-update");

        modelAndView.addObject("operation", "create");
        modelAndView.addObject("voucher", new VoucherDTO());
        addEnumsForVoucherCreationView(modelAndView);

        return modelAndView;
    }
    @PostMapping("/create")
    public ModelAndView createVoucher(@ModelAttribute("voucher") @Valid CreateVoucherRequest voucher) {
        ModelAndView modelAndView = new ModelAndView("/tour/success");
        log.info("Request to create new tour from frontend");
        VoucherDTO createdVoucher = voucherRestController.createVoucher(voucher);

        String text = "Created voucher successfully. Id: " + createdVoucher.getId();
        modelAndView.addObject("message", text);
        log.info(text);
        return modelAndView;
    }

    @GetMapping("/{voucherId}/update")
    public ModelAndView updateVoucherView(@PathVariable("voucherId") UUID id) {
        ModelAndView modelAndView = new ModelAndView("/tour/create-update");

        modelAndView.addObject("operation", "update");
        modelAndView.addObject("id", id);
        modelAndView.addObject("voucher", new VoucherDTO());
        addEnumsForVoucherCreationView(modelAndView);

        return modelAndView;
    }
    @PostMapping("/{voucherId}/update")
    public ModelAndView updateVoucher(@PathVariable("voucherId") UUID id,
                                        @ModelAttribute("voucher") @Valid UpdateVoucherRequest voucher) {
        ModelAndView modelAndView = new ModelAndView("/tour/success");
        log.info("Request to update tour from frontend with id: {}", id);
        VoucherDTO createdVoucher = voucherRestController.updateVoucher(id, voucher);

        String text = "Updated voucher successfully. Id: " + createdVoucher.getId();
        modelAndView.addObject("message", text);
        log.info(text);
        return modelAndView;
    }

    @GetMapping("/{voucherId}/delete")
    public ModelAndView deleteVoucher(@PathVariable("voucherId") UUID id) {
        ModelAndView modelAndView = new ModelAndView("/tour/success");
        log.info("Request to delete tour from frontend with id: {}", id);

        String text = voucherRestController.deleteVoucher(id).getBody();
        modelAndView.addObject("message", text);
        log.info(text);
        return modelAndView;
    }

    @GetMapping("/{voucherId}/order/{userId}")
    public ModelAndView orderVoucher(@PathVariable("voucherId") UUID voucherId,
                                     @PathVariable("userId") UUID userId) {
        ModelAndView modelAndView = new ModelAndView("/tour/success");
        log.info("User with id: {}. Trying to order tour with id: {}, request from frontend",
                userId, voucherId);

        String text = voucherRestController.orderVoucher(voucherId, userId).getBody();
        modelAndView.addObject("message", text);
        log.info(text);
        return modelAndView;
    }

    @GetMapping("/{voucherId}/status")
    public ModelAndView updateStatusView(@PathVariable("voucherId") UUID voucherId) {
        ModelAndView modelAndView = new ModelAndView("/tour/change-status");

        modelAndView.addObject("type", "status");
        modelAndView.addObject("voucherId", voucherId);
        modelAndView.addObject("statuses", VoucherStatus.values());

        return modelAndView;
    }
    @PostMapping("/{voucherId}/status")
    public ModelAndView updateStatus(@PathVariable("voucherId") UUID voucherId,
                                     @ModelAttribute("status") @Valid ChangeStatusVoucherRequest status) {
        ModelAndView modelAndView = new ModelAndView("/tour/success");
        log.info("Trying to change tour status with id: {}, request from frontend", voucherId);

        String text = voucherRestController.changeVoucherStatus(voucherId, status.getStatus()).getBody();
        modelAndView.addObject("message", text);
        log.info(text);
        return modelAndView;
    }

    @GetMapping("/{voucherId}/status/hot")
    public ModelAndView updateHotStatusView(@PathVariable("voucherId") UUID voucherId) {
        ModelAndView modelAndView = new ModelAndView("/tour/change-status");

        modelAndView.addObject("type", "hot");
        modelAndView.addObject("voucherId", voucherId);

        return modelAndView;
    }
    @PostMapping("/{voucherId}/status/hot")
    public ModelAndView updateHotStatus(@PathVariable("voucherId") UUID voucherId,
                                        @ModelAttribute("status") @Valid ChangeHotStatusVoucherRequest status) {
        ModelAndView modelAndView = new ModelAndView("/tour/success");
        log.info("Trying to change tour hot status with id: {}, request from frontend", voucherId);

        String text = voucherRestController.changeVoucherHotStatus(voucherId, status.getIsHot()).getBody();
        modelAndView.addObject("message", text);
        log.info(text);
        return modelAndView;
    }

    private void addEnumsForVoucherCreationView(ModelAndView modelAndView) {
        modelAndView.addObject("tours", TourType.values());
        modelAndView.addObject("transfers", TransferType.values());
        modelAndView.addObject("hotels", HotelType.values());
        modelAndView.addObject("statuses", VoucherStatus.values());
    }

    private void configureModelAndView(ModelAndView modelAndView, String paramName,
                                       String paramValue, Pageable pageable, Page<VoucherDTO> vouchers) {
        String linkStarter = "?" + paramName + "=" + paramValue + "&pageSize=" + pageable.getPageSize();
        modelAndView.addObject("linkPrevious",
                linkStarter +
                        "&pageNumber=" + (pageable.getPageNumber() - 1));
        modelAndView.addObject("linkForward",
                linkStarter +
                        "&pageNumber=" + (pageable.getPageNumber() + 1));
        modelAndView.addObject("vouchers", vouchers);
    }
}
