package com.epam.finaltask.model;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private Date expiryDate;
    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;
}
