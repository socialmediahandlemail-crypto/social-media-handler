package com.example.SocialSync.model;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
@Document(collection = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {
@Id
private String id;
private String username;
private String email;
private String password;
private String role; // e.g., "ROLE_ADMIN" or "ROLE_USER"
private boolean isAdmin;
// 🔥 NEW: Stores the Permanent Key (Admin) or Random OTP (User)
private String secretKey;
private LocalDateTime createdAt;
private String resetToken;
private LocalDateTime resetTokenExpiry;
@DBRef(lazy = true)
@JsonManagedReference
private List<YouTubeAccount> youtubeAccounts = new ArrayList<>();
/* ======= YOUTUBE ======= */
public void addYouTubeAccount(YouTubeAccount account) {
youtubeAccounts.add(account);
account.setUser(this);
}
public void removeYouTubeAccount(YouTubeAccount account) {
youtubeAccounts.remove(account);
account.setUser(null);
}
@Override
public Collection<? extends GrantedAuthority> getAuthorities() {
return List.of(new SimpleGrantedAuthority(this.role != null  ? this.role : "ROLE_USER"));}
@Override
public boolean isAccountNonExpired() {
return true;
}
@Override
public boolean isAccountNonLocked() {
return true;
}
@Override
public boolean isCredentialsNonExpired() {
return true;
}
@Override
public boolean isEnabled() {
return true;
}
@jakarta.persistence.PrePersist
public void prePersist() {
if (id == null) {
id = UUID.randomUUID().toString();
}
if (createdAt == null) {
createdAt = LocalDateTime.now();
}
}
}