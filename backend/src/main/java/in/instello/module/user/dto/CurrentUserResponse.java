package in.instello.module.user.dto;

import java.time.Instant;
import java.util.List;

import in.instello.module.user.entity.AppAuthority;
import org.jspecify.annotations.Nullable;


public record CurrentUserResponse(
        String id,
        String email,
        @Nullable Instant emailVerifiedAt,
        List<AppAuthority> authorities,
        @Nullable Profile profile
) {

    public record Profile(
            String firstName,
            @Nullable String lastName
    ) {
    }
}
