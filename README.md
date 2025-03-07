# Customer API Service

A Spring Boot service for managing customer data with distributed tracing support using Jaeger.

## Running Locally with Docker Compose

1. Start the service and Jaeger:
```bash
docker compose up
```

2. Wait for the service to start. You can check the health endpoint at http://localhost:8080/actuator/health

## Available Endpoints

The service is secured with Spring Security. All API endpoints require authentication.

The service exposes several REST endpoints for customer management:

- Create a customer: `POST /api/customers`
- Get a customer: `GET /api/customers/{id}`
- Update a customer: `PUT /api/customers/{id}`
- Delete a customer: `DELETE /api/customers/{id}`
- Search customers: `GET /api/customers` (with optional query params: firstName, lastName, email)

## Example Usage and Trace Viewing

1. Create a new customer:
```bash
curl -X POST http://localhost:8080/api/customers \
  -H "Content-Type: application/json" \
  -H "Authorization: Basic dXNlcjpwYXNzd29yZA==" \
  -d '{"firstName": "John", "lastName": "Doe", "email": "john.doe@example.com"}'
```

Note: The default credentials are:
- Username: user
- Password: password

The Basic Auth header above is the base64 encoded version of "user:password".

2. View the trace in Jaeger UI:
   - Open http://localhost:16686 in your browser
   - Select "customer-api" from the Service dropdown
   - Click "Find Traces"
   - You should see the trace for your API call showing the complete request flow

The Jaeger UI allows you to:
- View detailed timing information for each operation
- See the relationships between different spans in the trace
- Analyze tags and logs associated with each span
- Compare different traces to identify performance patterns

## Ports

- 8080: Customer API service
- 16686: Jaeger UI
- 4317: OTLP gRPC (used for trace ingestion)
- 4318: OTLP HTTP
- 9411: Zipkin compatibility endpoint
