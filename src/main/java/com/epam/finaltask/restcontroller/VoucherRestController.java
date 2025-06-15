package com.epam.finaltask.restcontroller;

import com.epam.finaltask.dto.VoucherDTO;
import com.epam.finaltask.dto.request.CreateVoucherRequest;
import com.epam.finaltask.dto.request.UpdateVoucherRequest;
import com.epam.finaltask.model.HotelType;
import com.epam.finaltask.model.TourType;
import com.epam.finaltask.model.VoucherStatus;
import com.epam.finaltask.service.VoucherService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static com.epam.finaltask.mapper.EnumConvertor.getCorrectEnumType;

@RestController
@RequestMapping(path = "/api/vouchers")
@RequiredArgsConstructor
@Log4j2
@SecurityRequirement(name = "Bearer Authentication")
public class VoucherRestController {
    private final VoucherService voucherService;

    @Operation(summary = "Get all tours")
    @ApiResponse(responseCode = "200",
            description = "All tours",
            content = @Content(
                    schema = @Schema(implementation = VoucherDTO.class),
                    examples = @ExampleObject(
                            value = "{[\"title\": \"Tour Title\"," +
                                    "\"description\": \"Tour Description\"," +
                                    "\"price\":35.35,\"tourType\":\"HEALTH\"," +
                                    "\"transferType\":\"BUS\",\"hotelType\":\"ONE_STAR\"," +
                                    "\"voucherStatus\":\"REGISTERED\"," +
                                    "\"arrivalDate\":\"2024-05-19T09:01:06\",\"evictionDate\":\"2024-05-19T09:01:06\"," +
                                    "\"isHot\": \"true\"]}")))
	@GetMapping(path ="/")
    public Page<VoucherDTO> getVouchers(@PageableDefault(size = 5) Pageable pageable) {
        log.info("Request to get all vouchers");
        Pageable actualPageable = getPageableWithSort(pageable);

        log.info("Page of vouchers. Size: {}. Page: {}. Sort: {}",
                actualPageable.getPageSize(), actualPageable.getPageNumber(), actualPageable.getSort().toString());
        return voucherService.findAll(actualPageable);
    }

    @Operation(summary = "Get all tours by user")
    @ApiResponse(responseCode = "200",
            description = "All tours order by user with specific id",
            content = @Content(
                    schema = @Schema(implementation = VoucherDTO.class),
                    examples = @ExampleObject(
                            value = "{[\"title\": \"Tour Title\"," +
                                    "\"description\": \"Tour Description\"," +
                                    "\"price\":35.35,\"tourType\":\"HEALTH\"," +
                                    "\"transferType\":\"BUS\",\"hotelType\":\"ONE_STAR\"," +
                                    "\"voucherStatus\":\"REGISTERED\"," +
                                    "\"arrivalDate\":\"2024-05-19T09:01:06\",\"evictionDate\":\"2024-05-19T09:01:06\"," +
                                    "\"userId\":\"00000000-0000-0000-0000-000000000000\"," +
                                    "\"isHot\": \"true\"]}")))
    @GetMapping(path = "/user/{userId}")
    public Page<VoucherDTO> getVouchersByUser(@PathVariable("userId") UUID userId,
            @PageableDefault(size = 5) Pageable pageable) {
        log.info("Request to get all vouchers by user with id: {}", userId);
        Pageable actualPageable = getPageableWithSort(pageable);

        log.info("Page of vouchers by user with id: {}. Size: {}. Page: {}. Sort: {}",
                userId, actualPageable.getPageSize(),
                actualPageable.getPageNumber(), actualPageable.getSort().toString());
        return voucherService.findAllByUserId(userId, pageable);
    }

