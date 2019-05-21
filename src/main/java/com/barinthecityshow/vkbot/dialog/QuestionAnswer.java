package com.barinthecityshow.vkbot.dialog;

import java.util.ArrayList;
import java.util.List;

public class QuestionAnswer {
    private final String question;
    private final List<String> correctAnswers;
    private final List<String> options;

    private QuestionAnswer(String question, List<String> correctAnswers, List<String> options) {
        this.question = question;
        this.correctAnswers = correctAnswers;
        this.options = options;
    }

    public String getQuestion() {
        return question;
    }

    public List<String> getCorrectAnswers() {
        return correctAnswers;
    }

    public List<String> getOptions() {
        return options;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String question;
        private List<String> correctAnswers = new ArrayList<>();
        private List<String> options = new ArrayList<>();

        public Builder question(String question) {
            this.question = question;
            return this;
        }

        public Builder addCorrectAnswer(String answer) {
            this.correctAnswers.add(answer);
            return this;
        }

        public Builder addCorrectAnswers(List<String> answers) {
            this.correctAnswers.addAll(answers);
            return this;
        }

        public Builder addOption(String option) {
            this.options.add(option);
            return this;
        }

        public Builder addOptions(List<String> options) {
            this.options.addAll(options);
            return this;
        }

        public QuestionAnswer build() {
            return new QuestionAnswer(question, correctAnswers, options);
        }
    }
}
