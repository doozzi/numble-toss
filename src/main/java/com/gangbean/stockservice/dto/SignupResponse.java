package com.gangbean.stockservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gangbean.stockservice.domain.Authority;
import com.gangbean.stockservice.domain.Member;
import lombok.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SignupResponse {

    private Long id;

    @NotNull
    @Size(min = 3, max = 50)
    private String username;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotNull
    @Size(min = 3, max = 100)
    private String password;

    @NotNull
    @Size(min = 3, max = 50)
    private String nickname;

    private Set<AuthorityResponse> authorityResponseSet;

    public static SignupResponse from(Member member) {
        if(member == null) return null;

        return SignupResponse.builder()
                .id(member.getId())
                .username(member.getUsername())
                .nickname(member.getNickname())
                .authorityResponseSet(member.getAuthorities().stream()
                .map(authority -> AuthorityResponse.builder().authorityName(authority.getAuthorityName()).build())
                .collect(Collectors.toSet()))
            .build();
    }

    public Member asMember() {
        return new Member(id, username, password
                , nickname
                , authorityResponseSet.stream()
                    .map(AuthorityResponse::getAuthorityName)
                    .map(Authority::new)
                    .collect(Collectors.toSet()));
    }
}