    @Operation(summary = "Create new tour, \"ROLE_ADMIN\" role required")
    @ApiResponse(responseCode = "200",
            description = "Created tour data",
            content = @Content(
                    schema = @Schema(implementation = VoucherDTO.class),
                    examples = @ExampleObject(
                            value = "{\"title\": \"Tour Title\"," +
                                    "\"description\": \"Tour Description\"," +
                                    "\"price\":35.35,\"tourType\":\"HEALTH\"," +
                                    "\"transferType\":\"BUS\",\"hotelType\":\"ONE_STAR\"," +
                                    "\"voucherStatus\":\"REGISTERED\"," +
                                    "\"arrivalDate\":\"2024-05-19T09:01:06\",\"evictionDate\":\"2024-05-19T09:01:06\"," +
                                    "\"isHot\": \"false\"}")))
    @PostMapping("/")
    public VoucherDTO createVoucher(@RequestBody @Valid CreateVoucherRequest voucher) {
        log.info("Request to create new tour");
        VoucherDTO voucherDTO = VoucherDTO.builder()
                .title(voucher.getTitle())
                .description(voucher.getDescription())
                .price(voucher.getPrice())
                .tourType(voucher.getTourType())
                .transferType(voucher.getTransferType())
                .status(voucher.getStatus())
                .hotelType(voucher.getHotelType())
                .arrivalDate(voucher.getArrivalDate())
                .evictionDate(voucher.getEvictionDate())
                .isHot(voucher.getIsHot())
                .build();

        return voucherService.create(voucherDTO);
    }

    @Operation(summary = "Update tour, \"ROLE_ADMIN\" role required")
    @ApiResponse(responseCode = "200",
            description = "Updated tour data",
            content = @Content(
                    schema = @Schema(implementation = VoucherDTO.class),
                    examples = @ExampleObject(
                            value = "{\"title\": \"Tour Title\"," +
                                    "\"description\": \"Tour Description\"," +
                                    "\"price\":35.35,\"tourType\":\"HEALTH\"," +
                                    "\"transferType\":\"BUS\",\"hotelType\":\"ONE_STAR\"," +
                                    "\"voucherStatus\":\"REGISTERED\"," +
                                    "\"arrivalDate\":\"2024-05-19T09:01:06\",\"evictionDate\":\"2024-05-19T09:01:06\"," +
                                    "\"isHot\": \"false\"}")))
    @PatchMapping("/{voucherId}")
    public VoucherDTO updateVoucher(@PathVariable("voucherId") UUID voucherId,
                                    @RequestBody @Valid UpdateVoucherRequest voucher) {
        log.info("Request to update tour's data with id: {}", voucherId);
        VoucherDTO voucherDTO = VoucherDTO.builder()
                .title(voucher.getTitle())
                .description(voucher.getDescription())
                .price(voucher.getPrice())
                .tourType(voucher.getTourType())
                .transferType(voucher.getTransferType())
                .status(voucher.getStatus())
                .hotelType(voucher.getHotelType())
                .arrivalDate(voucher.getArrivalDate())
                .evictionDate(voucher.getEvictionDate())
                .isHot(voucher.getIsHot())
                .build();

        return voucherService.update(String.valueOf(voucherId), voucherDTO);
    }

    @Operation(summary = "Delete tour, \"ROLE_ADMIN\" role required")
    @ApiResponse(responseCode = "200",
            description = "Voucher has been deleted",
            content = @Content)
    @DeleteMapping(path = "/{voucherId}")
    public ResponseEntity<String> deleteVoucher(@PathVariable("voucherId") UUID voucherId) {
        log.info("Request to delete tour with id: {}", voucherId);
        voucherService.delete(String.valueOf(voucherId));

        log.info("Tour was deleted. Tour id: {}", voucherId);
        return new ResponseEntity<>(String.format("Voucher with Id %s has been deleted", voucherId),
                HttpStatus.OK);
    }

    @Operation(summary = "Change tour hot status, \"ROLE_ADMIN\" or \"ROLE_MANAGER\" role required")
    @ApiResponse(responseCode = "200",
            description = "Voucher hot status is successfully changed",
            content = @Content)
    @PatchMapping("/{voucherId}/status")
    public ResponseEntity<String> changeVoucherHotStatus(@PathVariable("voucherId") UUID voucherId,
                                                      @RequestBody @Valid Boolean status) {
        log.info("Request to change tour hot status. Tour with id: {}", voucherId);
        VoucherDTO statusUpdate = VoucherDTO.builder()
                .isHot(status)
                .build();
        VoucherDTO voucherDTO = voucherService.changeHotStatus(String.valueOf(voucherId), statusUpdate);

        log.info("Tour hot status was set to {}. Tour id: {}", voucherDTO.getStatus(), voucherId);
        return new ResponseEntity<>("Voucher hot status is successfully changed",
                HttpStatus.OK);
    }

