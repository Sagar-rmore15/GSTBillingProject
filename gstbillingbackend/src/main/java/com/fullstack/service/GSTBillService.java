package com.fullstack.service;

import com.fullstack.entity.GSTBill;
import com.fullstack.exception.RecordNotFoundException;
import com.fullstack.repository.GSTBillRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GSTBillService implements IGSTBillService {

    private final GSTBillRepository gstBillRepository;

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    //Generate GST Invoice with 18% GST
    @Override
    public GSTBill createGSTInvoice(GSTBill gstBill) {

        log.info("Invoice Amount: " + gstBill.getInvoiceAmount());

        //Calculate total amount including 18% GST...
        double totalAmount = gstBill.getInvoiceAmount() * 1.18;

        log.info("After GST amount: " + totalAmount);
        gstBill.setInvoiceAmount(totalAmount);

        GSTBill finalGSTBill = gstBillRepository.save(gstBill); //GST invoice ID generated here and then it has been used for sending email and for return response.

        sendEmail(finalGSTBill); //Calling this method to send Email.

        return finalGSTBill;
    }

    @Override
    public Optional<GSTBill> findById(long invoiceId) {

        return Optional.of(gstBillRepository.findById(invoiceId).orElseThrow(() -> new RecordNotFoundException("GST Bill Invoice does not exist.")));
    }

    @Override
    public List<GSTBill> findAll() {
        return gstBillRepository.findAll();
    }

    @Override
    public GSTBill update(long invoiceId, GSTBill gstBill) {

        //fetching detials from DB using invoice ID...
        GSTBill gstBill1 = findById(invoiceId).get();

        gstBill1.setCustName(gstBill.getCustName());
        gstBill1.setCustGSTNumber(gstBill.getCustGSTNumber());
        gstBill1.setCustAddress(gstBill.getCustAddress());
        gstBill1.setInvoiceAmount(gstBill.getInvoiceAmount());
        gstBill1.setInvoiceDate(gstBill.getInvoiceDate());
        gstBill1.setCustEmailId(gstBill.getCustEmailId());
        gstBill1.setCustContactNumber(gstBill.getCustContactNumber());

        //updating the new details into DB...
        return gstBillRepository.save(gstBill1);
    }

    @Override
    public List<GSTBill> searchByAnyInput(String input) {

        return gstBillRepository.findAll().stream().
                filter(gstBill ->
                        gstBill.getCustName().equals(input)
                                || gstBill.getCustEmailId().equals(input)
                                || String.valueOf(gstBill.getInvoiceId()).equals(input)
                                || gstBill.getCustGSTNumber().equals(input)
                                || String.valueOf(gstBill.getInvoiceDate()).equals(input)
                                || String.valueOf(gstBill.getCustContactNumber()).equals(input)
                                || gstBill.getCustAddress().equals(input)
                                || String.valueOf(gstBill.getInvoiceAmount()).equals(input)
                ).toList();
    }

    @Override
    public List<GSTBill> sortBy(String input) {
        //Sort by invoice Id, Customer Name, Amount, etc by Desc/Asc;

        return switch (input) {
            case "sortByNameAsc" ->
                    gstBillRepository.findAll().stream().sorted(Comparator.comparing(GSTBill::getCustName)).toList();

            case "sortByNameDesc" ->
                    gstBillRepository.findAll().stream().sorted(Comparator.comparing(GSTBill::getCustName).reversed()).toList();

            case "sortByAmountAsc" ->
                    gstBillRepository.findAll().stream().sorted(Comparator.comparing(GSTBill::getInvoiceAmount)).toList();

            case "sortByAmountDesc" ->
                    gstBillRepository.findAll().stream().sorted(Comparator.comparing(GSTBill::getInvoiceAmount).reversed()).toList();

            case "sortByInvoiceIdDesc" ->
                    gstBillRepository.findAll().stream().sorted(Comparator.comparing(GSTBill::getInvoiceId).reversed()).toList();

            default ->
                    gstBillRepository.findAll().stream().sorted(Comparator.comparing(GSTBill::getInvoiceId)).toList();
        };
    }

    @Override
    public void deleteById(long invoiceId) {
        gstBillRepository.deleteById(invoiceId);
    }

    private void sendEmail(GSTBill gstBill) {

        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);

            mimeMessageHelper.setFrom(fromEmail);

            mimeMessageHelper.setTo(gstBill.getCustEmailId());

            mimeMessageHelper.setSubject("GST Invoice - " + gstBill.getInvoiceId());

            String emailContent =
                    "Dear " + gstBill.getCustName() + ",\n\n" +

                            "Thank you for choosing our services. " +
                            "Please find your GST invoice details below.\n\n" +

                            "========== GST INVOICE ==========\n\n" +

                            "Invoice ID       : " + gstBill.getInvoiceId() + "\n" +
                            "Customer Name    : " + gstBill.getCustName() + "\n" +
                            "Customer Address: " + gstBill.getCustAddress() + "\n" +
                            "Customer Email   : " + gstBill.getCustEmailId() + "\n" +
                            "Invoice Date     : " + gstBill.getInvoiceDate() + "\n" +
                            "Invoice Amount   : Rs. " + gstBill.getInvoiceAmount() + "\n\n" +

                            "=================================\n\n" +

                            "Please keep this email for your records.\n\n" +

                            "Thank you!\n" +
                            "Regards,\n" +
                            "GST Billing Team";

            mimeMessageHelper.setText(emailContent);

            javaMailSender.send(mimeMessage);

            log.info("Email Sent Successfully.");

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }


    }
}
