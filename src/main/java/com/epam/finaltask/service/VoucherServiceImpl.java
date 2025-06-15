package com.epam.finaltask.service;

import com.epam.finaltask.dto.VoucherDTO;
import com.epam.finaltask.exception.exceptions.UserSearchException;
import com.epam.finaltask.exception.exceptions.VoucherException;
import com.epam.finaltask.exception.exceptions.VoucherSearchException;
import com.epam.finaltask.mapper.VoucherMapper;
import com.epam.finaltask.model.*;
import com.epam.finaltask.repository.UserRepository;
import com.epam.finaltask.repository.VoucherRepository;

import jakarta.transaction.Transactional;

import lombok.AllArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.epam.finaltask.mapper.EnumConvertor.getCorrectEnumType;

@Service
@AllArgsConstructor
@Transactional
public class VoucherServiceImpl implements VoucherService {
    private final VoucherRepository voucherRepository;
    private final UserRepository userRepository;
    private final VoucherMapper voucherMapper;

    @Override
    public VoucherDTO create(VoucherDTO voucherDTO) {
        //create voucher
        Voucher voucher = voucherMapper.toVoucher(voucherDTO);
        voucherRepository.save(voucher);

        return voucherMapper.toVoucherDTO(voucher);
    }

    @Override
    public VoucherDTO order(String id, String userId) {
        //search for data
        User user = findUserById(userId);
        Voucher voucher = findVoucherById(UUID.fromString(id));
        //check if voucher is available
        if (voucher.getStatus() != VoucherStatus.REGISTERED) {
            throw new VoucherException("Voucher not available");
        }
        //check if user has enough money
        BigDecimal newBalance = user.getBalance()
                .subtract(BigDecimal.valueOf(voucher.getPrice()));
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new VoucherException("Sorry! You don't have enough balance to order this tour");
        }
        //update data
        user.setBalance(newBalance);
        user.getVouchers().add(voucher);
        userRepository.save(user);
        voucher.setUser(user);
        voucher.setStatus(VoucherStatus.PAID);
        voucherRepository.save(voucher);

