package com.epam.finaltask.mapper;

import com.epam.finaltask.dto.VoucherDTO;
import com.epam.finaltask.model.*;
import com.epam.finaltask.service.UserServiceImpl;

import lombok.AllArgsConstructor;

import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

import static com.epam.finaltask.mapper.EnumConvertor.*;

@Component
@AllArgsConstructor
public class VoucherMapperImpl implements VoucherMapper {
    private final UserMapper userMapper;
    private final UserServiceImpl userServiceImpl;
    @Override
    public Voucher toVoucher(VoucherDTO voucherDTO) {
        return Voucher.builder()
                .id(Optional.ofNullable(voucherDTO.getId()).map(UUID::fromString).orElse(null))
                .title(voucherDTO.getTitle())
                .description(voucherDTO.getDescription())
                .price(voucherDTO.getPrice())
                .tourType(getCorrectEnumType(TourType.class, voucherDTO.getTourType()))
                .transferType(getCorrectEnumType(TransferType.class, voucherDTO.getTransferType()))
                .hotelType(getCorrectEnumType(HotelType.class, voucherDTO.getHotelType()))
                .status(getCorrectEnumType(VoucherStatus.class, voucherDTO.getStatus()))
                .arrivalDate(voucherDTO.getArrivalDate())
                .evictionDate(voucherDTO.getEvictionDate())
                .user(Optional.ofNullable(voucherDTO.getUserId()).map(id ->
                        userMapper.toUser(userServiceImpl.getUserById(id))).orElse(null))
                .isHot(voucherDTO.getIsHot())
                .build();
    }

    @Override
    public VoucherDTO toVoucherDTO(Voucher voucher) {
        return VoucherDTO.builder()
                .id(Optional.ofNullable(voucher.getId()).map(UUID::toString).orElse(null))
                .title(voucher.getTitle())
                .description(voucher.getDescription())
                .price(voucher.getPrice())
                .tourType(getStringValueOfEnumType(voucher.getTourType()))
                .transferType(getStringValueOfEnumType(voucher.getTransferType()))
                .hotelType(getStringValueOfEnumType(voucher.getHotelType()))
                .status(getStringValueOfEnumType(voucher.getStatus()))
                .arrivalDate(voucher.getArrivalDate())
                .evictionDate(voucher.getEvictionDate())
                .userId(Optional.ofNullable(voucher.getUser()).map(User::getId).orElse(null))
                .isHot(voucher.isHot())
                .build();
    }
}
