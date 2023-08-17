package com.gangbean.stockservice.service;

import com.gangbean.stockservice.domain.Account;
import com.gangbean.stockservice.domain.Authority;
import com.gangbean.stockservice.domain.Member;
import com.gangbean.stockservice.domain.Role;
import com.gangbean.stockservice.dto.SignupRequest;
import com.gangbean.stockservice.dto.SignupResponse;
import com.gangbean.stockservice.exception.member.MemberDuplicateException;
import com.gangbean.stockservice.exception.member.MemberNotFoundException;
import com.gangbean.stockservice.repository.AccountRepository;
import com.gangbean.stockservice.repository.MemberRepository;
import com.gangbean.stockservice.repository.TokenRepository;
import com.gangbean.stockservice.util.SecurityUtil;
import java.util.Collections;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccountRepository accountRepository;
    private final AccountService accountService;

    public MemberService(MemberRepository memberRepository, PasswordEncoder passwordEncoder,
        TokenRepository tokenRepository, AccountRepository accountRepository, AccountService accountService) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenRepository = tokenRepository;
        this.accountRepository = accountRepository;
        this.accountService = accountService;
    }

    @Transactional
    public SignupResponse signup(SignupRequest signupResponse) {
        memberRepository.findOneWithAuthoritiesByUsername(signupResponse.getUsername())
            .ifPresent(user -> {
                throw new MemberDuplicateException("이미 사용중인 유저이름 입니다: " + signupResponse.getUsername());
            });

        Authority authority = Authority.builder()
            .authorityName(Role.ROLE_USER)
            .build();

        Member member = Member.builder()
            .username(signupResponse.getUsername())
            .password(passwordEncoder.encode(signupResponse.getPassword()))
            .nickname(signupResponse.getNickname())
            .authorities(Collections.singleton(authority))
            .build();

        return SignupResponse.from(memberRepository.save(member));
    }

    public SignupResponse getUserWithAuthorities(String username) {
        return SignupResponse.from(memberRepository.findOneWithAuthoritiesByUsername(username)
            .orElseThrow(() -> new MemberNotFoundException("이름에 해당하는 멤버가 존재하지 않습니다: " + username)));
    }

    public SignupResponse getMyUserWithAuthorities() {
        return SignupResponse.from(
            SecurityUtil.getCurrentUsername()
                .flatMap(memberRepository::findOneWithAuthoritiesByUsername)
                .orElseThrow(() -> new MemberNotFoundException("로그인 정보가 잘못되었습니다. 다시 로그인 해주세요.")));
    }

    public SignupResponse memberOf(String username) {
        return SignupResponse.from(memberRepository.findById(Long.valueOf(username))
                .orElseThrow(() -> new MemberNotFoundException("로그인 정보가 잘못되었습니다. 다시 로그인 해주세요.")));
    }

    public Member memberOf(String username, String password) {
        Member loginMember = memberRepository.findByUsername(username)
            .orElseThrow(() -> new MemberNotFoundException("이름에 해당하는 멤버정보가 없습니다: " + username));

        if (!passwordEncoder.matches(password, loginMember.getPassword())) {
            throw new MemberNotFoundException("입력하신 ID와 패스워드에 해당하는 정보가 없습니다. 로그인 정보를 확인해주세요.");
        }

        return loginMember;
    }

    @Transactional
    public void withdraw(Long memberId) {
        Member loginMember = memberRepository.findById(memberId)
            .orElseThrow(() -> new MemberNotFoundException("존재하지 않는 회원 ID 입니다: " + memberId));

        tokenRepository.deleteAllByMemberId(memberId);

        accountRepository.findAllByMemberIdOrderByIdDesc(memberId).stream()
            .map(Account::id)
            .forEach(accountId -> accountService.close(accountId, loginMember));

        memberRepository.deleteById(memberId);
    }
}
