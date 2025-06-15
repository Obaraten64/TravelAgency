package com.epam.finaltask.viewcontroller;

import com.epam.finaltask.auth.UserAdapter;
import com.epam.finaltask.auth.bruteforce.BruteForceAuthProvider;
import com.epam.finaltask.config.ApplicationConfig;
import com.epam.finaltask.config.SecurityConfig;
import com.epam.finaltask.dto.VoucherDTO;
import com.epam.finaltask.dto.request.CreateVoucherRequest;
import com.epam.finaltask.dto.request.UpdateVoucherRequest;
import com.epam.finaltask.model.*;
import com.epam.finaltask.restcontroller.VoucherRestController;
import com.epam.finaltask.restcontroller.viewscontroller.VoucherController;
import com.epam.finaltask.service.AuthenticationService;
import com.epam.finaltask.service.RefreshTokenService;
import com.epam.finaltask.service.UserService;
import com.epam.finaltask.service.VoucherService;
import com.epam.finaltask.token.JWTService;

import lombok.extern.log4j.Log4j2;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VoucherController.class)
//importing configuration
@Import({SecurityConfig.class, ApplicationConfig.class})
@Log4j2
public class VoucherControllerTest {
    @Autowired
    private MockMvc mockMvc;

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
    @MockBean
    private VoucherRestController voucherRestController;

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
    @DisplayName("Test GET /search/{searchType} endpoint")
    public void testSearchEndpoint() throws Exception {
        String searchType = "searchType";

        var request = get("/vouchers/search/{searchType}", searchType);
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(model().attribute("type", searchType.toLowerCase()))
                .andExpect(view().name("/tour/search"));
    }

    @Test
    @DisplayName("Test GET / endpoint")
    public void testGetVouchersEndpoint() throws Exception {
        int pageNumber = 0;
        int pageSize = 5;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<VoucherDTO> vouchers = new PageImpl<>(Collections.emptyList(), pageable, 0);

        String linkStarter = "?pageSize=" + pageSize;

        when(voucherRestController.getVouchers(pageable)).thenReturn(vouchers);

        var request = get("/vouchers/");
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(model().attribute("linkPrevious",
                        linkStarter + "&pageNumber=" + (pageNumber - 1)))
                .andExpect(model().attribute("linkForward",
                        linkStarter + "&pageNumber=" + (pageNumber + 1)))
                .andExpect(model().attribute("vouchers", vouchers))
                .andExpect(view().name("/tour/dashboard"));

        verify(voucherRestController, times(1)).getVouchers(pageable);
    }

    @Test
    @DisplayName("Test GET /user endpoint")
    public void testGetVouchersByUserEndpoint() throws Exception {
        UUID uuid = UUID.randomUUID();
        int pageNumber = 1;
        int pageSize = 5;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<VoucherDTO> vouchers = new PageImpl<>(Collections.emptyList(), pageable, 0);

        String linkStarter = "?id=" + uuid + "&pageSize=" + pageSize;

        when(voucherRestController.getVouchersByUser(uuid, pageable)).thenReturn(vouchers);

        var request = get("/vouchers/user")
                .param("id", uuid.toString())
                .param("pageNumber", String.valueOf(pageNumber));
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(model().attribute("linkPrevious",
                        linkStarter + "&pageNumber=" + (pageNumber - 1)))
                .andExpect(model().attribute("linkForward",
                        linkStarter + "&pageNumber=" + (pageNumber + 1)))
                .andExpect(model().attribute("vouchers", vouchers))
                .andExpect(view().name("/tour/dashboard"));

        verify(voucherRestController, times(1)).getVouchersByUser(uuid, pageable);
    }

    @Test
    @DisplayName("Test GET /create endpoint")
    public void testCreateVoucherViewEndpoint() throws Exception {
        var request = get("/vouchers/create");
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(model().attribute("operation", "create"))
                .andExpect(model().attribute("voucher", new VoucherDTO()))
                .andExpect(model().attribute("tours", TourType.values()))
                .andExpect(model().attribute("transfers", TransferType.values()))
                .andExpect(model().attribute("hotels", HotelType.values()))
                .andExpect(model().attribute("statuses", VoucherStatus.values()))
                .andExpect(view().name("/tour/create-update"));
    }

    @Test
    @DisplayName("Test POST /create endpoint")
    public void testCreateVoucherEndpoint() throws Exception {
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

        when(voucherRestController.createVoucher(createVoucher)).thenReturn(expect);

        var request = post("/vouchers/create")
                .flashAttr("voucher", createVoucher);
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(model().attribute("message", "Created voucher successfully. Id: "
                        + expect.getId()))
                .andExpect(view().name("/tour/success"));

        verify(voucherRestController, times(1)).createVoucher(createVoucher);
    }

    @Test
    @DisplayName("Test GET /{voucherId}/update endpoint")
    public void testUpdateVoucherViewEndpoint() throws Exception {
        UUID uuid = UUID.randomUUID();

        var request = get("/vouchers/{voucherId}/update", uuid);
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(model().attribute("operation", "update"))
                .andExpect(model().attribute("id", uuid))
                .andExpect(model().attribute("voucher", new VoucherDTO()))
                .andExpect(model().attribute("tours", TourType.values()))
                .andExpect(model().attribute("transfers", TransferType.values()))
                .andExpect(model().attribute("hotels", HotelType.values()))
                .andExpect(model().attribute("statuses", VoucherStatus.values()))
                .andExpect(view().name("/tour/create-update"));
    }

    @Test
    @DisplayName("Test POST /{voucherId}/update endpoint")
    void testUpdateVoucher() throws Exception {
        UUID voucherId = UUID.randomUUID();
        String title = "voucher";
        var createVoucher = UpdateVoucherRequest.builder()
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

        when(voucherRestController.updateVoucher(voucherId, createVoucher)).thenReturn(expect);

        var request = post("/vouchers/{voucherId}/update", voucherId)
                .flashAttr("voucher", createVoucher);
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(model().attribute("message", "Updated voucher successfully. Id: "
                        + expect.getId()))
                .andExpect(view().name("/tour/success"));

        verify(voucherRestController, times(1)).updateVoucher(voucherId, createVoucher);
    }

    @Test
    @DisplayName("Test GET /{voucherId}/delete endpoint")
    void testDeleteVoucher() throws Exception {
        UUID voucherId = UUID.randomUUID();
        String answer = String.format("Voucher with Id %s has been deleted", voucherId);
        var response = new ResponseEntity<>(answer, HttpStatus.OK);
        log.error(SecurityContextHolder.getContext().getAuthentication());

        when(voucherRestController.deleteVoucher(voucherId)).thenReturn(response);

        var request = get("/vouchers/{voucherId}/delete", voucherId.toString());
        mockMvc.perform(request)
                .andExpect(model().attribute("message", answer))
                .andExpect(view().name("/tour/success"));

        verify(voucherRestController, times(1)).deleteVoucher(voucherId);
    }

    @Test
    @DisplayName("Test GET /{voucherId}/delete endpoint, unauthorized")
    void testDeleteVoucher_Unauthorized() throws Exception {
        UUID voucherId = UUID.randomUUID();
        SecurityContextHolder.getContext().setAuthentication(null);

        var request = get("/vouchers/{voucherId}/delete", voucherId.toString());
        mockMvc.perform(request)
                .andExpect(status().isForbidden());
    }
}
