from locust import HttpUser, task, between
import random

class ProductServiceLoadTest(HttpUser):
    wait_time = between(1, 3)

    @task(10)
    def get_products(self):
        """Get all products - main performance test"""
        self.client.get("/product-service/api/products")

    @task(2)
    def get_product_by_id(self):
        """Get a specific product by ID"""
        product_id = random.randint(1, 5)
        self.client.get(f"/product-service/api/products/{product_id}")

    @task(1)
    def create_product(self):
        """Create a product - minimal write operations"""
        sku = f"LOCUST-SKU-{random.randint(10000, 99999)}"
        product = {
            "productTitle": f"Locust Test Product {random.randint(1, 1000)}",
            "imageUrl": "http://example.com/image.jpg",
            "sku": sku,
            "priceUnit": round(random.uniform(10.0, 500.0), 2),
            "quantity": random.randint(1, 100),
            "category": {"categoryId": 1}
        }
        self.client.post("/product-service/api/products", json=product)
