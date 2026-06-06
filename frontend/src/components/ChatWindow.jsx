import { useState, useEffect, useRef } from 'react';
import './ChatWindow.css';

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

const SUGGESTED_QUESTIONS = [
  'How many leave days do I get?',
  'What is the notice period?',
  'What is the work from home policy?',
  'How do I download my salary slip?',
  'What is the health insurance policy?',
];

function ChatWindow() {
  const [messages, setMessages] = useState([
    {
      id: 1,
      type: 'bot',
      text: "Hello! I'm your HR Assistant. I can help you with questions about leave policies, salary, work from home rules, and much more. How can I help you today?",
      time: new Date(),
    },
  ]);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);
  const messagesEndRef = useRef(null);
  const inputRef = useRef(null);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const sendMessage = async (question) => {
    const text = question || input.trim();
    if (!text || loading) return;

    const userMessage = { id: Date.now(), type: 'user', text, time: new Date() };
    setMessages((prev) => [...prev, userMessage]);
    setInput('');
    setLoading(true);

    try {
      const response = await fetch(`${API_URL}/chat`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ question: text }),
      });

      if (!response.ok) throw new Error('Server error');

      const data = await response.json();
      const botMessage = { id: Date.now() + 1, type: 'bot', text: data.answer, time: new Date() };
      setMessages((prev) => [...prev, botMessage]);
    } catch {
      const errorMessage = {
        id: Date.now() + 1,
        type: 'bot',
        text: 'Sorry, I could not connect to the server. Please make sure the backend is running.',
        time: new Date(),
        isError: true,
      };
      setMessages((prev) => [...prev, errorMessage]);
    } finally {
      setLoading(false);
      inputRef.current?.focus();
    }
  };

  const handleKeyDown = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      sendMessage();
    }
  };

  const formatTime = (date) =>
    date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });

  return (
    <div className="chat-window">
      <div className="messages-area">
        {messages.map((msg) => (
          <div key={msg.id} className={`message-row ${msg.type}`}>
            {msg.type === 'bot' && (
              <div className="avatar bot-avatar">HR</div>
            )}
            <div className={`bubble ${msg.type} ${msg.isError ? 'error' : ''}`}>
              <p>{msg.text}</p>
              <span className="time">{formatTime(msg.time)}</span>
            </div>
            {msg.type === 'user' && (
              <div className="avatar user-avatar">You</div>
            )}
          </div>
        ))}

        {loading && (
          <div className="message-row bot">
            <div className="avatar bot-avatar">HR</div>
            <div className="bubble bot typing-bubble">
              <span className="dot"></span>
              <span className="dot"></span>
              <span className="dot"></span>
            </div>
          </div>
        )}

        <div ref={messagesEndRef} />
      </div>

      {messages.length === 1 && (
        <div className="suggestions">
          <p className="suggestions-label">Suggested questions</p>
          <div className="suggestions-list">
            {SUGGESTED_QUESTIONS.map((q) => (
              <button key={q} className="suggestion-chip" onClick={() => sendMessage(q)}>
                {q}
              </button>
            ))}
          </div>
        </div>
      )}

      <div className="input-area">
        <input
          ref={inputRef}
          type="text"
          className="chat-input"
          placeholder="Type your HR question here..."
          value={input}
          onChange={(e) => setInput(e.target.value)}
          onKeyDown={handleKeyDown}
          disabled={loading}
        />
        <button
          className="send-button"
          onClick={() => sendMessage()}
          disabled={!input.trim() || loading}
        >
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <line x1="22" y1="2" x2="11" y2="13" />
            <polygon points="22 2 15 22 11 13 2 9 22 2" />
          </svg>
        </button>
      </div>
    </div>
  );
}

export default ChatWindow;
