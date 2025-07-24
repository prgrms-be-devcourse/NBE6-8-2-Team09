package com.back.back9.domain.coin.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class Coin {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private int id;

    @NotBlank
    @Column(unique = true)
    private String symbol;

    @Column(unique = true)
    private String koreanName;

    @Column(unique = true)
    private String englishName;

    @CreatedDate
    private LocalDateTime created_at;

    public void modify(String symbol, String koreanName, String englishName) {
        this.symbol = symbol;
        this.koreanName = koreanName;
        this.englishName = englishName;
    }
}
