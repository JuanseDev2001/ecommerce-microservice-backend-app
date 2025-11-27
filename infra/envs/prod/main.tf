# Simplified Production Environment Configuration

module "network" {
  source = "../../modules/network"

  project_id    = var.project_id
  region        = var.region
  environment   = "prod"
  app_name      = var.app_name
  subnet_cidr   = "10.30.0.0/24"
  pods_cidr     = "10.31.0.0/16"
  services_cidr = "10.32.0.0/16"
}

module "iam" {
  source = "../../modules/iam"

  project_id  = var.project_id
  environment = "prod"
  app_name    = var.app_name
}

module "artifact_registry" {
  source = "../../modules/artifact-registry"

  project_id              = var.project_id
  region                  = var.region
  environment             = "prod"
  app_name                = var.app_name
  writer_service_accounts = [module.iam.gke_sa_email]
  reader_service_accounts = [module.iam.gke_sa_email]

  depends_on = [module.iam]
}

module "gke_cluster" {
  source = "../../modules/gke-cluster"

  project_id             = var.project_id
  region                 = var.region
  zone                   = var.zone
  environment            = "prod"
  app_name               = var.app_name
  network_self_link      = module.network.vpc_self_link
  subnet_self_link       = module.network.subnet_self_link
  pods_ip_range_name     = module.network.pods_ip_range_name
  services_ip_range_name = module.network.services_ip_range_name
  gke_nodes_sa_email     = module.iam.gke_sa_email

  # Prod settings: larger and no preemptible
  default_pool_node_count   = 3
  default_pool_machine_type = "e2-standard-4"
  use_preemptible           = false

  depends_on = [module.network, module.iam]
}
