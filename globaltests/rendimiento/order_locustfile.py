from locust import HttpUser, task, between
from datetime import datetime
import random

class OrderServiceLoadTest(HttpUser):
    wait_time = between(1, 3)

    @task(10)
    def get_orders(self):
        """Get all orders - main performance test"""
        self.client.get("/order-service/api/orders")

    @task(2)
    def get_order_by_id(self):
        """Get a specific order by ID"""
        order_id = random.randint(1, 5)
        self.client.get(f"/order-service/api/orders/{order_id}")

    @task(1)
    def create_order(self):
        """Create a new order - minimal write operations"""
        order_date = datetime.now().strftime("%d-%m-%Y__%H:%M:%S:%f")
        order = {
            "orderDate": order_date,
            "orderDesc": "Locust Test Order",
            "orderFee": round(random.uniform(10.0, 500.0), 2),
            "cart": {"cartId": 1}
        }
        self.client.post("/order-service/api/orders", json=order)
