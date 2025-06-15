package com.epam.finaltask.model;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tours")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Voucher {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String title;

    private String description;

    private Double price;

    private TourType tourType;

    private TransferType transferType;

    private HotelType hotelType;

    private VoucherStatus status;

    private LocalDate arrivalDate;

    private LocalDate evictionDate;
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    private boolean isHot = false;


}
