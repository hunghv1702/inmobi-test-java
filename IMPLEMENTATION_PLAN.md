# Guess Number Game API - Implementation Plan

## 1. Muc tieu ky thuat

Xay dung REST API game doan so bang Java + Spring Boot voi cac diem chinh:

- Co authentication bang JWT.
- Password duoc hash bang BCrypt.
- API ro rang, dung REST style.
- Logic game nam trong service, controller mong.
- Co transaction cho cac thao tac thay doi du lieu.
- Chong race condition khi user goi `/guess` nhieu lan cung luc.
- `/leaderboard` va `/me` duoc toi uu query.
- Co README day du.
- Co test cho business logic va API quan trong.

## 2. Tech stack de xuat

```text
Java 17
Spring Boot 3.x
Spring Web
Spring Security
Spring Data JPA
Spring Validation
H2 database cho local/dev
MySQL profile optional neu muon chay that
JWT: jjwt
Maven
JUnit 5
MockMvc
```

Ly do dung H2 mac dinh: nguoi review co the clone project va chay ngay ma khong can setup database. Neu muon chuyen nghiep hon, co the them profile MySQL trong README.

## 3. API design

Prefix chung:

```text
/api
```

Public APIs:

```text
POST /api/auth/register
POST /api/auth/login
```

Protected APIs:

```text
GET  /api/me
POST /api/guess
POST /api/buy-turns
GET  /api/leaderboard
```

### Register

```http
POST /api/auth/register
```

Request:

```json
{
  "email": "user@example.com",
  "password": "123456"
}
```

Response:

```json
{
  "id": 1,
  "email": "user@example.com",
  "score": 0,
  "turns": 0
}
```

### Login

```http
POST /api/auth/login
```

Request:

```json
{
  "email": "user@example.com",
  "password": "123456"
}
```

Response:

```json
{
  "accessToken": "jwt",
  "tokenType": "Bearer"
}
```

### Guess

```http
POST /api/guess
```

Request:

```json
{
  "number": 3
}
```

Response:

```json
{
  "guess": 3,
  "serverNumber": 4,
  "correct": false,
  "score": 2,
  "turns": 7
}
```

Rules:

- `number` phai tu `1` den `5`.
- Moi lan goi hop le tru `1` turn.
- Server random so tu `1` den `5`.
- Neu doan dung thi `score + 1`.
- Tra ve so cua server theo yeu cau nang cao.

### Buy turns

```http
POST /api/buy-turns
```

Response:

```json
{
  "email": "user@example.com",
  "turns": 5
}
```

Bai test cho phep cong truc tiep, nen khong can tich hop VNPAY, MOMO hay PayPal that. Trong README se ghi ro day la diem co the mo rong thanh payment flow that.

### Leaderboard

```http
GET /api/leaderboard
```

Response:

```json
[
  {
    "rank": 1,
    "email": "a@example.com",
    "score": 10
  }
]
```

### Me

```http
GET /api/me
```

Response:

```json
{
  "email": "user@example.com",
  "score": 3,
  "turns": 4
}
```

## 4. Database design

Dung bang `users`, nhung doi `username` thanh `email` de dung yeu cau `/me`.

```sql
CREATE TABLE users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  email VARCHAR(255) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  score INT NOT NULL DEFAULT 0,
  turns INT NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP
);

CREATE INDEX idx_users_score_id ON users(score DESC, id ASC);
```

Ly do:

- `/me` query theo `id`, dung primary key nen rat nhanh.
- `/leaderboard` order by `score desc`, limit `10`, dung index de tranh scan/sort toan bang khi data lon.

## 5. Package structure

Cau truc source theo feature, khong gom het theo technical layer:

