package com.padmajeet.mgi.techforedu.faculty.model;

import com.google.firebase.firestore.Exclude;

import java.util.Date;

public class StudentFeesSummary {
    @Exclude
    String id;
    private float totalFees;//total fees for the class
    private float discount;
    private String currentBatchId;
    private String studentId;
    private String academicYearId;
    private Date discountDate;
    private String discounterId;//Id of the person who added the discount;
    private Date createdDate = new Date();
    private Date modifiedDate = new Date();
    private String creatorId;
    private String modifierId;
    private String creatorType;
    private String modifierType;
}
