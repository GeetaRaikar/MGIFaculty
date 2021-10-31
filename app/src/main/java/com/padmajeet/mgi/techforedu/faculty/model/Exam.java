package com.padmajeet.mgi.techforedu.faculty.model;

import com.google.firebase.firestore.Exclude;

import java.util.Date;

public class Exam {
	@Exclude
	private String id;
	private String examSeriesId;
	private String subjectId;
	private String batchId;
	private Date date;
	//private int fromHrs;
	//private int fromMins;
	private String fromPeriod;// 09:00 AM
	//private int toHrs;
	//private int toMins;
	private String toPeriod;// 10:00 AM
	private int durationInMin;
	private float totalMarks;
	private float cutOffMarks;
	//private int totalQuestions;
	//private float marksPerQuestion;
	//private float negativeMarking;
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

	public String getExamSeriesId() {
		return examSeriesId;
	}

	public void setExamSeriesId(String examSeriesId) {
		this.examSeriesId = examSeriesId;
	}

	public String getSubjectId() {
		return subjectId;
	}

	public void setSubjectId(String subjectId) {
		this.subjectId = subjectId;
	}

	public String getBatchId() {
		return batchId;
	}

	public void setBatchId(String batchId) {
		this.batchId = batchId;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}
	/*
	public int getFromHrs() {
		return fromHrs;
	}

	public void setFromHrs(int fromHrs) {
		this.fromHrs = fromHrs;
	}

	public int getFromMins() {
		return fromMins;
	}

	public void setFromMins(int fromMins) {
		this.fromMins = fromMins;
	}


	public int getToHrs() {
		return toHrs;
	}

	public void setToHrs(int toHrs) {
		this.toHrs = toHrs;
	}

	public int getToMins() {
		return toMins;
	}

	public void setToMins(int toMins) {
		this.toMins = toMins;
	}
	*/
	public String getToPeriod() {
		return toPeriod;
	}
	public String getFromPeriod() {
		return fromPeriod;
	}

	public void setFromPeriod(String fromPeriod) {
		this.fromPeriod = fromPeriod;
	}

	public void setToPeriod(String toPeriod) {
		this.toPeriod = toPeriod;
	}

	public int getDurationInMin() {
		return durationInMin;
	}

	public void setDurationInMin(int durationInMin) {
		this.durationInMin = durationInMin;
	}

	public float getTotalMarks() {
		return totalMarks;
	}

	public void setTotalMarks(float totalMarks) {
		this.totalMarks = totalMarks;
	}

	public float getCutOffMarks() {
		return cutOffMarks;
	}

	public void setCutOffMarks(float cutOffMarks) {
		this.cutOffMarks = cutOffMarks;
	}
	/*
        public int getTotalQuestions() {
            return totalQuestions;
        }

        public void setTotalQuestions(int totalQuestions) {
            this.totalQuestions = totalQuestions;
        }


        public float getMarksPerQuestion() {
            return marksPerQuestion;
        }

        public void setMarksPerQuestion(float marksPerQuestion) {
            this.marksPerQuestion = marksPerQuestion;
        }

        public float getNegativeMarking() {
            return negativeMarking;
        }

        public void setNegativeMarking(float negativeMarking) {
            this.negativeMarking = negativeMarking;
        }

        */
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