```text
src/main/java/com/example/guessgame
  GuessGameApplication.java

  auth
    AuthController.java
    AuthService.java
    JwtService.java
    JwtAuthenticationFilter.java
    SecurityConfig.java
    dto
      LoginRequest.java
      RegisterRequest.java
      AuthResponse.java

  user
    User.java
    UserRepository.java
    UserPrincipal.java
    dto
      MeResponse.java

  game
    GameController.java
    GameService.java
    RandomNumberGenerator.java
    dto
      GuessRequest.java
      GuessResponse.java
      BuyTurnsResponse.java
      LeaderboardResponse.java

  common
    ApiError.java
    GlobalExceptionHandler.java
    CurrentUser.java
    exception
      BadRequestException.java
      ConflictException.java
      UnauthorizedException.java
      ResourceNotFoundException.java
```

Diem clean:

- Controller chi nhan request, goi service, tra response.
- Service chua business logic.
- DTO rieng, khong expose entity ra API.
- Exception thong nhat.
- Random tach class rieng de test de hon.

## 6. Security plan

Dung JWT stateless.

Luong xu ly:

1. `register`: luu user voi password BCrypt.
2. `login`: verify password, generate JWT.
3. Request protected gui header:

```http
Authorization: Bearer <token>
```

4. `JwtAuthenticationFilter` parse token, lay user id/email, set vao `SecurityContext`.

Security config:

- Disable CSRF vi REST stateless.
- Session policy: `STATELESS`.
- Public endpoints: `/api/auth/register`, `/api/auth/login`.
- Cac endpoint con lai yeu cau authenticated.

JWT claims nen co:

```json
{
  "sub": "userId",
  "email": "user@example.com",
  "iat": "...",
  "exp": "..."
}
```

## 7. Concurrency plan cho `/guess`

Day la phan quan trong vi de bai hoi truc tiep.

Van de:

Neu user con `1` turn nhung gui dong thoi 10 request `/guess`, neu code chi read user roi update binh thuong, nhieu request co the cung thay `turns = 1`, dan den tru sai hoac score sai.

Giai phap chinh:

Dung transaction + pessimistic write lock tren row user.

Repository:

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("select u from User u where u.id = :id")
Optional<User> findByIdForUpdate(Long id);
```

Service:

```java
@Transactional
public GuessResponse guess(Long userId, int number) {
    User user = userRepository.findByIdForUpdate(userId)
        .orElseThrow(...);

    if (user.getTurns() <= 0) {
        throw new BadRequestException("Not enough turns");
    }

    user.decreaseTurn();

    int serverNumber = randomNumberGenerator.nextNumber();
    boolean correct = number == serverNumber;

    if (correct) {
        user.increaseScore();
    }

    return GuessResponse.from(...);
}
```

Ly do chon pessimistic lock:

- De hieu, de chung minh trong phong van.
- Dam bao moi user chi co mot request guess duoc update tai mot thoi diem.
- Khong anh huong toan he thong, chi lock row cua user hien tai.

## 8. Performance plan

### `/me`

- Lay user id tu JWT.
- Query theo primary key.
- Tra DTO.
- Khong load thong tin du thua ra response.

### `/leaderboard`

- Query top 10.
- Dung projection DTO thay vi tra entity.
- Co index `score DESC, id ASC`.
- Sort phu theo `id ASC` de ket qua on dinh khi bang diem.

Repository style:

```java
List<LeaderboardProjection> findTop10ByOrderByScoreDescIdAsc();
```

Co the giai thich them khi phong van:

- Voi traffic rat lon, leaderboard co the cache bang Redis ngan han.
- Voi bai test nay, DB index + limit 10 la du hop ly.

## 9. Validation va error handling

Dung Bean Validation:

```java
@NotNull
@Min(1)
@Max(5)
private Integer number;
```

Error response thong nhat:

```json
{
  "status": 400,
  "message": "number must be between 1 and 5",
  "path": "/api/guess",
  "timestamp": "2026-08-27T10:00:00"
}
```

Cac loi chinh:

```text
400: invalid guess number, not enough turns
401: missing/invalid token
409: email already exists
500: unexpected error
```

## 10. Test plan

Nen co test du de the hien tu duy tot, khong can qua nhieu.

### Unit test

`GameServiceTest`:

- Guess dung thi score tang.
- Guess sai thi score khong tang.
- Guess luon tru 1 turn.
- Het turn thi throw exception.
- Number ngoai `1-5` bi reject o validation/API layer.

### Integration/API test

`AuthControllerTest`:

- Register thanh cong.
- Register trung email tra `409`.
- Login thanh cong tra token.
- Login sai password tra `401`.

`GameControllerTest`:

- Goi `/me` khong token tra `401`.
- Login xong goi `/buy-turns` thanh cong.
- Goi `/guess` thanh cong voi token.
- Goi `/leaderboard` tra top 10.

### Concurrency test

Quan trong nhat:

- Tao user co `1` turn.
- Ban 5 hoac 10 request `/guess` song song.
- Ky vong:
  - Chi 1 request thanh cong.
  - Cac request con lai bao het luot.
  - `turns` cuoi cung bang `0`, khong am.
  - `score` khong bi cong sai.

## 11. README plan

README nen co cac phan:

```text
# Guess Number Game API

