package nz.ac.auckland.concert.service.domain;

import nz.ac.auckland.concert.common.types.SeatNumber;
import nz.ac.auckland.concert.common.types.SeatRow;
import nz.ac.auckland.concert.service.domain.jpa.SeatCompositePK;
import nz.ac.auckland.concert.service.domain.jpa.SeatNumberConverter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;

@Entity
@Table(name = "BOOKED_SEATS")
public class Seat {

    @EmbeddedId
    private SeatCompositePK seatCompositePK;

    public Seat(SeatCompositePK seatCompositePK) {
        this.seatCompositePK = seatCompositePK;
    }

    public Seat() { }

    public SeatCompositePK getSeatCompositePK() {
        return seatCompositePK;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Seat))
            return false;
        if (obj == this)
            return true;

        Seat rhs = (Seat) obj;
        return new EqualsBuilder().
                append(seatCompositePK.getRow(), rhs.seatCompositePK.getRow()).
                append(seatCompositePK.getNumber(), rhs.seatCompositePK.getNumber()).
                isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31).
                append(seatCompositePK.getRow()).
                append(seatCompositePK.getNumber()).
                hashCode();
    }

    @Override
    public String toString() {
        return seatCompositePK.getRow() + seatCompositePK.getNumber().toString();
    }
}
