package nz.ac.auckland.concert.service.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;
import javax.ws.rs.core.Cookie;

@Entity
@Table(name = "USERS")
@Access(AccessType.FIELD)
public class User {

    @Id
    @GeneratedValue
    private Long uid;

    private String username;

    private String password;

    private String firstname;

    private String lastname;

    private String token;

    public User(){}

    public User(String username, String password, String firstname, String lastname, String token) {
        this.username = username;
        this.password = password;
        this.firstname = firstname;
        this.lastname = lastname;
        this.token = token;
    }

    public User(String username, String password, String firstname, String lastname, Cookie cookie) {
        this.username = username;
        this.password = password;
        this.firstname = firstname;
        this.lastname = lastname;
        this.token = cookie.toString();
    }

    public Long getUid() {
        return uid;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getFirstname() {
        return firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public String getToken() {
        return token;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof User))
            return false;
        if (obj == this)
            return true;

        User rhs = (User) obj;
        return new EqualsBuilder().
                append(uid, rhs.uid).
                append(firstname, rhs.firstname).
                append(lastname, rhs.lastname).
                append(username, rhs.username).
                append(password, rhs.password).
                append(token, rhs.token).
                isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31).
                append(uid).
                append(firstname).
                append(lastname).
                append(username).
                append(password).
                append(token).
                hashCode();
    }

}