    @Operation(summary = "Change tour status, \"ROLE_ADMIN\" or \"ROLE_MANAGER\" role required")
    @ApiResponse(responseCode = "200",
            description = "Voucher status is successfully changed",
            content = @Content)
    @PutMapping("/{voucherId}/status")
    public ResponseEntity<String> changeVoucherStatus(@PathVariable("voucherId") UUID voucherId,
                                                      @RequestBody @Valid VoucherStatus status) {
        log.info("Request to change tour status. Tour with id: {}", voucherId);
        VoucherDTO statusUpdate = VoucherDTO.builder()
                .status(status.name())
                .build();
        VoucherDTO voucherDTO = voucherService.updateStatus(String.valueOf(voucherId), statusUpdate);

        log.info("Tour status was set to {}. Tour id: {}", voucherDTO.getStatus(), voucherId);
        return new ResponseEntity<>("Voucher status is successfully changed",
                HttpStatus.OK);
    }

    @Operation(summary = "Order tour for yourself")
    @ApiResponse(responseCode = "200",
            description = "Voucher is successfully ordered",
            content = @Content)
    @PatchMapping("/{voucherId}/order")
    public ResponseEntity<String> orderVoucher(@PathVariable("voucherId") UUID voucherId,
                                                         @RequestParam("userId") UUID userId) {
        log.info("Request to order. Tour with id: {}. User with id: {}", voucherId, userId);
        VoucherDTO voucherDTO = voucherService.order(voucherId.toString(), userId.toString());

        log.info("Tour was ordered {}. Tour id: {}", voucherDTO.getStatus(), voucherId);
        return new ResponseEntity<>("Voucher is successfully ordered",
                HttpStatus.OK);
    }

    @Operation(summary = "Get all tours by tour type")
    @ApiResponse(responseCode = "200",
            description = "All tours order with specific tour type",
            content = @Content(
                    schema = @Schema(implementation = VoucherDTO.class),
                    examples = @ExampleObject(
                            value = "{[\"title\": \"Tour Title\"," +
                                    "\"description\": \"Tour Description\"," +
                                    "\"price\":35.35,\"tourType\":\"HEALTH\"," +
                                    "\"transferType\":\"BUS\",\"hotelType\":\"ONE_STAR\"," +
                                    "\"voucherStatus\":\"REGISTERED\"," +
                                    "\"arrivalDate\":\"2024-05-19T09:01:06\",\"evictionDate\":\"2024-05-19T09:01:06\"," +
                                    "\"userId\":\"00000000-0000-0000-0000-000000000000\"," +
                                    "\"isHot\": \"true\"]}")))
    @GetMapping(path ="/tour/{tourType}")
    public Page<VoucherDTO> getVouchersByTourType(@PathVariable("tourType") String tourType,
                                              @PageableDefault(size = 5) Pageable pageable) {
        log.info("Request to get all vouchers by tour type: {}", tourType);
        Pageable actualPageable = getPageableWithSort(pageable);

        log.info("Page of vouchers by by tour type. Size: {}. Page: {}. Sort: {}",
                actualPageable.getPageSize(),
                actualPageable.getPageNumber(), actualPageable.getSort().toString());
        return voucherService.findAllByTourType(getCorrectEnumType(TourType.class, tourType), pageable);
    }

    @Operation(summary = "Get all tours by transfer type")
    @ApiResponse(responseCode = "200",
            description = "All tours order with specific transfer type",
            content = @Content(
                    schema = @Schema(implementation = VoucherDTO.class),
                    examples = @ExampleObject(
                            value = "{[\"title\": \"Tour Title\"," +
                                    "\"description\": \"Tour Description\"," +
                                    "\"price\":35.35,\"tourType\":\"HEALTH\"," +
                                    "\"transferType\":\"BUS\",\"hotelType\":\"ONE_STAR\"," +
                                    "\"voucherStatus\":\"REGISTERED\"," +
                                    "\"arrivalDate\":\"2024-05-19T09:01:06\",\"evictionDate\":\"2024-05-19T09:01:06\"," +
                                    "\"userId\":\"00000000-0000-0000-0000-000000000000\"," +
                                    "\"isHot\": \"true\"]}")))
    @GetMapping(path = "/transfer/{transferType}")
    public Page<VoucherDTO> getVouchersByTransferType(@PathVariable("transferType") String transferType,
                                                  @PageableDefault(size = 5) Pageable pageable) {
        log.info("Request to get all vouchers by transfer type: {}", transferType);
        Pageable actualPageable = getPageableWithSort(pageable);

        log.info("Page of vouchers by transfer type. Size: {}. Page: {}. Sort: {}",
                actualPageable.getPageSize(),
                actualPageable.getPageNumber(), actualPageable.getSort().toString());
        return voucherService.findAllByTransferType(transferType, pageable);
    }

