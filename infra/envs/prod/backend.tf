# GCS backend configuration for production environment
# Remote state for collaboration and safety

terraform {
  backend "gcs" {
    bucket = "ecommerce-microservices-479322-terraform-state"
    prefix = "prod/terraform.tfstate"
  }
}
