# HR Chatbot

A keyword-based HR chatbot built with Java Spring Boot (backend) and React (frontend). No LLM or API key required.

## Prerequisites

- Java 17+
- Maven 3.6+
- Node.js 18+

## Running the Backend

```bash
cd backend
mvn spring-boot:run
```

The backend starts at: http://localhost:8080

- H2 Console (to view database): http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:hrchatbot`
  - Username: `sa` | Password: (leave blank)

## Running the Frontend

```bash
cd frontend
npm install
npm run dev
```

The frontend starts at: http://localhost:5173

## API Endpoints

| Method | URL | Description |
|--------|-----|-------------|
| POST | /api/chat | Send a question, get an answer |
| GET | /api/chat/history | Get all past chat messages |
| GET | /api/faq | List all FAQ entries |
| POST | /api/faq | Add a new FAQ entry |
| DELETE | /api/faq/{id} | Delete a FAQ entry |

## Adding New FAQ Pairs

Use the POST /api/faq endpoint:

```json
{
  "question": "What is the dress code?",
  "answer": "Business casual is the standard dress code Monday to Thursday. Fridays are casual.",
  "keywords": "dress,code,attire,clothing,casual,formal"
}
```
