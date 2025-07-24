package com.back.back9.domain.coin.service;

import com.back.back9.domain.coin.entity.Coin;
import com.back.back9.domain.coin.repository.CoinRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CoinService {
    private final CoinRepository coinRepository;

    public List<Coin> findAll() {
        return coinRepository.findAll();
    }

    public Optional<Coin> findById(int id) {
        return coinRepository.findById(id);
    }

    public void delete(Coin coin) {
        coinRepository.delete(coin);
    }

    public long count() {
        return coinRepository.count();
    }

    public Coin add(String symbol, String koreanName, String englishName) {
        Coin coin = new Coin();
        coin.setSymbol(symbol);
        coin.setKoreanName(koreanName);
        coin.setEnglishName(englishName);
        return coinRepository.save(coin);
    }

    public Optional<Coin> findLastest(){
        return coinRepository.findFirstByOrderByIdDesc();
    }

    public void modify(Coin coin, String symbol, String koreanName, String englishName) {
        coin.modify(symbol, koreanName, englishName);
    }
}
