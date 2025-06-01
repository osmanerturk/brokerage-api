package com.brokerage.api.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "ASSETS")
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column
    private String customerId;
    @Column
    private String assetName;
    @Column
    private Double size;
    @Column
    private Double usableSize;
}
