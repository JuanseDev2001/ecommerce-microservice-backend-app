from locust import HttpUser, task, between
from datetime import datetime
import random

class FavouriteServiceLoadTest(HttpUser):
    wait_time = between(1, 3)

    @task(1)
    def get_favourites(self):
        """Get all favourites - main performance test"""
        self.client.get("/favourite-service/api/favourites")

    @task(1)
    def add_favourite(self):
        """Add a favourite - minimal write operations"""
        like_date = datetime.now().strftime("%d-%m-%Y__%H:%M:%S:%f")
        
        fav = {
            "userId": random.randint(1, 5),
            "productId": random.randint(1, 5),
            "likeDate": like_date
        }
        self.client.post("/favourite-service/api/favourites", json=fav)

