package ro.iss.domain;

import jakarta.persistence.*;

import java.io.Serializable;

@jakarta.persistence.Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED)

public class User extends Entity<Long> implements Serializable {

    @Column(nullable = false, unique = true)
    protected String username;

    @Column(nullable = false)
    protected String password;

    public User() {}

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() { return username; }

    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }

    public void setPassword(String password) { this.password = password; }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
