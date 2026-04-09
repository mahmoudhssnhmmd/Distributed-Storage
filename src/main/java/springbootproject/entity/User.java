package springbootproject.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name="users")
@Data
public class User {
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

}
