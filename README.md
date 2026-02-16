# Future Payments REST API

A Spring Boot application for managing future payment groups with Kafka event-driven processing and ECS-compatible logging.

## Prerequisites

- **Java 21** or higher
- **Maven 3.8+**
- **Docker Desktop** (for PostgreSQL, Kafka, and Elasticsearch)

## Database Setup

This application requires a PostgreSQL database. The easiest way to set it up is using Docker Compose.

### 1. Start the Database and Infrastructure

Run the following command in the project root directory:

```powershell
docker-compose up -d
```

This will start:
- **PostgreSQL** on `localhost:5432`
- **Kafka** on `localhost:9092`
- **Elasticsearch** on `localhost:9200`

### 2. Verify the Database is Running

Check that PostgreSQL is running:

```powershell
docker ps
```

You should see a container named `postgres` running.

### 3. Database Connection Details

The application connects to PostgreSQL with these credentials (configured in `application.properties`):

- **URL**: `jdbc:postgresql://localhost:5432/payments_db`
- **Username**: `user`
- **Password**: `password`

**Note**: Liquibase will automatically create the required tables (`payment_groups` and `payments`) on application startup.

## Running the Application

### Start the Application

```powershell
mvn spring-boot:run
```

The application will:
1. Connect to PostgreSQL
2. Run Liquibase migrations to create tables
3. Start the REST API on `http://localhost:8080`
4. Connect to Kafka for event processing

### Access Swagger UI

Once the application is running, access the API documentation at:

```
http://localhost:8080/swagger-ui.html
```

## API Endpoints

- `POST /api/payments` - Import payment groups
- `GET /api/payments` - Get all payment groups
- `GET /api/payments/{id}` - Get payment group by ID
- `DELETE /api/payments/{id}` - Delete payment group by ID

## Testing the API

### Example: Import Payment Groups

```bash
curl -X POST http://localhost:8080/api/payments \
  -H "Content-Type: application/json" \
  -d '[
    {
      "debtorAccount": {"iban": "EE021010220208830224"},
      "debtorName": "John Doe",
      "requestedExecutionDate": "2024-01-15",
      "payments": [
        {
          "endToEndIdentification": "payment-001",
          "instructedAmount": {"currency": "EUR", "amount": 100.00},
          "creditorName": "Jane Smith",
          "creditorAccount": {"iban": "EE701700017001577198"},
          "remittanceInformation": "Invoice payment"
        }
      ],
      "externalId": "unique-external-id-123",
      "uname": "user123",
      "tcif": "tcif456",
      "country": "EE",
      "sourceSystem": "mobile-app"
    }
  ]'
```

## Kafka Topics

The application uses the following Kafka topics:
- `payment.created` - Emitted when a payment group is created
- `payment.modified` - Emitted when a payment group is modified
- `payment.deleted` - Emitted when a payment group is deleted

## Running Tests

**Note**: Tests require Docker Desktop to be running because they use Testcontainers.

```powershell
mvn test
```

## Stopping the Infrastructure

To stop all Docker containers:

```powershell
docker-compose down
```

To stop and remove all data:

```powershell
docker-compose down -v
```

## Troubleshooting

### Error: "Connection to localhost:5432 refused"

**Solution**: The PostgreSQL database is not running. Start it with:
```powershell
docker-compose up -d
```

### Error: "Could not find a valid Docker environment"

**Solution**: Docker Desktop is not running or not properly configured. Ensure Docker Desktop is started and accessible.

### Port Already in Use

If you see errors about ports already in use (5432, 9092, 9200), either:
1. Stop the conflicting service, or
2. Modify the port mappings in `docker-compose.yml`

## Project Structure

```
src/
├── main/
│   ├── java/com/example/demo/
│   │   ├── config/          # Kafka topic configuration
│   │   ├── controller/      # REST controllers
│   │   ├── dto/             # Data Transfer Objects
│   │   ├── model/           # JPA entities
│   │   ├── repository/      # Spring Data repositories
│   │   └── service/         # Business logic and Kafka services
│   └── resources/
│       ├── db/changelog/    # Liquibase YAML migrations
│       ├── application.properties
│       └── logback-spring.xml  # ECS-compatible logging config
└── test/                    # Unit and integration tests
```

## Technologies Used

- Spring Boot 3.4.2
- Spring Data JPA
- Spring Kafka
- PostgreSQL 16
- Liquibase (YAML format)
- Kafka (Confluent Platform 7.5.0)
- Elasticsearch 8.12.0
- OpenTelemetry (for distributed tracing)
- Testcontainers (for integration testing)