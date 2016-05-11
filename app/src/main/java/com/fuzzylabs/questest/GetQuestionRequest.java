package com.fuzzy.questest;

public class GetQuestionRequest {

	private UserAttempt userAttempt;
	private Question question;
	private User user;

	public UserAttempt getUserAttempt() {
		return userAttempt;
	}

	public void setUserAttempt(UserAttempt userAttempt) {
		this.userAttempt = userAttempt;
	}

	public Question getQuestion() {
		return question;
	}

	public void setQuestion(Question question) {
		this.question = question;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

}
