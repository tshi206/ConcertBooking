package nz.ac.auckland.concert.service.domain.jpa;

import nz.ac.auckland.concert.common.types.SeatNumber;
import nz.ac.auckland.concert.common.types.SeatRow;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;
import java.io.Serializable;

@Embeddable
public class SeatCompositePK implements Serializable {

    @Enumerated(EnumType.STRING)
    @Column(name = "row", nullable = false)
    private SeatRow _row;

    @Convert(converter = SeatNumberConverter.class)
    @Column(name = "seatNumber", nullable = false)
    private SeatNumber _number;

    public SeatCompositePK() {}

    public SeatCompositePK(SeatRow row, SeatNumber number) {
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
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof SeatCompositePK)) return false;

        SeatCompositePK that = (SeatCompositePK) o;

        return new EqualsBuilder()
                .append(_row, that._row)
                .append(_number, that._number)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(_row)
                .append(_number)
                .toHashCode();
    }
}
