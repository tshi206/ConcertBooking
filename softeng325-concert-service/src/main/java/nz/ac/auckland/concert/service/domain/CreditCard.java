package nz.ac.auckland.concert.service.domain;

import nz.ac.auckland.concert.common.dto.CreditCardDTO;
import nz.ac.auckland.concert.service.domain.jpa.LocalDateConverter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "CREDITCARDS")
public class CreditCard {

    public enum Type {Visa, Master};

    @Id
    @GeneratedValue
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private CreditCard.Type _type;

    @Column(name = "ownerName", nullable = false)
    private String _name;

    @Column(name = "number", nullable = false)
    private String _number;

    @Convert(converter = LocalDateConverter.class)
    @Column(name = "expiryDate", nullable = false)
    private LocalDate _expiryDate;

    @ManyToOne
    @JoinColumn(name = "uid", nullable = false)
    private User user;

    public CreditCard() {}

    public CreditCard(CreditCard.Type type, String name, String number, LocalDate expiryDate) {
        _type = type;
        _name = name;
        _number = number;
        _expiryDate = expiryDate;
    }

    public CreditCard(CreditCard.Type type, String name, String number, LocalDate expiryDate, User user) {
        _type = type;
        _name = name;
        _number = number;
        _expiryDate = expiryDate;
        this.user = user;
    }

    public CreditCard.Type getType() {
        return _type;
    }

    public String getName() {
        return _name;
    }

    public String getNumber() {
        return _number;
    }

    public LocalDate getExpiryDate() {
        return _expiryDate;
    }


    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }


    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof CreditCard))
            return false;
        if (obj == this)
            return true;

        CreditCard rhs = (CreditCard) obj;
        return new EqualsBuilder().
                append(_type, rhs._type).
                append(_name, rhs._name).
                append(_number, rhs._number).
                append(_expiryDate, rhs._expiryDate).
                append(user, rhs.user).
                isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31).
                append(_type).
                append(_name).
                append(_number).
                append(_expiryDate).
                append(user.getToken()).
                hashCode();
    }
}
