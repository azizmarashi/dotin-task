package ir.dotin.model;

import lombok.*;

@Data
@AllArgsConstructor
public class Bill {

    private String billId;
    private String paymentId;
    private int amount;

}
