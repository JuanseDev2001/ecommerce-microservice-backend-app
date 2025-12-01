variable "project_id" {
  description = "The GCP project ID"
  type        = string
}

variable "region" {
  description = "The GCP region"
  type        = string
}

variable "zone" {
  description = "The GCP zone"
  type        = string
}

variable "environment" {
  description = "Environment name"
  type        = string
}

variable "app_name" {
  description = "Application name"
  type        = string
}

variable "network_self_link" {
  description = "Self link of the VPC network"
  type        = string
}

variable "subnet_self_link" {
  description = "Self link of the subnet"
  type        = string
}

variable "pods_ip_range_name" {
  description = "Name of the secondary IP range for pods"
  type        = string
}

variable "services_ip_range_name" {
  description = "Name of the secondary IP range for services"
  type        = string
}

variable "gke_nodes_sa_email" {
  description = "Email of the service account for GKE nodes"
  type        = string
}

variable "default_pool_node_count" {
  description = "Number of nodes in the default pool"
  type        = number
  default     = 2
}

variable "default_pool_machine_type" {
  description = "Machine type for nodes"
  type        = string
  default     = "e2-medium"
}

variable "use_preemptible" {
  description = "Use preemptible VMs for cost savings"
  type        = bool
  default     = true
}
