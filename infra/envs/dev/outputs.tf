output "vpc_name" {
  description = "The name of the VPC"
  value       = module.network.vpc_name
}

output "gke_cluster_name" {
  description = "The name of the GKE cluster"
  value       = module.gke_cluster.cluster_name
}

output "artifact_registry_url" {
  description = "The URL of the Artifact Registry"
  value       = module.artifact_registry.repository_url
}

output "gke_connect_command" {
  description = "Command to connect to the GKE cluster"
  value       = "gcloud container clusters get-credentials ${module.gke_cluster.cluster_name} --zone ${var.zone} --project ${var.project_id}"
}

# Jenkins outputs
output "jenkins_url" {
  description = "URL to access Jenkins on DigitalOcean"
  value       = module.jenkins.jenkins_url
}

output "jenkins_droplet_ip" {
  description = "Public IP of Jenkins droplet"
  value       = module.jenkins.droplet_ipv4
}

output "jenkins_ssh_command" {
  description = "SSH command to connect to Jenkins droplet"
  value       = module.jenkins.ssh_connection_string
}

output "jenkins_sa_email" {
  description = "GCP service account email for Jenkins"
  value       = module.iam.jenkins_sa_email
}