## Requirements
- Java 17+
- Maven

## Tech stack
- Spring Boot
- Spring Security JWT
- Spring Data JPA
- H2/MySQL
- JUnit

## Run locally
mvn clean test
mvn spring-boot:run

## H2 Console
URL, username, password

## Authentication flow
1. Register
2. Login
3. Use Bearer token

## API examples
curl register
curl login
curl buy-turns
curl guess
curl me
curl leaderboard

## Design notes
- JWT security
- BCrypt password
- Pessimistic lock for concurrent guessing
- Leaderboard index
```

Phan `Design notes` rat quan trong vi nguoi review se thay minh khong chi code chay duoc ma con hieu ly do thiet ke.

## 12. Thu tu implement

Trien khai theo thu tu sau de code sach va de kiem soat:

1. Tao Spring Boot Maven project.
2. Them dependencies can thiet.
3. Cau hinh `application.yml` voi H2.
4. Tao entity `User`.
5. Tao repository va index.
6. Tao exception + global error handler.
7. Lam auth register/login.
8. Lam JWT service/filter/security config.
9. Lam `/me`.
10. Lam `/buy-turns`.
11. Lam `/guess` voi transaction + lock.
12. Lam `/leaderboard`.
13. Viet test auth/game/concurrency.
14. Viet README.
15. Chay `mvn test`.
16. Chay app local va kiem tra nhanh bang curl.

## 13. Tieu chi source code clean

Khi implement can giu cac nguyen tac sau:

- Khong de business logic trong controller.
- Khong tra entity truc tiep ra API.
- Khong dung magic number rai rac. Vi du: `MIN_GUESS = 1`, `MAX_GUESS = 5`, `BUY_TURNS_AMOUNT = 5`.
- Method nho, ten ro nghia.
- Entity co behavior co ban nhu `increaseScore()`, `decreaseTurn()`, `addTurns(int amount)`.
- Exception ro domain.
- Security config tach rieng.
- DTO dung `record` neu phu hop de code gon.
- README co du cach chay va test API.
- Test tap trung vao cac diem bai test danh gia: auth, business logic, race condition, leaderboard.

## 14. Diem nhan de noi khi phong van

Co the giai thich ngan gon:

- Dung JWT de API stateless, phu hop REST API.
- Password duoc hash bang BCrypt, khong luu plain text.
- Voi `/guess`, dung transaction va pessimistic write lock tren row user de tranh tru turn sai khi request song song.
- Voi `/leaderboard`, dung index theo score va chi lay top 10 bang projection de query nhe.
- Controller chi dieu phoi request/response, business logic nam trong service.
- README co flow test nhanh bang curl de reviewer chay duoc ngay.
