package nz.ac.auckland.concert.service.domain;

import nz.ac.auckland.concert.common.types.SeatNumber;
import nz.ac.auckland.concert.common.types.SeatRow;
import nz.ac.auckland.concert.service.domain.jpa.SeatNumberConverter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;

@Entity
@Table(name = "BOOKED_SEATS")
public class Seat {

    @Id
    @GeneratedValue
    private Long sid;

    @Enumerated
    @Column(name = "row", nullable = false)
    private SeatRow _row;

    @Convert(converter = SeatNumberConverter.class)
    @Column(name = "seatNumber", nullable = false)
    private SeatNumber _number;

    public Seat() {}

    public Seat(SeatRow row, SeatNumber number) {
        _row = row;
        _number = number;
    }

    public SeatRow getRow() {
        return _row;
    }

    public SeatNumber getNumber() {
        return _number;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Seat))
            return false;
        if (obj == this)
            return true;

        Seat rhs = (Seat) obj;
        return new EqualsBuilder().
                append(_row, rhs._row).
                append(_number, rhs._number).
                isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31).
                append(_row).
                append(_number).
                hashCode();
    }

    @Override
    public String toString() {
        return _row + _number.toString();
    }
}
