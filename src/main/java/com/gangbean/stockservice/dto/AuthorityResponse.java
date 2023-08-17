package com.gangbean.stockservice.dto;

import com.gangbean.stockservice.domain.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthorityResponse {
    private Role authorityName;
}