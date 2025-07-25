package com.back.back9.domain.exchange.service;

import com.back.back9.domain.exchange.dto.ExchangeDTO;
import com.back.back9.domain.exchange.entity.Exchange;
import com.back.back9.domain.exchange.repository.ExchangeRepository;
import com.back.back9.global.error.ErrorCode;
import com.back.back9.global.error.ErrorException;
import com.back.back9.global.problem.ProblemDetails;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ExchangeBackupService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final ExchangeRepository exchangeRepository;

    public void backupPreviousDayRedisToDB() {
        // 전날 날짜
        LocalDate yesterday = LocalDate.now().minusDays(1);
        String pattern = "*:" + yesterday;

        Set<String> keys = redisTemplate.keys(pattern);
        if (keys.isEmpty()) {
            throw new ErrorException(ErrorCode.BACKUP_DATA_NOT_FOUND);
        }

        List<Exchange> saveList = new ArrayList<>();

        for (String key : keys) {
            try {
                String json = redisTemplate.opsForValue().get(key);
                if (json == null) continue;

                String[] parts = key.split(":");
                if (parts.length != 2) continue;

                String symbol = parts[0];
                String candleTimeStr = parts[1];
                LocalDateTime candleTime = LocalDateTime.parse(candleTimeStr);

                // JSON -> DTO → Exchange 변환
                ExchangeDTO dto = objectMapper.readValue(json, ExchangeDTO.class);

                Exchange exchange = new Exchange();
                exchange.setSymbol(symbol);
                exchange.setCandleTime(candleTime);
                exchange.setOpen(dto.getOpen());
                exchange.setHigh(dto.getHigh());
                exchange.setLow(dto.getLow());
                exchange.setClose(dto.getClose());
                exchange.setVolume(dto.getVolume());
                exchange.setTimestamp(dto.getTimestamp());

                saveList.add(exchange);
            } catch (Exception ex) {
                throw new ErrorException(ErrorCode.BACKUP_FAIL);
            }
        }

        exchangeRepository.saveAll(saveList);
        redisTemplate.delete(keys);
    }
}