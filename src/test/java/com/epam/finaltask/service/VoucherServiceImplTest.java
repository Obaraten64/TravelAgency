package com.epam.finaltask.service;

import com.epam.finaltask.dto.VoucherDTO;
import com.epam.finaltask.exception.exceptions.UserSearchException;
import com.epam.finaltask.exception.exceptions.VoucherException;
import com.epam.finaltask.mapper.VoucherMapper;
import com.epam.finaltask.model.*;
import com.epam.finaltask.repository.UserRepository;
import com.epam.finaltask.repository.VoucherRepository;

import org.assertj.core.data.Index;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VoucherServiceImplTest {

    @Mock
    private VoucherRepository voucherRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private VoucherMapper voucherMapper;

    @InjectMocks
    private VoucherServiceImpl voucherService;

    @Test
    @DisplayName("Create voucher")
    public void testCreateVoucher() {
        //Given
        Voucher voucher = new Voucher();
        VoucherDTO voucherDTO = new VoucherDTO();
        //When
        when(voucherMapper.toVoucher(voucherDTO)).thenReturn(voucher);
        when(voucherRepository.save(voucher)).thenReturn(voucher);
        when(voucherMapper.toVoucherDTO(voucher)).thenReturn(voucherDTO);
        //Then
        assertThat(voucherService.create(voucherDTO)).isEqualTo(voucherDTO);

        verify(voucherMapper, times(1)).toVoucher(voucherDTO);
        verify(voucherRepository, times(1)).save(any(Voucher.class));
        verify(voucherMapper, times(1)).toVoucherDTO(any(Voucher.class));
    }

    @Test
    @DisplayName("Order voucher")
    public void testOrderVoucher() {
        UUID userId = UUID.randomUUID();
        UUID voucherId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .balance(BigDecimal.valueOf(150L))
                .vouchers(new LinkedList<>())
                .build();
        Voucher voucher = Voucher.builder()
                .id(voucherId)
                .title("Title")
                .status(VoucherStatus.REGISTERED)
                .price(100D)
                .build();
        VoucherDTO voucherDTO = VoucherDTO.builder()
                .id(voucherId.toString())
                .title("Title")
                .status(VoucherStatus.PAID.name())
                .price(100D)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(voucherRepository.findById(voucherId)).thenReturn(Optional.of(voucher));
        when(voucherMapper.toVoucherDTO(any(Voucher.class))).thenReturn(voucherDTO);

        VoucherDTO result = voucherService.order(voucherId.toString(), userId.toString());

        assertThat(result).isEqualTo(voucherDTO);

        verify(userRepository, times(1)).findById(userId);
        verify(voucherRepository, times(1)).findById(voucherId);
        verify(userRepository, times(1)).save(any(User.class));
        verify(voucherRepository, times(1)).save(any(Voucher.class));
        verify(voucherMapper, times(1)).toVoucherDTO(any(Voucher.class));
    }

    @Test
    @DisplayName("Order voucher, user not found")
    public void testOrderVoucher_UserNotFound() {
        UUID userId = UUID.randomUUID();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> voucherService.order(UUID.randomUUID().toString(), userId.toString()))
                .isInstanceOf(UserSearchException.class)
                .hasMessage("User not found");

        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    @DisplayName("Order voucher, voucher not available")
    public void testOrderVoucher_VoucherNotAvailable() {
        UUID userId = UUID.randomUUID();
        UUID voucherId = UUID.randomUUID();
        Voucher voucher = Voucher.builder()
                .id(voucherId)
                .title("Title")
                .status(VoucherStatus.CANCELED)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));
        when(voucherRepository.findById(voucherId)).thenReturn(Optional.of(voucher));

        assertThatThrownBy(() -> voucherService.order(voucherId.toString(), userId.toString()))
                .isInstanceOf(VoucherException.class)
                .hasMessage("Voucher not available");

        verify(userRepository, times(1)).findById(userId);
        verify(voucherRepository, times(1)).findById(voucherId);
    }

    @Test
    @DisplayName("Order voucher, user do not have enough money")
    public void testOrderVoucher_UserNotEnoughMoney() {
        UUID userId = UUID.randomUUID();
        UUID voucherId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .balance(BigDecimal.ZERO)
                .build();
        Voucher voucher = Voucher.builder()
                .id(voucherId)
                .title("Title")
                .status(VoucherStatus.REGISTERED)
                .price(100D)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(voucherRepository.findById(voucherId)).thenReturn(Optional.of(voucher));

        assertThatThrownBy(() -> voucherService.order(voucherId.toString(), userId.toString()))
                .isInstanceOf(VoucherException.class)
                .hasMessage("Sorry! You don't have enough balance to order this tour");

        verify(userRepository, times(1)).findById(userId);
        verify(voucherRepository, times(1)).findById(voucherId);
    }

    @Test
    @DisplayName("Update voucher")
    public void testUpdateVoucher() {
        UUID id = UUID.randomUUID();
        Voucher voucher = Voucher.builder()
                .id(id)
                .build();
        VoucherDTO voucherDTO = VoucherDTO.builder()
                .id(id.toString())
                .build();

        when(voucherRepository.findById(id)).thenReturn(Optional.of(voucher));
        when(voucherMapper.toVoucherDTO(any(Voucher.class))).thenReturn(voucherDTO);

        VoucherDTO result = voucherService.update(id.toString(), voucherDTO);

        assertThat(result).isEqualTo(voucherDTO);

        verify(voucherRepository, times(1)).findById(id);
        verify(voucherRepository, times(1)).save(any(Voucher.class));
        verify(voucherMapper, times(1)).toVoucherDTO(voucher);
    }

    @Test
    @DisplayName("Delete voucher")
    public void testDeleteVoucher() {
        UUID id = UUID.randomUUID();
        User owner = User.builder()
                .id(UUID.randomUUID())
                .vouchers(new ArrayList<>())
                .build();
        Voucher voucher = Voucher.builder()
                .id(id)
                .user(owner)
                .build();
        owner.getVouchers().add(voucher);

        when(voucherRepository.findById(id)).thenReturn(Optional.of(voucher));

        voucherService.delete(id.toString());

        verify(voucherRepository, times(1)).findById(id);
        verify(voucherRepository, times(1)).delete(any(Voucher.class));
    }

    @Test
    @DisplayName("Change voucher hot status")
    public void testChangeHotStatusVoucher() {
        UUID id = UUID.randomUUID();
        VoucherDTO voucherDTO = VoucherDTO.builder()
                .id(id.toString())
                .isHot(false)
                .build();
        Voucher voucher = Voucher.builder()
                .id(id)
                .isHot(true)
                .build();

        when(voucherRepository.findById(id)).thenReturn(Optional.of(voucher));
        when(voucherMapper.toVoucherDTO(any(Voucher.class))).thenReturn(voucherDTO);

        VoucherDTO result = voucherService.changeHotStatus(id.toString(), voucherDTO);

        assertThat(result).isEqualTo(voucherDTO);

        verify(voucherRepository, times(1)).findById(id);
        verify(voucherRepository, times(1)).save(any(Voucher.class));
        verify(voucherMapper, times(1)).toVoucherDTO(any(Voucher.class));
    }

    @Test
    @DisplayName("Change voucher status")
    public void testChangeStatusVoucher() {
        UUID id = UUID.randomUUID();
        VoucherDTO voucherDTO = VoucherDTO.builder()
                .id(id.toString())
                .status("PAID")
                .build();
        Voucher voucher = Voucher.builder()
                .id(id)
                .status(VoucherStatus.REGISTERED)
                .build();

        when(voucherRepository.findById(id)).thenReturn(Optional.of(voucher));
        when(voucherMapper.toVoucherDTO(any(Voucher.class))).thenReturn(voucherDTO);

        VoucherDTO result = voucherService.updateStatus(id.toString(), voucherDTO);

        assertThat(result).isEqualTo(voucherDTO);

        verify(voucherRepository, times(1)).findById(id);
        verify(voucherRepository, times(1)).save(any(Voucher.class));
        verify(voucherMapper, times(1)).toVoucherDTO(any(Voucher.class));
    }

    @Test
    @DisplayName("Change voucher status, not updatable")
    public void testChangeStatusVoucher_NotUpdatable() {
        UUID id = UUID.randomUUID();
        VoucherDTO voucherDTO = VoucherDTO.builder()
                .id(id.toString())
                .status("PAID")
                .build();
        Voucher voucher = Voucher.builder()
                .id(id)
                .status(VoucherStatus.PAID)
                .build();

        when(voucherRepository.findById(id)).thenReturn(Optional.of(voucher));

        assertThatThrownBy(() -> voucherService.updateStatus(id.toString(), voucherDTO))
                .isInstanceOf(VoucherException.class)
                .hasMessage("Voucher not available for update");

        verify(voucherRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Change voucher status, wrong status")
    public void testChangeStatusVoucher_WrongStatus() {
        UUID id = UUID.randomUUID();
        VoucherDTO voucherDTO = VoucherDTO.builder()
                .id(id.toString())
                .status("PAID_BLABLA")
                .build();
        Voucher voucher = Voucher.builder()
                .id(id)
                .status(VoucherStatus.REGISTERED)
                .build();

        when(voucherRepository.findById(id)).thenReturn(Optional.of(voucher));

        assertThatThrownBy(() -> voucherService.updateStatus(id.toString(), voucherDTO))
                .isInstanceOf(VoucherException.class)
                .hasMessage("Wrong VoucherStatus provided");

        verify(voucherRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Get vouchers by user id")
    public void testGetVouchersByUserId() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .username("vadimliakh")
                .build();
        //vouchers
        Voucher voucher1 = Voucher.builder()
                .user(user)
                .isHot(true)
                .build();
        Voucher voucher2 = Voucher.builder()
                .user(user)
                .isHot(false)
                .build();
        Voucher voucher3 = Voucher.builder()
                .user(user)
                .isHot(false)
                .build();
        List<Voucher> vouchers = List.of(voucher2, voucher1, voucher3);
        //voucher dtos
        VoucherDTO voucherDTO1 = VoucherDTO.builder()
                .userId(userId)
                .isHot(true)
                .build();
        VoucherDTO voucherDTO2 = VoucherDTO.builder()
                .userId(userId)
                .isHot(false)
                .build();
        VoucherDTO voucherDTO3 = VoucherDTO.builder()
                .userId(userId)
                .isHot(false)
                .build();

        when(voucherRepository.findAllByUserId(userId)).thenReturn(vouchers);
        when(voucherMapper.toVoucherDTO(any(Voucher.class))).thenReturn(voucherDTO1, voucherDTO2, voucherDTO3);

        List<VoucherDTO> result = voucherService.findAllByUserId(userId.toString());

        assertThat(result)
                .hasOnlyElementsOfType(VoucherDTO.class)
                .hasSize(3)
                .contains(voucherDTO1, Index.atIndex(0));

        verify(voucherRepository, times(1)).findAllByUserId(userId);
        verify(voucherMapper, times(3)).toVoucherDTO(any(Voucher.class));
    }

    @Test
    @DisplayName("Get vouchers by tour type")
    public void testGetVouchersByTourType() {
        when(voucherRepository.findAllByTourType(TourType.SAFARI)).thenReturn(Collections.emptyList());

        List<VoucherDTO> result = voucherService.findAllByTourType(TourType.SAFARI);

        assertThat(result).isEmpty();

        verify(voucherRepository, times(1)).findAllByTourType(TourType.SAFARI);
    }

    @Test
    @DisplayName("Get vouchers by transfer type")
    public void testGetVouchersByTransferType() {
        Voucher voucher = Voucher.builder()
                .transferType(TransferType.JEEPS)
                .isHot(false)
                .build();
        VoucherDTO voucherDTO = VoucherDTO.builder()
                .transferType(TransferType.JEEPS.name())
                .isHot(false)
                .build();

        when(voucherRepository.findAllByTransferType(TransferType.JEEPS)).thenReturn(List.of(voucher));
        when(voucherMapper.toVoucherDTO(any(Voucher.class))).thenReturn(voucherDTO);

        List<VoucherDTO> result = voucherService.findAllByTransferType(TransferType.JEEPS.name());

        assertThat(result)
                .hasOnlyElementsOfType(VoucherDTO.class)
                .hasSize(1)
                .contains(voucherDTO, Index.atIndex(0));

        verify(voucherRepository, times(1)).findAllByTransferType(TransferType.JEEPS);
        verify(voucherMapper, times(1)).toVoucherDTO(any(Voucher.class));
    }

    @Test
    @DisplayName("Get vouchers by price")
    public void testGetVouchersByPrice() {
        when(voucherRepository.findAllByPrice(0D)).thenReturn(Collections.emptyList());

        List<VoucherDTO> result = voucherService.findAllByPrice(0D);

        assertThat(result).isEmpty();

        verify(voucherRepository, times(1)).findAllByPrice(0D);
    }

    @Test
    @DisplayName("Get vouchers by hotel type")
    public void testGetVouchersByHotelType() {
        when(voucherRepository.findAllByHotelType(HotelType.FIVE_STARS)).thenReturn(Collections.emptyList());

        List<VoucherDTO> result = voucherService.findAllByHotelType(HotelType.FIVE_STARS);

        assertThat(result).isEmpty();

        verify(voucherRepository, times(1)).findAllByHotelType(HotelType.FIVE_STARS);
    }

    @Test
    @DisplayName("Get vouchers")
    public void testGetVouchers() {
        when(voucherRepository.findAll()).thenReturn(Collections.emptyList());

        List<VoucherDTO> result = voucherService.findAll();

        assertThat(result).isEmpty();

        verify(voucherRepository, times(1)).findAll();
    }
}
