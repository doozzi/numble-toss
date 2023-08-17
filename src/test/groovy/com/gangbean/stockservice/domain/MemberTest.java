package com.gangbean.stockservice.domain;

import java.util.Set;

public class MemberTest {
    public static final Member TEST_MEMBER = new Member(1L, "사용자", "사용자","사용자", Set.of(new Authority(Role.ROLE_USER)));

}
