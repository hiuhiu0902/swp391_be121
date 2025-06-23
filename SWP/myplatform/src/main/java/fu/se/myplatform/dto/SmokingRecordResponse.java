package fu.se.myplatform.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class SmokingRecordResponse {
    public LocalDate date;
    public int ciagrettesSmoked;
}
