# How the HR Chatbot Was Built

## Overview

This is a full-stack HR Chatbot application built with:
- **Backend:** Java 17 + Spring Boot 3.x
- **Frontend:** React (via Vite)
- **Database:** H2 (in-memory, no setup required)
- **Matching Engine:** Custom keyword-based FAQ matcher (no LLM or API key needed)

---

## Project Structure

```
hr-chatbot/
├── backend/                                   ← Spring Boot application
│   ├── pom.xml                                ← Maven dependencies
│   └── src/main/
│       ├── java/com/hr/chatbot/
│       │   ├── HrChatbotApplication.java      ← App entry point
│       │   ├── config/
│       │   │   └── WebConfig.java             ← CORS configuration
│       │   ├── controller/
│       │   │   └── ChatController.java        ← REST API endpoints
│       │   ├── service/
│       │   │   ├── FaqService.java            ← Keyword matching engine
│       │   │   └── ChatService.java           ← Orchestrates responses
│       │   ├── model/
│       │   │   ├── Faq.java                   ← FAQ database entity
│       │   │   └── ChatMessage.java           ← Chat history entity
│       │   └── repository/
│       │       ├── FaqRepository.java         ← FAQ DB operations
│       │       └── ChatMessageRepository.java ← Chat history DB operations
│       └── resources/
│           ├── application.yml                ← App configuration
│           └── data.sql                       ← 15 seed FAQ entries
│
└── frontend/                                  ← React application
    └── src/
        ├── App.jsx                            ← Root component + header
        ├── App.css                            ← Header + layout styles
        └── components/
            ├── ChatWindow.jsx                 ← Chat UI logic
            └── ChatWindow.css                 ← Chat bubble styles
```

---

## Backend — How It Works

### 1. Entry Point (`HrChatbotApplication.java`)

Standard Spring Boot entry point. Starts the embedded Tomcat server on port 8080.

```java
@SpringBootApplication
public class HrChatbotApplication {
    public static void main(String[] args) {
        SpringApplication.run(HrChatbotApplication.class, args);
    }
}
```

---

### 2. Database Layer

#### `Faq.java` — The FAQ entity

Each FAQ entry has three fields:
- `question` — The full question text
- `answer` — The full answer text
- `keywords` — Comma-separated keywords (e.g. `leave,days,annual,vacation`)

This is stored in an H2 in-memory database table named `faq`.

#### `ChatMessage.java` — Chat history entity

Stores every question asked and the bot's response, with a timestamp.

#### `data.sql` — Seed data

15 pre-loaded HR FAQ entries covering topics like:
- Leave policy, sick leave, maternity leave
- Notice period, probation period
- Salary slips, performance reviews
- Work from home, office timings
- Health insurance, travel reimbursement
- Employee referrals, personal loans

Spring Boot automatically runs this file after Hibernate creates the tables
(made possible by `defer-datasource-initialization: true` in `application.yml`).

---

### 3. The Keyword Matching Engine (`FaqService.java`)

This is the core intelligence of the chatbot. It works in 4 steps:

```
Employee question: "How many leaves do I get per year?"
        |
        v
Step 1: Tokenize
        ["how", "many", "leaves", "get", "per", "year"]
        |
        v
Step 2: Remove stop words (a, an, the, is, how, do, i, ...)
        ["leaves", "get", "year"]
        |
        v
Step 3: Score each FAQ in the database
        - FAQ keywords match  → +2 points per match
        - FAQ question match  → +1 point per match
        |
        v
Step 4: Return the FAQ with the highest score
        If no FAQ scores above the minimum threshold → return fallback message
```

**Scoring example:**

| FAQ Entry | Keywords | Score |
|---|---|---|
| "How many leave days do I get?" | `leave,days,annual,vacation,holiday` | 4 pts (leave+2, year keyword match+1...) |
| "What is the notice period?" | `notice,period,resignation` | 0 pts |

The FAQ with the highest score wins.

---

### 4. Chat Orchestration (`ChatService.java`)

