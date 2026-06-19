package com.apartment.deposit.service;

import com.apartment.deposit.dto.LoginDTO;
import com.apartment.deposit.dto.LoginVO;

public interface AuthService {

    LoginVO login(LoginDTO dto);
}
