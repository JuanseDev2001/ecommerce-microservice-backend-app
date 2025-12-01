# Local backend configuration - Use this for initial setup and testing
# Comment out this file when ready to use GCS backend

terraform {
  backend "local" {
    path = "./terraform.tfstate"
  }
}
