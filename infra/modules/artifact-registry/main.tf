# Artifact Registry module - Docker repositories

resource "google_artifact_registry_repository" "microservices" {
  location      = var.region
  repository_id = "${var.app_name}-${var.environment}"
  description   = "Docker repository for ${var.app_name} microservices in ${var.environment}"
  format        = "DOCKER"
  project       = var.project_id

  labels = {
    environment = var.environment
    app         = var.app_name
    managed-by  = "terraform"
  }
}

# Optional: Create separate repositories for different microservices if needed
resource "google_artifact_registry_repository" "jenkins" {
  count         = var.create_jenkins_repo ? 1 : 0
  location      = var.region
  repository_id = "${var.app_name}-jenkins-${var.environment}"
  description   = "Docker repository for Jenkins custom images in ${var.environment}"
  format        = "DOCKER"
  project       = var.project_id

  labels = {
    environment = var.environment
    app         = var.app_name
    component   = "jenkins"
    managed-by  = "terraform"
  }
}

# IAM bindings for service accounts
# Using count instead of for_each to avoid dependency issues
resource "google_artifact_registry_repository_iam_member" "readers" {
  count = length(var.reader_service_accounts)

  project    = var.project_id
  location   = google_artifact_registry_repository.microservices.location
  repository = google_artifact_registry_repository.microservices.name
  role       = "roles/artifactregistry.reader"
  member     = "serviceAccount:${var.reader_service_accounts[count.index]}"
}

resource "google_artifact_registry_repository_iam_member" "writers" {
  count = length(var.writer_service_accounts)

  project    = var.project_id
  location   = google_artifact_registry_repository.microservices.location
  repository = google_artifact_registry_repository.microservices.name
  role       = "roles/artifactregistry.writer"
  member     = "serviceAccount:${var.writer_service_accounts[count.index]}"
}
