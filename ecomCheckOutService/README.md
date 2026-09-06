# ecomCheckOutService

Simple checkout microservice for the ecom project.

Quick start:

1. cd ecomCheckOutService
2. npm install
3. npm start

Endpoints:
- POST /checkout  => create an order (expects JSON { cart: [...], user: {...} })
- GET  /health    => simple health check
