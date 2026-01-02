package com.example.auth.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.auth.dto.OrderRequest;
import com.example.auth.entity.Property;
import com.example.auth.repository.PropertyRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;

@RestController
@RequestMapping("/api/property")
@CrossOrigin(origins = { "http://localhost:5173", "http://192.168.1.71:5173" })
public class AddPropertyController {

	private static final Logger logger = LoggerFactory.getLogger(AddPropertyController.class);

	private final PropertyRepository propertyRepository;

	public AddPropertyController(PropertyRepository propertyRepository) {
		this.propertyRepository = propertyRepository;
	}

	@GetMapping("/all")
	public ResponseEntity<?> getAllProperties() {
		// logger.info("GET /api/property/all called");
		try {
			List<Property> list = propertyRepository.findAllByOrderByIdDesc();
			// logger.info("Properties found: {}", list.size());
			return ResponseEntity.ok(list);
		} catch (DataAccessException dae) {
			logger.error("Database access error: {}", dae.getMessage(), dae);
			return ResponseEntity.status(500).body("Database error while fetching properties");
		} catch (NullPointerException npe) {
			logger.error("Null pointer exception: {}", npe.getMessage(), npe);
			return ResponseEntity.status(500).body("Server error: repository is null");
		} catch (Exception e) {
			logger.error("Unexpected error: {}", e.getMessage(), e);
			return ResponseEntity.status(500).body("Unexpected error occurred");
		}
	}

	@PostMapping(value = "/addProperty", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<?> addProperty(@RequestPart("image") MultipartFile image,
			@RequestPart("property") String propertyJson) {
		try {
			Property property = new ObjectMapper().readValue(propertyJson, Property.class);

			// 1. Define and Create Directory
			String uploadDir = "E:/java_projects/freelance_project's/spring-auth-app/uploads/";
			Path uploadPath = Paths.get(uploadDir);

			if (!Files.exists(uploadPath)) {
				Files.createDirectories(uploadPath);
			}

			// 2. Sanitize and Create Unique Filename
			String originalFileName = image.getOriginalFilename() != null ? image.getOriginalFilename() : "image.jpg";
			String cleanFileName = System.currentTimeMillis() + "_"
					+ originalFileName.replaceAll("[^a-zA-Z0-9\\.\\-]", "_");

			Path filePath = uploadPath.resolve(cleanFileName);

			// 3. Save the File to Disk
			Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

			// 4. Generate Browser-Accessible URL
			String imageUrl = "http://localhost:8080/uploads/" + cleanFileName;

			property.setPropertiesImageUrl(imageUrl);

			// 5. Save to Database
			Property saved = propertyRepository.save(property);
			return ResponseEntity.ok(saved);

		} catch (IOException e) {
			logger.error("File Upload Error: {}", e.getMessage());
			return ResponseEntity.status(500).body("Could not save image file");
		} catch (Exception e) {
			logger.error("Unexpected Error: {}", e.getMessage());
			return ResponseEntity.status(500).body("Error: " + e.getMessage());
		}
	}

	@PutMapping("/updateFeeStatus")
	public ResponseEntity<?> updateFeeStatus(@RequestBody Map<String, Long> req)
			throws NullPointerException, DataAccessException {
		//logger.info("PUT /updateFeeStatus called with body: {}", req);
		try {
			Long propertyId = req.get("propertyId");
		//	logger.info("propertyId  : {}", propertyId);
			if (propertyId == null) {
				return ResponseEntity.status(400).body("Property ID is required");
			}

			Property property = propertyRepository.findById(propertyId)
					.orElseThrow(() -> new RuntimeException("Property not found"));
			//logger.info("property  : {}", property);
			property.setIsFeePaid(true);
			propertyRepository.save(property);
			//logger.info("Fee status updated for propertyId: {}", propertyId);
			return ResponseEntity.ok("Consulting fee paid");
		} catch (RuntimeException re) {
			logger.error("Runtime exception: {}", re.getMessage(), re);
			return ResponseEntity.status(404).body(re.getMessage());
		} catch (Exception e) {
			logger.error("Unexpected error: {}", e.getMessage(), e);
			return ResponseEntity.status(500).body("Unexpected error occurred");
		}
	}

	@PostMapping("/createOrder")
	public ResponseEntity<?> createOrder(@RequestBody OrderRequest request) {
		//logger.info("POST /createOrder called with request: {}", request);
		try {
			if (request == null) {
				return ResponseEntity.status(400).body("Amount is required");
			}

			RazorpayClient razorpay = new RazorpayClient("rzp_live_RoeISLr1UGT1gl", "jUgOGuxDZzeiZz4F81OKiV5M");

			JSONObject orderRequest = new JSONObject();
			orderRequest.put("amount", request.getAmount()); // in paise
			orderRequest.put("currency", "INR");
			orderRequest.put("payment_capture", true);

			Order order = razorpay.orders.create(orderRequest);
			return ResponseEntity.ok(order.toString());

		} catch (RazorpayException re) {
			logger.error("Razorpay API error: {}", re.getMessage(), re);
			return ResponseEntity.status(500).body("Razorpay order creation failed");
		} catch (JSONException je) {
			logger.error("JSON error: {}", je.getMessage(), je);
			return ResponseEntity.status(500).body("Error creating order JSON");
		} catch (NullPointerException npe) {
			logger.error("Null pointer exception: {}", npe.getMessage(), npe);
			return ResponseEntity.status(400).body("Invalid order request");
		} catch (Exception e) {
			logger.error("Unexpected error: {}", e.getMessage(), e);
			return ResponseEntity.status(500).body("Unexpected error occurred");
		}
	}

}
