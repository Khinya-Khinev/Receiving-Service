package com.waregang.receiving_service.security.configuration;

import com.waregang.receiving_service.security.UserPrincipal;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class JwtToUserPrincipalConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {

        List<String> roles = jwt.getClaimAsStringList("roles");
        Collection<SimpleGrantedAuthority> authorities = roles == null ? List.of() :
                roles.stream().map(SimpleGrantedAuthority::new).toList();

        UserPrincipal principal = new UserPrincipal(
                UUID.fromString(Objects.requireNonNull(jwt.getSubject())),
                jwt.getClaim("email"),
                jwt.getClaim("warehouse_id"),
                authorities
        );

        return new UsernamePasswordAuthenticationToken(principal, null, authorities);
    }
}
