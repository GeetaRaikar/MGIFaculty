package com.padmajeet.mgi.techforedu.faculty.model;

import com.google.firebase.firestore.Exclude;

import java.util.Date;

public class Attendance {
	@Exclude
    private String id;
	private String academicYearId;
	private String batchId;
	private String sectionId;
	private String subjectId;
	private String periodId;
	private String studentId;
	private Date date;
	private Boolean isLate;
	private int lateTimeInMin;
	private String instituteId;
	private String status;	//P- present, A- Absent
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

	public String getSubjectId() {
		return subjectId;
	}

	public void setSubjectId(String subjectId) {
		this.subjectId = subjectId;
	}

	public String getPeriodId() {
		return periodId;
	}

	public void setPeriodId(String periodId) {
		this.periodId = periodId;
	}

	public String getStudentId() {
		return studentId;
	}

	public void setStudentId(String studentId) {
		this.studentId = studentId;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public Boolean getLate() {
		return isLate;
	}

	public void setLate(Boolean late) {
		isLate = late;
	}

	public int getLateTimeInMin() {
		return lateTimeInMin;
	}

	public void setLateTimeInMin(int lateTimeInMin) {
		this.lateTimeInMin = lateTimeInMin;
	}

	public String getInstituteId() {
		return instituteId;
	}

	public void setInstituteId(String instituteId) {
		this.instituteId = instituteId;
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
