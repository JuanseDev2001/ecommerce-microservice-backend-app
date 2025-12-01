output "gke_sa_email" {
  description = "Email of the GKE service account"
  value       = google_service_account.gke.email
}

output "jenkins_sa_email" {
  description = "Email of the Jenkins service account"
  value       = google_service_account.jenkins.email
}

output "jenkins_sa_key" {
  description = "Private key for Jenkins service account (base64 encoded)"
  value       = google_service_account_key.jenkins.private_key
  sensitive   = true
}

output "jenkins_sa_key_decoded" {
  description = "Private key for Jenkins service account (JSON)"
  value       = base64decode(google_service_account_key.jenkins.private_key)
  sensitive   = true
}
