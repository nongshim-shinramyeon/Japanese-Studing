# Japanese King

Simple Spring Boot web app for Japanese study.

This project includes:
- words
- grammar notes
- community

## Features

- save, edit, and delete words
- save, edit, and delete grammar notes
- create, edit, and delete community posts
- create, edit, and delete comments
- reply to comments
- pagination on list screens

## Tech

- Java 21
- Spring Boot 4
- Spring Web MVC
- Spring Data JPA
- Spring Validation
- H2 Database
- HTML / CSS / JavaScript
- Gradle

## Run

```powershell
cd C:\Users\User\Desktop\sbb
.\gradlew.bat bootRun
```

Pages:
- Home: [http://localhost:8080/](http://localhost:8080/)
- Words: [http://localhost:8080/words.html](http://localhost:8080/words.html)
- Grammar Notes: [http://localhost:8080/grammar-notes.html](http://localhost:8080/grammar-notes.html)
- Community: [http://localhost:8080/community.html](http://localhost:8080/community.html)
- H2 Console: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)

Stop the server with `Ctrl + C`.

## Test

```powershell
cd C:\Users\User\Desktop\sbb
.\gradlew.bat test
```

## API

### Words
- `POST /api/words`
- `GET /api/words`
- `GET /api/words/{id}`
- `PUT /api/words/{id}`
- `DELETE /api/words/{id}`

### Grammar Notes
- `POST /api/grammar-notes`
- `GET /api/grammar-notes`
- `GET /api/grammar-notes/{id}`
- `PUT /api/grammar-notes/{id}`
- `DELETE /api/grammar-notes/{id}`

### Community
- `POST /api/community/posts`
- `GET /api/community/posts`
- `GET /api/community/posts/{postId}`
- `PUT /api/community/posts/{postId}`
- `DELETE /api/community/posts/{postId}`
- `POST /api/community/posts/{postId}/comments`
- `GET /api/community/posts/{postId}/comments`
- `PUT /api/community/comments/{commentId}`
- `DELETE /api/community/comments/{commentId}`

## Database

H2 in-memory database is used for local testing.

- URL: `jdbc:h2:mem:sbb`
- Username: `sa`
- Password: blank
