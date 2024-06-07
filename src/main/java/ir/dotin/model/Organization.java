package ir.dotin.model;

import lombok.*;

@Data
@AllArgsConstructor
public class Organization {

    private String organizationType;

    private boolean enable;

    private String companyCode;

    private String account;

    private String name;

}
