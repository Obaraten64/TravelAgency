package com.epam.finaltask.service;

import java.util.List;
import java.util.UUID;

import com.epam.finaltask.dto.VoucherDTO;
import com.epam.finaltask.model.HotelType;
import com.epam.finaltask.model.TourType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface VoucherService {
    VoucherDTO create(VoucherDTO voucherDTO);
    VoucherDTO order(String id, String userId);
    VoucherDTO update(String id, VoucherDTO voucherDTO);
    void delete(String voucherId);
    VoucherDTO changeHotStatus(String id, VoucherDTO voucherDTO);
    VoucherDTO updateStatus(String id, VoucherDTO voucherDTO);
    List<VoucherDTO> findAllByUserId(String userId);

    List<VoucherDTO> findAllByTourType(TourType tourType);
    List<VoucherDTO> findAllByTransferType(String transferType);
    List<VoucherDTO> findAllByPrice(Double price);
    List<VoucherDTO> findAllByHotelType(HotelType hotelType);

    List<VoucherDTO> findAll();
    //pageable methods
    Page<VoucherDTO> findAllByUserId(UUID userId, Pageable pageable);

    Page<VoucherDTO> findAllByTourType(TourType tourType, Pageable pageable);
    Page<VoucherDTO> findAllByTransferType(String transferType, Pageable pageable);
    Page<VoucherDTO> findAllByPrice(Double price, Pageable pageable);
    Page<VoucherDTO> findAllByHotelType(HotelType hotelType, Pageable pageable);

    Page<VoucherDTO> findAll(Pageable pageable);
}
