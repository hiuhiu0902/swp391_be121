package fu.se.myplatform.entity;

import fu.se.myplatform.enums.Gender;
import fu.se.myplatform.enums.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
//haha
@Getter
@Setter
@Entity
public class  Account implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    public long userId;

    @Column(name = "user_name", unique = true, nullable = false)
    public String userName;

    public String password;

    public String fullName;
    public String email;

    public String phoneNumber;

    @Enumerated(EnumType.STRING)
    public Role role;

    @Enumerated(EnumType.STRING)
    public Gender gender;

    @Column(name = "created_at", updatable = false)
    @org.hibernate.annotations.CreationTimestamp
    public LocalDateTime createdAt;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + this.role.name()));
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
