package com.mlog.ps.api.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mlog.ps.api.entity.Payment;
import com.mlog.ps.api.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payment")
public class PaymentController {

    @Autowired
    private PaymentService service;

    @PostMapping("/doPayment")
    public Payment doPayment(@RequestBody Payment payment) {
        try {
            return service.doPayment(payment);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/{orderId}")
    public Payment findPaymentHistoryByOrderId(@PathVariable int orderId){
              return service.findPaymentHistoryByOrderId(orderId);
    }


}
