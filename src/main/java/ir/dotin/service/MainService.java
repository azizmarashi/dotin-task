package ir.dotin.service;

import ir.dotin.model.Bill;
import ir.dotin.model.Organization;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.io.*;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class MainService {

    private final String billPathFile = "src/main/resources/otherFiles/01_bill/01_bill";
    private final String organizationPathFile = "src/main/resources/otherFiles/01_bill/02_organization";

    private Bill parseBill(String data) {

        Pattern regexPattern = Pattern.compile("\\{\"billId\":\"(\\d+)\",\"paymentId\":\"(\\d+)\",\"amount\":\"(\\d+)\"\\}");
        Matcher matcher = regexPattern.matcher(data);

        if (matcher.find()) {
            String billId = matcher.group(1);
            String paymentId = matcher.group(2);
            int amount = Integer.parseInt(matcher.group(3));
            return new Bill(billId, paymentId, amount);
        }
        return null;
    }

    @SneakyThrows
    public List<Bill> getValidatedBills(String bufferFilePath) {

        List<Bill> billList = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(bufferFilePath))) {
            String bufferReadLine;
            while ((bufferReadLine = reader.readLine()) != null) {
                Bill bill = parseBill(bufferReadLine);
                if (bill != null) billList.add(bill);
            }
        }
        return billList;
    }

    @SneakyThrows
    private Organization parseOrganization(String data) {

        Pattern regexPattern = Pattern.compile("(.+?),(\\d+),(.+?),(.+?),(.*)");
        Matcher matcher = regexPattern.matcher(data);

        if (matcher.find()) {
            String organizationType = matcher.group(1);
            boolean enable = matcher.group(2).equals("1");
            String companyCode = matcher.group(3);
            String account = matcher.group(4);
            String name = matcher.group(5);
            return new Organization(organizationType, enable, companyCode, account, name);
        }
        return null;
    }

    @SneakyThrows
    public List<Organization> getOrganizations(String bufferFilePath) {

        List<Organization> organizationList = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(bufferFilePath))) {
            String bufferReadLine;
            while ((bufferReadLine = reader.readLine()) != null) {
                Organization organization = parseOrganization(bufferReadLine);
                if (organization != null) organizationList.add(organization);
            }
        }
        return organizationList;
    }


    @SneakyThrows
    public void createValidateFile(List<Bill> bills) {

        String log = "";

        for (Bill bill : bills) {

            Organization organization = getOrganizationByOrganizationCode(getOrganizationCodeFromBillId(bill.getBillId()));

            if (organization == null) {
                assert false;
                log = log.concat(String.format("line \"%s\" invalid organizaion \"%s\" , %s\n", findLineWithBillId(billPathFile, bill.getBillId()), organization.getOrganizationType(), organization.getCompanyCode()));
            }

            if (!organization.isEnable()) {
                log = log.concat(String.format("line \"%s\" organization \"%s\" , \"%s\" not enabled\n", findLineWithBillId(billPathFile, bill.getBillId()), organization.getOrganizationType(), organization.getCompanyCode()));
            }
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("out.validate.txt"))) {
            writer.write(log);
        }

    }

    @SneakyThrows
    public void createReportFile(List<Bill> bills, List<Organization> organizations) {

        String log = "";

        for (Organization organization : organizations) {

            long count = bills.stream().filter(bill -> organization.getCompanyCode().equals(getOrganizationCodeFromBillId(bill.getBillId()))).count();

            BigInteger totalAmount = bills.stream().filter(bill -> organization.getCompanyCode().equals(getOrganizationCodeFromBillId(bill.getBillId()))).map(bill -> new BigInteger(String.valueOf(bill.getAmount()))).reduce(BigInteger.ZERO, BigInteger::add);

            log = log.concat(String.format("\"%s\" \"%s\" , %s , %s \n", organization.getOrganizationType(), organization.getCompanyCode(), count, totalAmount));
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("out.report1.txt"))) {
            writer.write(log);
        }

    }

    public boolean validationAmount(Bill bill) {
        return bill.getPaymentId().substring(0, 5).equals(String.valueOf(bill.getAmount()).substring(0, 5));
    }

    public void printInvalidPayments(List<Bill> bills) {

        bills.stream().filter(bill -> !validationAmount(bill)).forEach(System.out::println);

    }

    private String getOrganizationCodeFromBillId(String billId) {
        return billId.substring(billId.length() - 5, billId.length() - 2);
    }

    private Organization getOrganizationByOrganizationCode(String organizationCode) {
        return getOrganizations(organizationPathFile).stream().filter(org -> organizationCode.equals(org.getCompanyCode())).findFirst().orElse(null);
    }

    @SneakyThrows
    private Integer findLineWithBillId(String filePath, String BillId) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.contains(BillId)) {
                    return lineNumber;
                }
            }
        }
        return null;
    }

}