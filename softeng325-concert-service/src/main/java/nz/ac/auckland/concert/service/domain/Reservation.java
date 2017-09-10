package nz.ac.auckland.concert.service.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "RESERVATIONS")
public class Reservation {

    @Id
    private Long rid;

    @ManyToOne
    @JoinColumn(name = "uid", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "date", nullable = false)
    private ConcertDate concertDate;

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "cid", referencedColumnName = "cid"),
            @JoinColumn(name = "price", referencedColumnName = "price"),
            @JoinColumn(name = "priceBand", referencedColumnName = "priceBand")
    })
    private ConcertTarif concertTarif;

    @OneToMany(cascade={CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})
    @org.hibernate.annotations.Fetch(
            org.hibernate.annotations.FetchMode.SUBSELECT)
    @JoinColumn(
            name = "reservation_id",
            nullable= false
    )
    private Set<Seat> bookedSeats = new HashSet<>();

    public Reservation() {}

    public Reservation(Long rid, User user, ConcertDate concertDate, ConcertTarif concertTarif, Set<Seat> bookedSeats) {
        this.rid = rid;
        this.user = user;
        this.concertDate = concertDate;
        this.concertTarif = concertTarif;
        this.bookedSeats.addAll(bookedSeats);
    }

    public Long getRid() {
        return rid;
    }

    public User getUser() {
        return user;
    }

    public ConcertDate getConcertDate() {
        return concertDate;
    }

    public ConcertTarif getConcertTarif() {
        return concertTarif;
    }

    public Set<Seat> getBookedSeats() {
        return bookedSeats;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Reservation))
            return false;
        if (obj == this)
            return true;

        Reservation rhs = (Reservation) obj;
        return new EqualsBuilder().
                append(rid, rhs.rid).
                append(user, rhs.user).
                append(concertDate, rhs.concertDate).
                append(concertTarif, rhs.concertTarif).
                append(bookedSeats, rhs.bookedSeats).
                isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31).
                append(rid).
                append(user.getToken()).
                append(concertDate.getDate()).
                append(concertTarif.getConcertTarifCompositePK().getConcert().getId()).
                append(bookedSeats.hashCode()).
                hashCode();
    }

}
