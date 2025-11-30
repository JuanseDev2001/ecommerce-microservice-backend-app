# Simplified IAM module - Service Accounts for GKE and Jenkins

# Service Account for GKE nodes and workloads
resource "google_service_account" "gke" {
  account_id   = "${var.app_name}-gke-${var.environment}"
  display_name = "GKE Service Account for ${var.environment}"
  description  = "Service account for GKE nodes and workloads"
  project      = var.project_id
}

# Basic permissions for GKE
resource "google_project_iam_member" "gke_roles" {
  for_each = toset([
    "roles/logging.logWriter",
    "roles/monitoring.metricWriter",
    "roles/artifactregistry.reader",
    "roles/artifactregistry.writer",
  ])

  project = var.project_id
  role    = each.value
  member  = "serviceAccount:${google_service_account.gke.email}"
}

# Service Account for Jenkins (running on DigitalOcean)
resource "google_service_account" "jenkins" {
  account_id   = "${var.app_name}-jenkins-${var.environment}"
  display_name = "Jenkins Service Account for ${var.environment}"
  description  = "Service account for Jenkins CI/CD running on DigitalOcean"
  project      = var.project_id
}

# Jenkins permissions for GCP resources
resource "google_project_iam_member" "jenkins_roles" {
  for_each = toset([
    "roles/container.admin",           # Manage GKE clusters
    "roles/artifactregistry.writer",   # Push Docker images
    "roles/storage.admin",             # Access Cloud Storage
    "roles/logging.logWriter",         # Write logs
    "roles/iam.serviceAccountUser",    # Use service accounts
  ])

  project = var.project_id
  role    = each.value
  member  = "serviceAccount:${google_service_account.jenkins.email}"
}

# Create service account key for Jenkins
resource "google_service_account_key" "jenkins" {
  service_account_id = google_service_account.jenkins.name
  public_key_type    = "TYPE_X509_PEM_FILE"
}
