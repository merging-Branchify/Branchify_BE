package com.merging.branchify.service;

import com.merging.branchify.dto.BetaTesterDTO;
import com.merging.branchify.entity.BetaTester;
import com.merging.branchify.respository.BetaTesterRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BetaTesterService {

    private final BetaTesterRepository betaTesterRepository;

    public BetaTesterService(BetaTesterRepository betaTesterRepository) {
        this.betaTesterRepository = betaTesterRepository;
    }

    // 베타 테스터 등록
    public BetaTester register(BetaTesterDTO betaTesterDTO) {
        if(betaTesterRepository.existsByEmail(betaTesterDTO.getEmail())) {
            throw new IllegalArgumentException("이미 등록된 이메일입니다.");
        }

        BetaTester betaTester = new BetaTester();
        betaTester.setEmail(betaTesterDTO.getEmail());

        return betaTesterRepository.save(betaTester);
    }

    // 베타테스터 조회
    public List<String> getAllEmails() {
        return betaTesterRepository.findAllEmails();
    }
}
