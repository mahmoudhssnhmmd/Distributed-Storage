package springbootproject.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name="users")
@Data
public class User implements UserDetails {
    @Id
    @GeneratedValue
    private Long id ;
    @Column(nullable = false , unique = true)
    private String Username ;
    @Column(nullable = false, unique = true)
    private String   email;
    @Column(nullable = false)
    private String password ;
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public void prepersist(){
        this.createdAt = LocalDateTime.now();

    }
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }

}