        return voucherMapper.toVoucherDTO(voucher);
    }

    @Override
    public VoucherDTO update(String id, VoucherDTO voucherDTO) {
        //search for data
        Voucher voucher = findVoucherById(UUID.fromString(id));
        //update data
        Voucher updatedVoucher = Voucher.builder()
                .id(voucher.getId())
                .title(Optional.ofNullable(voucherDTO.getTitle()).orElse(voucher.getTitle()))
                .description(Optional.ofNullable(voucherDTO.getDescription()).orElse(voucher.getDescription()))
                .price(Optional.ofNullable(voucherDTO.getPrice()).orElse(voucher.getPrice()))
                .tourType(Optional.ofNullable(voucherDTO.getTourType()).map(tourType ->
                        getCorrectEnumType(TourType.class, tourType)).orElse(voucher.getTourType()))
                .transferType(Optional.ofNullable(voucherDTO.getTransferType()).map(transferType ->
                        getCorrectEnumType(TransferType.class, transferType)).orElse(voucher.getTransferType()))
                .hotelType(Optional.ofNullable(voucherDTO.getHotelType()).map(hotelType ->
                                getCorrectEnumType(HotelType.class, hotelType)).orElse(voucher.getHotelType()))
                .status(Optional.ofNullable(voucherDTO.getStatus()).map(status ->
                        getCorrectEnumType(VoucherStatus.class, status)).orElse(voucher.getStatus()))
                .arrivalDate(Optional.ofNullable(voucherDTO.getArrivalDate()).orElse(voucher.getArrivalDate()))
                .evictionDate(Optional.ofNullable(voucherDTO.getEvictionDate()).orElse(voucher.getEvictionDate()))
                .isHot(Optional.ofNullable(voucherDTO.getIsHot()).orElse(voucher.isHot()))
                .user(Optional.ofNullable(voucherDTO.getUserId()).map(userId ->
                        findUserById(userId.toString())).orElse(voucher.getUser()))
                .build();

        voucherRepository.save(updatedVoucher);

        return voucherMapper.toVoucherDTO(voucher);
    }

    @Override
    public void delete(String voucherId) {
        Voucher voucher = findVoucherById(UUID.fromString(voucherId));
        voucherRepository.delete(voucher);
    }

    @Override
    public VoucherDTO changeHotStatus(String id, VoucherDTO voucherDTO) {
        Voucher voucher = findVoucherById(UUID.fromString(id));
        voucher.setHot(voucherDTO.getIsHot());

        voucherRepository.save(voucher);

        return voucherMapper.toVoucherDTO(voucher);
    }

    @Override
    public VoucherDTO updateStatus(String id, VoucherDTO voucherDTO) {
        Voucher voucher = findVoucherById(UUID.fromString(id));
        if (voucher.getStatus() != VoucherStatus.REGISTERED) {
            throw new VoucherException("Voucher not available for update");
        }

        voucher.setStatus(getCorrectEnumType(VoucherStatus.class, voucherDTO.getStatus()));
        voucherRepository.save(voucher);

        return voucherMapper.toVoucherDTO(voucher);
    }

    @Override
    public List<VoucherDTO> findAllByUserId(String userId) {
        return voucherRepository.findAllByUserId(UUID.fromString(userId))
                .stream()
                .sorted(Comparator.comparing(Voucher::isHot))
                .map(voucherMapper::toVoucherDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<VoucherDTO> findAllByTourType(TourType tourType) {
        return voucherRepository.findAllByTourType(tourType)
                .stream()
                .sorted(Comparator.comparing(Voucher::isHot))
                .map(voucherMapper::toVoucherDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<VoucherDTO> findAllByTransferType(String transferType) {
        return voucherRepository.findAllByTransferType(getCorrectEnumType(TransferType.class, transferType))
                .stream()
                .sorted(Comparator.comparing(Voucher::isHot))
                .map(voucherMapper::toVoucherDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<VoucherDTO> findAllByPrice(Double price) {
        return voucherRepository.findAllByPrice(price)
                .stream()
                .sorted(Comparator.comparing(Voucher::isHot))
                .map(voucherMapper::toVoucherDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<VoucherDTO> findAllByHotelType(HotelType hotelType) {
        return voucherRepository.findAllByHotelType(hotelType)
                .stream()
                .sorted(Comparator.comparing(Voucher::isHot))
                .map(voucherMapper::toVoucherDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<VoucherDTO> findAll() {
        return voucherRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(Voucher::isHot))
                .map(voucherMapper::toVoucherDTO)
                .collect(Collectors.toList());
    }
    //pageable methods
    @Override
    public Page<VoucherDTO> findAllByUserId(UUID userId, Pageable pageable) {
        return voucherRepository.findAllByUserId(userId, pageable)
                .map(voucherMapper::toVoucherDTO);
    }

    @Override
    public Page<VoucherDTO> findAllByTourType(TourType tourType, Pageable pageable) {
        return voucherRepository.findAllByTourType(tourType, pageable)
                .map(voucherMapper::toVoucherDTO);
    }

    @Override
    public Page<VoucherDTO> findAllByTransferType(String transferType, Pageable pageable) {
        return voucherRepository.findAllByTransferType(getCorrectEnumType(TransferType.class, transferType), pageable)
                .map(voucherMapper::toVoucherDTO);
    }

    @Override
    public Page<VoucherDTO> findAllByPrice(Double price, Pageable pageable) {
        return voucherRepository.findAllByPrice(price, pageable)
                .map(voucherMapper::toVoucherDTO);
    }

    @Override
    public Page<VoucherDTO> findAllByHotelType(HotelType hotelType, Pageable pageable) {
        return voucherRepository.findAllByHotelType(hotelType, pageable)
                .map(voucherMapper::toVoucherDTO);
    }

    @Override
    public Page<VoucherDTO> findAll(Pageable pageable) {
        return voucherRepository.findAll(pageable)
                .map(voucherMapper::toVoucherDTO);
    }

    private Voucher findVoucherById(UUID id) {
        return voucherRepository.findById(id)
                .orElseThrow(() -> new VoucherSearchException("Voucher not found!"));
    }

    private User findUserById(String id) {
        return userRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new UserSearchException("User not found"));
    }
}
