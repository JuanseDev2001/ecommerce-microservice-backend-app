variable "project_id" {
  description = "The GCP project ID"
  type        = string
}

variable "region" {
  description = "The GCP region"
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

variable "create_jenkins_repo" {
  description = "Whether to create a separate repository for Jenkins images"
  type        = bool
  default     = false
}

variable "reader_service_accounts" {
  description = "List of service account emails that should have read access"
  type        = list(string)
  default     = []
}

variable "writer_service_accounts" {
  description = "List of service account emails that should have write access"
  type        = list(string)
  default     = []
}
