# Simplified Production Environment Outputs

output "vpc_name" {
  description = "VPC network name"
  value       = module.network.vpc_name
}

output "subnet_name" {
  description = "Subnet name"
  value       = module.network.subnet_name
}

output "gke_cluster_name" {
  description = "GKE cluster name"
  value       = module.gke_cluster.cluster_name
}

output "gke_cluster_endpoint" {
  description = "GKE cluster endpoint"
  value       = module.gke_cluster.cluster_endpoint
  sensitive   = true
}

output "gke_connect_command" {
  description = "Command to connect to the cluster"
  value       = "gcloud container clusters get-credentials ${module.gke_cluster.cluster_name} --zone=${var.zone} --project=${var.project_id}"
}

output "artifact_registry_url" {
  description = "Artifact Registry repository URL"
  value       = module.artifact_registry.repository_url
}

output "gke_sa_email" {
  description = "GKE service account email"
  value       = module.iam.gke_sa_email
}
