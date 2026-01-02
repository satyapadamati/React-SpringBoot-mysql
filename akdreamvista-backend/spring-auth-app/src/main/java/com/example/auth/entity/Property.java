package com.example.auth.entity;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "properties")
public class Property {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "properties_title")
	private String propertiesTitle;

	@Column(name = "properties_type")
	private String propertiesType;

	@Column(name = "land_area")
	private String landArea;

	@Column(name = "facing")
	private String facing;

	@Column(name = "floors")
	private String floors;

	@Column(name = "price")
	private Double price;

	@Column(name = "property_status")
	private String propertyStatus;

	@Column(name = "owner_details")
	private String ownerDetails;

	@Column(name = "consulting_fee")
	private Double fee;

	@Column(name = "youtube_link")
	private String youtubeLink;

	@Column(name = "image_url")
	private String propertiesImageUrl;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@Column(name = "created_date")
	private LocalDateTime createdDate;

	@Column(name = "description")

	private String description;
	@Column(name = "owner_contact")
	private String ownerContact;

	@Column(name = "is_fee_paid")
	private Boolean isFeePaid = false;

	public Boolean getIsFeePaid() {
		return isFeePaid;
	}

	public void setIsFeePaid(Boolean isFeePaid) {
		this.isFeePaid = (isFeePaid != null ? isFeePaid : false);
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getOwnerContact() {
		return ownerContact;
	}

	public void setOwnerContact(String ownerContact) {
		this.ownerContact = ownerContact;
	}

	public Property() {
	}

	public Property(Long id, String propertiesTitle, String propertiesType, String landArea, String facing,
			String floors, Double price, String propertyStatus, String ownerDetails, Double fee, String youtubeLink,
			String propertiesImageUrl, LocalDateTime createdDate) {
		this.id = id;
		this.propertiesTitle = propertiesTitle;
		this.propertiesType = propertiesType;
		this.landArea = landArea;
		this.facing = facing;
		this.floors = floors;
		this.price = price;
		this.propertyStatus = propertyStatus;
		this.ownerDetails = ownerDetails;
		this.fee = fee;
		this.youtubeLink = youtubeLink;
		this.propertiesImageUrl = propertiesImageUrl;
		this.createdDate = createdDate;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getPropertiesTitle() {
		return propertiesTitle;
	}

	public void setPropertiesTitle(String propertiesTitle) {
		this.propertiesTitle = propertiesTitle;
	}

	public String getPropertiesType() {
		return propertiesType;
	}

	public void setPropertiesType(String propertiesType) {
		this.propertiesType = propertiesType;
	}

	public String getLandArea() {
		return landArea;
	}

	public void setLandArea(String landArea) {
		this.landArea = landArea;
	}

	public String getFacing() {
		return facing;
	}

	public void setFacing(String facing) {
		this.facing = facing;
	}

	public String getFloors() {
		return floors;
	}

	public void setFloors(String floors) {
		this.floors = floors;
	}

	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}

	public String getPropertyStatus() {
		return propertyStatus;
	}

	public void setPropertyStatus(String propertyStatus) {
		this.propertyStatus = propertyStatus;
	}

	public String getOwnerDetails() {
		return ownerDetails;
	}

	public void setOwnerDetails(String ownerDetails) {
		this.ownerDetails = ownerDetails;
	}

	public Double getFee() {
		return fee;
	}

	public void setFee(Double fee) {
		this.fee = fee;
	}

	public String getYoutubeLink() {
		return youtubeLink;
	}

	public void setYoutubeLink(String youtubeLink) {
		this.youtubeLink = youtubeLink;
	}

	public String getPropertiesImageUrl() {
		return propertiesImageUrl;
	}

	public void setPropertiesImageUrl(String propertiesImageUrl) {
		this.propertiesImageUrl = propertiesImageUrl;
	}

	public LocalDateTime getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(LocalDateTime createdDate) {
		this.createdDate = createdDate;
	}

}
