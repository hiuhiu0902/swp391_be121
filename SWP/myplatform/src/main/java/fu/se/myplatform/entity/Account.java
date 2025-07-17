package fu.se.myplatform.entity;

import fu.se.myplatform.enums.Gender;
import fu.se.myplatform.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
    public Long userId;

    @Column(name = "user_name", unique = true, nullable = false)
    @Size(min = 4, message = "UserName phải có ít nhất 6 kí tự")
    public String userName;

    @Column
    @Size(min = 6, message = "Password phải ít nhất 6 kí tự")
    public String password;

    public String fullName;

    @Column(unique = true, nullable = false)
    @Email(message = "Email không hợp lệ")
    @NotBlank(message = "Email không được để trống")
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
