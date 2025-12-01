# GCS backend configuration for stage environment
# Remote state for collaboration and safety

terraform {
  backend "gcs" {
    bucket = "ecommerce-microservices-479322-terraform-state"
    prefix = "stage/terraform.tfstate"
  }
}
