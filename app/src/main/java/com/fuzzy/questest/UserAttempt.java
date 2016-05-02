package com.fuzzy.questest;

public class UserAttempt {

	private String id;
	private String userId;
	private String subject;
	private String questionId;
	private String marked;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getQuestionId() {
		return questionId;
	}

	public void setQuestionId(String questionId) {
		this.questionId = questionId;
	}

	public String getMarked() {
		return marked;
	}

	public void setMarked(String marked) {
		this.marked = marked;
	}

}