package com.back.back9.domain.exchange.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "exchange")
@Getter @Setter
public class Exchange {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String symbol;

    private LocalDateTime candleTime;

    private String open;
    private String high;
    private String low;
    private String close;
    private String volume;
    private String timestamp;
}