package com.flab.coongyapay.user.domain;

import lombok.Getter;

@Getter
public class User {
    private final Long id;
    private String email;
    private String name;

    private User(Long id, String email, String name) {
        this.id = id;
        this.email = email;
        this.name = name;
    }

    public static User create(String email, String name) {
        return new User(null, email, name);
    }

    public static User from(Long id, String email, String name) {
        return new User(id, email, name);
    }

    //TODO 회원정보변경 메서드 추가
    //changeEmail()
    //changeUserName()
}
