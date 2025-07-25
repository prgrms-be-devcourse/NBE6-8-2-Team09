package com.back.back9.domain.coin.controller;

import com.back.back9.domain.coin.dto.CoinDto;
import com.back.back9.domain.coin.entity.Coin;
import com.back.back9.domain.coin.service.CoinService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CoinController {

    private final CoinService coinService;

    @GetMapping("/coins")
    @Transactional
    public List<CoinDto> getCoins() {
        List<Coin> coins = coinService.findAll();

        return coins
                .stream()
                .map(c -> new CoinDto(c))
                .toList();
    }

    @GetMapping("/coins/{id}")
    @Transactional
    public CoinDto getCoin(
        @PathVariable int id
    ){
        Coin coin = coinService.findById(id).get();
        return new CoinDto(coin);
    }

    @DeleteMapping("/coins/{id}")
    @Transactional
    public String deleteCoin(
            @PathVariable int id
    ){
        Coin coin = coinService.findById(id).get();

        coinService.delete(coin);

        return "%d번 코인이 삭제되었습니다.".formatted(id);
    }

    record CoinAddReqBody(
            @NotBlank
            String symbol,

            @NotBlank
            String koreanName,

            @NotBlank
            String englishName
    ){}

    @PostMapping("/coins")
    @Transactional
    public CoinDto addCoin(
            @Valid @RequestBody CoinAddReqBody reqBody
    ){
        Coin coin = coinService.add(reqBody.symbol, reqBody.koreanName, reqBody.englishName);

        return new CoinDto(coin);
    }

    @PutMapping("/coins/{id}")
    @Transactional
    public void modifyCoin(
            @PathVariable int id,
            @RequestBody CoinAddReqBody reqBody
    ){
        Coin coin = coinService.findById(id).get();

        coinService.modify(coin, reqBody.symbol, reqBody.koreanName, reqBody.englishName);
    }
}
