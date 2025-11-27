# Simplified Development Environment Configuration

module "network" {
  source = "../../modules/network"

  project_id    = var.project_id
  region        = var.region
  environment   = "dev"
  app_name      = var.app_name
  subnet_cidr   = "10.10.0.0/24"
  pods_cidr     = "10.11.0.0/16"
  services_cidr = "10.12.0.0/16"
}

module "iam" {
  source = "../../modules/iam"

  project_id  = var.project_id
  environment = "dev"
  app_name    = var.app_name
}

module "artifact_registry" {
  source = "../../modules/artifact-registry"

  project_id              = var.project_id
  region                  = var.region
  environment             = "dev"
  app_name                = var.app_name
  writer_service_accounts = [module.iam.gke_sa_email, module.iam.jenkins_sa_email]
  reader_service_accounts = [module.iam.gke_sa_email]

  depends_on = [module.iam]
}

module "gke_cluster" {
  source = "../../modules/gke-cluster"

  project_id             = var.project_id
  region                 = var.region
  zone                   = var.zone
  environment            = "dev"
  app_name               = var.app_name
  network_self_link      = module.network.vpc_self_link
  subnet_self_link       = module.network.subnet_self_link
  pods_ip_range_name     = module.network.pods_ip_range_name
  services_ip_range_name = module.network.services_ip_range_name
  gke_nodes_sa_email     = module.iam.gke_sa_email

  # Dev settings: small and cheap
  default_pool_node_count   = 2
  default_pool_machine_type = "e2-medium"
  use_preemptible           = true

  depends_on = [module.network, module.iam]
}

# Jenkins on DigitalOcean (Simplified)
module "jenkins" {
  source = "../../modules/digitalocean-jenkins"

  app_name               = var.app_name
  environment            = "dev"
  region                 = var.do_region
  droplet_size           = var.jenkins_droplet_size
  ssh_public_key         = var.ssh_public_key
  jenkins_admin_user     = var.jenkins_admin_user
  jenkins_admin_password = var.jenkins_admin_password
  gcp_project_id         = var.project_id
  gcp_region             = var.region
  gcp_service_account_key = module.iam.jenkins_sa_key_decoded

  depends_on = [module.iam, module.artifact_registry]
}
