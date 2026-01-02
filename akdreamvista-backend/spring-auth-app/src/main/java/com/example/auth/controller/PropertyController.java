package com.example.auth.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.auth.entity.Property;
import com.example.auth.repository.PropertyRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/properties")

public class PropertyController {
	private static final Logger logger = LoggerFactory.getLogger(PropertyController.class);

	private final PropertyRepository repo;

	public PropertyController(PropertyRepository repo) {
		this.repo = repo;
	}

	@GetMapping
	public List<Property> getAllPropertiesDefault() {
		return repo.findAll();
	}

	@GetMapping("/count")
	public ResponseEntity<Map<String, Long>> getPropertyCounts() {

		// logger.info("GET /count - Fetching property counts");

		try {
			long total = repo.totalCount();
			long paid = repo.paidCount();
			long unpaid = repo.unpaidCount();

			// logger.info("Property counts fetched successfully | total={} paid={}
			// unpaid={}", total, paid, unpaid);

			return ResponseEntity.ok(Map.of("total", total, "paid", paid, "unpaid", unpaid));

		} catch (Exception e) {
			logger.error("Failed to fetch property counts", e);
			return ResponseEntity.status(500).body(Map.of("total", 0L, "paid", 0L, "unpaid", 0L));
		}
	}

	// GET all properties for admin table
	// ADD new property
	@PostMapping
	public Property addProperty(@RequestBody Property property) {
		return repo.save(property);
	}

	@GetMapping("/{id}")
	public ResponseEntity<?> getProperty(@PathVariable Long id) {
		logger.info("Fetching property details for ID: {}", id);
		java.util.Optional<Property> propertyOpt = repo.findById(id);
		if (propertyOpt.isPresent()) {
			return ResponseEntity.ok(propertyOpt.get());
		} else {
			logger.error("Property with ID {} not found", id);
			return ResponseEntity.status(404).body("Property not found with ID: " + id);
		}
	}

	// UPDATE property
	@PutMapping("/{id}")
	public Property updateProperty(@PathVariable Long id, @RequestBody Property updated) {
		return repo.findById(id).map(x -> {
			x.setDescription(updated.getDescription());
			x.setFacing(updated.getFacing());
			x.setFee(updated.getFee());
			x.setFloors(updated.getFloors());
			x.setLandArea(updated.getLandArea());
			x.setOwnerContact(updated.getOwnerContact());
			x.setOwnerDetails(updated.getOwnerDetails());
			x.setPrice(updated.getPrice());
			x.setPropertiesImageUrl(updated.getPropertiesImageUrl());
			x.setPropertiesTitle(updated.getPropertiesTitle());
			x.setPropertiesType(updated.getPropertiesType());
			x.setPropertyStatus(updated.getPropertyStatus());
			x.setYoutubeLink(updated.getYoutubeLink());
			x.setIsFeePaid(updated.getIsFeePaid());
			return repo.save(x);
		}).orElse(null);
	}

	@PutMapping("/update/{id}")
	public ResponseEntity<?> updateProperty(@PathVariable Long id,
			@RequestParam(value = "image", required = false) MultipartFile image,
			@RequestParam("property") String propertyJson) {

		try {
			// 1. Convert JSON String to Property Object
			ObjectMapper objectMapper = new ObjectMapper();
			Property updatedData = objectMapper.readValue(propertyJson, Property.class);

			return repo.findById(id).map(existing -> {
				existing.setPropertiesTitle(updatedData.getPropertiesTitle());
				existing.setPropertiesType(updatedData.getPropertiesType());
				existing.setPrice(updatedData.getPrice());
				existing.setLandArea(updatedData.getLandArea());
				existing.setFacing(updatedData.getFacing());
				existing.setFloors(updatedData.getFloors());
				existing.setPropertyStatus(updatedData.getPropertyStatus());
				existing.setOwnerContact(updatedData.getOwnerContact());
				existing.setFee(updatedData.getFee());
				existing.setYoutubeLink(updatedData.getYoutubeLink());
				existing.setDescription(updatedData.getDescription());
				existing.setIsFeePaid(updatedData.getIsFeePaid());
				existing.setPropertiesImageUrl(updatedData.getPropertiesImageUrl());

				if (image != null && !image.isEmpty()) {
					try {
						// Ikkada mee Cloudinary leda Folder upload logic rayali
						// Example (Just getting bytes for now):
						// String imageUrl = uploadService.upload(image);
						// existing.setPropertiesImageUrl(imageUrl);

						logger.info("New image received for Property ID: {}", id);
					} catch (Exception e) {
						logger.error("Image upload failed", e);
					}
				}

				Property saved = repo.save(existing);
				return ResponseEntity.ok(saved);
			}).orElse(ResponseEntity.notFound().build());

		} catch (IOException e) {
			logger.error("JSON parsing error", e);
			return ResponseEntity.status(400).body("Invalid property data format");
		}
	}
	// DELETE property

	@DeleteMapping("/{id}")
	public ResponseEntity<Map<String, String>> deleteProperty(@PathVariable Long id) {
		repo.deleteById(id);

		Map<String, String> response = new HashMap<>();
		response.put("message", "Property deleted successfully");

		return ResponseEntity.ok(response); // ✅ JSON
	}

	@GetMapping("/by-fee-status")
	public List<Property> filterByFeePaid(@RequestParam boolean paid) {

		// logger.info("GET /by-fee-status - paid={}", paid);

		return repo.findByIsFeePaid(paid);
	}

	@GetMapping("/all")
	public List<Property> getAllProperties() {
		return repo.findAll();
	}

}
