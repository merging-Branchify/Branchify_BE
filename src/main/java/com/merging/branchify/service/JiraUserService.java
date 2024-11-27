package com.merging.branchify.service;

import com.merging.branchify.respository.JiraUserRepository;
import org.springframework.stereotype.Service;

@Service
public class JiraUserService {

    private final JiraUserRepository userRepository;

    public JiraUserService(JiraUserRepository userRepository) {
        this.userRepository = userRepository;
    }


}
