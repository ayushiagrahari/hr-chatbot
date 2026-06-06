package com.hr.chatbot.service;

import com.hr.chatbot.model.Faq;
import com.hr.chatbot.repository.FaqRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class FaqService {

    private static final Set<String> STOP_WORDS = Set.of(
        "a", "an", "the", "is", "it", "in", "on", "at", "to", "for",
        "of", "and", "or", "but", "i", "my", "me", "do", "does", "how",
        "what", "when", "where", "who", "can", "will", "are", "was",
        "be", "have", "has", "get", "about", "with", "from", "by"
    );

    private final FaqRepository faqRepository;

    @Value("${chatbot.min-match-score:1}")
    private int minMatchScore;

    public FaqService(FaqRepository faqRepository) {
        this.faqRepository = faqRepository;
    }

    public Optional<Faq> findBestMatch(String question) {
        List<String> questionTokens = tokenize(question);
        List<Faq> allFaqs = faqRepository.findAll();

        Faq bestMatch = null;
        int bestScore = 0;

        for (Faq faq : allFaqs) {
            int score = computeScore(questionTokens, faq);
            if (score > bestScore) {
                bestScore = score;
                bestMatch = faq;
            }
        }

        return (bestScore >= minMatchScore) ? Optional.ofNullable(bestMatch) : Optional.empty();
    }

    public List<Faq> getAllFaqs() {
        return faqRepository.findAll();
    }

    public Faq saveFaq(Faq faq) {
        return faqRepository.save(faq);
    }

    public void deleteFaq(Long id) {
        faqRepository.deleteById(id);
    }

    private int computeScore(List<String> questionTokens, Faq faq) {
        int score = 0;

        // Score against FAQ keywords
        List<String> keywordTokens = Arrays.stream(faq.getKeywords().split(","))
            .map(String::trim)
            .map(String::toLowerCase)
            .toList();

        for (String token : questionTokens) {
            for (String keyword : keywordTokens) {
                if (keyword.contains(token) || token.contains(keyword)) {
                    score += 2;
                }
            }
        }

        // Additional score from question text match
        List<String> faqQuestionTokens = tokenize(faq.getQuestion());
        for (String token : questionTokens) {
            if (faqQuestionTokens.contains(token)) {
                score += 1;
            }
        }

        return score;
    }

    private List<String> tokenize(String text) {
        return Arrays.stream(text.toLowerCase().replaceAll("[^a-z0-9 ]", " ").split("\\s+"))
            .filter(word -> !word.isBlank() && !STOP_WORDS.contains(word) && word.length() > 2)
            .toList();
    }
}
