package com.hr.chatbot.controller;

import com.hr.chatbot.model.ChatMessage;
import com.hr.chatbot.model.Faq;
import com.hr.chatbot.service.ChatService;
import com.hr.chatbot.service.FaqService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ChatController {

    private final ChatService chatService;
    private final FaqService faqService;

    public ChatController(ChatService chatService, FaqService faqService) {
        this.chatService = chatService;
        this.faqService = faqService;
    }

    @PostMapping("/chat")
    public ResponseEntity<Map<String, String>> chat(@Valid @RequestBody ChatRequest request) {
        String answer = chatService.processQuestion(request.question());
        return ResponseEntity.ok(Map.of("answer", answer));
    }

    @GetMapping("/chat/history")
    public ResponseEntity<List<ChatMessage>> getChatHistory() {
        return ResponseEntity.ok(chatService.getChatHistory());
    }

    @GetMapping("/faq")
    public ResponseEntity<List<Faq>> getAllFaqs() {
        return ResponseEntity.ok(faqService.getAllFaqs());
    }

    @PostMapping("/faq")
    public ResponseEntity<Faq> addFaq(@Valid @RequestBody Faq faq) {
        return ResponseEntity.ok(faqService.saveFaq(faq));
    }

    @DeleteMapping("/faq/{id}")
    public ResponseEntity<Void> deleteFaq(@PathVariable Long id) {
        faqService.deleteFaq(id);
        return ResponseEntity.noContent().build();
    }

    record ChatRequest(@NotBlank(message = "Question must not be blank") String question) {}
}
