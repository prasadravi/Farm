# Natural Milk Backend

Spring Boot backend with Firebase Firestore integration for the Natural Milk e-commerce platform.

## Prerequisites

- Java 17 or later
- Maven 3.6+
- Firebase project (Firebase Project ID: fram-bc35f)

## Setup Instructions

### 1. Place Firebase Service Account Key

Copy your Firebase service account JSON file to:
```
backend/src/main/resources/firebase-service-account.json
```

### 2. Install Dependencies

```bash
mvn clean install
```

### 3. Run the Application

```bash
mvn spring-boot:run
```

The backend will start on `http://localhost:4000`

## API Endpoints

### Authentication
- **POST** `/api/auth/register` - User signup
- **POST** `/api/auth/login` - User login
- **GET** `/api/auth/me` - Get current user profile
- **PUT** `/api/auth/update-profile` - Update user profile

### Orders
- **POST** `/api/orders` - Create new order
- **GET** `/api/orders/{orderId}` - Get order details
- **GET** `/api/orders/myorders` - Get user's orders
- **PUT** `/api/orders/{orderId}/status` - Update order status

## Request/Response Examples

### Signup
```json
POST /api/auth/register
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "password123"
}

Response:
{
  "token": "jwt-token-here",
  "user": {
    "name": "John Doe",
    "email": "john@example.com",
    ...
  }
}
```

### Create Order
```json
POST /api/orders
Headers: Authorization: Bearer <jwt-token>
{
  "items": [
    {
      "id": "milk500",
      "title": "Fresh Cow Milk",
      "qty": 2,
      "price": 28
    }
  ],
  "total": 56
}
```

## Environment Configuration

Edit `src/main/resources/application.properties` to configure:
- Server port (default: 4000)
- JWT secret key
- Firebase database URL
- CORS settings

## Database Structure (Firestore)

### Users Collection
```
users/{email}
  - id: string
  - name: string
  - email: string
  - password: string (hashed)
  - phone: string
  - address: string
  - createdAt: long
  - updatedAt: long
```

### Orders Collection
```
orders/{orderId}
  - id: string
  - userId: string
  - items: array
  - total: double
  - status: string (pending, processing, delivered)
  - createdAt: long
  - updatedAt: long
```

## Troubleshooting

1. **Firebase connection fails**: Ensure service account JSON is in the correct location
2. **Port 4000 already in use**: Change `server.port` in application.properties
3. **CORS errors**: Verify CORS configuration matches your frontend URL

## Security Notes

- Change the JWT secret in production
- Use environment variables for sensitive data
- Enable Firestore security rules in production
- Use HTTPS in production

## Built With

- Spring Boot 3.2.0
- Firebase Admin SDK
- JWT (JSON Web Tokens)
- Firestore
