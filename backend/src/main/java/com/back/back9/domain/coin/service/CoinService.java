package com.back.back9.domain.coin.service;

import com.back.back9.domain.coin.entity.Coin;
import com.back.back9.domain.coin.repository.CoinRepository;
import com.back.back9.global.error.ErrorCode;
import com.back.back9.global.error.ErrorException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CoinService {
    private final CoinRepository coinRepository;

    // 코인 전체 조회
    public List<Coin> findAll() {
        return coinRepository.findAll();
    }

    // 코인 단건 조회
    public Coin findById(long id) {
        return coinRepository.findById(id)
                .orElseThrow(() -> new ErrorException(ErrorCode.COIN_NOT_FOUND, id));
    }

    // 코인 삭제
    public void delete(Coin coin) {
        coinRepository.delete(coin);
    }

    // 코인 개수
    public long count() {
        return coinRepository.count();
    }

    // 코인 추가
    public Coin add(String symbol, String koreanName, String englishName) {
        Coin coin = new Coin(symbol, koreanName, englishName);
        return coinRepository.save(coin);
    }

    // 코인 수정
    public void modify(Coin coin, String symbol, String koreanName, String englishName) {
        coin.modify(symbol, koreanName, englishName);
    }

    // 마지막 코인 조회
    public Optional<Coin> findLastest() {
        return coinRepository.findFirstByOrderByIdDesc();
    }
}
