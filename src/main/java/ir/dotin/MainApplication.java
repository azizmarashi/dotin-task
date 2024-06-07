package ir.dotin;

import ir.dotin.model.Bill;
import ir.dotin.model.Organization;
import ir.dotin.service.MainService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import java.util.List;

@SpringBootApplication
public class MainApplication {

    public static void main(String[] args) {
        SpringApplication.run(MainApplication.class, args);
    }

    @Bean
    CommandLineRunner run(MainService service) {
        return args -> {

            List<Bill> validatedBills = service.getValidatedBills("src/main/resources/otherFiles/01_bill/01_bill");
            List<Organization> organizations = service.getOrganizations("src/main/resources/otherFiles/01_bill/02_organization");

            //Q1 - create: out.validate.txt
            service.createValidateFile(validatedBills);

            //Q2 - create: out.report1.txt
            service.createReportFile(validatedBills,organizations);

            //Q3 - print invalid payments
            service.printInvalidPayments(validatedBills);
        };

    }

}