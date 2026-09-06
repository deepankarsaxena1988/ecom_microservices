# ecomCheckOutService

Simple checkout microservice for the ecom project (Spring Boot).

Quick start (maven):

1. cd ecomCheckOutService
2. mvn clean package
3. mvn spring-boot:run

Razorpay (integration)
- Configure razorpay.keyId and razorpay.keySecret via environment or config server (do NOT commit keys).
- POST /checkout with { cart: [...], amount: 123.45 } will create a Razorpay order when amount is provided (amount in rupees).
- Webhook: POST /webhook/razorpay will receive webhook events; set webhook secret to verify signatures.

Endpoints:
- POST /checkout  => create an order or create Razorpay order if amount provided
- POST /webhook/razorpay => webhook receiver for Razorpay
- GET  /health    => simple health check
