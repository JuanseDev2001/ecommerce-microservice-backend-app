variable "app_name" {
  description = "Application name"
  type        = string
  default     = "ecommerce"
}

variable "environment" {
  description = "Environment name (dev, stage, prod)"
  type        = string
}

variable "region" {
  description = "DigitalOcean region"
  type        = string
  default     = "nyc1"
}

variable "droplet_size" {
  description = "Droplet size/plan"
  type        = string
  default     = "s-2vcpu-4gb" # $24/month
}

variable "ssh_public_key" {
  description = "SSH public key for droplet access"
  type        = string
}

variable "jenkins_admin_user" {
  description = "Jenkins admin username"
  type        = string
  default     = "admin"
}

variable "jenkins_admin_password" {
  description = "Jenkins admin password"
  type        = string
  sensitive   = true
}

variable "gcp_project_id" {
  description = "GCP Project ID for Jenkins to deploy to"
  type        = string
}

variable "gcp_region" {
  description = "GCP region for deployments"
  type        = string
}

variable "gcp_service_account_key" {
  description = "GCP Service Account JSON key for Jenkins authentication"
  type        = string
  sensitive   = true
}