    @Operation(summary = "Get all tours by price")
    @ApiResponse(responseCode = "200",
            description = "All tours order with specific price",
            content = @Content(
                    schema = @Schema(implementation = VoucherDTO.class),
                    examples = @ExampleObject(
                            value = "{[\"title\": \"Tour Title\"," +
                                    "\"description\": \"Tour Description\"," +
                                    "\"price\":35.35,\"tourType\":\"HEALTH\"," +
                                    "\"transferType\":\"BUS\",\"hotelType\":\"ONE_STAR\"," +
                                    "\"voucherStatus\":\"REGISTERED\"," +
                                    "\"arrivalDate\":\"2024-05-19T09:01:06\",\"evictionDate\":\"2024-05-19T09:01:06\"," +
                                    "\"userId\":\"00000000-0000-0000-0000-000000000000\"," +
                                    "\"isHot\": \"true\"]}")))
    @GetMapping(path = "/price/{price}")
    public Page<VoucherDTO> getVouchersByPrice(@PathVariable("price") Double price,
                                                  @PageableDefault(size = 5) Pageable pageable) {
        log.info("Request to get all vouchers by price: {}", price);
        Pageable actualPageable = getPageableWithSort(pageable);

        log.info("Page of vouchers by price. Size: {}. Page: {}. Sort: {}",
                actualPageable.getPageSize(),
                actualPageable.getPageNumber(), actualPageable.getSort().toString());
        return voucherService.findAllByPrice(price, pageable);
    }

    @Operation(summary = "Get all tours by hotel type")
    @ApiResponse(responseCode = "200",
            description = "All tours order with specific hotel type",
            content = @Content(
                    schema = @Schema(implementation = VoucherDTO.class),
                    examples = @ExampleObject(
                            value = "{[\"title\": \"Tour Title\"," +
                                    "\"description\": \"Tour Description\"," +
                                    "\"price\":35.35,\"tourType\":\"HEALTH\"," +
                                    "\"transferType\":\"BUS\",\"hotelType\":\"ONE_STAR\"," +
                                    "\"voucherStatus\":\"REGISTERED\"," +
                                    "\"arrivalDate\":\"2024-05-19T09:01:06\",\"evictionDate\":\"2024-05-19T09:01:06\"," +
                                    "\"userId\":\"00000000-0000-0000-0000-000000000000\"," +
                                    "\"isHot\": \"true\"]}")))
    @GetMapping(path = "/hotel/{hotelType}")
    public Page<VoucherDTO> getVouchersByHotelType(@PathVariable("hotelType") String hotelType,
                                                      @PageableDefault(size = 5) Pageable pageable) {
        log.info("Request to get all vouchers by hotel type: {}", hotelType);
        Pageable actualPageable = getPageableWithSort(pageable);

        log.info("Page of vouchers by hotel type. Size: {}. Page: {}. Sort: {}",
                actualPageable.getPageSize(),
                actualPageable.getPageNumber(), actualPageable.getSort().toString());
        return voucherService.findAllByHotelType(getCorrectEnumType(HotelType.class, hotelType), pageable);
    }

    private Pageable getPageableWithSort(Pageable pageable) {
        Sort hotSort = Sort.by(Sort.Direction.DESC, "isHot");
        Sort givenSort = pageable.getSort();

        return PageRequest.of(pageable.getPageNumber(),
                pageable.getPageSize(),
                givenSort.isEmpty() ? hotSort : givenSort.and(hotSort));
    }
}
