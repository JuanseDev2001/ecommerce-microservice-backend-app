from locust import HttpUser, task, between
import random

class UserServiceLoadTest(HttpUser):
    wait_time = between(1, 3)

    @task(1)
    def get_users(self):
        """Get all users - main performance test"""
        self.client.get("/user-service/api/users")

    @task(2)
    def get_user_by_id(self):
        """Get a specific user by ID - test with commonly used IDs"""
        user_id = random.randint(1, 5)
        self.client.get(f"/user-service/api/users/{user_id}")

    @task(1)
    def create_user(self):
        """Create a new user - minimal write operations"""
        user_id = random.randint(10000, 99999)
        user = {
            "userId": user_id,
            "firstName": "Locust",
            "lastName": "User",
            "email": f"locustuser{user_id}@email.com",
            "credential": {
                "username": f"locustuser{user_id}",
                "password": f"password{user_id}"
            }
        }
        self.client.post("/user-service/api/users", json=user)

