# NeuraSearch 🔍🧠

NeuraSearch is a Java-based file parser and semantic search engine that:
- Uses **Apache Tika** to extract content from all files in a directory.
- Stores the extracted data in a **Vector Database** for deep semantic search.
- Secures endpoints using **JWT-based authentication**.

## 💻 Tech Stack
- Java 17+
- Spring Boot
- Apache Tika
- MySQL
- JWT
- Vector DB (e.g., Qdrant / Pinecone / Faiss)
- Elasticsearch (optional)

## 🔐 JWT Auth
Includes a complete security layer with JWT token generation, validation, and secure role-based endpoints.

## ⚙️ How to Run

1. Clone the repo
2. Copy `application-template.properties` to `application.properties`
3. Fill in DB and JWT credentials
4. Run the app using IntelliJ or `mvn spring-boot:run`

## 🚀 Future Plans
- Integrate file watcher for real-time parsing
- UI for user-friendly semantic search
- Multi-user auth with roles

---

_💡 Built by Shivansh Shukla_
