package com.back.back9.domain.coin.controller;

import com.back.back9.domain.coin.dto.CoinAddRequest;
import com.back.back9.domain.coin.dto.CoinDto;
import com.back.back9.domain.coin.entity.Coin;
import com.back.back9.domain.coin.service.CoinService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/coins")
public class CoinController {

    private final CoinService coinService;

    // 전체 코인 조회 (GET)
    @GetMapping()
    @Transactional
    public ResponseEntity<List<CoinDto>> getCoins() {
        List<Coin> coins = coinService.findAll();

        List<CoinDto> coinDtos = coins
                .stream()
                .map(c -> new CoinDto(c))
                .toList();

        return ResponseEntity.ok(coinDtos);
    }

    // 코인 단건 조회 (GET)
    @GetMapping("/{id}")
    @Transactional
    public ResponseEntity<CoinDto> getCoin(
            @PathVariable int id
    ) {
        Coin coin = coinService.findById(id);

        return ResponseEntity.ok(new CoinDto(coin));
    }

    // 코인 삭제 (DELETE)
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<String> deleteCoin(
            @PathVariable int id
    ) {
        Coin coin = coinService.findById(id);

        coinService.delete(coin);

        String result = "%d번 코인이 삭제 되었습니다.".formatted(id);

        return ResponseEntity.ok(result);
    }


    // 코인 추가 (POST)
    @PostMapping()
    @Transactional
    public ResponseEntity<CoinDto> addCoin(
            @Valid @RequestBody CoinAddRequest reqBody
    ) {
        Coin coin = coinService.add(reqBody.symbol(), reqBody.koreanName(), reqBody.englishName());

        return ResponseEntity.ok(new CoinDto(coin));
    }

    // 코인 수정 (PUT)
    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<String> modifyCoin(
            @PathVariable int id,
            @RequestBody CoinAddRequest reqBody
    ) {
        Coin coin = coinService.findById(id);

        coinService.modify(coin, reqBody.symbol(), reqBody.koreanName(), reqBody.englishName());

        String result = "%d번 코인이 수정되었습니다.".formatted(id);

        return ResponseEntity.ok(result);
    }
}
