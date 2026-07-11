package in.shubhamprakash681.common_lib.security;

import java.util.List;

public record JwtPrincipal(Long userId, String email, List<String> roles) {
}
