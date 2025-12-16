package com.mycom.myapp.domain.auth.service;

import com.mycom.myapp.domain.auth.dto.LoginRequest;
import com.mycom.myapp.domain.auth.dto.LoginResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface LoginService {
	LoginResponse login(LoginRequest loginRequest, HttpServletResponse response);
	String refresh(HttpServletRequest request, HttpServletResponse response);
	void logout(HttpServletRequest request, HttpServletResponse response);

}
