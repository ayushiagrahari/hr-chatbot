package com.hr.chatbot.service;

import com.hr.chatbot.model.ChatMessage;
import com.hr.chatbot.model.Faq;
import com.hr.chatbot.repository.ChatMessageRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ChatService {

    private final FaqService faqService;
    private final ChatMessageRepository chatMessageRepository;

    @Value("${chatbot.fallback-message}")
    private String fallbackMessage;

    public ChatService(FaqService faqService, ChatMessageRepository chatMessageRepository) {
        this.faqService = faqService;
        this.chatMessageRepository = chatMessageRepository;
    }

    public String processQuestion(String question) {
        Optional<Faq> match = faqService.findBestMatch(question);
        String answer = match.map(Faq::getAnswer).orElse(fallbackMessage);
        chatMessageRepository.save(new ChatMessage(question, answer));
        return answer;
    }

    public List<ChatMessage> getChatHistory() {
        return chatMessageRepository.findAllByOrderByTimestampDesc();
    }
}
