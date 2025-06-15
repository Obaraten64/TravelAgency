package com.epam.finaltask.restcontroller;

import com.epam.finaltask.auth.UserAdapter;
import com.epam.finaltask.auth.bruteforce.BruteForceAuthProvider;
import com.epam.finaltask.config.ApplicationConfig;
import com.epam.finaltask.config.SecurityConfig;
import com.epam.finaltask.dto.VoucherDTO;
import com.epam.finaltask.dto.request.CreateVoucherRequest;
import com.epam.finaltask.dto.request.UpdateVoucherRequest;
import com.epam.finaltask.exception.exceptions.VoucherException;
import com.epam.finaltask.model.*;
import com.epam.finaltask.service.AuthenticationService;
import com.epam.finaltask.service.RefreshTokenService;
import com.epam.finaltask.service.UserService;
import com.epam.finaltask.service.VoucherService;
import com.epam.finaltask.token.JWTService;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.log4j.Log4j2;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VoucherRestController.class)
//importing configuration
@Import({SecurityConfig.class, ApplicationConfig.class})
@Log4j2
public class VoucherRestControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private VoucherService voucherService;
    @MockBean
    private BruteForceAuthProvider bruteForceAuthProvider;
    @MockBean
    private JWTService jwtService;
    @MockBean
    private UserDetailsService userDetailsService;
    @MockBean
    private RefreshTokenService refreshTokenService;
    @MockBean
    private UserService userService;
    @MockBean
    private AuthenticationService authenticationService;

    //perform authorization before every request
    String username = "username";
    UserAdapter userDetails = new UserAdapter(User.builder()
            .username(username)
            .password("123")
            .role(Role.ADMIN)
            .build());
    @BeforeEach
    void setup() {
        //immediately pre-populate SecurityContext with authentication instead of doing it in JWT filter
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails,
                        null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    @DisplayName("Test GET / endpoint")
    void testGetVouchers() throws Exception {
        Pageable pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "isHot"));
        Page<VoucherDTO> page = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(voucherService.findAll(any(Pageable.class))).thenReturn(page);

        var request = get("/api/vouchers/");
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(page)));

        verify(voucherService, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("Test GET /user/{userId} endpoint")
    void testGetVouchersByUser() throws Exception {
        UUID userId = UUID.randomUUID();
        VoucherDTO hotVoucher = VoucherDTO.builder()
                .title("Hot Voucher")
                .userId(userId)
                .isHot(true)
                .build();
        VoucherDTO voucher1 = VoucherDTO.builder()
                .title("voucher1")
                .userId(userId)
                .isHot(false)
                .build();
        VoucherDTO voucher2 = VoucherDTO.builder()
                .title("voucher2")
                .userId(userId)
                .isHot(false)
                .build();
        List<VoucherDTO> vouchers = List.of(hotVoucher, voucher1, voucher2);
        Pageable pageable = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "isHot"));
        Page<VoucherDTO> page = new PageImpl<>(vouchers, pageable, 3);

        when(voucherService.findAllByUserId(eq(userId), any(Pageable.class))).thenReturn(page);

        var request = get("/api/vouchers/user/{userId}", userId)
                .param("size", "3");
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(page)));

        verify(voucherService, times(1)).findAllByUserId(eq(userId), any(Pageable.class));
    }

    @Test
    @DisplayName("Test GET /tour/{tourType} endpoint")
    void testGetVouchersByTourType() throws Exception {
        TourType tourType = TourType.SAFARI;
        Pageable pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "isHot"));
        Page<VoucherDTO> page = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(voucherService.findAllByTourType(eq(tourType), any(Pageable.class))).thenReturn(page);

        var request = get("/api/vouchers/tour/{tourType}", tourType.name());
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(page)));

        verify(voucherService, times(1)).findAllByTourType(eq(tourType), any(Pageable.class));
    }

    @Test
    @DisplayName("Test GET /transfer/{transferType} endpoint")
    void testGetVouchersByTransferType() throws Exception {
        String type = TransferType.BUS.name();
        Pageable pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "isHot"));
        Page<VoucherDTO> page = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(voucherService.findAllByTransferType(eq(type), any(Pageable.class))).thenReturn(page);

        var request = get("/api/vouchers/transfer/{transferType}", type);
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(page)));

        verify(voucherService, times(1)).findAllByTransferType(eq(type), any(Pageable.class));
    }

    @Test
    @DisplayName("Test GET /transfer/{transferType} endpoint, invalid type")
    void testGetVouchersByTransferType_InvalidType() throws Exception {
        String type = "invalid";

        when(voucherService.findAllByTransferType(eq(type), any(Pageable.class)))
                .thenThrow(new VoucherException("Wrong TransferType provider"));

        var request = get("/api/vouchers/transfer/{transferType}", type);
        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(content().string("{\"exception\":\"Wrong TransferType provider\"}"));

        verify(voucherService, times(1)).findAllByTransferType(eq(type), any(Pageable.class));
    }

    @Test
    @DisplayName("Test GET /price/{price} endpoint")
    void testGetVouchersByPrice() throws Exception {
        Double price = 0.0;
        Pageable pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "isHot"));
        Page<VoucherDTO> page = new PageImpl<>(List.of(VoucherDTO.builder()
                .title("voucher1")
                .price(price)
                .build()), pageable, 0);

        when(voucherService.findAllByPrice(eq(price), any(Pageable.class))).thenReturn(page);

        var request = get("/api/vouchers/price/{price}", price);
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(page)));

        verify(voucherService, times(1)).findAllByPrice(eq(price), any(Pageable.class));
    }

    @Test
    @DisplayName("Test GET /hotel/{hotelType} endpoint")
    void testGetVouchersByHotelType() throws Exception {
        HotelType type = HotelType.FIVE_STARS;
        Pageable pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "isHot"));
        Page<VoucherDTO> page = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(voucherService.findAllByHotelType(eq(type), any(Pageable.class))).thenReturn(page);

        var request = get("/api/vouchers/hotel/{hotelType}", type.name());
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(page)));

        verify(voucherService, times(1)).findAllByHotelType(eq(type), any(Pageable.class));
    }

    @Test
    @DisplayName("Test GET /hotel/{hotelType} endpoint, unauthenticated")
    void testGetVouchersByHotelType_Unauthenticated() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(null);

        var request = get("/api/vouchers/hotel/{hotelType}", HotelType.FIVE_STARS.name());
        mockMvc.perform(request)
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Test POST / endpoint")
    void testCreateVoucher() throws Exception {
        String title = "voucher";
        String description = "description";
        Double price = 5.0;
        String tourType = "tour";
        String transferType = "transfer";
        String status = "registered";
        LocalDate arrival = LocalDate.now();
        LocalDate eviction = LocalDate.now().plusDays(1);
        String hotelType = "FIVE_STARS";
        boolean isHot = true;
        var createVoucher = CreateVoucherRequest.builder()
                .title(title)
                .description(description)
                .price(price)
                .tourType(tourType)
                .transferType(transferType)
                .status(status)
                .arrivalDate(arrival)
                .evictionDate(eviction)
                .hotelType(hotelType)
                .isHot(isHot)
                .build();

        var expect = VoucherDTO.builder()
                .title(title)
                .description(description)
                .price(price)
                .tourType(tourType)
                .transferType(transferType)
                .status(status)
                .arrivalDate(arrival)
                .evictionDate(eviction)
                .hotelType(hotelType)
                .isHot(isHot)
                .build();

        when(voucherService.create(expect)).thenReturn(expect);

        var request = post("/api/vouchers/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(createVoucher))
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(expect)));

        verify(voucherService, times(1)).create(any(VoucherDTO.class));
    }

    @Test
    @DisplayName("Test PATCH /{voucherId} endpoint")
    void testUpdateVoucher() throws Exception {
        UUID voucherId = UUID.randomUUID();
        String title = "voucher";
        var createVoucher = UpdateVoucherRequest.builder()
                .title(title)
                .build();
        var toUpdate = VoucherDTO.builder()
                .title(title)
                .build();

        var expect = VoucherDTO.builder()
                .id(voucherId.toString())
                .title(title)
                .description("description")
                .price(5.0)
                .tourType("tour")
                .transferType("transfer")
                .status("registered")
                .arrivalDate(LocalDate.now())
                .evictionDate(LocalDate.now().plusDays(1))
                .hotelType("FIVE_STARS")
                .isHot(false)
                .build();

        when(voucherService.update(voucherId.toString(), toUpdate)).thenReturn(expect);

        var request = patch("/api/vouchers/{voucherId}", voucherId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(createVoucher))
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(expect)));

        verify(voucherService, times(1)).update(voucherId.toString(), toUpdate);
    }

    @Test
    @DisplayName("Test PATCH /{voucherId} endpoint, wrong authority")
    void testUpdateVoucher_WrongAuthority() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(null);
        UserAdapter userDetails = new UserAdapter(User.builder()
                .username("manager")
                .password("123")
                .role(Role.MANAGER)
                .build());
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails,
                        null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        var createVoucher = UpdateVoucherRequest.builder()
                .title("voucher")
                .build();

        var request = patch("/api/vouchers/{voucherId}", UUID.randomUUID().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(createVoucher))
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(request)
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Test DELETE /{voucherId} endpoint")
    void testDeleteVoucher() throws Exception {
        UUID voucherId = UUID.randomUUID();

        var request = delete("/api/vouchers/{voucherId}", voucherId.toString())
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().string(String.format("Voucher with Id %s has been deleted", voucherId)));

        verify(voucherService, times(1)).delete(voucherId.toString());
    }

    @Test
    @DisplayName("Test PATCH /{voucherId}/status endpoint")
    void testChangeHotStatusVoucher() throws Exception {
        UUID voucherId = UUID.randomUUID();

        var statusUpdate = VoucherDTO.builder()
                .isHot(false)
                .build();

        var expect = VoucherDTO.builder()
                .id(voucherId.toString())
                .title("title")
                .description("description")
                .price(5.0)
                .tourType("tour")
                .transferType("transfer")
                .status("registered")
                .arrivalDate(LocalDate.now())
                .evictionDate(LocalDate.now().plusDays(1))
                .hotelType("FIVE_STARS")
                .isHot(false)
                .build();

        when(voucherService.changeHotStatus(voucherId.toString(), statusUpdate)).thenReturn(expect);

        var request = patch("/api/vouchers/{voucherId}/status", voucherId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(false))
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().string("Voucher hot status is successfully changed"));

        verify(voucherService, times(1)).changeHotStatus(voucherId.toString(), statusUpdate);
    }

    @Test
    @DisplayName("Test PATCH /{voucherId}/status endpoint, invalid boolean")
    void testChangeHotStatusVoucher_InvalidBoolean() throws Exception {
        var request = patch("/api/vouchers/{voucherId}/status", UUID.randomUUID().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString("asdasdasd"))
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Test PUT /{voucherId}/status endpoint")
    void testChangeStatusVoucher() throws Exception {
        UUID voucherId = UUID.randomUUID();

        var statusUpdate = VoucherDTO.builder()
                .status(VoucherStatus.PAID.name())
                .build();

        var expect = VoucherDTO.builder()
                .id(voucherId.toString())
                .title("title")
                .description("description")
                .price(5.0)
                .tourType("tour")
                .transferType("transfer")
                .status(VoucherStatus.PAID.name())
                .arrivalDate(LocalDate.now())
                .evictionDate(LocalDate.now().plusDays(1))
                .hotelType("FIVE_STARS")
                .isHot(false)
                .build();

        when(voucherService.updateStatus(voucherId.toString(), statusUpdate)).thenReturn(expect);

        var request = put("/api/vouchers/{voucherId}/status", voucherId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(VoucherStatus.PAID))
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().string("Voucher status is successfully changed"));

        verify(voucherService, times(1)).updateStatus(voucherId.toString(), statusUpdate);
    }

    @Test
    @DisplayName("Test PUT /{voucherId}/status endpoint, voucher not available for update")
    void testChangeStatusVoucher_NotAvailableForUpdate() throws Exception {
        UUID voucherId = UUID.randomUUID();

        var statusUpdate = VoucherDTO.builder()
                .status(VoucherStatus.PAID.name())
                .build();

        when(voucherService.updateStatus(voucherId.toString(), statusUpdate))
                .thenThrow(new VoucherException("Voucher not available for update"));

        var request = put("/api/vouchers/{voucherId}/status", voucherId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(VoucherStatus.PAID))
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(content().string("{\"exception\":\"Voucher not available for update\"}"));

        verify(voucherService, times(1)).updateStatus(voucherId.toString(), statusUpdate);
    }

    @Test
    @DisplayName("Test PATCH /{voucherId}/order endpoint")
    void testOrderVoucher() throws Exception {
        UUID voucherId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        var expect = VoucherDTO.builder()
                .id(voucherId.toString())
                .title("title")
                .description("description")
                .price(5.0)
                .tourType("tour")
                .transferType("transfer")
                .status(VoucherStatus.PAID.name())
                .arrivalDate(LocalDate.now())
                .evictionDate(LocalDate.now().plusDays(1))
                .hotelType("FIVE_STARS")
                .userId(userId)
                .isHot(false)
                .build();

        when(voucherService.order(voucherId.toString(), userId.toString())).thenReturn(expect);

        var request = patch("/api/vouchers/{voucherId}/order", voucherId.toString())
                .param("userId", userId.toString())
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().string("Voucher is successfully ordered"));

        verify(voucherService, times(1)).order(voucherId.toString(), userId.toString());
    }

    @Test
    @DisplayName("Test PATCH /{voucherId}/order endpoint, not enough balance")
    void testOrderVoucher_NotEnoughBalance() throws Exception {
        UUID voucherId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(voucherService.order(voucherId.toString(), userId.toString()))
                .thenThrow(new VoucherException("Sorry! You don't have enough balance to order this tour"));

        var request = patch("/api/vouchers/{voucherId}/order", voucherId.toString())
                .param("userId", userId.toString())
                .accept(MediaType.APPLICATION_JSON);
        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(content().string("{\"exception\":\"Sorry! You don't have enough balance to order this tour\"}"));

        verify(voucherService, times(1)).order(voucherId.toString(), userId.toString());
    }
}
