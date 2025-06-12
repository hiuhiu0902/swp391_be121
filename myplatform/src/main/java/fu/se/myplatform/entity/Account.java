package fu.se.myplatform.entity;

import fu.se.myplatform.enums.Gender;
import fu.se.myplatform.enums.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
@Setter
@Entity
public class Account implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long userId;

    public String userName;

    public String password;

    public String email;

    public String phoneNumber;

    @Enumerated(EnumType.STRING)
    public Role role;

    @Enumerated(EnumType.STRING)
    public Gender gender;

    public String profilePictureUrl;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getUsername() {
        return this.userName;
    }

    @Override
    public String getPassword() {
        return this.password;
    }
}
