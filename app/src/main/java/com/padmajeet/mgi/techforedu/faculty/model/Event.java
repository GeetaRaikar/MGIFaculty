package com.padmajeet.mgi.techforedu.faculty.model;
import com.google.firebase.firestore.Exclude;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Event {
	@Exclude
	private String id;
	private String academicYearId;
	private String instituteId;
	private String typeId;//event type id
	private String batchId;
	private String sectionId;//new field
	private String attachmentUrl;
	private String category;//N- Not expecting response; R - Expect an response
	private String description;
	private String dressCode;
	private Date fromDate;
	private Date toDate;
	private String name;
	private String recipientType;// A - All,P - Parent,F - faculty,S - Student,PS - Parent and student,PF-Parent and faculty,FS-Student and faculty
	private Map<String, String> studentResponses = new HashMap<>();
	private Map<String, String> parentResponses = new HashMap<>();
	private Map<String, String> staffResponses = new HashMap<>();//faculty
	private Boolean schoolScope;
	private String status = "A";
	private Date createdDate = new Date();
	private Date modifiedDate = new Date();
	private String creatorId;
	private String modifierId;
	private String creatorType="A";
	private String modifierType="A";

	@Exclude
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getAcademicYearId() {
		return academicYearId;
	}

	public void setAcademicYearId(String academicYearId) {
		this.academicYearId = academicYearId;
	}

	public String getInstituteId() {
		return instituteId;
	}

	public void setInstituteId(String instituteId) {
		this.instituteId = instituteId;
	}

	public String getTypeId() {
		return typeId;
	}

	public void setTypeId(String typeId) {
		this.typeId = typeId;
	}

	public String getBatchId() {
		return batchId;
	}

	public void setBatchId(String batchId) {
		this.batchId = batchId;
	}

	public String getSectionId() {
		return sectionId;
	}

	public void setSectionId(String sectionId) {
		this.sectionId = sectionId;
	}

	public String getAttachmentUrl() {
		return attachmentUrl;
	}

	public void setAttachmentUrl(String attachmentUrl) {
		this.attachmentUrl = attachmentUrl;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDressCode() {
		return dressCode;
	}

	public void setDressCode(String dressCode) {
		this.dressCode = dressCode;
	}

	public Date getFromDate() {
		return fromDate;
	}

	public void setFromDate(Date fromDate) {
		this.fromDate = fromDate;
	}

	public Date getToDate() {
		return toDate;
	}

	public void setToDate(Date toDate) {
		this.toDate = toDate;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getRecipientType() {
		return recipientType;
	}

	public void setRecipientType(String recipientType) {
		this.recipientType = recipientType;
	}

	public Map<String, String> getStudentResponses() {
		return studentResponses;
	}

	public void setStudentResponses(Map<String, String> studentResponses) {
		this.studentResponses = studentResponses;
	}

	public Map<String, String> getParentResponses() {
		return parentResponses;
	}

	public void setParentResponses(Map<String, String> parentResponses) {
		this.parentResponses = parentResponses;
	}

	public Map<String, String> getStaffResponses() {
		return staffResponses;
	}

	public void setStaffResponses(Map<String, String> staffResponses) {
		this.staffResponses = staffResponses;
	}

	public Boolean getSchoolScope() {
		return schoolScope;
	}

	public void setSchoolScope(Boolean schoolScope) {
		this.schoolScope = schoolScope;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public Date getModifiedDate() {
		return modifiedDate;
	}

	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}

	public String getCreatorId() {
		return creatorId;
	}

	public void setCreatorId(String creatorId) {
		this.creatorId = creatorId;
	}

	public String getModifierId() {
		return modifierId;
	}

	public void setModifierId(String modifierId) {
		this.modifierId = modifierId;
	}

	public String getCreatorType() {
		return creatorType;
	}

	public void setCreatorType(String creatorType) {
		this.creatorType = creatorType;
	}

	public String getModifierType() {
		return modifierType;
	}

	public void setModifierType(String modifierType) {
		this.modifierType = modifierType;
	}
}