//package com.account.entity;
//
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import javax.persistence.*;
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Getter
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//@Entity
//@Table(name= "member",schema="dbo")
///*
//* UserDetails는 스프링 시큐리티에서 제공하는 인터페이스로서, 사용자의 정보를 담고 있습니다. 이 정보에는 사용자의 아이디, 비밀번호, 권한 등이 포함
//* */
//public class Member implements UserDetails {
//
//    @Id
//    @Column(name = "member_id", updatable = false, unique = true, nullable = false)
//    private String memberId;
//
//    @Column(nullable = false)
//    private String password;
//
//    @ElementCollection(fetch = FetchType.EAGER)/* 조인 관계 */
//    @Builder.Default
//    @CollectionTable(name= "member_roles",schema="dbo")
//    private List<String> roles = new ArrayList<>();
//
//    @Override
//    public Collection<? extends GrantedAuthority> getAuthorities() {
//        return this.roles.stream()
//                .map(SimpleGrantedAuthority::new)
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    public String getUsername() {
//        return memberId;
//    }
//
//    @Override
//    public String getPassword() {
//        return password;
//    }
//
//    @Override
//    public boolean isAccountNonExpired() {
//        return true;
//    }
//
//    @Override
//    public boolean isAccountNonLocked() {
//        return true;
//    }
//
//    @Override
//    public boolean isCredentialsNonExpired() {
//        return true;
//    }
//
//    @Override
//    public boolean isEnabled() {
//        return true;
//    }
//}