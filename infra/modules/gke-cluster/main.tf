# Simplified GKE Cluster - Minimal configuration for debugging

resource "google_container_cluster" "primary" {
  name     = "${var.app_name}-${var.environment}-gke"
  location = var.zone
  project  = var.project_id

  deletion_protection = false

  # Simple: use default node pool for simplicity
  initial_node_count = var.default_pool_node_count

  network    = var.network_self_link
  subnetwork = var.subnet_self_link

  # IP allocation for pods and services
  ip_allocation_policy {
    cluster_secondary_range_name  = var.pods_ip_range_name
    services_secondary_range_name = var.services_ip_range_name
  }

  # Basic node configuration
  node_config {
    preemptible  = var.use_preemptible
    machine_type = var.default_pool_machine_type

    service_account = var.gke_nodes_sa_email
    oauth_scopes = [
      "https://www.googleapis.com/auth/cloud-platform"
    ]

    labels = {
      environment = var.environment
    }
  }

  # Maintenance window
  maintenance_policy {
    daily_maintenance_window {
      start_time = "03:00"
    }
  }
}