Simple orchestrator that:
1. Calls `FaqService.findBestMatch(question)` to get the best FAQ
2. If a match is found → returns that FAQ's answer
3. If no match → returns the fallback message from `application.yml`
4. Saves the question + answer to the `chat_message` table for history

---

### 5. REST API (`ChatController.java`)

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/chat` | Send a question, get an answer |
| GET | `/api/chat/history` | Retrieve all past chat messages |
| GET | `/api/faq` | List all FAQ entries |
| POST | `/api/faq` | Add a new FAQ entry |
| DELETE | `/api/faq/{id}` | Delete a FAQ entry |

**Request format:**
```json
POST /api/chat
{ "question": "What is the notice period?" }
```

**Response format:**
```json
{ "answer": "The notice period is 30 days for all employees..." }
```

---

### 6. CORS Configuration (`WebConfig.java`)

Allows the React frontend (running on port 5173) to call the Spring Boot API (running on port 8080) without browser CORS errors.

---

### 7. Application Configuration (`application.yml`)

Key settings:
- **H2 in-memory database** — no installation needed; data lives in RAM
- **H2 Console** at `/h2-console` — lets you browse the database in a browser
- **`defer-datasource-initialization: true`** — ensures Hibernate creates tables before `data.sql` inserts data
- **Fallback message** — configurable message shown when no FAQ matches

---

## Frontend — How It Works

### `App.jsx` — Root Component

Renders the top header bar (logo, title, online status indicator) and wraps the `ChatWindow` component.

### `ChatWindow.jsx` — Chat UI

All chat logic lives here:

1. **Initial state** — Shows a welcome message from the bot on load
2. **Suggested questions** — Shows 5 clickable question chips on first load to help users get started quickly
3. **Sending a message** — When the user types and presses Enter (or clicks Send):
   - The user's message is added to the chat immediately
   - A typing indicator (animated dots) appears
   - A `POST /api/chat` request is sent to the Spring Boot backend
   - The bot's response replaces the typing indicator
4. **Chat history** — Messages are stored in component state and displayed as styled bubbles
5. **Error handling** — If the backend is unreachable, a red error bubble is shown

---

## Data Flow (End to End)

```
Employee types: "What is the work from home policy?"
        |
        v
React ChatWindow.jsx
  → POST http://localhost:8080/api/chat
    { "question": "What is the work from home policy?" }
        |
        v
ChatController.java (Spring Boot)
  → calls ChatService.processQuestion(question)
        |
        v
ChatService.java
  → calls FaqService.findBestMatch(question)
        |
        v
FaqService.java
  → tokenizes question: ["work", "home", "policy"]
  → loads all FAQs from H2 database
  → scores each FAQ by keyword overlap
  → best match: FAQ with keywords "work,home,remote,wfh,policy"  ← score: 6
  → returns that Faq object
        |
        v
ChatService.java
  → saves ChatMessage(question, answer) to database
  → returns answer string
        |
        v
ChatController.java
  → returns { "answer": "Employees may work from home up to 2 days per week..." }
        |
        v
React ChatWindow.jsx
  → displays bot reply as a chat bubble
```

---

## How to Add More HR Questions

You have two options:

### Option A — Edit data.sql (before first run)

Add a new line to `backend/src/main/resources/data.sql`:
```sql
INSERT INTO faq (question, answer, keywords) VALUES
('What is the dress code?', 'Business casual Monday to Thursday. Fridays are casual.', 'dress,code,attire,clothing,casual,formal');
```

### Option B — Use the REST API (while app is running)

```bash
curl -X POST http://localhost:8080/api/faq \
  -H "Content-Type: application/json" \
  -d '{"question":"What is the dress code?","answer":"Business casual Monday to Thursday.","keywords":"dress,code,attire,clothing"}'
```

---

## Technologies Used

| Technology | Version | Purpose |
|---|---|---|
| Java | 17 | Backend language |
| Spring Boot | 3.2.5 | Backend framework |
| Spring Data JPA | (included) | Database ORM |
| H2 Database | 2.2.224 | In-memory database |
| React | 18 | Frontend framework |
| Vite | 5 | Frontend build tool |
| Maven | 3.x | Backend build tool |
| npm | 10.x | Frontend package manager |
