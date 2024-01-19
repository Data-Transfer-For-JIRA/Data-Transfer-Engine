package com.account.service;

import com.account.dto.JwtToken;

public interface Login {

    public JwtToken signIn(String id, String pw);
}
