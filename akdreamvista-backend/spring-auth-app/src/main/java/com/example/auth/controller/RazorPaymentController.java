package com.example.auth.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.auth.dto.PaymentVerificationRequestDto;
import com.example.auth.entity.Property;
import com.example.auth.model.PropertyPayment;
import com.example.auth.repository.PropertyPaymentRepository;
import com.example.auth.repository.PropertyRepository;
import com.example.auth.security.RazorpaySignatureUtil;
import com.example.auth.service.InvoiceService;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;

import jakarta.transaction.Transactional;

@RestController
@RequestMapping("/api/payment")

public class RazorPaymentController {
	private static final Logger logger = LoggerFactory.getLogger(RazorPaymentController.class);

	@Value("${razorpay.key.id}")
	private String razorpayKeyId;

	@Value("${razorpay.key.secret}")
	private String razorpayKeySecret;

	private final PropertyRepository propertyRepository;
	@Autowired
	private PropertyPaymentRepository paymentRepository;
	@Autowired
	private InvoiceService invoiceService;

	public RazorPaymentController(PropertyRepository propertyRepository) {
		this.propertyRepository = propertyRepository;

	}

	// 1️⃣ Create Order
	@PostMapping("/create-order")
	public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> data) {
		logger.info(" createOrder is coming...");
		try {
			Object feeObj = data.get("amount");
	        double fee = Double.parseDouble(feeObj.toString()); 
	        int amount = (int) (fee * 100);
			
			
			logger.info("amount is coming: {}", amount);

			RazorpayClient client = new RazorpayClient(razorpayKeyId, razorpayKeySecret);

			String receiptId = "rcpt_" + UUID.randomUUID().toString().substring(0, 25);
			JSONObject orderRequest = new JSONObject();
			orderRequest.put("amount", amount);
			orderRequest.put("currency", "INR");
			orderRequest.put("receipt", receiptId);

			Order order = client.orders.create(orderRequest);
			logger.info("order is coming: {}", order);

			Map<String, Object> response = new HashMap<>();
			response.put("id", order.get("id"));
			response.put("amount", order.get("amount"));
			response.put("currency", "INR");
			response.put("key", razorpayKeyId);
			logger.info("response is coming: {}", response);
			return ResponseEntity.ok(response);

		} catch (Exception e) {
			logger.error("Error creating order: {}", e.getMessage());
			return ResponseEntity.status(500).body("Failed to create order: " + e.getMessage());
		}
	}

	@Transactional
	@PostMapping("/verify")
	public ResponseEntity<?> verifyPayment(@RequestBody PaymentVerificationRequestDto request) {
		try {
			String payload = request.getRazorpayOrderId() + "|" + request.getRazorpayPaymentId();
			String expectedSignature = RazorpaySignatureUtil.hmacSHA256(payload, razorpayKeySecret);
			logger.info("verifyPayment payload is coming: {}", payload);
			logger.info("verifyPayment expectedSignature is coming: {}", expectedSignature);
			if (!expectedSignature.equals(request.getRazorpaySignature())) {
				return ResponseEntity.status(400).body("Invalid Payment Signature");
			}

			// 1) Get property
			Property property = propertyRepository.findById(request.getPropertyId())
					.orElseThrow(() -> new RuntimeException("Property not found"));
			logger.info("property is coming: {}", property);
			// 2) Update property fee status
			property.setIsFeePaid(true);
			propertyRepository.save(property);

			// 3) Save payment history
			PropertyPayment payment = new PropertyPayment();
			payment.setUserId(request.getUserId()); // 13 or dynamic user ID
			payment.setPropertyId(request.getPropertyId());
			payment.setRazorpayOrderId(request.getRazorpayOrderId());
			payment.setRazorpayPaymentId(request.getRazorpayPaymentId());
			payment.setAmount(property.getFee());
			payment.setStatus("SUCCESS");
			logger.info("payment is coming: {}", payment);
			paymentRepository.save(payment);

			return ResponseEntity.ok(Map.of("propertyId", request.getPropertyId(), "status", "SUCCESS"));

		} catch (Exception e) {
			return ResponseEntity.status(500).body("Verification Failed: " + e.getMessage());
		}
	}

	@GetMapping("/invoice/property/{propertyId}")
	public ResponseEntity<byte[]> downloadInvoice(@PathVariable Long propertyId) {
		logger.info("propertyId is coming: {}", propertyId);
		Property property = propertyRepository.findById(propertyId)
				.orElseThrow(() -> new RuntimeException("Property not found"));

		PropertyPayment payment = paymentRepository.findTopByPropertyIdOrderByIdDesc(propertyId);

		if (payment == null || !"SUCCESS".equals(payment.getStatus())) {
			throw new RuntimeException("Invoice not available");
		}

		byte[] pdf = invoiceService.generateInvoice(property, payment);
		logger.info("pdf is coming: {}", pdf);
		return ResponseEntity.ok().header("Content-Disposition", "attachment; filename=invoice_" + propertyId + ".pdf")
				.contentType(org.springframework.http.MediaType.APPLICATION_PDF).body(pdf);
	}

}
