from locust import HttpUser, task, between
import random

class PaymentServiceLoadTest(HttpUser):
    wait_time = between(1, 3)

    @task(10)
    def get_payments(self):
        """Get all payments - main performance test"""
        self.client.get("/payment-service/api/payments")

    @task(2)
    def get_payment_by_id(self):
        """Get a specific payment by ID"""
        payment_id = random.randint(1, 5)
        self.client.get(f"/payment-service/api/payments/{payment_id}")

    @task(1)
    def create_payment(self):
        """Create a payment - minimal write operations"""
        payment = {
            "isPayed": False,
            "paymentStatus": "NOT_STARTED",
            "order": {"orderId": 1}
        }
        self.client.post("/payment-service/api/payments", json=payment)

