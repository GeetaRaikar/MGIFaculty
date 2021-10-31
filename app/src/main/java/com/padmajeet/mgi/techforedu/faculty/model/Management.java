package com.padmajeet.mgi.techforedu.faculty.model;

import com.google.firebase.firestore.Exclude;

import java.util.Date;

//TODO we should include in Staff
public class Management {
	@Exclude
	private String id;
	private String mobileNumber;
	private String firstName;
	private String middleName;
	private String lastName;
	private String emailId;
	private String password;
	private String status = "A";
	private Date createdDate = new Date();
	private Date modifiedDate = new Date();
	private String gender;
	private String imageUrl;
}
