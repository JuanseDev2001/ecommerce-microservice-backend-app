# Simplified DigitalOcean Jenkins Droplet Module
# Creates a basic droplet with Docker and Jenkins

terraform {
  required_providers {
    digitalocean = {
      source  = "digitalocean/digitalocean"
      version = "~> 2.0"
    }
  }
}

# SSH Key for accessing the droplet
resource "digitalocean_ssh_key" "jenkins" {
  name       = "${var.app_name}-${var.environment}-jenkins-key"
  public_key = var.ssh_public_key
}

# Jenkins Droplet
resource "digitalocean_droplet" "jenkins" {
  name   = "${var.app_name}-${var.environment}-jenkins"
  region = var.region
  size   = var.droplet_size
  image  = "docker-20-04" # Ubuntu 20.04 with Docker pre-installed

  ssh_keys = [digitalocean_ssh_key.jenkins.fingerprint]
  tags     = ["${var.environment}", "jenkins", "ci-cd", var.app_name]

  # Cloud-init script to setup Jenkins
  user_data = templatefile("${path.module}/scripts/install-jenkins.sh", {
    jenkins_admin_user     = var.jenkins_admin_user
    jenkins_admin_password = var.jenkins_admin_password
    gcp_project_id         = var.gcp_project_id
    gcp_region             = var.gcp_region
    environment            = var.environment
  })

  monitoring = true
}

# Firewall rules for Jenkins
resource "digitalocean_firewall" "jenkins" {
  name = "${var.app_name}-${var.environment}-jenkins-firewall"

  droplet_ids = [digitalocean_droplet.jenkins.id]

  # SSH access
  inbound_rule {
    protocol         = "tcp"
    port_range       = "22"
    source_addresses = ["0.0.0.0/0", "::/0"]
  }

  # Jenkins web UI
  inbound_rule {
    protocol         = "tcp"
    port_range       = "8080"
    source_addresses = ["0.0.0.0/0", "::/0"]
  }

  # Allow all outbound traffic
  outbound_rule {
    protocol              = "tcp"
    port_range            = "1-65535"
    destination_addresses = ["0.0.0.0/0", "::/0"]
  }

  outbound_rule {
    protocol              = "udp"
    port_range            = "1-65535"
    destination_addresses = ["0.0.0.0/0", "::/0"]
  }

  outbound_rule {
    protocol              = "icmp"
    destination_addresses = ["0.0.0.0/0", "::/0"]
  }
}
